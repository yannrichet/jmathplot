package org.math.plot.plotObjects;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */

import java.awt.*;

import org.math.plot.render.*;

public class BasePlot implements /*Plotable,*/BaseDependant {

	public static Color DEFAULT_COLOR = Color.DARK_GRAY;

	protected Base base;

	protected Axis[] axis;

	protected boolean visible = true;

	protected Color color;

	public BasePlot(Base b, String... as) {
		this(b, DEFAULT_COLOR, as);
	}

	public BasePlot(Base b, Color c, Axis... a) {
		base = b;
		axis = a;
		color = c;
	}

	public BasePlot(Base b, Color c, String... as) {
		base = b;
		if (as.length != base.dimension) {
			throw new IllegalArgumentException("String array of axes names must have " + base.dimension + " elements.");
		}
		color = c;
		axis = new Axis[base.dimension];
		for (int i = 0; i < base.dimension; i++) {
			axis[i] = new Axis(base, as[i], color, i);
		}
		// resetBase();
	}

	public void setVisible(boolean v) {
		visible = v;
	}

	public void setVisible(int i, boolean v) {
		axis[i].setVisible(v);
	}

	public void setGridVisible(int i, boolean v) {
		axis[i].setGridVisible(v);
	}

	public boolean getVisible() {
		return visible;
	}

	public void setColor(Color c) {
		color = c;
		for (int i = 0; i < axis.length; i++) {
			axis[i].setColor(c);
		}
	}

	public Color getColor() {
		return color;
	}

	public void setLegend(String[] as) {
		if (as.length != base.dimension) {
			throw new IllegalArgumentException("String array of axes names must have " + base.dimension + " elements.");
		}
		for (int i = 0; i < axis.length; i++) {
			axis[i].setLegend(as[i]);
		}
		// resetBase();
	}

	public void setLegend(int i, String as) {
		axis[i].setLegend(as);
		// resetBase();
	}

	public String[] getLegend() {
		String[] array = new String[axis.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = axis[i].getLegend();
		}
		return array;
	}

	public String getLegend(int i) {
		return axis[i].getLegend();
	}

	public void setBase(Base b) {
		base = b;
		for (int i = 0; i < axis.length; i++) {
			axis[i].base = base;
		}
		resetBase();
	}

	public void plot(AbstractDrawer draw) {
		if (!visible)
			return;

		for (int i = 0; i < axis.length; i++)
			axis[i].plot(draw);
	}

	public Axis getAxis(int i) {
		return axis[i];
	}

	public Axis[] getAxis() {
		return axis;
	}

	public void resetBase() {
		// System.out.println("BasePlot.resetBase");
		for (int i = 0; i < axis.length; i++) {
			axis[i].resetBase();
			//base.setAxesScales(i, Base.LINEAR);
		}
	}

}