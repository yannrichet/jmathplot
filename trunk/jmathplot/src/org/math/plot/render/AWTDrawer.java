/*
 * Created on 31 mai 2005 by richet
 */
package org.math.plot.render;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import org.math.plot.canvas.*;

import static java.lang.Math.*;

public abstract class AWTDrawer extends AbstractDrawer {

    protected Projection projection;

    public AWTDrawer(PlotCanvas _canvas) {
        super(_canvas);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#resetProjection()
     */
    public void resetBaseProjection() {
        projection.initBaseCoordsProjection();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#setColor(java.awt.Color)
     */
    public void setColor(Color c) {
        comp2D.setColor(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#setGradient
     */
    public void setGradient(double[] xy0, Color c0, double[] xy1, Color c1) {
        int[] s0 = project(xy0);
        int[] s1 = project(xy1);
        comp2D.setPaint(new GradientPaint(s0[0], s0[1], c0, s1[0], s1[1], c1));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#setFont(java.awt.Font)
     */
    public void setFont(Font f) {
        comp2D.setFont(f);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#getColor()
     */
    public Color getColor() {
        return comp2D.getColor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#getFont()
     */
    public Font getFont() {
        return comp2D.getFont();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#project(double[])
     */
    public int[] project(double... pC) {
        return projection.screenProjection(pC);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#projectRatio(double[])
     */
    public int[] projectBase(double... rC) {
        return projection.screenProjectionBaseRatio(rC);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#translate(int[])
     */
    public void translate(int... t) {
        projection.translate(t);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#dilate(int[], double[])
     */
    public void dilate(int[] screenOrigin, double[] screenRatio) {
        projection.dilate(screenOrigin, screenRatio);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawString(java.lang.String,
     *      double[], double, double, double)
     */
    public void drawText(String label, double... pC) {
        int[] sC = projection.screenProjection(pC);

        // Corner offset adjustment : Text Offset is used Here
        FontRenderContext frc = comp2D.getFontRenderContext();
        Font font1 = comp2D.getFont();
        int x = sC[0];
        int y = sC[1];
        double w = font1.getStringBounds(label, frc).getWidth();
        double h = font1.getSize2D();
        x -= (int) (w * text_Eastoffset);
        y += (int) (h * text_Northoffset);

        if (text_angle != 0) {
            comp2D.rotate(text_angle, x + w / 2, y - h / 2);
        }

        comp2D.drawString(label, x, y);

        if (text_angle != 0) {
            comp2D.rotate(-text_angle, x + w / 2, y - h / 2);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawStringRatio(java.lang.String,
     *      double[], double, double, double)
     */
    public void drawTextBase(String label, double... rC) {
        int[] sC = projection.screenProjectionBaseRatio(rC);

        // Corner offset adjustment : Text Offset is used Here
        FontRenderContext frc = comp2D.getFontRenderContext();
        Font font1 = comp2D.getFont();
        int x = sC[0];
        int y = sC[1];
        double w = font1.getStringBounds(label, frc).getWidth();
        double h = font1.getSize2D();
        x -= (int) (w * text_Eastoffset);
        y += (int) (h * text_Northoffset);

        if (text_angle != 0) {
            comp2D.rotate(text_angle, x + w / 2, y - h / 2);
        }

        comp2D.drawString(label, x, y);

        if (text_angle != 0) {
            comp2D.rotate(-text_angle, x + w / 2, y - h / 2);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawLineRatio(double[],
     *      double[])
     */
    public void drawLineBase(double[]... rC) {
        int[][] sC = new int[rC.length][];
        for (int i = 0; i < sC.length; i++) {
            sC[i] = projection.screenProjectionBaseRatio(rC[i]);
        }
        drawLine(sC);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawLine(double[], double[])
     */
    public void drawLine(double[]... pC) {
        int[][] sC = new int[pC.length][];
        for (int i = 0; i < sC.length; i++) {
            sC[i] = projection.screenProjection(pC[i]);
        }
        drawLine(sC);
    }

    private void drawLine(int[]... c) {
        Stroke s = null;
        switch (line_type) {
            case CONTINOUS_LINE:
                s = new BasicStroke(line_width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
                break;
            case DOTTED_LINE:
                s = new BasicStroke(line_width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f);
                break;
        }
        comp2D.setStroke(s);

        int[] x = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            x[i] = c[i][0];
        }
        int[] y = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            y[i] = c[i][1];
        }
        comp2D.drawPolyline(x, y, c.length);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawDot(double[])
     */
    public void drawDot(double... pC) {
        int[] sC = projection.screenProjection(pC);
        switch (dot_type) {
            case ROUND_DOT:
                comp2D.fillOval(sC[0] - dot_radius, sC[1] - dot_radius, 2 * dot_radius, 2 * dot_radius);
                break;
            case CROSS_DOT:
                comp2D.drawLine(sC[0] - dot_radius, sC[1] - dot_radius, sC[0] + dot_radius, sC[1] + dot_radius);
                comp2D.drawLine(sC[0] + dot_radius, sC[1] - dot_radius, sC[0] - dot_radius, sC[1] + dot_radius);
                break;
            case PATTERN_DOT:
                int yoffset = (int) Math.ceil(dot_pattern.length / 2.0);
                int xoffset = (int) Math.ceil(dot_pattern[0].length / 2.0);
                for (int i = 0; i < dot_pattern.length; i++) {
                    for (int j = 0; j < dot_pattern[i].length; j++) {
                        if (dot_pattern[i][j]) // comp2D.setColor(new Color(getColor())
                        {
                            comp2D.fillRect(sC[0] - xoffset + j, sC[1] - yoffset + i, 1, 1);
                        }
                    }
                }
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#drawPloygon(double[][])
     */
    public void drawPolygon(double[]... pC) {
        int[][] c = new int[pC.length][2];
        for (int i = 0; i < pC.length; i++) {
            c[i] = projection.screenProjection(pC[i]);
        }

        int[] x = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            x[i] = c[i][0];
        }
        int[] y = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            y[i] = c[i][1];
        }
        comp2D.drawPolygon(x, y, c.length);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.math.plot.render.AbstractDrawer#fillPloygon(double[][])
     */
    public void fillPolygon(float alpha, double[]... pC) {
        int[][] c = new int[pC.length][2];
        for (int i = 0; i < pC.length; i++) {
            c[i] = projection.screenProjection(pC[i]);
        }

        int[] x = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            x[i] = c[i][0];
        }
        int[] y = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            y[i] = c[i][1];
        }
        Composite cs = comp2D.getComposite();
        comp2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        comp2D.fillPolygon(x, y, c.length);
        comp2D.setComposite(cs);
    }

    public void drawImage(Image img, float alpha, double[] _xyzSW, double[] _xyzSE, double[] _xyzNW) {
        Composite cs = comp2D.getComposite();
        comp2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        AffineTransform t = getAffineTransform(img.getWidth(canvas), img.getHeight(canvas), _xyzSW, _xyzSE, _xyzNW);
        if (t != null) {
            comp2D.drawImage(img, t, canvas);
        }
        comp2D.setComposite(cs);
    }

    /*public void drawShape(Shape shape, float alpha, double[] _xyzSW, double[] _xyzSE, double[] _xyzNW) {
    AffineTransform t = getAffineTransform(shape.getBounds().width,shape.getBounds().height, _xyzSW,  _xyzSE,  _xyzNW);
    Shape t_shape = t.createTransformedShape(shape);
    Composite cs = comp2D.getComposite();
    comp2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    comp2D.draw(t_shape);
    comp2D.setComposite(cs);
    }*/
    static boolean isDiff(double[] x, int[] y) {
        return abs(x[0] - y[0]) > 1 || abs(x[1] - y[1]) > 1;
    }

    static double sign(double x) {
        if (x != 0) {
            return signum(x);
        } else {
            return 1.0;
        }
    }

    static double sqr(double x) {
        return x * x;
    }

    public AffineTransform getAffineTransform(int width, int height, double[] _xyzSW, double[] _xyzSE, double[] _xyzNW) {
        int[] cornerNW = projection.screenProjection(_xyzNW);
        int[] cornerSE = projection.screenProjection(_xyzSE);
        int[] cornerSW = projection.screenProjection(_xyzSW);

        double[] vectWE = {(double) cornerSE[0] - (double) cornerSW[0], (double) cornerSE[1] - (double) cornerSW[1]};
        double normvectWE = sqrt(sqr(vectWE[0]) + sqr(vectWE[1]));
        double[] vectSN = {(double) cornerNW[0] - (double) cornerSW[0], (double) cornerNW[1] - (double) cornerSW[1]};
        double normvectSN = sqrt(sqr(vectSN[0]) + sqr(vectSN[1]));
        double angleSW = acos((vectWE[0] * vectSN[0] + vectWE[1] * vectSN[1]) / (normvectWE * normvectSN));

        if (angleSW == 0.0) {
            return null;
        }

        AffineTransform t = new AffineTransform();

        t.translate(cornerNW[0], cornerNW[1]);
        t.scale(sign(vectWE[0]), -sign(vectSN[1]));
        t.rotate(-atan(vectSN[0] / vectSN[1]));
        t.shear(0, 1 / tan(PI - angleSW));
        t.scale(normvectWE * cos(angleSW - PI / 2) / (double) width, normvectSN / (double) height);

        double[] _cornerSE_tr = new double[2];
        double[] _cornerSE = {width, height};
        t.transform(_cornerSE, 0, _cornerSE_tr, 0, 1);

        if (isDiff(_cornerSE_tr, cornerSE)) {
            double[] vectSE_NW_1 = {(double) cornerNW[0] - (double) cornerSE[0], (double) cornerNW[1] - (double) cornerSE[1]};
            double[] vectSE_NW_2 = {(double) cornerNW[0] - (double) _cornerSE_tr[0], (double) cornerNW[1] - (double) _cornerSE_tr[1]};

            double normvect_1 = sqrt(sqr(vectSE_NW_1[0]) + sqr(vectSE_NW_1[1]));
            double normvect_2 = sqrt(sqr(vectSE_NW_1[0]) + sqr(vectSE_NW_1[1]));

            double cos_angle = (((vectSE_NW_1[0] * vectSE_NW_2[0] + vectSE_NW_1[1] * vectSE_NW_2[1]) / (normvect_1 * normvect_2)));
            double vect = (vectSE_NW_1[0] * vectSE_NW_2[1] - vectSE_NW_1[1] * vectSE_NW_2[0]);

            AffineTransform t2 = new AffineTransform();
            if (vect < 0) {
                t2.rotate(acos(cos_angle), cornerNW[0], cornerNW[1]);
            } else {
                t2.rotate(-acos(cos_angle), cornerNW[0], cornerNW[1]);
            }
            t.preConcatenate(t2);
        }

        // TODO patch for many cases...

        /*double[] _cornerSW_tr = new double[2];
        double[] _cornerSW = { 0, img.getHeight(canvas) };
        t.transform(_cornerSW, 0, _cornerSW_tr, 0, 1);

        if (isDiff(_cornerSW_tr, cornerSW)) {
        double[] vectSW_NW_1 = { (double) cornerNW[0] - (double) cornerSW[0], (double) cornerNW[1] - (double) cornerSW[1] };
        double[] vectSW_NW_2 = { (double) cornerNW[0] - (double) _cornerSW_tr[0], (double) cornerNW[1] - (double) _cornerSW_tr[1] };

        double normvect_1 = sqrt(sqr(vectSW_NW_1[0]) + sqr(vectSW_NW_1[1]));
        double normvect_2 = sqrt(sqr(vectSW_NW_1[0]) + sqr(vectSW_NW_1[1]));

        double cos_angle = (((vectSW_NW_1[0] * vectSW_NW_2[0] + vectSW_NW_1[1] * vectSW_NW_2[1]) / (normvect_1 * normvect_2)));
        double vect = (vectSW_NW_1[0] * vectSW_NW_2[1] - vectSW_NW_1[1] * vectSW_NW_2[0]);

        System.out.println(cos_angle + " " + vect + " -> " + toDegrees(acos(cos_angle)));

        //System.out.println(" "+vectSE_NW_1[0]+","+vectSE_NW_1[1]+"  "+vectSE_NW_2[0]+","+vectSE_NW_2[1]);
        AffineTransform t2 = new AffineTransform();
        if (vect > 0)
        t2.rotate(acos(cos_angle), cornerNW[0], cornerNW[1]);
        else
        t2.rotate(-acos(cos_angle), cornerNW[0], cornerNW[1]);
        t.preConcatenate(t2);

        }*/

        return t;
    }
}
