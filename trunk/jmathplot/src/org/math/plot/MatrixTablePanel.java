package org.math.plot;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;
import javax.swing.table.*;

import org.math.io.files.*;
import org.math.plot.utils.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class MatrixTablePanel extends DataPanel {

	private static final long serialVersionUID = 1L;

	private JTable table;

	private TableModel model;

	private Object[][] M;

	private boolean viewHeaders = false;

	private String[] headers;

	public MatrixTablePanel(Object[][] m) {
		super();
		M = m;
		if (M.length == 0)
			headers = new String[0];
		else
			headers = new String[M[0].length];
		setModel();
		toWindow();
	}

	public void toClipBoard() {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Array.cat(M)), null);
		} catch (IllegalStateException e) {
			JOptionPane.showConfirmDialog(null, "Copy to clipboard failed : " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
		}
	}

	public String getText() {
		return M.toString();
	}

	public void toASCIIFile(File file) {
		try {
			ASCIIFile.write(file, Array.cat(M));
		} catch (NullPointerException e) {
			// System.out.println("File not saved");
		}
	}

	private void setModel() {
		/*Double[][] array = null;
		if (M.length != 0) {
			array = new Double[M.length][M[0].length];
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					array[i][j] = new Double(M[i][j]);
				}
			}
		} else
			array = new Double[0][0];*/

		model = new DefaultTableModel(M, headers) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

	}

	public void setHeaders(String[] h) {
		if (M.length != 0)
			if (h.length != M[0].length) {
				throw new IllegalArgumentException("Headers of the table must have " + M[0].length + " elements.");
			}

		headers = h;
		update();
	}

	public void update() {
		setModel();
		super.update();
	}

	public void setMatrix(Object[][] m) {
		M = m;

		if (M.length == 0)
			headers = new String[0];
		else
			headers = new String[M[0].length];

		update();
	}

	protected void toWindow() {
		table = new JTable(model);

		if (!viewHeaders) {
			table.setTableHeader(null);
		}

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// table.setCellSelectionEnabled(true);
		table.setEnabled(false);

		scrollPane = new JScrollPane(table);

		/*
		 * scrollPane.setPreferredSize(getSize());
		 * scrollPane.setSize(getSize());
		 */

		add(scrollPane, BorderLayout.CENTER);
	}

	public Object[][] getMatrix() {
		return M;
	}
}