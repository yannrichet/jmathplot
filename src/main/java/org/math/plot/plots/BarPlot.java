package org.math.plot.plots;

import java.awt.*;

import javax.swing.*;

import org.math.plot.*;
import org.math.plot.render.*;
import org.math.plot.utils.*;

public class BarPlot extends ScatterPlot {

	public boolean draw_dot = true;

	public BarPlot(String n, Color c, boolean[][] _pattern, double[][] _XY) {
		super(n, c, _pattern, _XY);
	}

	public BarPlot(String n, Color c, int _type, int _radius, double[][] _XY) {
		super(n, c, _type, _radius, _XY);
	}

	public BarPlot(String n, Color c, double[][] _XY) {
		super(n, c, _XY);
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		if (draw_dot)
			super.plot(draw, c);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length; i++) {
			double[] axeprojection = Array.copy(XY[i]);
			axeprojection[axeprojection.length - 1] = draw.canvas.base.baseCoords[0][axeprojection.length - 1];
			draw.drawLine(XY[i], axeprojection);
		}
	}

	public static void main(String[] args) {
		Plot2DPanel p2 = new Plot2DPanel();
		for (int i = 0; i < 3; i++) {
			double[][] XYZ = new double[10][2];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = /*1 + */Math.random();
				XYZ[j][1] = /*100 * */Math.random();
			}
			p2.addBarPlot("toto" + i, XYZ);
		}

		p2.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Plot3DPanel p = new Plot3DPanel();
		for (int i = 0; i < 3; i++) {
			double[][] XYZ = new double[10][3];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = /*1 +*/Math.random();
				XYZ[j][1] = /*100 **/Math.random();
				XYZ[j][2] = /*0.0001 **/Math.random();
			}
			p.addBarPlot("toto" + i, XYZ);
		}

		p.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}