package org.math.plot.plots;

import java.awt.*;

import javax.swing.*;

import org.math.plot.*;
import org.math.plot.render.*;

public class LinePlot extends ScatterPlot {

	public boolean draw_dot = false;

	public LinePlot(String n, Color c, boolean[][] _pattern, double[][] _XY) {
		super(n, c, _pattern, _XY);
	}

	public LinePlot(String n, Color c, int _type, int _radius, double[][] _XY) {
		super(n, c, _type, _radius, _XY);
	}

	public LinePlot(String n, Color c, double[][] _XY) {
		super(n, c, _XY);
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		if (draw_dot)
			super.plot(draw, c);

		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length - 1; i++)
			draw.drawLine(XY[i], XY[i + 1]);
	}

	public static void main(String[] args) {
		Plot2DPanel p2 = new Plot2DPanel();

			double[][] XYZ = new double[100][2];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = 2*Math.PI*(double)j/XYZ.length;
				XYZ[j][1] = Math.sin(XYZ[j][0]);
			}
			p2.addLinePlot("sin" , XYZ);
		

		p2.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Plot3DPanel p = new Plot3DPanel();

			XYZ = new double[100][3];
			for (int j = 0; j < XYZ.length; j++) {
				XYZ[j][0] = 2*Math.PI*(double)j/XYZ.length;
				XYZ[j][1] = Math.sin(XYZ[j][0]);
				XYZ[j][2] = Math.sin(XYZ[j][0])*Math.cos(XYZ[j][1]);
			}
			p.addLinePlot("toto" , XYZ);
		
		p.setLegendOrientation(PlotPanel.SOUTH);
		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}