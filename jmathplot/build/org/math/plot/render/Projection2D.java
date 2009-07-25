package org.math.plot.render;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class Projection2D extends Projection {

	public Projection2D(AWTDrawer _draw) {
		super(_draw);
		initBaseCoordsProjection();
	}

	protected double[] baseCoordsScreenProjectionRatio(double[] xy) {
		double[] sC = new double[2];
		sC[0] = (xy[0] - draw.canvas.base.roundXmin[0]) / (draw.canvas.base.roundXmax[0] - draw.canvas.base.roundXmin[0]);
		sC[1] = (xy[1] - draw.canvas.base.roundXmin[1]) / (draw.canvas.base.roundXmax[1] - draw.canvas.base.roundXmin[1]);
		// System.out.println("(" + xy[0] +"," + xy[1] + ") -> (" + sC[0] + ","
		// + sC[1] + ")");
		return sC;
	}
}