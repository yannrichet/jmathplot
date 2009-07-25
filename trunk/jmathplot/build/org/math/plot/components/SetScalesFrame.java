package org.math.plot.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.math.io.*;
import org.math.plot.canvas.*;
import org.math.plot.plotObjects.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */

public class SetScalesFrame extends JFrame implements WindowFocusListener, WindowListener {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private PlotCanvas plotCanvas;

	private JPanel scalespanel;

	private ScalePanel[] scalepanels;

	public SetScalesFrame(PlotCanvas p) {
		super("scales settings");
		plotCanvas = p;
		setPanel();
		setContentPane(scalespanel);

		setResizable(false);
		setVisible(true);

		addWindowFocusListener(this);
		addWindowListener(this);
	}

	private void setPanel() {
		int nbAxes = plotCanvas.base.dimension;

		this.setSize(nbAxes * 300, 200);

		scalespanel = new JPanel();
		scalespanel.setLayout(new GridLayout(1, nbAxes));

		scalepanels = new ScalePanel[nbAxes];
		for (int i = 0; i < nbAxes; i++) {
			scalepanels[i] = new ScalePanel(plotCanvas, i);
			scalespanel.add(scalepanels[i]);
		}
	}

	/*
	 * private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int
	 * gw, int gh, int wx, int wy) { gbc.gridx = gx; gbc.gridy = gy;
	 * gbc.gridwidth = gw; gbc.gridheight = gh; gbc.weightx = wx; gbc.weighty =
	 * wy; }
	 */

	public void setDefaultCloseOperation(int operation) {
		for (int i = 0; i < scalepanels.length; i++) {
			scalepanels[i].updateBoundsFields();
		}
		super.setDefaultCloseOperation(operation);
	}

	public void windowGainedFocus(WindowEvent e) {
		for (int i = 0; i < scalepanels.length; i++) {
			scalepanels[i].update();
		}
	}

