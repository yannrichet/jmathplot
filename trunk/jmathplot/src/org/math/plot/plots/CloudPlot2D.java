/*
 * Created on 13 juil. 07 by richet
 */
package org.math.plot.plots;

import java.awt.Color;

import javax.swing.JFrame;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.render.AbstractDrawer;

public class CloudPlot2D extends Plot {

	double[][] NW;

	double[][] NE;

	double[][] SW;

	double[][] SE;

	double[] width_constant = { -1, -1 };

	double[][] XY;

	float[] f;

	boolean fill_shape = true;

	public CloudPlot2D(String n, Color c, double[][] _XYcard, double wX, double wY) {
		super(n, c);
		splitXYf(_XYcard);
		width_constant = new double[] { wX, wY };

		build();
	}

	private void splitXYf(double[][] xycard) {
		XY = new double[xycard.length][2];
		f = new float[xycard.length];
		float normf = 0;
		for (int i = 0; i < xycard.length; i++) {
			XY[i][0] = xycard[i][0];
			XY[i][1] = xycard[i][1];
			f[i] = (float) xycard[i][2];
			normf += f[i];//Math.max(normf, f[i]);
		}

		for (int i = 0; i < f.length; i++) {
			f[i] = f[i] / normf;
		}

	}

	private void build() {
		if (width_constant[0] > 0) {
			NW = new double[XY.length][];
			NE = new double[XY.length][];
			SW = new double[XY.length][];
			SE = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				NW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] + width_constant[1] / 2 };
				NE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] + width_constant[1] / 2 };
				SW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] - width_constant[1] / 2 };
				SE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] - width_constant[1] / 2 };
			}
		}
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		draw.canvas.includeInBounds(SW[0]);
		draw.canvas.includeInBounds(NE[XY.length - 1]);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			if (f[i] > 0) {
				draw.fillPolygon(f[i], NW[i], NE[i], SE[i], SW[i]);
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
		Plot2DPanel p = new Plot2DPanel();

		double[][] cloud = new double[100][2];
		for (int i = 0; i < cloud.length; i++) {
			cloud[i][0] = Math.random() + Math.random();
			cloud[i][1] = Math.random() + Math.random();
		}
		p.addCloudPlot("cloud", Color.RED, cloud, 5, 5);

		double[][] cloud2 = new double[100][2];
		for (int i = 0; i < cloud2.length; i++) {
			cloud2[i][0] = 2 + Math.random() + Math.random();
			cloud2[i][1] = 2 + Math.random() + Math.random();
		}
		p.addCloudPlot("cloud2", Color.RED, cloud2, 5, 5);

		p.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
