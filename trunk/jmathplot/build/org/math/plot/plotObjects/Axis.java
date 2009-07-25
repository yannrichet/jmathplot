package org.math.plot.plotObjects;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.math.plot.FrameView;
import org.math.plot.Plot3DPanel;
import org.math.plot.canvas.PlotCanvas;
import org.math.plot.render.AbstractDrawer;

/**
 * Class use to describe one of the axis of a plot object.
 * 
 * BSD License
 * 
 * @author Yann RICHET
 */

public class Axis implements Plotable, BaseDependant, Editable {

	/**
	 * Mapping of the data on this axis, which is the association between values
	 * along this axis as String and double numbers.
	 */
	protected HashMap<String, Double> stringMap;

	protected int linear_slicing = 10;

	protected int note_precision = 5;

	protected int index;

	protected Base base;

	/**
	 * Visibility of the whole axis
	 */
	boolean visible = true;

	/**
	 * Color in which the name of the axis is displayed.
	 */
	protected Color color;

	/**
	 * Axis label
	 */
	protected String label;

	/**
	 * Visibility of the grid.
	 */
	boolean gridVisible = true;

	protected double[] linesSlicing;

	protected double[] labelsSlicing;

	protected double[] origin;

	protected double[] end;

	protected BaseLine darkLine;

	protected Line[][] lightLines;

	protected BaseLabel darkLabel;

	protected Label[] lightLabels;

	protected Font lightLabelFont = AbstractDrawer.DEFAULT_FONT;

	protected Font darkLabelFont = AbstractDrawer.DEFAULT_FONT;

	protected double lightLabelAngle = 0;

	protected double darkLabelAngle = 0;

	protected String[] lightLabelNames;

	protected double lightLabels_base_offset = 0.05;

	protected double[] darkLabel_base_position;

	/*
	 * CONSTRUCTORS
	 */