	public void windowLostFocus(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
		for (int i = 0; i < scalepanels.length; i++) {
			scalepanels[i].update();
		}
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public class ScalePanel extends JPanel implements StringPrintable {

		private static final long serialVersionUID = 1L;

		// private PlotCanvas plotCanvas;

		private int numAxe;

		private String title;

		private String scaleType;

		private double min;

		private double max;

		private JLabel title_label = new JLabel("Title");

		private JTextField title_field = new JTextField();

		private JLabel scale_label = new JLabel("Scale");

		private ButtonGroup scale_group = new ButtonGroup();

		private JRadioButton linear_check = new JRadioButton("Linear");

		private JRadioButton log_check = new JRadioButton("Logarithmic");

		// private JCheckBox gridVisible = new JCheckBox("Grid visible");

		private JLabel bounds_label = new JLabel("Bounds");

		private JLabel min_label = new JLabel("Min");

		private JLabel max_label = new JLabel("Max");

		private JTextField min_field = new JTextField();

		private JTextField max_field = new JTextField();

		private JButton bounds_auto = new JButton("Automatic");

		public ScalePanel(PlotCanvas p, int i) {
			numAxe = i;
			plotCanvas = p;

			update();

			addComponents();
			setListeners();
		}

		public void update() {
			title = plotCanvas.getGrid().getAxis(numAxe).getLegend();// getLegend(numAxe);
			title_field.setText(title);

			scaleType = plotCanvas.getAxisScales()[numAxe];
			log_check.setSelected(scaleType.equals(Base.LOGARITHM));
			linear_check.setSelected(scaleType.equals(Base.LINEAR));
			if (scaleType.equals(Base.STRINGS)) {log_check.setEnabled(false); linear_check.setEnabled(false);}

			updateBoundsFields();
		}

		private void addComponents() {
			this.setSize(300, 200);

			scale_group.add(linear_check);
			scale_group.add(log_check);

			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			this.setLayout(gbl);

			buildConstraints(c, 0, 0, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(title_label, c);
			this.add(title_label);

			buildConstraints(c, 1, 0, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(title_field, c);
			this.add(title_field);

			buildConstraints(c, 0, 1, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(scale_label, c);
			this.add(scale_label);

			buildConstraints(c, 1, 1, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(linear_check, c);
			this.add(linear_check);

			buildConstraints(c, 1, 2, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(log_check, c);
			this.add(log_check);

			buildConstraints(c, 0, 3, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(bounds_label, c);
			this.add(bounds_label);

			buildConstraints(c, 1, 3, 1, 1, 20, 20);
			c.fill = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(min_label, c);
			this.add(min_label);

			buildConstraints(c, 2, 3, 1, 1, 50, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(min_field, c);
			this.add(min_field);

			buildConstraints(c, 1, 4, 1, 1, 20, 20);
			c.fill = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(max_label, c);
			this.add(max_label);

			buildConstraints(c, 2, 4, 1, 1, 50, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(max_field, c);
			this.add(max_field);

			buildConstraints(c, 1, 5, 2, 1, 60, 20);
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(bounds_auto, c);
			this.add(bounds_auto);

			// this.add(gridVisible);
		}

		private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
			gbc.gridx = gx;
			gbc.gridy = gy;
			gbc.gridwidth = gw;
			gbc.gridheight = gh;
			gbc.weightx = wx;
			gbc.weighty = wy;
		}

		private void setListeners() {
			title_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setTitle();
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}
			});

			log_check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setScale();
					updateBoundsFields();
				}
			});
			linear_check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setScale();
					updateBoundsFields();
				}
			});
			/*
			 * gridVisible.addChangeListener(new ChangeListener() { public void
			 * stateChanged(ChangeEvent e) {
			 * plotPanel.getGrid().getAxe(numAxe).setVisible(
			 * gridVisible.isSelected()); plotPanel.repaint(); } });
			 */

			min_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setBounds();
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}
			});

			max_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setBounds();
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}
			});

			bounds_auto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setBoundsAuto();
				}
			});

		}

		public String getText() {
			return "title = " + title + "\nscaleType = " + scaleType + "\nmin = " + min + "\nmax = " + max;
		}

		private void setTitle() {
			// System.out.println("title setting n�" + numAxe + " : " +
			// title_field.getText());
			plotCanvas.setAxisLabel(numAxe, title_field.getText());
		}

		private void setBounds() {
			// System.out.println("bounds setting n�" + numAxe + " : " +
			// min_field.getText() + " - " + max_field.getText());
			try {
				double min1 = Double.parseDouble(min_field.getText());
				double max1 = Double.parseDouble(max_field.getText());
				plotCanvas.setFixedBounds(numAxe, min1, max1);
			} catch (IllegalArgumentException iae) {
				// JOptionPane.showConfirmDialog(null, iae.getMessage(),
				// "Error", JOptionPane.DEFAULT_OPTION,
				// JOptionPane.ERROR_MESSAGE);
				// updateBoundsFields();
			}
		}

		private void setScale() {
			// System.out.println("scale setting n�" + numAxe + " : " + (
			// (log_check.isSelected()) ? ("LOG") : ("LINEAR")));
			try {
				plotCanvas.setAxiScale(numAxe, (log_check.isSelected()) ? (Base.LOGARITHM) : (Base.LINEAR));
			} catch (IllegalArgumentException iae) {
				JOptionPane.showConfirmDialog(null, iae.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				updateBoundsFields();
			}
		}

		private void setBoundsAuto() {
			plotCanvas.setAutoBounds(numAxe);
			updateBoundsFields();
			// System.out.println("auto-Bounds setting n�"+numAxe+" :
			// "+plotPanel.getBase().getMinBounds()[numAxe]+" -
			// "+plotPanel.getBase().getMaxBounds()[numAxe]);
		}

		private void updateBoundsFields() {
			min = plotCanvas.base.getMinBounds()[numAxe];
			max = plotCanvas.base.getMaxBounds()[numAxe];
			min_field.setText("" + min);
			max_field.setText("" + max);
			// log_check.setSelected(plotCanvas.base.getAxeScale(numAxe)==Base.LOG);
		}

	}

}