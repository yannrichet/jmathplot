/*
 * Created on 3 juin 2005 by richet
 */
package org.math.plot.plots;

import java.awt.*;
import java.util.TreeSet;

import org.math.plot.*;
import org.math.plot.render.*;
import org.math.plot.utils.Array;

public class GridPlot3D extends Plot {

    double[] X;
    double[] Y;
    double[][] Z;
    private double[][] XYZ_list;
    private Color[][] colors = null;
    public boolean draw_lines = true;
    public boolean fill_shape = true;

    public GridPlot3D(String n, Color c, double[] _X, double[] _Y, double[][] _Z) {
        this(n, new Color[][] { new Color[] { c } }, _X, _Y, _Z);
    }

    public GridPlot3D(String n, Color[][] c, double[] _X, double[] _Y, double[][] _Z) {
        super(n, c[0][0]);
    	colors = c;
        X = _X;
        Y = _Y;
        Z = _Z;
        buildXYZ_list();
    }

    public void plot(AbstractDrawer draw, Color[] c) {
        if (!visible) {
            return;
        }
        
        boolean monoColor = false;
        if (colors.length == 1) {
        	monoColor = true;
        }
        else if (colors.length != Y.length || colors[0].length != X.length) {
        	throw new IllegalArgumentException("Color array length must match length of data arrays.");
        }
        
        if (monoColor) {
            draw.setColor(c[0]);
        }
        
    	double [] cP;
    	double [] nP;
        if (draw_lines) {
            draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
            for (int i = 0; i < X.length; i++) {
                for (int j = 0; j < Y.length - 1; j++) {
                	cP = new double[]{X[i], Y[j], Z[j][i]};
                	nP = new double[]{X[i], Y[j + 1], Z[j + 1][i]};
                	if (!monoColor) {
        				draw.setGradient(cP, colors[j][i], nP, colors[j + 1][i]);
                	}
                    draw.drawLine(cP, nP);
                }
            }

            for (int j = 0; j < Y.length; j++) {
                for (int i = 0; i < X.length - 1; i++) {
                	cP = new double[]{X[i], Y[j], Z[j][i]};
                	nP = new double[]{X[i + 1], Y[j], Z[j][i + 1]};
                	if (!monoColor) {
        				draw.setGradient(cP, colors[j][i], nP, colors[j][i + 1]);
                	}
                    draw.drawLine(cP, nP);
                }
            }
        } else {
            draw.setDotType(AbstractDrawer.ROUND_DOT);
            draw.setDotRadius(AbstractDrawer.DEFAULT_DOT_RADIUS);
            for (int i = 0; i < X.length; i++) {
                for (int j = 0; j < Y.length; j++) {
                	if (!monoColor) {
                		draw.setColor(colors[j][i]);
                	}
                    draw.drawDot(new double[]{X[i], Y[j], Z[j][i]});
                }
            }
        }

        if (fill_shape) {
        	if (monoColor) {
	            for (int j = 0; j < Y.length - 1; j++) {
	                for (int i = 0; i < X.length - 1; i++) {
	                    draw.fillPolygon(0.2f, new double[]{X[i], Y[j], Z[j][i]}, new double[]{X[i + 1], Y[j], Z[j][i + 1]}, new double[]{X[i + 1], Y[j + 1],
	                                Z[j + 1][i + 1]}, new double[]{X[i], Y[j + 1], Z[j + 1][i]});
	                }
	            }
        	}
        	else {
	            for (int j = 0; j < Y.length - 1; j++) {
	                for (int i = 0; i < X.length - 1; i++) {
	                	cP = new double[]{X[i], Y[j], Z[j][i]};
	                	nP = new double[]{X[i], Y[j + 1], Z[j + 1][i]};
        				draw.setGradient(cP, colors[j][i], nP, colors[j + 1][i]);
	                    draw.fillPolygon(0.2f, cP, new double[]{X[i + 1], Y[j], Z[j][i + 1]}, new double[]{X[i + 1], Y[j + 1],
	                                Z[j + 1][i + 1]}, nP);

	                	cP = new double[]{X[i], Y[j], Z[j][i]};
	                	nP = new double[]{X[i + 1], Y[j], Z[j][i + 1]};
        				draw.setGradient(cP, colors[j][i], nP, colors[j][i + 1]);
	                    draw.fillPolygon(0.2f, cP, nP, new double[]{X[i + 1], Y[j + 1],
	                                Z[j + 1][i + 1]}, new double[]{X[i], Y[j + 1], Z[j + 1][i]});
	                }
	            }
	            for (int j = 1; j < Y.length; j++) {
	                for (int i = 1; i < X.length; i++) {
	                	cP = new double[]{X[i], Y[j], Z[j][i]};
	                	nP = new double[]{X[i], Y[j - 1], Z[j - 1][i]};
	                	draw.setGradient(cP, colors[j][i], nP, colors[j - 1][i]);
	                    draw.fillPolygon(0.2f, cP, new double[]{X[i - 1], Y[j], Z[j][i - 1]}, new double[]{X[i - 1], Y[j - 1],
	                            	Z[j - 1][i - 1]}, nP);

	                	cP = new double[]{X[i], Y[j], Z[j][i]};
	                	nP = new double[]{X[i - 1], Y[j], Z[j][i - 1]};
        				draw.setGradient(cP, colors[j][i], nP, colors[j][i - 1]);
	                    draw.fillPolygon(0.2f, cP, nP, new double[]{X[i - 1], Y[j - 1],
	                                Z[j - 1][i - 1]}, new double[]{X[i], Y[j - 1], Z[j - 1][i]});
	                }
	            }

        	}
        }
    }

