package org.math.plot.plots;

import java.awt.*;

import org.math.plot.*;
import org.math.plot.render.*;

public class HistogramPlot2D extends Plot {

	double[][] topLeft;

	double[][] topRight;

	double[][] bottomLeft;

	double[][] bottomRight;

	double[] widths;

	double width_constant = -1;

	double offsetCenter_perWidth;

	double factorWidth;

	boolean autowidth;
	
	boolean fill_shape = true;

	double[][] XY;

	public HistogramPlot2D(String n, Color c, double[][] _XY, double w) {
		this(n, c, _XY, w, 0.5, 1);
	}

	public HistogramPlot2D(String n, Color c, double[][] _XY, double[] w) {
		this(n, c, _XY, w, 0.5, 1);
	}

	// TODO Histogram group plots

	public HistogramPlot2D(String n, Color c, double[][] _XY, double w, double _offsetCenter_perWidth, double _factorWidth) {
		super(n, c);
		XY = _XY;
		width_constant = w;

		autowidth = false;
		offsetCenter_perWidth = _offsetCenter_perWidth;
		factorWidth = _factorWidth;

		build();
	}

	public HistogramPlot2D(String n, Color c, double[][] _XY, double[] w, double _offsetCenter_perWidth, double _factorWidth) {
		super(n, c);
		XY = _XY;
		widths = w;

		autowidth = false;
		offsetCenter_perWidth = _offsetCenter_perWidth;
		factorWidth = _factorWidth;

		build();
	}

	private void build() {
		if (width_constant > 0) {
			topLeft = new double[XY.length][];
			topRight = new double[XY.length][];
			bottomLeft = new double[XY.length][];
			bottomRight = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				topLeft[i] = new double[] { XY[i][0] - factorWidth * width_constant / 2 + (offsetCenter_perWidth - 0.5) * width_constant, XY[i][1] };
				topRight[i] = new double[] { XY[i][0] + factorWidth * width_constant / 2 + (offsetCenter_perWidth - 0.5) * width_constant, XY[i][1] };
				bottomLeft[i] = new double[] { XY[i][0] - factorWidth * width_constant / 2 + (offsetCenter_perWidth - 0.5) * width_constant, 0 };
				bottomRight[i] = new double[] { XY[i][0] + factorWidth * width_constant / 2 + (offsetCenter_perWidth - 0.5) * width_constant, 0 };
			}
		} else {
			topLeft = new double[XY.length][];
			topRight = new double[XY.length][];
			bottomLeft = new double[XY.length][];
			bottomRight = new double[XY.length][];
			for (int i = 0; i < XY.length; i++) {
				topLeft[i] = new double[] { XY[i][0] - factorWidth * widths[i] / 2 + (offsetCenter_perWidth - 0.5) * widths[i], XY[i][1] };
				topRight[i] = new double[] { XY[i][0] + factorWidth * widths[i] / 2 + (offsetCenter_perWidth - 0.5) * widths[i], XY[i][1] };
				bottomLeft[i] = new double[] { XY[i][0] - factorWidth * widths[i] / 2 + (offsetCenter_perWidth - 0.5) * widths[i], 0 };
				bottomRight[i] = new double[] { XY[i][0] + factorWidth * widths[i] / 2 + (offsetCenter_perWidth - 0.5) * widths[i], 0 };
			}
		}
	}

	/*
	 * public HistogramPlot2D(double[][] XY, Color c, String n, ProjectionBase
	 * b) { super(XY, c, n, PlotPanel.HISTOGRAM, b);
	 * 
	 * autowidth = true;
	 * 
	 * topLeft = new double[datas.length][]; topRight = new
	 * double[datas.length][]; bottomLeft = new double[datas.length][];
	 * bottomRight = new double[datas.length][];
	 * 
	 * Sorting sort = new Sorting(DoubleArray.getColumnCopy(datas, 0), false);
	 * datas = DoubleArray.getRowsCopy(XY, sort.getIndex());
	 * 
	 * topLeft[0] = new double[] { datas[0][0] + (datas[0][0] - datas[1][0]) /
	 * 2, datas[0][1] }; topRight[0] = new double[] { (datas[0][0] +
	 * datas[1][0]) / 2, datas[0][1] }; bottomLeft[0] = new double[] {
	 * datas[0][0] + (datas[0][0] - datas[1][0]) / 2, 0 }; bottomRight[0] = new
	 * double[] { (datas[0][0] + datas[1][0]) / 2, 0 }; for (int i = 1; i <
	 * datas.length - 1; i++) { topLeft[i] = new double[] { (datas[i][0] +
	 * datas[i - 1][0]) / 2, datas[i][1] }; topRight[i] = new double[] {
	 * (datas[i][0] + datas[i + 1][0]) / 2, datas[i][1] }; bottomLeft[i] = new
	 * double[] { (datas[i][0] + datas[i - 1][0]) / 2, 0 }; bottomRight[i] = new
	 * double[] { (datas[i][0] + datas[i + 1][0]) / 2, 0 }; }
	 * topLeft[datas.length - 1] = new double[] { (datas[datas.length - 1][0] +
	 * datas[datas.length - 2][0]) / 2, datas[datas.length - 1][1] };
	 * topRight[datas.length - 1] = new double[] { datas[datas.length - 1][0] +
	 * (datas[datas.length - 1][0] - datas[datas.length - 2][0]) / 2,
	 * datas[datas.length - 1][1] }; bottomLeft[datas.length - 1] = new double[] {
	 * (datas[datas.length - 1][0] + datas[datas.length - 2][0]) / 2, 0 };
	 * bottomRight[datas.length - 1] = new double[] { datas[datas.length - 1][0] +
	 * (datas[datas.length - 1][0] - datas[datas.length - 2][0]) / 2, 0 }; }
	 */

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		draw.canvas.includeInBounds(bottomLeft[0]);
		draw.canvas.includeInBounds(topRight[XY.length - 1]);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			draw.drawLine(bottomLeft[i], topLeft[i]);
			draw.drawLine(topLeft[i], topRight[i]);
			draw.drawLine(topRight[i], bottomRight[i]);
			draw.drawLine(bottomRight[i], bottomLeft[i]);
			
			if (fill_shape)
				draw.fillPolygon(0.2f,bottomLeft[i],topLeft[i],topRight[i],bottomRight[i]);
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

	public void setDataWidth(double[] w) {
		widths = w;
		width_constant = -1;
		build();
	}

	public void setDataWidth(double w) {
		width_constant = w;
		build();
	}

	public double[] getDataWidth() {
		if (width_constant > 0) {
			widths = new double[XY.length];
			for (int i = 0; i < widths.length; i++)
				widths[i] = width_constant;
		}
		return widths;
	}

	public void setData(double[][] d, double[] w) {
		XY = d;
		setDataWidth(w);
	}

	public void setData(double[][] d, double w) {
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
		double[] X = new double[500];
		for (int i = 0; i < X.length; i++) {
			X[i] = Math.random()+Math.random();
		}
		Plot2DPanel p = new Plot2DPanel("SOUTH");
		p.addHistogramPlot("test", X, 10);
		new FrameView(p);
	}

}