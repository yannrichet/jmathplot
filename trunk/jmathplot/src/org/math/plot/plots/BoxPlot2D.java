package org.math.plot.plots;

import java.awt.*;

import org.math.plot.render.*;

public class BoxPlot2D extends Plot {

	double[] Xmin;

	double[] Xmax;

	double[] Ymin;

	double[] Ymax;

	double[][] widths;

	double[][] XY;

	public BoxPlot2D(double[][] _XY, double[][] w, Color c, String n) {
		super(n, c);
		XY = _XY;
		widths = w;

		// double[] datasMin = Array.min(XY);
		// double[] datasMax = Array.max(XY);
		// double[] widthsMax = Array.max(widths);
		// double[] min = { datasMin[0] - widthsMax[0] / 2, datasMin[1] -
		// widthsMax[1] / 2 };
		// double[] max = { datasMax[0] + widthsMax[0] / 2, datasMax[1] +
		// widthsMax[1] / 2 };
		// base.includeInBounds(min);
		// base.includeInBounds(max);

		Xmin = new double[XY.length];
		Xmax = new double[XY.length];
		Ymin = new double[XY.length];
		Ymax = new double[XY.length];
		for (int i = 0; i < XY.length; i++) {
			Xmin[i] = XY[i][0] - widths[i][0] / 2;
			Xmax[i] = XY[i][0] + widths[i][0] / 2;
			Ymin[i] = XY[i][1] - widths[i][1] / 2;
			Ymax[i] = XY[i][1] + widths[i][1] / 2;
		}

	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			draw.drawLine(new double[] { Xmin[i], Ymin[i] }, new double[] { Xmax[i], Ymin[i] });
			draw.drawLine(new double[] { Xmax[i], Ymin[i] }, new double[] { Xmax[i], Ymax[i] });
			draw.drawLine(new double[] { Xmax[i], Ymax[i] }, new double[] { Xmin[i], Ymax[i] });
			draw.drawLine(new double[] { Xmin[i], Ymax[i] }, new double[] { Xmin[i], Ymin[i] });
			draw.setDotType(AbstractDrawer.ROUND_DOT);
			draw.setDotRadius(AbstractDrawer.DEFAULT_DOT_RADIUS);
			draw.drawDot(XY[i]);
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

	public double[][] getDataWidth() {
		return widths;
	}

	public void setData(double[][] d, double[][] w) {
		XY = d;
		widths = w;
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

}