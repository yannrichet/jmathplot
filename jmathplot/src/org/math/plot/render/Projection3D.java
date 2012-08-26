package org.math.plot.render;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class Projection3D extends Projection {

    public double theta;
    public double phi;

    // protected boolean useRoundTrigonometry = false;
    public Projection3D(AWTDrawer _draw) {
        super(_draw);
        theta(Math.PI / 4);
        phi(Math.PI / 4);
        initBaseCoordsProjection(true);
    }
    double factor = 1.7;
    public double x0, y0, z0;
    public double cos_phi, sin_phi,tan_phi, cos_theta, sin_theta,tan_theta;
    static double pi = Math.PI;
    private void theta(double theta) {
        this.theta = theta;
        cos_theta = cos(theta);
        sin_theta = sin(theta);
        tan_theta = tan(theta);
    }

    private void phi(double phi) {
        this.phi = phi;
        cos_phi = cos(phi);
        sin_phi = sin(phi);
        tan_phi = tan(phi);
    }

    protected void initBaseCoordsProjection(boolean reset) {
        if (reset) {
            x0 = (draw.canvas.base.roundXmax[0] + draw.canvas.base.roundXmin[0]) / 2;
            y0 = (draw.canvas.base.roundXmax[1] + draw.canvas.base.roundXmin[1]) / 2;
            z0 = (draw.canvas.base.roundXmax[2] + draw.canvas.base.roundXmin[2]) / 2;
        }
        super.initBaseCoordsProjection(reset);
    }
    // search for (x0,y0,z0) , matching center of the screen [.5,.5] and closest to the center (.5,.5,.5) of the plot
    protected void updateCoordsCenterScreen() {
        double dx0 = (draw.canvas.getWidth() * .5 - baseScreenCoords[0][0]) / (baseScreenCoords[1][0] - baseScreenCoords[0][0]);
        double dy0 = (draw.canvas.getWidth() * .5 - baseScreenCoords[0][0]) / (baseScreenCoords[2][0] - baseScreenCoords[0][0]);

        double dz0 = (draw.canvas.getHeight() * .5 - baseScreenCoords[0][1]) / (baseScreenCoords[3][1] - baseScreenCoords[0][1]);
        double dx, dy, dz = 0;
        if ((theta - pi / 4) % pi > pi / 2) {
            dx = (.5 * (sin_theta + cos_theta) - tan_theta * dy0) / (sin_theta * sin_theta + cos_theta);
            dy = tan_theta * dx + dy0;
        } else {
            dy = (.5 * (sin_theta + cos_theta) - cos_theta * dx0) / (cos_theta / tan_theta + sin_theta);
            dx = 1 / tan_theta * dy + dx0;
        }
        dz = dz0 + .5 * tan_phi;

        // uuuhhh :) I've always dreamed to speak perl...
        dx = (dx < 0 ? 0 : (dx > 1 ? 1 : dx));
        dy = (dy < 0 ? 0 : (dy > 1 ? 1 : dy));
        dz = (dz < 0 ? 0 : (dz > 1 ? 1 : dz));

        x0 = draw.canvas.base.roundXmin[0] + (draw.canvas.base.roundXmax[0] - draw.canvas.base.roundXmin[0]) * dx;
        y0 = draw.canvas.base.roundXmin[1] + (draw.canvas.base.roundXmax[1] - draw.canvas.base.roundXmin[1]) * dy;
        z0 = draw.canvas.base.roundXmin[2] + (draw.canvas.base.roundXmax[2] - draw.canvas.base.roundXmin[2]) * dz;
        //System.err.println("(x0,y0,z0) = " + x0 + " " + y0 + " " + z0);
    }

    protected double[] baseCoordsScreenProjectionRatio(double[] xyz) {
        double normdist_xyz_x0 = ((xyz[0] - x0) / (draw.canvas.base.roundXmax[0] - draw.canvas.base.roundXmin[0]));
        double normdist_xyz_y0 = ((xyz[1] - y0) / (draw.canvas.base.roundXmax[1] - draw.canvas.base.roundXmin[1]));
        double normdist_xyz_z0 = ((xyz[2] - z0) / (draw.canvas.base.roundXmax[2] - draw.canvas.base.roundXmin[2]));

        double[] sC = new double[2];
        sC[0] = (cos_theta * normdist_xyz_y0
                - sin_theta * normdist_xyz_x0)
                / factor;
        sC[1] = (cos_phi * normdist_xyz_z0
                - sin_phi * cos_theta * normdist_xyz_x0
                - sin_phi * sin_theta * normdist_xyz_y0)
                / factor;
        //System.out.println("Theta = " + theta + " Phi = " + phi);
        // System.err.println("(" + xyz[0] +"," + xyz[1] +"," + xyz[2] + ") ->  (" + sC[0] + "," + sC[1] + ")");
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

    private double tan(double x) {
        return Math.tan(x);
    }

    private double sin(double x) {
        return Math.sin(x);
    }

    public void rotate(double _theta, double _phi) {
        theta(_theta);
        phi(_phi);
        initBaseCoordsProjection(false);
    }

    public void rotate(int[] screenTranslation, int[] dimension) {
        theta(theta - ((double) screenTranslation[0]) / 100);
        phi(phi + ((double) screenTranslation[1]) / 100);
        initBaseCoordsProjection(false);
    }
}