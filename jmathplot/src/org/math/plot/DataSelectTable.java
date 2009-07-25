/*
 * Created on 6 juil. 07 by richet
 */
package org.math.plot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.math.plot.utils.Array;

/**
 * Panel designed to select a given number of columns in a multi columns matrix.
 * Useful to provide a 3d plot view of Nd data matrix.
 */
public class DataSelectTable extends JPanel {

	private static final long serialVersionUID = 41918175232722331L;

	LinkedList<ParameterRow> rows;

	private Object[][] _data, _selecteddata;

	private LinkedList<Object[]> _tmpselecteddata;

	boolean dataUpdated = false;

	private int[] _tmpselectedIndex;

	private int _nbselected;

	private int[] _selectedindex;

	private String[] _parametersNames;

	private JTable _table;

	int _dimension;

	public DataSelectTable(Object[][] data, int dimension, String... parametersNames) {
		_data = data;
		_dimension = dimension;

		_parametersNames = parametersNames;

		if (_dimension > parametersNames.length)
			throw new IllegalArgumentException("Number of parameters must be > to dimension=" + _dimension);

		if (_dimension == 1)
			buildRows(0);
		else if (_dimension == 2)
			buildRows(0, 1);
		else if (_dimension == 3)
			buildRows(0, 1, 2);

		add(new JScrollPane(_table));

	}

	LinkedList<String> header;

	LinkedList<Class< ? >> columnclasses;

	class Model implements TableModel {

		public Model(int... selectedaxis) {

		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub

		}

		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return _parametersNames[rowIndex];
			if (columnIndex == _dimension - 2)
				return rows.get(rowIndex).xaxis;
			if (columnIndex == _dimension - 1)
				return rows.get(rowIndex).yaxis;
			if (columnIndex == _dimension)
				return rows.get(rowIndex).zaxis;

			return null;
		}

		public int getRowCount() {
			return _parametersNames.length;
		}

		public String getColumnName(int columnIndex) {
			return header.get(columnIndex);
		}

		public int getColumnCount() {
			return header.size();
		}

		public Class< ? > getColumnClass(int columnIndex) {
			return columnclasses.get(columnIndex);
		}

