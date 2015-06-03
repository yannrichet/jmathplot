package org.math.plot.plots;

import java.awt.Color;

import javax.swing.JFrame;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.render.AbstractDrawer;
import org.math.plot.utils.Array;

/**
 * @author Yann RICHET
 */

public class GaussianDensityLayerPlot extends LayerPlot {

	public static int WIDHT = 2;

	int axis;

	Color gradC_0sigma, gradC_1sigma, gradC_2sigma, gradC_3sigma;

	double constant_sigma = 0;

	double[] sigma;

	private float[][] gausspdf_sigma;

	public GaussianDensityLayerPlot(Plot p, int ax, double sigma) {
		this(p, ax, null);
		constant_sigma = sigma;

		gausspdf_sigma = new float[1][4];
		for (int i = 0; i < gausspdf_sigma.length; i++)
			for (int j = 0; j < 4; j++)
				gausspdf_sigma[i][j] = (float) (/*1.0 / Math.sqrt(2 * Math.PI * constant_sigma * constant_sigma) */Math.exp(-(j * j)
						/ (2.0 * constant_sigma * constant_sigma)));

	}

	/*public QuantilePlot(Plot p, int a, double q) {
		this(p, a, q, DEFAULT_RATE,true);
	}*/

	/** Build a gauss quantile plot based on given plot. The quantile is drawn as a gaussian gradient from the base plot dots.
	 * @param p base plot
	 * @param a axis number of quantile : 0=X quantile, 1=Y quantile, 2=Z quantile
	 * @param sigma array of standard deviation values
	 */
	public GaussianDensityLayerPlot(Plot p, int ax, double[] sigma) {
		super("Gauss quantile of " + p.name, p);
		if (sigma != null)
			Array.checkLength(sigma, p.getData().length);
		this.sigma = sigma;
		axis = ax;

		if (sigma != null) {
			gausspdf_sigma = new float[sigma.length][4];
			for (int i = 0; i < gausspdf_sigma.length; i++) {
				for (int j = 0; j < 4; j++)
					gausspdf_sigma[i][j] = (float) (/*1.0 / Math.sqrt(2 * Math.PI * sigma[i] * sigma[i]) */Math.exp(-(j * j) / (2.0 * sigma[i] * sigma[i])));
			}
		}

	}

	public double getQuantilesValue(int numCoord) {
		return sigma[numCoord];
	}

	public int getAxe() {
		return axis;
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!plot.visible)
			return;

		draw.setColor(c);

		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		draw.setLineWidth(WIDHT);
		if (constant_sigma == 0)
			for (int i = 0; i < plot.getData().length; i++) {
				gradC_0sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[i][0])));
				gradC_1sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[i][1])));
				gradC_2sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[i][2])));
				gradC_3sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[i][3])));

				double[] d = Array.getRowCopy(plot.getData(), i);
				double[] d2 = Array.getRowCopy(plot.getData(), i);
				d2[axis] += sigma[i];
				draw.setGradient(d, gradC_0sigma, d2, gradC_1sigma);
				draw.drawLine(d, d2);

				d[axis] += sigma[i];
				d2[axis] += sigma[i];
				draw.setGradient(d, gradC_1sigma, d2, gradC_2sigma);
				draw.drawLine(d, d2);

				d[axis] += sigma[i];
				d2[axis] += sigma[i];
				draw.setGradient(d, gradC_2sigma, d2, gradC_3sigma);
				draw.drawLine(d, d2);

				d = Array.getRowCopy(plot.getData(), i);
				d2 = Array.getRowCopy(plot.getData(), i);
				d2[axis] -= sigma[i];
				draw.setGradient(d2, gradC_1sigma, d, gradC_0sigma);
				draw.drawLine(d2, d);

				d[axis] -= sigma[i];
				d2[axis] -= sigma[i];
				draw.setGradient(d2, gradC_2sigma, d, gradC_1sigma);
				draw.drawLine(d2, d);

				d[axis] -= sigma[i];
				d2[axis] -= sigma[i];
				draw.setGradient(d2, gradC_3sigma, d, gradC_2sigma);
				draw.drawLine(d2, d);
			}
		else {
			gradC_0sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[0][0])));
			gradC_1sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[0][1])));
			gradC_2sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[0][2])));
			gradC_3sigma = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * (gausspdf_sigma[0][3])));

			for (int i = 0; i < plot.getData().length; i++) {

				double[] d = Array.getRowCopy(plot.getData(), i);
				double[] d2 = Array.getRowCopy(plot.getData(), i);
				d2[axis] += constant_sigma;
				draw.setGradient(d, gradC_0sigma, d2, gradC_1sigma);
				draw.drawLine(d, d2);

				d[axis] += constant_sigma;
				d2[axis] += constant_sigma;
				draw.setGradient(d, gradC_1sigma, d2, gradC_2sigma);
				draw.drawLine(d, d2);

				d[axis] += constant_sigma;
				d2[axis] += constant_sigma;
				draw.setGradient(d, gradC_2sigma, d2, gradC_3sigma);
				draw.drawLine(d, d2);

				d = Array.getRowCopy(plot.getData(), i);
				d2 = Array.getRowCopy(plot.getData(), i);
				d2[axis] -= constant_sigma;
				draw.setGradient(d2, gradC_1sigma, d, gradC_0sigma);
				draw.drawLine(d2, d);

				d[axis] -= constant_sigma;
				d2[axis] -= constant_sigma;
				draw.setGradient(d2, gradC_2sigma, d, gradC_1sigma);
				draw.drawLine(d2, d);

				d[axis] -= constant_sigma;
				d2[axis] -= constant_sigma;
				draw.setGradient(d2, gradC_3sigma, d, gradC_2sigma);
				draw.drawLine(d2, d);
			}
		}
		draw.resetGradient();
		draw.setLineWidth(AbstractDrawer.DEFAULT_LINE_WIDTH);

	}

	@Override
	public void setData(double[][] d) {
		sigma = d[0];
	}

	@Override
	public double[][] getData() {
		return new double[][] { sigma };
	}

	public static void main(String[] args) {
		double[] sXYZ = null;

		Plot2DPanel p2 = new Plot2DPanel();
		for (int i = 0; i < 2; i++) {
			double[][] XYZ = new double[10][2];
			sXYZ = new double[10];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = /*1 + */Math.random();
				XYZ[j][1] = /*100 * */Math.random();
				sXYZ[j] = /*100 * */Math.random();
			}

			p2.addScatterPlot("toto" + i, XYZ);
		}
		p2.getPlot(0).addGaussQuantiles(0, sXYZ);
		p2.getPlot(1).addGaussQuantiles(1, 0.1);

		new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}