package org.math.plot;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */

public class ParametersPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Dimension defaultSize;

	private String[] paramLabels;

	private String[] paramValues;

	private boolean[] isList;

	private int[] paramValuesIndex;

	private String[][] paramChoices;

	private JLabel[] labels;

	private JComboBox[] fields;

	private Runnable action;

	public ParametersPanel(String[] lab, String[] val) {
		this(lab, new int[lab.length], new String[][] { val });
	}

	public ParametersPanel(String[] lab) {
		this(lab, new String[lab.length]);
	}

	public ParametersPanel(String[] lab, int[] selected, String[][] ch) {
		paramLabels = lab;

		isList = new boolean[paramLabels.length];
		for (int i = 0; i < isList.length; i++) {
			isList[i] = true;
		}

		paramValuesIndex = selected;

		paramChoices = ch;

		paramValues = new String[paramLabels.length];
		for (int i = 0; i < paramChoices.length; i++) {
			paramValues[i] = paramChoices[i][paramValuesIndex[i]];
		}

		setComponents();
		setAppearence();
		draw();
	}

	public ParametersPanel(String[] lab, String[][] ch) {
		this(lab, new int[lab.length], ch);
	}

	private void setComponents() {
		labels = new JLabel[paramLabels.length];
		fields = new JComboBox[paramLabels.length];
		for (int i = 0; i < paramLabels.length; i++) {
			labels[i] = new JLabel(paramLabels[i], JLabel.RIGHT);
			if (isList[i]) {
				fields[i] = new JComboBox(paramChoices[i]);
			} else {
				fields[i] = new JComboBox();
			}
			fields[i].setEditable(!isList[i]);
		}
		defaultSize = new Dimension(400, paramLabels.length * 30);
	}

	private void setAppearence() {
		setPreferredSize(defaultSize);
		setSize(defaultSize);
	}

	private void update() {
		updateValues();
		updateValuesIndex();
	}

	private void updateValues() {
		for (int i = 0; i < paramLabels.length; i++) {
			paramValues[i] = (String) (fields[i].getSelectedItem());
		}
	}

	private void updateValuesIndex() {
		for (int i = 0; i < paramLabels.length; i++) {
			if (isList[i]) {
				paramValuesIndex[i] = fields[i].getSelectedIndex();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		update();
		new Thread(action, "PanelParameters " + this.toString() + " selection").start();
	}

	public int getValueIndex(int i) {
		if (!isList[i]) {
			throw new IllegalArgumentException("This PanelParameter element is not set to give an Index.");
		}
		update();
		return paramValuesIndex[i];
	}

	public int[] getValuesIndex() {
		update();
		return paramValuesIndex;
	}

	public String[] getValues() {
		update();
		return paramValues;
	}

	public String getValue(int i) {
		update();
		return paramValues[i];
	}

	public void setAction(Runnable t) {
		action = t;
	}

	private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	private void draw() {
		JPanel panel = new JPanel();

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gbl);

		for (int i = 0; i < paramLabels.length; i++) {
			fields[i].addActionListener(this);

			// Ajout du panel de la chaine
			buildConstraints(c, 0, i, 1, 1, 50, 20);
			c.anchor = GridBagConstraints.EAST;
			gbl.setConstraints(labels[i], c);
			panel.add(labels[i]);

			// Ajout du panel de la chaine
			buildConstraints(c, 1, i, 1, 1, 50, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(fields[i], c);
			panel.add(fields[i]);
		}

		JScrollPane scrollPane = new JScrollPane(panel);

		scrollPane.setPreferredSize(getSize());
		scrollPane.setSize(getSize());

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

	}

}