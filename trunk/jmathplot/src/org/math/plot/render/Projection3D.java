package org.math.plot.render;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class Projection3D extends Projection {

	protected double theta;

	protected double phi;

	// protected boolean useRoundTrigonometry = false;

	public Projection3D(AWTDrawer _draw) {
		super(_draw);
		theta = Math.PI / 4;
		phi = Math.PI / 4;
		initBaseCoordsProjection();
	}

	protected double[] baseCoordsScreenProjectionRatio(double[] xyz) {
		double factor = 1.7;
		double[] sC = new double[2];
		sC[0] = 0.5
				+ (cos(theta)
						* ((xyz[1] - (draw.canvas.base.roundXmax[1] + draw.canvas.base.roundXmin[1]) / 2) / (draw.canvas.base.roundXmax[1] - draw.canvas.base.roundXmin[1])) - sin(theta)
						* ((xyz[0] - (draw.canvas.base.roundXmax[0] + draw.canvas.base.roundXmin[0]) / 2) / (draw.canvas.base.roundXmax[0] - draw.canvas.base.roundXmin[0])))
				/ factor;
		sC[1] = 0.5
				+ (cos(phi)
						* ((xyz[2] - (draw.canvas.base.roundXmax[2] + draw.canvas.base.roundXmin[2]) / 2) / (draw.canvas.base.roundXmax[2] - draw.canvas.base.roundXmin[2]))
						- sin(phi)
						* cos(theta)
						* ((xyz[0] - (draw.canvas.base.roundXmax[0] + draw.canvas.base.roundXmin[0]) / 2) / (draw.canvas.base.roundXmax[0] - draw.canvas.base.roundXmin[0])) - sin(phi)
						* sin(theta)
						* ((xyz[1] - (draw.canvas.base.roundXmax[1] + draw.canvas.base.roundXmin[1]) / 2) / (draw.canvas.base.roundXmax[1] - draw.canvas.base.roundXmin[1])))
				/ factor;
		// System.out.println("Theta = " + theta + " Phi = " + phi);
		// System.out.println("(" + xyz[0] +"," + xyz[1] +"," + xyz[2] + ") ->
		// (" + sC[0] + "," + sC[1] + ")");
		return sC;
	}

	// TODO test efficiceny of an approximation of cos and sin fuctions.
	
	/*
	 * private final static double _2PI = 2 * Math.PI;
	 * 
	 * private final static int N = 100;
	 * 
	 * private final static double[] COS =
	 * DoubleArray.f(DoubleArray.increment(N, 0, 2 * Math.PI / (N - 1)), new
	 * Function() { public double f(double x) { return Math.cos(x); } });
	 * 
	 * private final static double[] SIN =
	 * DoubleArray.f(DoubleArray.increment(N, 0, 2 * Math.PI / (N - 1)), new
	 * Function() { public double f(double x) { return Math.sin(x); } });
	 */
	private double cos(double x) {
		return Math.cos(x);
	}

	private double sin(double x) {
		return Math.sin(x);
	}

	public void setView(double _theta, double _phi) {
		theta = _theta;
		phi = _phi;
		initBaseCoordsProjection();
	}

	public void rotate(int[] screenTranslation, int[] dimension) {
		theta = theta - ((double) screenTranslation[0]) / 100;
		phi = phi + ((double) screenTranslation[1]) / 100;
		initBaseCoordsProjection();
	}

}