/*
 * Created on 6 juil. 07 by richet
 */
package org.math.plot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.math.plot.utils.Array;

/**
 * Panel designed to select a given number of columns in a multi columns matrix.
 * Useful to provide a 3d plot view of Nd data matrix.
 */
public class DataSelectPanel extends JPanel {

    private static final long serialVersionUID = 419181752327223313L;
    ParameterRow[] rows;
    private Object[][] _data, _selecteddata;
    private LinkedList<Object[]> _tmpselecteddata;
    boolean dataUpdated = false;
    private int[] _tmpselectedIndex;
    private int _nbselected;
    private int[] _selectedindex;
    private String[] _parametersNames;
    //boolean Zselected = false;
    int _dimension;

    public DataSelectPanel(Object[][] data, int dimension, String... parametersNames) {
        _data = data;
        _dimension = dimension;

        _parametersNames = parametersNames;

        if (_dimension > parametersNames.length) {
            throw new IllegalArgumentException("Number of parameters must be > to dimension=" + _dimension);
        }

        setLayout(new GridLayout(_parametersNames.length /*+ 1*/, 1));
        //JPanel title = new JPanel();
        //title.setLayout(new GridLayout(1, 2));
        //title.add(new JLabel("Variable"));
        //title.add(new JLabel("Axis / Parameter"));
        //add(title);

        if (_dimension == 1) {
            buildRows(0);
        } else if (_dimension == 2) {
            buildRows(0, 1);
        } else if (_dimension == 3) {
            buildRows(0, 1, 2);
        }

        fireSelectedDataChanged(null);
    }

    void buildRows(int... selectedaxis) {
        ButtonGroup xgrp = new ButtonGroup();
        ButtonGroup ygrp = new ButtonGroup();
        ButtonGroup zgrp = new ButtonGroup();
        rows = new ParameterRow[_parametersNames.length];
        for (int i = 0; i < _parametersNames.length; i++) {
            rows[i] = new ParameterRow(_parametersNames[i], getColumn(i, _data));

            rows[i].xaxis.setSelected(selectedaxis[0] == i);
            if (selectedaxis.length >= 2) {
                rows[i].yaxis.setSelected(selectedaxis[1] == i);
            }
            if (selectedaxis.length == 3) {
                rows[i].zaxis.setSelected(selectedaxis[2] == i);
            }

            xgrp.add(rows[i].xaxis);
            ygrp.add(rows[i].yaxis);
            zgrp.add(rows[i].zaxis);

            add(rows[i]);
        }
        setPreferredSize(new Dimension(row_width, row_height * _parametersNames.length));
        setSize(new Dimension(row_width, row_height * _parametersNames.length));

        updateSelectedData();
    }

