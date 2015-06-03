package org.math.plot.plotObjects;

import java.awt.*;

import org.math.plot.render.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class Label implements Plotable {

	protected double[] coord;

	protected double[] base_offset;

	protected String label;

	protected Color color;

	protected double cornerN = 0.5;

	protected double cornerE = 0.5;

	boolean visible = true;

	public double angle = 0;

	public Font font = AbstractDrawer.DEFAULT_FONT;

	// private static DecimalFormat dec = new DecimalFormat("##0.###E0");

	public Label(String l, Color col, double... c) {
		label = l;
		coord = c;
		color = col;
	}

	public Label(String l, double... c) {
		this(l, AbstractDrawer.DEFAULT_COLOR, c);
	}

	/**
	 * show coord itself
	 */
	public Label(double... c) {
		this(coordToString(c), AbstractDrawer.DEFAULT_COLOR, c);
	}

	public void setText(String _t) {
		label = _t;
	}

	public String getText() {
		return label;
	}

	public void setCoord(double... _c) {
		coord = _c;
	}

	public void setColor(Color c) {
		color = c;
	}

	public Color getColor() {
		return color;
	}

	/**
	 * reference point center: 0.5, 0.5 lowerleft: 0,0 upperleft 1, 0 ...
	 */
	public void setCorner(double north_south, double east_west) {
		cornerN = north_south;
		cornerE = east_west;
	}

	public void setVisible(boolean v) {
		visible = v;
	}

	public boolean getVisible() {
		return visible;
	}

	/**
	 * shift by given screen coordinates offset
	 */
	/*
	 * public void setOffset(double[] offset) { double[] newCoord =
	 * coord.getPlotCoordCopy(); for (int i = 0; i < newCoord.length; i++) {
	 * newCoord[i] += offset[i]; } coord.setPlotCoord(newCoord); }
	 */

	/**
	 * see Text for formatted text output
	 */
	public void plot(AbstractDrawer draw) {
		if (!visible) return;
		
		draw.setColor(color);
		draw.setFont(font);
		draw.setBaseOffset(base_offset);
		draw.setTextOffset(cornerE, cornerN);
		draw.setTextAngle(angle);
		draw.drawText(label, coord);
		draw.setBaseOffset(null);
	}

	public void rotate(double _angle) {
		angle = _angle;
	}

	public void setFont(Font _font) {
		font = _font;
	}

	public static double approx(double val, int decimal) {
		// double timesEn = val*Math.pow(10,decimal);
		// if (Math.rint(timesEn) == timesEn) {
		// return val;
		// } else {
		// to limit precision loss, you need to separate cases where decimal<0
		// and >0
		// if you don't you'll have this : approx(10000.0,-4) => 10000.00000001
		if (decimal < 0) {
			return Math.rint(val / Math.pow(10, -decimal)) * Math.pow(10, -decimal);
		} else {
			return Math.rint(val * Math.pow(10, decimal)) / Math.pow(10, decimal);
		}
		// }
	}

	public static String coordToString(double... c) {
		StringBuffer sb = new StringBuffer("(");
		for (int i = 0; i < c.length; i++)
			sb.append(approx(c[i], 2)).append(",");
		// sb.append(dec.format(c.getPlotCoordCopy()[i])).append(",");

		sb.setLength(sb.length() - 1);
		if (sb.length() > 0)
			sb.append(")");

		return sb.toString();
	}

	public Font getFont() {
		return font;
	}
}