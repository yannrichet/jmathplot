package org.math.plot.plots;

import java.awt.*;

import org.math.plot.*;
import org.math.plot.render.*;

public class HistogramPlot3D extends Plot {

	double[][] topNW;

	double[][] topNE;

	double[][] topSW;

	double[][] topSE;

	double[][] bottomNW;

	double[][] bottomNE;

	double[][] bottomSW;

	double[][] bottomSE;

	double[][] widths;

	double[] width_constant = { -1, -1 };

	double[][] XY;

	boolean fill_shape = true;

	public HistogramPlot3D(String n, Color c, double[][] _XY, double[][] w) {
		super(n, c);
		XY = _XY;
		widths = w;

		build();
	}

	public HistogramPlot3D(String n, Color c, double[][] _XY, double wX, double wY) {
		super(n, c);
		XY = _XY;
		width_constant = new double[] { wX, wY };

		build();
	}

	public HistogramPlot3D(String n, Color c, double[][] _XY, double[] w) {
		super(n, c);
		XY = _XY;
		width_constant = w;

		build();
	}

	private void build() {
		if (width_constant[0] > 0) {
			topNW = new double[XY.length][];
			topNE = new double[XY.length][];
			topSW = new double[XY.length][];
			topSE = new double[XY.length][];
			bottomNW = new double[XY.length][];
			bottomNE = new double[XY.length][];
			bottomSW = new double[XY.length][];
			bottomSE = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				topNW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] };
				topNE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, XY[i][2] };
				topSW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] };
				topSE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, XY[i][2] };
				bottomNW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, 0 };
				bottomNE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] + width_constant[1] / 2, 0 };
				bottomSW[i] = new double[] { XY[i][0] - width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, 0 };
				bottomSE[i] = new double[] { XY[i][0] + width_constant[0] / 2, XY[i][1] - width_constant[1] / 2, 0 };
			}
		} else {
			topNW = new double[XY.length][];
			topNE = new double[XY.length][];
			topSW = new double[XY.length][];
			topSE = new double[XY.length][];
			bottomNW = new double[XY.length][];
			bottomNE = new double[XY.length][];
			bottomSW = new double[XY.length][];
			bottomSE = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				topNW[i] = new double[] { XY[i][0] - widths[i][0] / 2, XY[i][1] + widths[i][1] / 2, XY[i][2] };
				topNE[i] = new double[] { XY[i][0] + widths[i][0] / 2, XY[i][1] + widths[i][1] / 2, XY[i][2] };
				topSW[i] = new double[] { XY[i][0] - widths[i][0] / 2, XY[i][1] - widths[i][1] / 2, XY[i][2] };
				topSE[i] = new double[] { XY[i][0] + widths[i][0] / 2, XY[i][1] - widths[i][1] / 2, XY[i][2] };
				bottomNW[i] = new double[] { XY[i][0] - widths[i][0] / 2, XY[i][1] + widths[i][1] / 2, 0 };
				bottomNE[i] = new double[] { XY[i][0] + widths[i][0] / 2, XY[i][1] + widths[i][1] / 2, 0 };
				bottomSW[i] = new double[] { XY[i][0] - widths[i][0] / 2, XY[i][1] - widths[i][1] / 2, 0 };
				bottomSE[i] = new double[] { XY[i][0] + widths[i][0] / 2, XY[i][1] - widths[i][1] / 2, 0 };
			}
		}
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		draw.canvas.includeInBounds(bottomSW[0]);
		draw.canvas.includeInBounds(topNE[XY.length - 1]);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			if (topNW[i][2]!=bottomNW[i][2]) {
			draw.drawLine(topNW[i], topNE[i]);
			draw.drawLine(topNE[i], topSE[i]);
			draw.drawLine(topSE[i], topSW[i]);
			draw.drawLine(topSW[i], topNW[i]);

			draw.drawLine(bottomNW[i], bottomNE[i]);
			draw.drawLine(bottomNE[i], bottomSE[i]);
			draw.drawLine(bottomSE[i], bottomSW[i]);
			draw.drawLine(bottomSW[i], bottomNW[i]);

			draw.drawLine(bottomNW[i], topNW[i]);
			draw.drawLine(bottomNE[i], topNE[i]);
			draw.drawLine(bottomSE[i], topSE[i]);
			draw.drawLine(bottomSW[i], topSW[i]);

			if (fill_shape) {
				draw.fillPolygon(0.2f,topNW[i], topNE[i], topSE[i], topSW[i]);
				//draw.fillPolygon(bottomNW[i], bottomNE[i], bottomSE[i], bottomSW[i]);
				/*draw.fillPolygon(topNW[i], topNE[i], bottomNE[i], bottomNW[i]);
				draw.fillPolygon(topSW[i], topSE[i], bottomSE[i], bottomSW[i]);
				draw.fillPolygon(topNE[i], topSE[i], bottomSE[i], bottomNE[i]);
				draw.fillPolygon(topNW[i], topSW[i], bottomSW[i], bottomNW[i]);*/
			}
			}
		}
	}

	@Override
	public void setData(double[][] d) {
		XY = d;
	}

	@Override
	public double[][] getData() {
		return XY;
	}

	public void setDataWidth(double[][] w) {
		widths = w;
	}

	public void setDataWidth(double... w) {
		width_constant = w;
		build();
	}

	public double[][] getDataWidth() {
		if (width_constant[0] > 0) {
			widths = new double[XY.length][2];
			for (int i = 0; i < widths.length; i++) {
				widths[i][0] = width_constant[0];
				widths[i][1] = width_constant[1];
			}
		}
		return widths;
	}

	public void setData(double[][] d, double[][] w) {
		XY = d;
		widths = w;
	}

	public void setData(double[][] d, double... w) {
		XY = d;
		setDataWidth(w);
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
		double[][] XY = new double[500][2];
		for (int i = 0; i < XY.length; i++) {
			XY[i][0] = Math.random()+Math.random();
			XY[i][1] = Math.random()+Math.random();
		}
		Plot3DPanel p = new Plot3DPanel("SOUTH");
		p.addHistogramPlot("test", XY, 4, 6);
		new FrameView(p);
	}

}