    private void buildXYZ_list() {
        XYZ_list = new double[X.length * Y.length][3];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < Y.length; j++) {
                XYZ_list[i + (j) * X.length][0] = X[i];
                XYZ_list[i + (j) * X.length][1] = Y[j];
                XYZ_list[i + (j) * X.length][2] = Z[j][i];
            }
        }
    }

    @Override
    public void setData(double[][] _Z) {
        datapanel=null;
        Z = _Z;
        buildXYZ_list();
    }

    @Override
    public double[][] getData() {
        return XYZ_list;
    }
/*
    @Override
    public double[][] getBounds() {
        return new double[][]{{Array.min(X), Array.min(Y), Array.min(Array.min(Z))}, {Array.max(X), Array.max(Y), Array.max(Array.min(Z))}};
    }
*/
    public void setDataZ(double[][] _Z) {
        setData(_Z);
    }

    public double[][] getDataZ() {
        return Z;
    }

    public void setDataX(double[] _X) {
        datapanel=null;
        X = _X;
        buildXYZ_list();
    }

    public double[] getDataX() {
        return X;
    }

    public void setDataY(double[] _Y) {
        datapanel=null;
        Y = _Y;
        buildXYZ_list();
    }

    public double[] getDataY() {
        return Y;
    }

    public void setDataXYZ(double[] _X, double[] _Y, double[][] _Z) {
        datapanel=null;
        X = _X;
        Y = _Y;
        Z = _Z;
        buildXYZ_list();
    }

    public double[] isSelected(int[] screenCoordTest, AbstractDrawer draw) {
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < Y.length; j++) {
                double[] XY = {X[i], Y[j], Z[j][i]};
                int[] screenCoord = draw.project(XY);

                if ((screenCoord[0] + note_precision > screenCoordTest[0]) && (screenCoord[0] - note_precision < screenCoordTest[0])
                        && (screenCoord[1] + note_precision > screenCoordTest[1]) && (screenCoord[1] - note_precision < screenCoordTest[1])) {
                    return XY;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {

        int n = 14;
        int m = 16;
        Plot3DPanel p = new Plot3DPanel();
        double[] X = new double[n];
        double[] Y = new double[m];
        double[][] Z = new double[m][n];

        for (int i = 0; i < X.length; i++) {
            X[i] = 3 + i / (double) X.length;
            for (int j = 0; j < Y.length; j++) {
                Y[j] = 5 + j / (double) Y.length;
                Z[j][i] = Math.exp(X[i]) + Y[j];
            }
        }
        p.addGridPlot("toto", X, Y, Z);

        p.setLegendOrientation(PlotPanel.SOUTH);
        new FrameView(p);
        
        
        

        TreeSet<Double> uv = new TreeSet<Double>();
        Color[][] c = new Color[m][n];
        X = new double[n];
        Y = new double[m];
        Z = new double[m][n];
        p = new Plot3DPanel();
        for (int i = 0; i < X.length; i++) {
            X[i] = 3 + i / (double) X.length;
            for (int j = 0; j < Y.length; j++) {
                Y[j] = 5 + j / (double) Y.length;
                Z[j][i] = Math.random();
                uv.add(Z[j][i]);
            }
        }
        Color[] gc = new Color[uv.size()];
        Color low = Color.blue;
        Color high = Color.red;
        float[] hsv1 = Color.RGBtoHSB(low.getRed(), low.getGreen(), low.getBlue(), null);  
        float[] hsv2 = Color.RGBtoHSB(high.getRed(), high.getGreen(), high.getBlue(), null);  
        int a1 = low.getAlpha();
        float h1 = hsv1[0] ;  
        float s1 = hsv1[1];  
        float v1 = hsv1[2];  
        float da = high.getAlpha()- a1;  
        float dh = hsv2[0] - h1;  
        float ds = hsv2[1]- s1;  
        float dv = hsv2[2] - v1;
        for (int i = 0; i < gc.length; ++i) {
            float rel = i / (float)(gc.length - 1);
            int rgb = Color.HSBtoRGB(h1 + dh * rel, s1 + ds * rel, v1 + dv * rel);  
            rgb +=(((int)(a1 + da * rel)) << 24);
            gc[i] = new Color(rgb);
        }
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < Y.length; j++)
            	c[j][i] = gc[uv.headSet(Z[j][i]).size()];
        }
        p.addGridPlot("toto", c, X, Y, Z);
        p.setLegendOrientation(PlotPanel.SOUTH);
        new FrameView(p);
    }
}