		public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

		}

	}

	void buildRows(int... selectedaxis) {

		header = new LinkedList<String>();
		header.add("Parameter");
		if (_dimension <= 1)
			header.add("X");
		if (_dimension <= 2)
			header.add("Y");
		if (_dimension <= 3)
			header.add("Z");
		header.add("min");
		header.add("<>");
		header.add("=");
		header.add("<>");
		header.add("max");

		columnclasses = new LinkedList<Class< ? >>();
		columnclasses.add(String.class);
		if (_dimension <= 1)
			columnclasses.add(Boolean.class);
		if (_dimension <= 2)
			columnclasses.add(Boolean.class);
		if (_dimension <= 3)
			columnclasses.add(Boolean.class);
		columnclasses.add(Double.class);
		columnclasses.add(JSlider.class);
		columnclasses.add(Boolean.class);
		columnclasses.add(JSlider.class);
		columnclasses.add(Double.class);

		ButtonGroup xgrp = new ButtonGroup();
		ButtonGroup ygrp = new ButtonGroup();
		ButtonGroup zgrp = new ButtonGroup();
		rows = new LinkedList<ParameterRow>();
		for (int i = 0; i < _parametersNames.length; i++) {
			rows.add(new ParameterRow(_parametersNames[i], getColumn(i, _data)));

			rows.get(i).xaxis.setSelected(selectedaxis[0] == i);
			if (selectedaxis.length >= 2)
				rows.get(i).yaxis.setSelected(selectedaxis[1] == i);
			if (selectedaxis.length == 3)
				rows.get(i).zaxis.setSelected(selectedaxis[2] == i);

			xgrp.add(rows.get(i).xaxis);
			ygrp.add(rows.get(i).yaxis);
			zgrp.add(rows.get(i).zaxis);
		}
		updateSelectedData();
	}

	public void setData(Object[][] data) {
		if (data[0].length != _data[0].length)
			throw new IllegalArgumentException("new data dimension is not consistent with previous one.");
		_data = data;

		int[] selectedaxis = new int[_dimension];
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).xaxis.isSelected())
				selectedaxis[0] = i;
			if (selectedaxis.length >= 2)
				if (rows.get(i).yaxis.isSelected())
					selectedaxis[1] = i;
			if (selectedaxis.length == 3)
				if (rows.get(i).zaxis.isSelected())
					selectedaxis[2] = i;
			rows.remove(i);
		}

		dataUpdated = false;
		buildRows(selectedaxis);

		fireSelectedDataChanged("setData");
	}

	void updateSelectedData() {
		if (dataUpdated)
			return;

		for (ParameterRow row : rows) {
			boolean isaxis = row.xaxis.isSelected() || row.yaxis.isSelected() || row.zaxis.isSelected();
			if (row._isNumber) {
				row.min.setEnabled(!isaxis);
				row.max.setEnabled(!isaxis);
			} else
				row.list.setEnabled(!isaxis);
			/*if (!isaxis)
				if (row._isNumber)
					row.name.setText(row._paramName + "=[" + row._kernelDoubleValues[row.min.getValue() - 1] + ","
							+ row._kernelDoubleValues[row.max.getValue() - 1] + "]");
				else
					row.name.setText(row._paramName + "={" + Array.cat(row.list.getSelectedValues()) + "}");
			else
				row.name.setText(row._paramName);*/
		}

		_tmpselectedIndex = new int[_data.length];
		_nbselected = 0;
		_tmpselecteddata = new LinkedList<Object[]>();
		for (int i = 0; i < _data.length; i++) {
			boolean sel = true;
			/*for (int j = 0; j < rows.length; j++) {
				ParameterRow row = rows[j];
				if (!row.xaxis.isSelected() && !row.yaxis.isSelected() && !row.zaxis.isSelected() && !row.check(_data[i][j]))
					sel = false;
			}*/

			if (sel) {
				_tmpselecteddata.add(_data[i]);
				_tmpselectedIndex[_nbselected] = i;
				_nbselected++;
				/*System.out.print("OK:");
				for (int j = 0; j < _tmpselecteddata.getLast().length; j++) 
					System.out.print(_tmpselecteddata.getLast()[j]+",");
				System.out.println("");*/
			}
		}
		dataUpdated = true;
	}

	/**Method to override if you want to link to any gui component (for instance, a plotpanel).*/
	public void fireSelectedDataChanged(String from) {
		System.out.println("fireSelectedDataChanged from " + from);
		Object[][] sel = getSelectedFullData();
		System.out.println("selected full data :");
		System.out.println(Array.cat(_parametersNames));
		if (sel.length > 0)
			System.out.println(Array.cat(getSelectedFullData()));

		sel = getSelectedProjectedData();
		System.out.println("selected projected data :");
		switch (_dimension) {
		case 1:
			System.out.println(Array.cat(new String[] { getSelectedXAxis() }));
			break;
		case 2:
			System.out.println(Array.cat(new String[] { getSelectedXAxis(), getSelectedYAxis() }));
			break;
		case 3:
			System.out.println(Array.cat(new String[] { getSelectedXAxis(), getSelectedYAxis(), getSelectedZAxis() }));
			break;
		}
		if (sel.length > 0)
			System.out.println(Array.cat(getSelectedProjectedData()));

	}

	/**return selected data*/
	public int[] getSelectedDataIndex() {
		updateSelectedData();
		_selectedindex = new int[_nbselected];
		for (int i = 0; i < _nbselected; i++)
			_selectedindex[i] = _tmpselectedIndex[i];
		return _selectedindex;
	}

	/**return selected data*/
	public Object[][] getSelectedFullData() {
		updateSelectedData();
		_selecteddata = new Object[_tmpselecteddata.size()][_data[0].length];
		for (int i = 0; i < _selecteddata.length; i++)
			for (int j = 0; j < _selecteddata[i].length; j++)
				_selecteddata[i][j] = _tmpselecteddata.get(i)[j];
		return _selecteddata;
	}

	/**return selected data projected on axis selected*/
	public Object[][] getSelectedProjectedData() {
		updateSelectedData();
		int[] selextedaxis = getSelectedAxisIndex();
		_selecteddata = new Object[_tmpselecteddata.size()][_dimension];
		for (int i = 0; i < _selecteddata.length; i++)
			for (int j = 0; j < _dimension; j++)
				_selecteddata[i][j] = _tmpselecteddata.get(i)[selextedaxis[j]];
		return _selecteddata;
	}

	public int[] getSelectedAxisIndex() {
		int[] selextedaxis = new int[_dimension];
		updateSelectedData();
		/*for (int i = 0; i < rows.length; i++) {
			if (rows[i].xaxis.isSelected()) {
				//System.out.println("selextedaxis[0] =" + i);
				selextedaxis[0] = i;
			}
			if (rows[i].yaxis.isSelected()) {
				//System.out.println("selextedaxis[1] =" + i);
				selextedaxis[1] = i;
			}
			if (rows[i].zaxis.isSelected()) {
				//System.out.println("selextedaxis[2] =" + i);
				selextedaxis[2] = i;
			}
		}*/
		return selextedaxis;
	}

	/**return selected X axis name*/
	public String getSelectedXAxis() {
		updateSelectedData();
		for (ParameterRow row : rows)
			if (row.xaxis.isSelected())
				return row._paramName;
		return null;
	}

	/**return selected Y axis name*/
	public String getSelectedYAxis() {
		updateSelectedData();
		for (ParameterRow row : rows)
			if (row.yaxis.isSelected())
				return row._paramName;
		return null;
	}

	/**return selected Z axis name*/
	public String getSelectedZAxis() {
		updateSelectedData();
		for (ParameterRow row : rows)
			if (row.zaxis.isSelected())
				return row._paramName;
		return null;
	}

	static Object[] getColumn(int j, Object[][] mat) {
		Object[] col = new Object[mat.length];
		for (int i = 0; i < col.length; i++)
			col[i] = mat[i][j];
		return col;
	}

	class ParameterRow /*extends JPanel */{

		//private static final long serialVersionUID = -7301434647336910071L;

		String _paramName;

		//JLabel name;

		JRadioButton xaxis, yaxis, zaxis;

		JComponent parameter;

		JSlider min, max;

		JCheckBox linkminmax;

		JList list;

		//Object[] _values;

		Vector<Object> _kernelStringValues;

		boolean _isNumber;

		//double[] _dvalues;

		double[] _kernelDoubleValues;

		public ParameterRow(String paramName, Object[] values) {
			_paramName = paramName;
			_isNumber = Array.isDouble(values[0].toString());

			if (!_isNumber) {
				_kernelStringValues = new Vector<Object>(values.length);
				for (int i = 0; i < values.length; i++)
					if (!_kernelStringValues.contains(values[i]))
						_kernelStringValues.add(values[i]);
			} else {
				Vector<Double> _tmpdvalues = new Vector<Double>(values.length);
				for (int i = 0; i < values.length; i++)
					if (!_tmpdvalues.contains(Double.valueOf(values[i].toString())))
						_tmpdvalues.add(Double.valueOf(values[i].toString()));

				_kernelDoubleValues = new double[_tmpdvalues.size()];
				for (int i = 0; i < _kernelDoubleValues.length; i++)
					_kernelDoubleValues[i] = _tmpdvalues.get(i);
			}

			setLayout(new GridLayout(1, 2));

			//name = new JLabel(_paramName);
			//add(name, 0);

			JPanel type = new JPanel(new BorderLayout());

			JPanel XYZ = new JPanel(new GridLayout(_dimension, 1));
			xaxis = new JRadioButton("X");
			xaxis.addActionListener(new Action() {
				public void actionPerformed(ActionEvent e) {
					yaxis.setSelected(false);
					zaxis.setSelected(false);
					for (ParameterRow r : rows)
						if (!r._paramName.equals(_paramName))
							r.xaxis.setSelected(false);
					dataUpdated = false;
					fireSelectedDataChanged(_paramName + " xaxis");
				}

				public void setEnabled(boolean b) {
				}

				public void removePropertyChangeListener(PropertyChangeListener listener) {
				}

				public void putValue(String key, Object value) {
				}

				public boolean isEnabled() {
					return true;
				}

				public Object getValue(String key) {
					return null;
				}

				public void addPropertyChangeListener(PropertyChangeListener listener) {
				}
			});
			XYZ.add(xaxis);
			yaxis = new JRadioButton("Y");
			yaxis.addActionListener(new Action() {
				public void actionPerformed(ActionEvent e) {
					xaxis.setSelected(false);
					zaxis.setSelected(false);
					for (ParameterRow r : rows)
						if (!r._paramName.equals(_paramName))
							r.yaxis.setSelected(false);
					dataUpdated = false;
					fireSelectedDataChanged(_paramName + " yaxis");

				}

				public void setEnabled(boolean b) {
				}

				public void removePropertyChangeListener(PropertyChangeListener listener) {
				}

				public void putValue(String key, Object value) {
				}

				public boolean isEnabled() {
					return true;
				}

				public Object getValue(String key) {
					return null;
				}

				public void addPropertyChangeListener(PropertyChangeListener listener) {
				}
			});
			if (_dimension >= 2)
				XYZ.add(yaxis);

			zaxis = new JRadioButton("Z");
			zaxis.addActionListener(new Action() {
				public void actionPerformed(ActionEvent e) {
					xaxis.setSelected(false);
					yaxis.setSelected(false);
					for (ParameterRow r : rows)
						if (!r._paramName.equals(_paramName))
							r.zaxis.setSelected(false);
					dataUpdated = false;
					fireSelectedDataChanged(_paramName + " zaxis");
				}

				public void setEnabled(boolean b) {
				}

				public void removePropertyChangeListener(PropertyChangeListener listener) {
				}

				public void putValue(String key, Object value) {
				}

				public boolean isEnabled() {
					return true;
				}

				public Object getValue(String key) {
					return null;
				}

				public void addPropertyChangeListener(PropertyChangeListener listener) {
				}
			});
			if (_dimension == 3)
				XYZ.add(zaxis);

			type.add(XYZ, BorderLayout.WEST);

			if (_isNumber) {
				parameter = new JPanel();
				parameter.setLayout(new GridLayout(2, 1));

				min = new JSlider(1, _kernelDoubleValues.length, 1);

				min.setMinorTickSpacing(1);
				min.setSnapToTicks(true);
				min.setPaintTicks(true);
				max = new JSlider(1, _kernelDoubleValues.length, _kernelDoubleValues.length);
				max.setMinorTickSpacing(1);
				max.setSnapToTicks(true);
				max.setPaintTicks(true);
				min.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (max.getValue() < min.getValue())
							max.setValue(min.getValue());
						dataUpdated = false;
						fireSelectedDataChanged(_paramName + " min");

					}
				});
				max.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (max.getValue() < min.getValue())
							min.setValue(max.getValue());
						dataUpdated = false;
						fireSelectedDataChanged(_paramName + " max");

					}
				});
				parameter.add(min, 0);
				parameter.add(max, 1);
			} else {

				list = new JList(_kernelStringValues);
				list.setSelectedIndices(buildIntSeq(0, _kernelStringValues.size() - 1));
				list.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						dataUpdated = false;
						fireSelectedDataChanged(_paramName + " list");
					}
				});
				parameter = new JScrollPane(list);
			}
			type.add(parameter, BorderLayout.CENTER);
			add(type, 1);

			setBorder(BorderFactory.createEtchedBorder());
			setPreferredSize(new Dimension(400, 60));

		}

		int[] buildIntSeq(int min, int max) {
			int[] seq = new int[max - min + 1];
			for (int i = 0; i < seq.length; i++)
				seq[i] = min + i;
			return seq;
		}

		boolean check(Object value) {
			if (_isNumber) {
				double dval = Double.valueOf(value.toString());
				return (dval >= _kernelDoubleValues[min.getValue() - 1] && dval <= _kernelDoubleValues[max.getValue() - 1]);
			} else {
				for (int i = 0; i < list.getSelectedIndices().length; i++) {
					if (_kernelStringValues.get(list.getSelectedIndices()[i]).equals(value))
						return true;
				}
				return false;
			}
		}

	}

	public static void main(String[] args) {
		final PlotPanel pp = new Plot3DPanel(PlotPanel.WEST);
		pp.setPreferredSize(new Dimension(400, 400));
		new FrameView(pp).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Object[][] data = { { 0, 0, 0, 0, "a0" }, { 1, 1, 1, 1, "a1" }, { 2, 2, 2, 2, "a2" }, { 3, 3, 3, 3, "a3" }, { 4, 3, 3, 3, "a3" }, { 5, 3, 3, 3, "a4" } };

		DataSelectTable dsp = new DataSelectTable(data, 3, "x1", "x2", "x3", "x4", "x5") {
			private static final long serialVersionUID = 1L;

			@Override
			public void fireSelectedDataChanged(String from) {
				super.fireSelectedDataChanged(from);
				pp.setAxisLabel(0, getSelectedXAxis());
				pp.setAxisLabel(1, getSelectedYAxis());
				pp.setAxisLabel(2, getSelectedZAxis());

				if (pp.getPlots().size() == 0)
					pp.addPlot("SCATTER", "data", pp.mapData(getSelectedProjectedData()));
				else {
					if (from.endsWith("axis")) {
						pp.resetMapData();
						pp.removeAllPlots();
						pp.addPlot("SCATTER", "data", pp.mapData(getSelectedProjectedData()));
					} else
						pp.getPlot(0).setData(pp.mapData(getSelectedProjectedData()));
				}
				//System.out.println(Array.cat(pp.getAxesScales()));
			}
		};
		new FrameView(dsp).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Object[][] data2 = { { 0, 0, 0, 0, "a0" }, { 1, 1, 1, 1, "a1" }, { 2, 2, 2, 2, "a2" }, { 3, 3, 3, 3, "a3" }, { 4, 3, 3, 3, "a3" },
				{ 5, 3, 3, 3, "a4" }, { 5, 4, 3, 3, "a4" } };
		dsp.setData(data2);*/
	}

}
