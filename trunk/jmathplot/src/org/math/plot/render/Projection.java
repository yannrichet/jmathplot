/*
 * Created on 31 mai 2005 by richet
 */
package org.math.plot.render;

import org.math.plot.plotObjects.*;

public abstract class Projection {

    protected int[][] baseScreenCoords;
    public static double DEFAULT_BORDER = 0.15;
    protected double borderCoeff = 1 - 2 * DEFAULT_BORDER;
    protected AWTDrawer draw;

    public Projection(AWTDrawer _draw) {
        draw = _draw;
    }

    protected void initBaseCoordsProjection(boolean reset) {
        // System.out.println("Projection.initBaseCoordsProjection");
        if (baseScreenCoords == null) {
            baseScreenCoords = new int[draw.canvas.base.baseCoords.length][2];
        }
        if (reset) {
            totalScreenRatio[0] = 1;
            totalScreenRatio[1] = 1;
        }
        for (int i = 0; i < draw.canvas.base.dimension + 1; i++) {
            // Compute the basis extremity coordinates in the normed-centered screen (ie [-0.5,0.5]x[-0.5,0.5] screen)
            double[] ratio = baseCoordsScreenProjectionRatio(draw.canvas.base.baseCoords[i]);
            // Compute the basis extremity coordinates in the true screen (ie in px: [0,400]x[0,400])
            baseScreenCoords[i][0] = (int) (draw.canvas.getWidth() * (.5 + (borderCoeff * ratio[0] / totalScreenRatio[0])));
            baseScreenCoords[i][1] = (int) (draw.canvas.getHeight() * (.5 - (borderCoeff * ratio[1] / totalScreenRatio[1])));
        }
        //System.err.println("\n" + Array.toString(baseScreenCoords));
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
    // This stores the whole zooming ratio along all dilate calls.
    double[] totalScreenRatio = new double[]{1, 1};

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
        // Update the zooming ratio history
        totalScreenRatio[0] = totalScreenRatio[0] * screenRatio[0];
        totalScreenRatio[1] = totalScreenRatio[1] * screenRatio[1];
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
            double normdist_pC_baseCoords = 0;
            if (draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.LOGARITHM)) {
                normdist_pC_baseCoords = ((log(pC[i]) - log(draw.canvas.base.baseCoords[0][i])) / (log(draw.canvas.base.baseCoords[i + 1][i]) - log(draw.canvas.base.baseCoords[0][i])));
            } else if (draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.LINEAR) || draw.canvas.base.axesScales[i].equalsIgnoreCase(Base.STRINGS)) {
                normdist_pC_baseCoords = ((pC[i] - draw.canvas.base.baseCoords[0][i]) / (draw.canvas.base.baseCoords[i + 1][i] - draw.canvas.base.baseCoords[0][i]));
            }
            sC[0] += normdist_pC_baseCoords * (baseScreenCoords[i + 1][0] - baseScreenCoords[0][0]);
            sC[1] += normdist_pC_baseCoords * (baseScreenCoords[i + 1][1] - baseScreenCoords[0][1]);
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

        return new int[]{(int) sC[0], (int) sC[1]};
    }

    public int[] screenProjectionBase(double... rC) {
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

        return new int[]{(int) sC[0], (int) sC[1]};
    }

    private double log(double x) {
        return Math.log(x);
    }

    protected abstract double[] baseCoordsScreenProjectionRatio(double[] xyz);
}
