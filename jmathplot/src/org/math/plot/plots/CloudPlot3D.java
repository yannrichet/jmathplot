/*
 * Created on 13 juil. 07 by richet
 */
package org.math.plot.plots;

import java.awt.Color;

import javax.swing.JFrame;

import org.math.plot.FrameView;
import org.math.plot.Plot3DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.render.AbstractDrawer;

public class CloudPlot3D extends Plot {

	double[][] topNW;

	double[][] topNE;

	double[][] topSW;

	double[][] topSE;

	double[][] botNW;

	double[][] botNE;

	double[][] botSW;

	double[][] botSE;

	double[] width_constant = { -1, -1 };

	double[][] XY;

	float[] f;

	boolean fill_shape = true;

	public CloudPlot3D(String n, Color c, double[][] _XYcard, double wX, double wY, double wZ) {
		super(n, c);
		splitXYf(_XYcard);
		width_constant = new double[] { wX, wY, wZ };

		build();
	}

	private void splitXYf(double[][] xycard) {
		XY = new double[xycard.length][3];
		f = new float[xycard.length];
		float normf = 0;
		for (int i = 0; i < xycard.length; i++) {
			XY[i][0] = xycard[i][0];
			XY[i][1] = xycard[i][1];
			XY[i][2] = xycard[i][2];
			f[i] = (float) xycard[i][3];
			normf += f[i];//Math.max(normf, f[i]);
		}

		for (int i = 0; i < f.length; i++) {
			f[i] = f[i] / normf;
		}

	}

	private void build() {
		if (width_constant[0] > 0) {
			topNW = new double[XY.length][];
			topNE = new double[XY.length][];
			topSW = new double[XY.length][];
			topSE = new double[XY.length][];
			botNW = new double[XY.length][];
			botNE = new double[XY.length][];
			botSW = new double[XY.length][];
			botSE = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				topNW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] + width_constant[1] / 2 };
				topNE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] + width_constant[1] / 2 };
				topSW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] + width_constant[1] / 2 };
				topSE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] + width_constant[1] / 2 };
				botNW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] - width_constant[1] / 2 };
				botNE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] - width_constant[1] / 2 };
				botSW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] - width_constant[1] / 2 };
				botSE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] - width_constant[1] / 2 };
			}
		}
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		draw.canvas.includeInBounds(botSW[0]);
		draw.canvas.includeInBounds(topNE[XY.length - 1]);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			if (f[i] > 0) {
				draw.fillPolygon(f[i], topNW[i], topNE[i], topSE[i], topSW[i]);
				draw.fillPolygon(f[i], botNW[i], botNE[i], botSE[i], botSW[i]);

				draw.fillPolygon(f[i], botNW[i], botNE[i], topNE[i], topNW[i]);
				draw.fillPolygon(f[i], botSW[i], botSE[i], topSE[i], topSW[i]);

				draw.fillPolygon(f[i], botNW[i], botSW[i], topSW[i], topNW[i]);
				draw.fillPolygon(f[i], botNE[i], botSE[i], topSE[i], topNE[i]);
			}
		}
	}

	@Override
	public void setData(double[][] d) {
		splitXYf(d);
	}

	@Override
	public double[][] getData() {
		return XY;
	}

	public double[] isSelected(int[] screenCoordTest, AbstractDrawer draw) {
		for (int i = 0; i < XY.length; i++) {
			int[] screenCoord = draw.project(XY[i]);

			if ((screenCoord[0] + note_precision > screenCoordTest[0]) && (screenCoord[0] - note_precision < screenCoordTest[0])
					&& (screenCoord[1] + note_precision > screenCoordTest[1]) && (screenCoord[1] - note_precision < screenCoordTest[1]))
				return XY[i];
		}
		return null;
	}

	public static void main(String[] args) {
		Plot3DPanel p = new Plot3DPanel();

		//triangular random cloud (as sum of two uniform random numbers)
		double[][] cloud = new double[100][3];
		for (int i = 0; i < cloud.length; i++) {
			cloud[i][0] = Math.random() + Math.random();
			cloud[i][1] = Math.random() + Math.random();
			cloud[i][2] = Math.random() + Math.random();
		}
		p.addCloudPlot("cloud", Color.RED, cloud, 3, 3, 3);

		double[][] cloud2 = new double[100][3];
		for (int i = 0; i < cloud.length; i++) {
			cloud2[i][0] = 2 + Math.random() + Math.random();
			cloud2[i][1] = 2 + Math.random() + Math.random();
			cloud2[i][2] = 2 + Math.random() + Math.random();
		}
		p.addCloudPlot("cloud2", Color.RED, cloud2, 3, 3, 3);

		p.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