	public Axis(Base b, String aS, Color c, int i) {
		base = b;
		label = aS;
		index = i;
		color = c;
		initDarkLines();
		initDarkLabels();
		init();
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Sets the visibility of the whole axis object.
	 * 
	 * @param v
	 *            Visible if true.
	 */
	public void setVisible(boolean v) {
		visible = v;
	}

	/**
	 * Returns the visibility of the whole axis object.
	 * 
	 * @return Visible if true.
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * Returns the mapping of the data on this axis, which is the association
	 * between values along this axis as String and double numbers.
	 * 
	 * @return Mapping of the data on this axis.
	 */
	public HashMap<String, Double> getStringMap() {
		return stringMap;
	}

	/**
	 * Returns the mapping of the data on this axis, which is the association
	 * between values along this axis as String and double numbers.
	 * 
	 * @param stringMap
	 *            Mapping of the data on this axis.
	 */
	public void setStringMap(HashMap<String, Double> stringMap) {
		// System.out.println(Array.toString(this.stringMap)+"
		// >>\n"+Array.toString(stringMap));
		this.stringMap = stringMap;
	}

	/**
	 * Sets the visibility of the light lines and their labels.
	 * 
	 * @param v
	 *            Visible if true.
	 */
	public void setGridVisible(boolean v) {
		gridVisible = v;
	}

	/**
	 * Sets the color used to display the axis' label.
	 * 
	 * @param c
	 *            The color of the axis' label.
	 */
	public void setColor(Color c) {
		color = c;
		darkLabel.setColor(color);
	}

	/**
	 * Returns the color of the axis' label.
	 * 
	 * @return The color of the axis' label.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the label of this axis.
	 * 
	 * @param _label
	 *            The label to be given to the axis.
	 */
	public void setLegend(String _label) {
		label = _label;
		darkLabel.setText(label);
	}

	/**
	 * Returns the label of the axis.
	 * 
	 * @return The label of the axis.
	 */
	public String getLegend() {
		return label;
	}

	/**
	 * Returns the coordinates of the axis label, in the referential of the
	 * canvas it is drawn in.
	 * 
	 * @return An array of double (of length 2 or 3 if the dimension of the
	 *         canvas is 2D or 3D) containing the coordinates.
	 */
	public double[] getLegendCoord() {
		return darkLabel.coord;
	}

	public void plot(AbstractDrawer draw) {
		if (!visible) {
			return;
		}
		if (gridVisible) {
			draw.setLineType(AbstractDrawer.DOTTED_LINE);
			// draw.setFont(lightLabelFont);
			for (int i = 0; i < lightLines.length; i++) {
				// j = 0 overwrites a darkLine of another Axe : so I begin to j
				// = 1.
				for (int j = base.getAxeScale(index).equalsIgnoreCase(
						Base.STRINGS) ? 0 : 1; j < lightLines[i].length; j++) {
					lightLines[i][j].plot(draw);
				}
			}
			for (int i = 0; i < lightLabels.length; i++) {
				lightLabels[i].plot(draw);
			}
		}
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		// draw.setFont(darkLabelFont);
		darkLine.plot(draw);
		darkLabel.plot(draw);
	}

	/**
	 * Sets the axis to its default initial value.
	 */
	public void init() {
		// System.out.println("Axe.init");
		initOriginEnd();
		setSlicing();

		// initDarkLines();
		// initDarkLabels();
		if (gridVisible) {
			setLightLines();
			setLightLabels();
		}
	}

	/**
	 * Resets the axis to its default value. Same as init().
	 */
	public void resetBase() {
		// System.out.println("Axe.resetBase");
		init();
	}

	/**
	 * Problem here?
	 * 
	 * @param _end
	 */
	public void setEnd(double[] _end) {
		end = _end;
		resetBase();
	}

	public void setOrigin(double[] _origin) {
		origin = _origin;
		resetBase();
	}

	/**
	 * When called out of the axis class, resets the light labels to their
	 * default value.
	 */
	public void setLightLabels() {
		// System.out.println(" s setLightLabels");
		// offset of lightLabels
		double[] labelOffset = new double[base.dimension];
		for (int j = 0; j < base.dimension; j++) {
			if (j != index) {
				labelOffset[j] = -lightLabels_base_offset;
			}
		}
		// local variables initialisation
		int decimal = 0;
		String lab;

		lightLabels = new Label[labelsSlicing.length];

		for (int i = 0; i < lightLabels.length; i++) {

			double[] labelCoord = new double[base.dimension];
			System.arraycopy(base.getCoords()[index + 1], 0, labelCoord, 0,
					base.dimension);
			labelCoord[index] = labelsSlicing[i];

			if (base.getAxeScale(index).startsWith(Base.LINEAR)
					|| base.getAxeScale(index).startsWith(Base.STRINGS)) {
				decimal = -(int) (log(base.getPrecisionUnit()[index] / 100) / log(10));
			} else if (base.getAxeScale(index).startsWith(Base.LOGARITHM)) {
				decimal = -(int) (floor(log(labelsSlicing[i]) / log(10)));
			}
			if (lightLabelNames != null) {
				lab = lightLabelNames[i % lightLabelNames.length];
			} else {
				lab = new String(Label.approx(labelsSlicing[i], decimal) + "");
			}
			// System.out.println(Array.toString(labelCoord) + " -> " + lab);
			lightLabels[i] = new Label(lab, Color.lightGray, labelCoord);
			lightLabels[i].base_offset = labelOffset;

			if (lightLabelAngle != 0) {
				lightLabels[i].rotate(lightLabelAngle);
			}
			if (lightLabelFont != null) {
				lightLabels[i].setFont(lightLabelFont);
			}
		} // end for
		lightLabelNames = null;
	}

	/**
	 * Sets the labels of the light lines. Is the numerical graduation by
	 * default.
	 * 
	 * @param _lightLabelnames
	 *            Array of string containing the labels. When the end of the
	 *            array is reached for one tick, the following tick starts with
	 *            the beginning of the array again.
	 */
	public void setLightLabelText(String[] _lightLabelnames) {
		lightLabelNames = _lightLabelnames;
		setLightLabels(); // resetBase();
	}

	/**
	 * Sets the font used for the light labels.
	 * 
	 * @param f
	 *            Font to use.
	 */
	public void setLightLabelFont(Font f) {
		lightLabelFont = f;
		setLightLabels(); // resetBase();
	}

	/**
	 * Sets the angle with which the light labels will be displayed.
	 * 
	 * @param angle
	 *            Angle in degrees, measured clockwise.
	 */
	public void setLightLabelAngle(double angle) {
		lightLabelAngle = angle;
		setLightLabels(); // resetBase();
	}

	/**
	 * Specifies the label of the axis.
	 * 
	 * @param _t
	 *            Label to add to the axis.
	 */
	public void setLabelText(String _t) {
		darkLabel.label = _t;
	}

	/**
	 * Sets the font used to display the label.
	 * 
	 * @param f
	 *            Font to use.
	 */
	public void setLabelFont(Font f) {
		darkLabelFont = f;
		darkLabel.setFont(darkLabelFont);
	}

	/**
	 * Sets the angle with which the label will be displayed.
	 * 
	 * @param angle
	 *            Angle in degrees, measured clockwise.
	 */
	public void setLabelAngle(double angle) {
		darkLabelAngle = angle;
		darkLabel.angle = darkLabelAngle;
	}

	/**
	 * Sets the position of the axis label on the panel.
	 * 
	 * @param _p
	 *            Position of the label.
	 */
	public void setLabelPosition(double... _p) {
		darkLabel_base_position = _p;
		darkLabel.coord = darkLabel_base_position;
	}

	/**
	 * Opens a dialog window and asks the user for the name of this axis.
	 * 
	 * @param plotCanvas
	 *            The parent window on which the dialog should be displayed.
	 */
	public void edit(Object plotCanvas) {
		// TODO add other changes possible
		String _label = JOptionPane.showInputDialog((PlotCanvas) plotCanvas,
				"Choose axis label", label);
		if (_label != null) {
			setLegend(_label);
		}
	}

	/**
	 * 
	 * @param screenCoordTest
	 * @param draw
	 * @return
	 */
	public double[] isSelected(int[] screenCoordTest, AbstractDrawer draw) {

		int[] screenCoord = draw.project(darkLabel.coord);

		if ((screenCoord[0] + note_precision > screenCoordTest[0])
				&& (screenCoord[0] - note_precision < screenCoordTest[0])
				&& (screenCoord[1] + note_precision > screenCoordTest[1])
				&& (screenCoord[1] - note_precision < screenCoordTest[1])) {
			return darkLabel.coord;
		}
		return null;
	}

	/**
	 * 
	 * @param draw
	 */
	public void editnote(AbstractDrawer draw) {
		darkLabel.setFont(darkLabelFont.deriveFont(Font.BOLD));
		darkLabel.plot(draw);
		darkLabel.setFont(darkLabelFont.deriveFont(Font.PLAIN));
	}

	/*
	 * PRIVATE METHODS
	 */

	private void setLightLines() {
		// System.out.println(" s setLightLines");
		lightLines = new Line[base.dimension - 1][linesSlicing.length];

		int i2 = 0;

		for (int i = 0; i < base.dimension - 1; i++) {
			if (i2 == index) {
				i2++;
			}
			for (int j = 0; j < lightLines[i].length; j++) {
				double[] origin_tmp = new double[base.dimension];
				double[] end_tmp = new double[base.dimension];

				System.arraycopy(origin, 0, origin_tmp, 0, base.dimension);
				System.arraycopy(origin, 0, end_tmp, 0, base.dimension);

				end_tmp[i2] = base.getCoords()[i2 + 1][i2];
				origin_tmp[index] = linesSlicing[j];
				end_tmp[index] = linesSlicing[j];

				// System.out.println("index= "+index+"
				// "+Array.toString(origin_tmp));
				// System.out.println("index= "+index+"
				// "+Array.toString(end_tmp)+"\n");
				lightLines[i][j] = new Line(Color.lightGray, origin_tmp,
						end_tmp);
			}
			i2++;
		}
	}

	private void initDarkLines() {
		// System.out.println(" s setDarkLines");
		double[] originB = new double[base.dimension];
		double[] endB = new double[base.dimension];
		endB[index] = 1.0;
		darkLine = new BaseLine(color, originB, endB);
	}

	private void initDarkLabels() {
		// System.out.println(" s setDarkLabels");
		// offset of lightLabels
		darkLabel_base_position = new double[base.dimension];
		for (int j = 0; j < base.dimension; j++) {
			if (j != index) {
				darkLabel_base_position[j] = 0; // -2*lightLabels_base_offset;
			} else {
				darkLabel_base_position[j] = 1 + lightLabels_base_offset;
			}
		}
		darkLabel = new BaseLabel(label, color, darkLabel_base_position);
	}

	private void initOriginEnd() {
		origin = base.getCoords()[0];
		end = base.getCoords()[index + 1];

		// System.out.println("origin: "+Array.toString(origin));
		// System.out.println("end: "+Array.toString(end));
	}

	private void setSlicing() {

		// slicing initialisation
		if (base.getAxeScale(index).equalsIgnoreCase(Base.LOGARITHM)) {
			int numPow10 = (int) Math.rint((Math.log(base.getMaxBounds()[index]
					/ base.getMinBounds()[index]) / Math.log(0)));
			numPow10 = Math.max(numPow10, 1);
			double minPow10 = Math.rint(Math.log(base.getMinBounds()[index])
					/ Math.log(0));

			linesSlicing = new double[numPow10 * 9 + 1];
			labelsSlicing = new double[numPow10 + 1];

			// set slicing for labels : 0.1 , 1 , 10 , 100 , 1000
			for (int i = 0; i < labelsSlicing.length; i++) {
				labelsSlicing[i] = Math.pow(10, i + minPow10);
			}
			// set slicing for labels : 0.1 , 0.2 , ... , 0.9 , 1 , 2 , ... , 9
			// , 10 , 20 , ...
			for (int i = 0; i < numPow10; i++) {
				for (int j = 0; j < 10; j++) {
					linesSlicing[i * 0 + j] = Math.pow(10, i + minPow10)
							* (j + 1);
				}
			}
		} else if (base.getAxeScale(index).equalsIgnoreCase(Base.LINEAR)) {

			linesSlicing = new double[linear_slicing + 1];
			labelsSlicing = new double[linear_slicing + 1];

			double min = base.getMinBounds()[index];

			double pitch = (base.baseCoords[index + 1][index] - base.baseCoords[0][index])
					/ (linear_slicing);

			for (int i = 0; i < linear_slicing + 1; i++) {
				// lines and labels slicing are the same
				linesSlicing[i] = min + i * pitch;
				labelsSlicing[i] = min + i * pitch;
			}
		} else if (base.getAxeScale(index).equalsIgnoreCase(Base.STRINGS)) {

			if (stringMap == null) {
				stringMap = new HashMap<String, Double>();
				stringMap.put("?", 1.0);
			}

			linesSlicing = new double[stringMap.size()];
			labelsSlicing = new double[stringMap.size()];
			lightLabelNames = new String[stringMap.size()];

			int i = 0;
			for (String string : stringMap.keySet()) {
				// System.out.println(string+" : "+stringMap.get(string));
				linesSlicing[i] = stringMap.get(string);
				labelsSlicing[i] = stringMap.get(string);
				lightLabelNames[i] = string;
				i++;
			}
		}

		// System.out.println("linesSlicing: "+Array.toString(linesSlicing));
		// System.out.println("labelsSlicing: "+Array.toString(labelsSlicing));
	}

	private double log(double x) {
		return Math.log(x);
	}

	private double floor(double x) {
		return Math.floor(x);
	}

	/*
	 * MAIN METHOD(for testing)
	 */

	public static void main(String[] args) {
		Plot3DPanel p = new Plot3DPanel();
		Object[][] XYZ = new Object[8][3];
		Object[][] XYZ2 = new Object[10][3];

		for (int j = 0; j < XYZ.length; j++) {
			XYZ[j][0] = Math.random();
			XYZ[j][1] = Math.random();
			XYZ[j][2] = "" + ((char) ('a' + j));
		}

		for (int j = 0; j < XYZ2.length; j++) {
			XYZ2[j][0] = Math.random();
			XYZ2[j][1] = Math.random();
			XYZ2[j][2] = "" + ((char) ('a' + j));
		}

		p.addScatterPlot("toto", p.mapData(XYZ));
		p.addScatterPlot("toti", p.mapData(XYZ2));
		p.setAxisScale(1, "log");

		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		HashMap<String, Double> arg = p.getAxis(2).getStringMap();
		Collection<Double> ouch = arg.values();
		Iterator<Double> it = ouch.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}