    public void setData(Object[][] data) {
        if (data[0].length != _data[0].length) {
            throw new IllegalArgumentException("new data dimension is not consistent with previous one.");
        }
        _data = data;

        int[] selectedaxis = new int[_dimension];
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].xaxis.isSelected()) {
                selectedaxis[0] = i;
            }
            if (selectedaxis.length >= 2) {
                if (rows[i].yaxis.isSelected()) {
                    selectedaxis[1] = i;
                }
            }
            if (selectedaxis.length == 3) {
                if (rows[i].zaxis.isSelected()) {
                    selectedaxis[2] = i;
                }
            }
            remove(rows[i]);
        }

        dataUpdated = false;
        buildRows(selectedaxis);

        fireSelectedDataChanged("setData");
    }

    void updateSelectedData() {
        if (dataUpdated) {
            return;
        }

        for (ParameterRow row : rows) {
            boolean isaxis = row.xaxis.isSelected() || row.yaxis.isSelected() || row.zaxis.isSelected();
            if (row._isNumber) {
                row.min.setEnabled(!isaxis);
                row.max.setEnabled(!isaxis);
            } else {
                row.list.setEnabled(!isaxis);
            }
            if (!isaxis) {
                if (row._isNumber) {
                    row.name.setText(row._paramName + "=[" + row._kernelDoubleValues[row.min.getValue() - 1] + "," + row._kernelDoubleValues[row.max.getValue() - 1] + "]");
                } else {
                    row.name.setText(row._paramName + "={" + Array.cat(row.list.getSelectedValues()) + "}");
                }
            } else {
                row.name.setText(row._paramName);
            }
        }

        _tmpselectedIndex = new int[_data.length];
        _nbselected = 0;
        _tmpselecteddata = new LinkedList<Object[]>();
        for (int i = 0; i < _data.length; i++) {
            boolean sel = true;
            for (int j = 0; j < rows.length; j++) {
                ParameterRow row = rows[j];
                if (!row.xaxis.isSelected() && !row.yaxis.isSelected() && !row.zaxis.isSelected() && !row.check(_data[i][j])) {
                    sel = false;
                }
            }

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
        if (sel.length > 0) {
            System.out.println(Array.cat(getSelectedFullData()));
        }

        sel = getSelectedProjectedData();
        System.out.println("selected projected data :");
        switch (_dimension) {
            case 1:
                System.out.println(Array.cat(new String[]{getSelectedXAxis()}));
                break;
            case 2:
                System.out.println(Array.cat(new String[]{getSelectedXAxis(), getSelectedYAxis()}));
                break;
            case 3:
                System.out.println(Array.cat(new String[]{getSelectedXAxis(), getSelectedYAxis(), getSelectedZAxis()}));
                break;
        }
        if (sel.length > 0) {
            System.out.println(Array.cat(getSelectedProjectedData()));
        }

    }

    /**return selected data*/
    public int[] getSelectedDataIndex() {
        updateSelectedData();
        _selectedindex = new int[_nbselected];
        for (int i = 0; i < _nbselected; i++) {
            _selectedindex[i] = _tmpselectedIndex[i];
        }
        return _selectedindex;
    }

    /**return selected data*/
    public Object[][] getSelectedFullData() {
        updateSelectedData();
        _selecteddata = new Object[_tmpselecteddata.size()][_data[0].length];
        for (int i = 0; i < _selecteddata.length; i++) {
            for (int j = 0; j < _selecteddata[i].length; j++) {
                _selecteddata[i][j] = _tmpselecteddata.get(i)[j];
            }
        }
        return _selecteddata;
    }

    /**return selected data projected on axis selected*/
    public Object[][] getSelectedProjectedData() {
        updateSelectedData();
        int[] selextedaxis = getSelectedAxisIndex();
        _selecteddata = new Object[_tmpselecteddata.size()][_dimension];
        for (int i = 0; i < _selecteddata.length; i++) {
            for (int j = 0; j < _dimension; j++) {
                _selecteddata[i][j] = _tmpselecteddata.get(i)[selextedaxis[j]];
            }
        }
        return _selecteddata;
    }

    public int[] getSelectedAxisIndex() {
        int[] selextedaxis = new int[_dimension];
        updateSelectedData();
        for (int i = 0; i < rows.length; i++) {
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
        }
        return selextedaxis;
    }

    /**return selected X axis name*/
    public String getSelectedXAxis() {
        updateSelectedData();
        for (ParameterRow row : rows) {
            if (row.xaxis.isSelected()) {
                return row._paramName;
            }
        }
        return null;
    }

    /**return selected Y axis name*/
    public String getSelectedYAxis() {
        updateSelectedData();
        for (ParameterRow row : rows) {
            if (row.yaxis.isSelected()) {
                return row._paramName;
            }
        }
        return null;
    }

    /**return selected Z axis name*/
    public String getSelectedZAxis() {
        updateSelectedData();
        for (ParameterRow row : rows) {
            if (row.zaxis.isSelected()) {
                return row._paramName;
            }
        }
        return null;
    }

    static Object[] getColumn(int j, Object[][] mat) {
        Object[] col = new Object[mat.length];
        for (int i = 0; i < col.length; i++) {
            col[i] = mat[i][j];
        }
        return col;
    }
    public Font font = new Font("Arial", Font.PLAIN, 10);
    public int row_height = 60;
    public int row_width = 300;

    class ParameterRow extends JPanel {

        String _paramName;
        JLabel name;
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

        /**
         * Quick Sort algoritm.
         * <P>
         * Allows to sort a column quickly, Using a generic version of C.A.R Hoare's
         * Quick Sort algorithm.
         * <P>
         */
        public class Sorting {

            /*
             * ------------------------ Class variables ------------------------
             */
            /**
             * Array for internal storage of the matrix to sort.
             */
            private double[] A;
            /**
             * Array for internal storage of the order.
             */
            private int[] order;

            /*
             * ------------------------ Constructors ------------------------
             */
            /**
             * Construct an ascending order.
             *
             * @param array
             *            Array to sort.
             * @param copyArray
             *            Specify if the sort is made directly : true -> array is
             *            modified (usefull for big arrays !), false -> array is copied
             *            and not modified (more memory used).
             */
            public Sorting(double[] array, boolean copyArray) {
                if (copyArray) {
                    A = new double[array.length];
                    System.arraycopy(array, 0, A, 0, array.length);
                    // for (int i = 0; i < A.length; i++) {
                    // A[i] = array[i];
                    // }
                } else {
                    A = array;
                }

                order = new int[A.length];
                for (int i = 0; i < A.length; i++) {
                    order[i] = i;
                }
                sort(A);
            }

            /*
             * ------------------------ Public Methods ------------------------
             */
            public int[] invertIndex(int[] ind) {
                int[] invind = new int[ind.length];
                for (int i = 0; i < ind.length; i++) {
                    invind[ind[i]] = i;

                }
                return invind;
            }

            /**
             * Get the ascending order of one line.
             *
             * @param i
             *            Line number.
             * @return Ascending order of the line.
             */
            public int getIndex(int i) {
                return order[i];
            }

            /**
             * Get the ascending order of all lines.
             *
             * @return Ascending order of lines.
             */
            public int[] getIndex() {
                return order;
            }

            /*
             * ------------------------ Private Methods ------------------------
             */
            /**
             * This is a generic version of C.A.R Hoare's Quick Sort algorithm. This
             * will handle arrays that are already sorted, and arrays with duplicate
             * keys. <BR>
             *
             * If you think of a one dimensional array as going from the lowest index on
             * the left to the highest index on the right then the parameters to this
             * function are lowest index or left and highest index or right. The first
             * time you call this function it will be with the parameters 0, a.length -
             * 1.
             *
             * @param a
             *            A double array.
             * @param lo0
             *            Int.
             * @param hi0
             *            Int.
             */
            private void QuickSort(double a[], int lo0, int hi0) {

                int lo = lo0;
                int hi = hi0;
                double mid;

                if (hi0 > lo0) {
                    // Arbitrarily establishing partition element as the midpoint of the
                    // array.
                    mid = a[(lo0 + hi0) / 2];

                    // loop through the array until indices cross
                    while (lo <= hi) {
                        // find the first element that is greater than or equal to the
                        // partition element starting from the left Index.
                        while ((lo < hi0) && (a[lo] < mid)) {
                            ++lo;
                        }
                        // find an element that is smaller than or equal to the
                        // partition element starting from the right Index.
                        while ((hi > lo0) && (a[hi] > mid)) {
                            --hi;
                        }
                        // if the indexes have not crossed, swap
                        if (lo <= hi) {
                            swap(a, lo, hi);
                            ++lo;
                            --hi;
                        }
                    }

                    // If the right index has not reached the left side of array must
                    // now sort the left partition.
                    if (lo0 < hi) {
                        QuickSort(a, lo0, hi);

                        // If the left index has not reached the right side of array
                        // must now sort the right partition.
                    }
                    if (lo < hi0) {
                        QuickSort(a, lo, hi0);

                    }
                }
            }

            /**
             * Swap two positions.
             *
             * @param a
             *            Array.
             * @param i
             *            Line number.
             * @param j
             *            Line number.
             */
            private void swap(double a[], int i, int j) {
                double T;
                T = a[i];
                a[i] = a[j];
                a[j] = T;
                int t;
                t = order[i];
                order[i] = order[j];
                order[j] = t;
            }

            private void sort(double[] a) {
                QuickSort(a, 0, a.length - 1);
            }
        }

        public ParameterRow(String paramName, Object[] values) {
            _paramName = paramName;
            _isNumber = Array.isDouble(values[0].toString());

            if (!_isNumber) {
                _kernelStringValues = new Vector<Object>(values.length);
                for (int i = 0; i < values.length; i++) {
                    if (!_kernelStringValues.contains(values[i])) {
                        _kernelStringValues.add(values[i]);
                    }
                }
            } else {
                Vector<Double> _tmpdvalues = new Vector<Double>(values.length);
                for (int i = 0; i < values.length; i++) {
                    if (!_tmpdvalues.contains(Double.valueOf(values[i].toString()))) {
                        _tmpdvalues.add(Double.valueOf(values[i].toString()));
                    }
                }

                _kernelDoubleValues = new double[_tmpdvalues.size()];
                for (int i = 0; i < _kernelDoubleValues.length; i++) {
                    _kernelDoubleValues[i] = _tmpdvalues.get(i);
                }

                new Sorting(_kernelDoubleValues, false);
            }

            setLayout(new GridLayout(1, 2));

            name = new JLabel(_paramName);
            name.setFont(font);
            JPanel left = new JPanel(new BorderLayout());

            left.add(name, BorderLayout.CENTER);
            add(left, 0);

            JPanel right = new JPanel(new BorderLayout());

            JPanel XYZ = new JPanel(new GridLayout(_dimension, 1));
            xaxis = new JRadioButton("X");
            xaxis.setFont(font);
            xaxis.addActionListener(new Action() {

                public void actionPerformed(ActionEvent e) {
                    yaxis.setSelected(false);
                    zaxis.setSelected(false);
                    for (ParameterRow r : rows) {
                        if (!r._paramName.equals(_paramName)) {
                            r.xaxis.setSelected(false);
                        }
                    }
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
            yaxis.setFont(font);
            yaxis.addActionListener(new Action() {

                public void actionPerformed(ActionEvent e) {
                    xaxis.setSelected(false);
                    zaxis.setSelected(false);
                    for (ParameterRow r : rows) {
                        if (!r._paramName.equals(_paramName)) {
                            r.yaxis.setSelected(false);
                        }
                    }
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
            if (_dimension >= 2) {
                XYZ.add(yaxis);
            }

            zaxis = new JRadioButton("Z");
            zaxis.setFont(font);
            zaxis.addActionListener(new Action() {

                public void actionPerformed(ActionEvent e) {
                    xaxis.setSelected(false);
                    yaxis.setSelected(false);
                    for (ParameterRow r : rows) {
                        if (!r._paramName.equals(_paramName)) {
                            r.zaxis.setSelected(false);
                        }
                    }
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
            if (_dimension == 3) {
                XYZ.add(zaxis);
            }

            left.add(XYZ, BorderLayout.EAST);

            if (_isNumber) {
                parameter = new JPanel();
                parameter.setLayout(new GridLayout(2, 1));

                min = new JSlider(1, _kernelDoubleValues.length, 1);
                min.setFont(font);

                min.setMinorTickSpacing(1);
                min.setSnapToTicks(true);
                min.setPaintTicks(true);
                max = new JSlider(1, _kernelDoubleValues.length, _kernelDoubleValues.length);
                max.setFont(font);
                max.setMinorTickSpacing(1);
                max.setSnapToTicks(true);
                max.setPaintTicks(true);
                min.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        if (max.getValue() < min.getValue()) {
                            max.setValue(min.getValue());
                        }
                        dataUpdated = false;
                        fireSelectedDataChanged(_paramName + " min");

                    }
                });
                max.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        if (max.getValue() < min.getValue()) {
                            min.setValue(max.getValue());
                        }
                        dataUpdated = false;
                        fireSelectedDataChanged(_paramName + " max");

                    }
                });
                parameter.add(min, 0);
                parameter.add(max, 1);
            } else {

                list = new JList(_kernelStringValues);
                list.setFont(font);
                list.setSelectedIndices(buildIntSeq(0, _kernelStringValues.size() - 1));
                list.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {
                        dataUpdated = false;
                        fireSelectedDataChanged(_paramName + " list");
                    }
                });
                parameter = new JScrollPane(list);
            }
            right.add(parameter, BorderLayout.CENTER);
            add(right, 1);

            setBorder(BorderFactory.createEtchedBorder());
            setPreferredSize(new Dimension(row_width, row_height));
            setSize(new Dimension(row_width, row_height));

        }

        int[] buildIntSeq(int min, int max) {
            int[] seq = new int[max - min + 1];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = min + i;
            }
            return seq;
        }

        boolean check(Object value) {
            if (_isNumber) {
                double dval = Double.valueOf(value.toString());
                return (dval >= _kernelDoubleValues[min.getValue() - 1] && dval <= _kernelDoubleValues[max.getValue() - 1]);
            } else {
                for (int i = 0; i < list.getSelectedIndices().length; i++) {
                    if (_kernelStringValues.get(list.getSelectedIndices()[i]).equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public static void main(String[] args) {
        final PlotPanel pp = new Plot3DPanel(PlotPanel.WEST);
        pp.setPreferredSize(new Dimension(400, 400));
        new FrameView(pp).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Object[][] data = {{1, 3, 4, 5, "a0"}, {1, 3, 1, 1, "a1"}, {1, 3, 2, 2, "a2"}, {1, 3, 3, 3, "a5"}, {1, 3, 3, 3, "a3"}, {1, 3, 3, 4, "a2"}};

        DataSelectPanel dsp = new DataSelectPanel(data, 3, "x1", "x2", "x3", "x4", "x5") {

            private static final long serialVersionUID = 1L;

            @Override
            public void fireSelectedDataChanged(String from) {
                super.fireSelectedDataChanged(from);
                pp.setAxisLabel(0, getSelectedXAxis());
                pp.setAxisLabel(1, getSelectedYAxis());
                pp.setAxisLabel(2, getSelectedZAxis());

                if (pp.getPlots().size() == 0) {
                    pp.addPlot("SCATTER", "data", pp.mapData(getSelectedProjectedData()));
                } else {
                    if (from.endsWith("axis")) {
                        pp.resetMapData();
                        pp.removeAllPlots();
                        pp.addPlot("SCATTER", "data", pp.mapData(getSelectedProjectedData()));
                    } else {
                        pp.getPlot(0).setData(pp.mapData(getSelectedProjectedData()));
                    }
                }
                //System.out.println(Array.cat(pp.getAxesScales()));
            }
        };

        JFrame f = new JFrame("Test mat editor");
        f.setContentPane(dsp);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
