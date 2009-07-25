package org.math.plot.plots;

import java.awt.Color;

import javax.swing.JFrame;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.render.AbstractDrawer;
import org.math.plot.utils.Array;

/**
 * @author Yann RICHET
 * @version 1.0
 */

public class QuantileLayerPlot extends LayerPlot {

	public static int WIDTH = 2;

	int axe;

	double quantileRate;

	Color gradC;

	double main_data_constant = 0;

	public boolean symetric = false;

	double[] Q;

	//public static double DEFAULT_RATE=1.0;

	/*public QuantilePlot(Plot p, int a, double[] q, boolean _symetric) {
		this(p, a, q, DEFAULT_RATE,_symetric);
	
	}*/

	public QuantileLayerPlot(Plot p, int a, double q, double r, boolean _symetric) {
		this(p, a, null, r, true);
		main_data_constant = q;
	}

	/*public QuantilePlot(Plot p, int a, double q) {
		this(p, a, q, DEFAULT_RATE,true);
	}*/

	/** Build a quantile plot based on given plot. The quantile is drawn as a linear gradient from the base plot dots.
	 * @param p base plot
	 * @param a axis number of quantile : 0=X quantile, 1=Y quantile, 2=Z quantile
	 * @param q array of quantiles values
	 * @param r rate of the quantile. The gradient line length is q/r
	 * @param _symetric if yes, quantiles are drawn on both negative and positive sides of base plot dots
	 */
	public QuantileLayerPlot(Plot p, int a, double[] q, double r, boolean _symetric) {
		super(r + " quantile of " + p.name, p);
		if (q != null)
			Array.checkLength(q, p.getData().length);
		Q = q;
		axe = a;
		quantileRate = r;
		symetric = _symetric;

	}

	public double getQuantilesValue(int numCoord) {
		return Q[numCoord];
	}

	public int getAxe() {
		return axe;
	}

	public double getQuantileRate() {
		return quantileRate;
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!plot.visible)
			return;

		draw.setColor(c);
		gradC = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * (1 - quantileRate)));

		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		draw.setLineWidth(WIDTH);
		if (main_data_constant == 0)
			for (int i = 0; i < plot.getData().length; i++) {
				double[] d = Array.getRowCopy(plot.getData(), i);
				d[axe] += Q[i];///quantileRate;
				draw.setGradient(plot.getData()[i], c, d, gradC);
				draw.drawLine(plot.getData()[i], d);
				// draw.drawDot(d, RADIUS/*(int)(RADIUS*quantileRate)*/);

				if (symetric) {
					d[axe] -= 2 * Q[i];///quantileRate;
					draw.setGradient(plot.getData()[i], c, d, gradC);
					draw.drawLine(plot.getData()[i], d);
					// draw.drawDot(d, RADIUS/*(int)(RADIUS*quantileRate)*/);
				}
			}
		else
			for (int i = 0; i < plot.getData().length; i++) {
				double[] d = Array.getRowCopy(plot.getData(), i);
				d[axe] += main_data_constant;///quantileRate;
				draw.setGradient(plot.getData()[i], c, d, gradC);
				draw.drawLine(plot.getData()[i], d);
				// draw.drawDot(d, shape/*RADIUS/*(int)(RADIUS*quantileRate)*/);

				if (symetric) {
					d[axe] -= 2 * main_data_constant;///quantileRate;
					draw.setGradient(plot.getData()[i], c, d, gradC);
					draw.drawLine(plot.getData()[i], d);
					// draw.drawDot(d, RADIUS/*(int)(RADIUS*quantileRate)*/);
				}
			}
		draw.resetGradient();
		draw.setLineWidth(AbstractDrawer.DEFAULT_LINE_WIDTH);

	}

	@Override
	public void setData(double[][] d) {
		Q = d[0];
	}

	@Override
	public double[][] getData() {
		return new double[][] { Q };
	}

	public static void main(String[] args) {
		Plot2DPanel p2 = new Plot2DPanel();
		for (int i = 0; i < 1; i++) {
			double[][] XYZ = new double[10][2];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = /*1 + */Math.random();
				XYZ[j][1] = /*100 * */Math.random();
			}
			p2.addScatterPlot("toto" + i, XYZ);
		}
		p2.addQuantiletoPlot(0, 1, 1.0, true, 0.2);
		new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}