/*
 * Created on 31 mai 2005 by richet
 */
package org.math.plot.render;

import org.math.plot.plotObjects.*;

public abstract class Projection {

	int[][] baseScreenCoords;

	public static double DEFAULT_BORDER = 0.15;

	protected double borderCoeff = DEFAULT_BORDER;

	protected AWTDrawer draw;

	public Projection(AWTDrawer _draw) {
		draw = _draw;
	}

	protected void initBaseCoordsProjection() {
		// System.out.println("Projection.initBaseCoordsProjection");
		baseScreenCoords = new int[draw.canvas.base.baseCoords.length][2];
		for (int i = 0; i < draw.canvas.base.dimension + 1; i++) {
			double[] ratio = baseCoordsScreenProjectionRatio(draw.canvas.base.baseCoords[i]);
			baseScreenCoords[i][0] = (int) (draw.canvas.getWidth() * (borderCoeff + (1 - 2 * borderCoeff) * ratio[0]));
			baseScreenCoords[i][1] = (int) (draw.canvas.getHeight() - draw.canvas.getHeight() * (borderCoeff + (1 - 2 * borderCoeff) * ratio[1]));
		}
	}

	// ///////////////////////////////////////////
	// ////// move methods ///////////////////////
	// ///////////////////////////////////////////

	public void translate(int[] screenTranslation) {
		for (int i = 0; i < draw.canvas.base.dimension + 1; i++) {
			baseScreenCoords[i][0] = baseScreenCoords[i][0] + screenTranslation[0];
			baseScreenCoords[i][1] = baseScreenCoords[i][1] + screenTranslation[1];
		}
	}

	public void dilate(int[] screenOrigin, double[] screenRatio) {
		// System.out.println("screenOrigin = "+screenOrigin[0]+" ,
		// "+screenOrigin[1]);
		// System.out.println("screenRatio = "+screenRatio[0]+" ,
		// "+screenRatio[1]);
		for (int i = 0; i < draw.canvas.base.dimension + 1; i++) {
			// System.out.println("baseScreenCoords["+i+"] =
			// "+baseScreenCoords[i][0]+" , "+baseScreenCoords[i][1]);
			baseScreenCoords[i][0] = (int) ((baseScreenCoords[i][0] - screenOrigin[0]) / screenRatio[0]);
			baseScreenCoords[i][1] = (int) ((baseScreenCoords[i][1] - screenOrigin[1]) / screenRatio[1]);
			// System.out.println(" -> baseScreenCoords["+i+"] =
			// "+baseScreenCoords[i][0]+" , "+baseScreenCoords[i][1]);
		}
	}

	// ///////////////////////////////////////////
	// ////// projection method //////////////////
	// ///////////////////////////////////////////

	public int[] screenProjection(double... pC) {
		// System.out.println("Projection.screenProjection("+Array.toString(pC)+")");
		double[] sC = new double[2];
		sC[0] = baseScreenCoords[0][0];
		sC[1] = baseScreenCoords[0][1];
		for (int i = 0; i < draw.canvas.base.dimension; i++) {
			if (draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.LOGARITHM)) {
				sC[0] += ((log(pC[i]) - log(draw.canvas.base.baseCoords[0][i])) / (log(draw.canvas.base.baseCoords[i + 1][i]) - log(draw.canvas.base.baseCoords[0][i])))
						* (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
				sC[1] += ((log(pC[i]) - log(draw.canvas.base.baseCoords[0][i])) / (log(draw.canvas.base.baseCoords[i + 1][i]) - log(draw.canvas.base.baseCoords[0][i])))
						* (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
			} else if (draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.LINEAR)||draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.STRINGS)) {
				sC[0] += ((pC[i] - draw.canvas.base.baseCoords[0][i]) / (draw.canvas.base.baseCoords[i + 1][i] - draw.canvas.base.baseCoords[0][i]))
						* (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
				sC[1] += ((pC[i] - draw.canvas.base.baseCoords[0][i]) / (draw.canvas.base.baseCoords[i + 1][i] - draw.canvas.base.baseCoords[0][i]))
						* (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
			}
		}

		if (draw.base_offset != null) {
			for (int i = 0; i < draw.canvas.base.dimension; i++) {
				sC[0] += draw.base_offset[i] * (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
				sC[1] += draw.base_offset[i] * (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
			}
		}
		
		if (draw.screen_offset != null) {
				sC[0] += draw.screen_offset[0];
				sC[1] += draw.screen_offset[1];
		}

		return new int[] { (int) sC[0], (int) sC[1] };
	}

	public int[] screenProjectionBaseRatio(double... rC) {
		double[] sC = new double[2];
		sC[0] = baseScreenCoords[0][0];
		sC[1] = baseScreenCoords[0][1];
		for (int i = 0; i < draw.canvas.base.dimension; i++) {
			sC[0] += rC[i] * (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
			sC[1] += rC[i] * (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
		}
		
		if (draw.base_offset != null) {
			for (int i = 0; i < draw.canvas.base.dimension; i++) {
				sC[0] += draw.base_offset[i] * (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
				sC[1] += draw.base_offset[i] * (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
			}
		}
		
		if (draw.screen_offset != null) {
				sC[0] += draw.screen_offset[0];
				sC[1] += draw.screen_offset[1];
		}
		
		return new int[] { (int) sC[0], (int) sC[1] };
	}

	private double log(double x) {
		return Math.log(x);
	}

	protected abstract double[] baseCoordsScreenProjectionRatio(double[] xyz);

}
