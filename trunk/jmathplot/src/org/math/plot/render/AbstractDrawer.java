/*
 * Created on 1 juin 2005 by richet
 */
package org.math.plot.render;

import java.awt.*;

import org.math.plot.canvas.*;
import org.math.plot.plotObjects.Label;
import org.math.plot.utils.*;

public abstract class AbstractDrawer {

	// TODO comment this source for other implementations : java3d, jogl or vtk

	public PlotCanvas canvas;

	protected Graphics2D comp2D;

	public final static int ROUND_DOT = 1;

	public final static int CROSS_DOT = 2;

	public final static int PATTERN_DOT = 0;

	public final static int CONTINOUS_LINE = 1;

	public final static int DOTTED_LINE = 2;

	public final static int DEFAULT_DOT_RADIUS = 2;

	public final static int DEFAULT_LINE_WIDTH = 1;

	public final static boolean[][] DOT_TRIANGLE_PATTERN = stringToPattern("_", "___#___", "__#_#__", "__#_#__", "_#___#_", "_#___#_", "#######");

	public final static boolean[][] DOT_SQUARE_PATTERN = stringToPattern("_", "######",  "#____#", "#____#",  "#____#", "#____#", "######");

	public final static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 10);

	public final static Color DEFAULT_COLOR = Color.BLACK;

	protected Font font = DEFAULT_FONT;

	protected double text_Eastoffset, text_Northoffset;

	protected double text_angle;

	protected Color color = DEFAULT_COLOR;

	protected double[] base_offset;
	
	protected int[] screen_offset;
	
	//protected double alpha;

	protected int dot_type = ROUND_DOT;

	protected int dot_radius = DEFAULT_DOT_RADIUS;

	protected boolean[][] dot_pattern = DOT_TRIANGLE_PATTERN;

	protected int line_type = CONTINOUS_LINE;

	protected int line_width = DEFAULT_LINE_WIDTH;

	//protected boolean[][] line_pattern = DOT_TRIANGLE_PATTERN;

	public AbstractDrawer(PlotCanvas _canvas) {
		canvas = _canvas;
	}

	/**
	 * Method used to initialize drawer to DEFAULT values
	 */
	public void initGraphics(Graphics2D _comp2D) {
		comp2D = _comp2D;
	}

	/**
	 * Method used to reinitialize the plot when the base has changed (bounds or
	 * scale)
	 */
	public abstract void resetBaseProjection();

	public void setColor(Color c) {
		color = c;
	}
	
	public abstract void setGradient(double[] xy0, Color c0, double[] xy1, Color c1);
	
	public void resetGradient() {
		comp2D.setPaint(color);
	}
	

	public void setFont(Font f) {
		font = f;
	}

	public void setTextOffset(double _cornerEast, double _cornerNorth) {
		text_Eastoffset = _cornerEast;
		text_Northoffset = _cornerNorth;
	}

	public void setTextAngle(double _angle) {
		text_angle = _angle;
	}

	public void setDotType(int _dot_type) {
		dot_type = _dot_type;
	}

	public void setDotRadius(int _dot_radius) {
		dot_radius = _dot_radius;
	}

	public void setDotPattern(boolean[][] _dot_pattern) {
		dot_pattern = _dot_pattern;
	}

	public void setLineType(int _line_type) {
		line_type = _line_type;
	}

	public void setLineWidth(int _line_width) {
		line_width = _line_width;
	}

	public void setBaseOffset(double... _boffset) {
		base_offset = _boffset;
	}
	
	public void setScreenOffset(int... _soffset) {
		screen_offset = _soffset;
	}
	
	/*public void setAlpha(double _alpha) {
		alpha = _alpha;
	}*/

	public Color getColor() {
		return color;
	}

	public Font getFont() {
		return font;
	}

	public double[] getTextOffset() {
		return new double[] { text_Eastoffset, text_Northoffset };
	}

	public double getTextAngle() {
		return text_angle;
	}

	public int getDotType() {
		return dot_type;
	}

	public int getDotRadius() {
		return dot_radius;
	}

	public boolean[][] getDotPattern() {
		return dot_pattern;
	}

	public double[] getBaseOffset() {
		return base_offset;
	}
	
	public int[] getScreenOffset() {
		return screen_offset;
	}

	/**
	 * Returns the screen coordinates coresponding to plot coordinates Used to
	 * test if mouse is pointing on a plot.
	 * 
	 * @param pC
	 *            plot ccordinates to project in screen
	 * @return scrren coordinates
	 */
	public abstract int[] project(double... pC);

	/**
	 * Returns the screen coordinates coresponding to plot coordinates Used to
	 * test if mouse is pointing on a plot.
	 * 
	 * @param pC
	 *            plot ccordinates to project in screen
	 * @return scrren coordinates
	 */
	public abstract int[] projectBase(double... rC);

	/**
	 * Plot ActionMode : translation of the plot
	 * 
	 * @param t
	 *            mouse translation in pixels
	 */
	public abstract void translate(int... t);

	/**
	 * Plot ActionMode : dilatation of the plot
	 * 
	 * @param screenOrigin
	 *            mouse initial position
	 * @param screenRatio
	 *            mouse final position relative to plot panel size
	 */
	public abstract void dilate(int[] screenOrigin, double[] screenRatio);

	public void drawCoordinate(double[] pC) {
		for (int i = 0; i < pC.length; i++) {
			double[] axeprojection = Array.copy(pC);
			axeprojection[i] = canvas.base.baseCoords[0][i];
			drawLine(pC, axeprojection);
		}
		setTextAngle(0);
		setTextOffset(0, 0);
		//drawText(Label.coordToString(pC), pC);
	}

	public abstract void drawText(String label, double... pC);

	public abstract void drawTextBase(String label, double... rC);

	public abstract void drawLineBase(double[]... rC);

	public abstract void drawLine(double[]... pC);

	public abstract void drawDot(double... pC);

	public abstract void drawPolygon(double[]... pC);

	public abstract void fillPolygon(float alpha,double[]... pC);

	public abstract void drawImage(Image img, float alpha,double[] _xyzSW, double[] _xyzSE,double[] _xyzNW);

	// needs to be discussed... Maybe a geometric addon should be more interesting...
	//public abstract void drawShape(Shape shape, float alpha,double[] _xyzSW, double[] _xyzSE,double[] _xyzNW);
	
	public static boolean[][] stringToPattern(String empty, String... c) {
		boolean[][] p = new boolean[c.length][];
		for (int i = 0; i < p.length; i++)
			p[i] = stringToPattern(empty, c[i]);
		return p;
	}

	public static boolean[] stringToPattern(String empty, String c) {
		boolean[] p = new boolean[c.length()];
		for (int i = 0; i < p.length; i++)
			p[i] = !(c.substring(i, i + 1).equals(empty));
		return p;
	}
}