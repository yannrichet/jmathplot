package org.math.plot.plots;

import java.awt.*;

import javax.swing.*;

import org.math.plot.*;
import org.math.plot.render.*;

public class StaircasePlot extends ScatterPlot {

	public boolean link = true;

	public StaircasePlot(String n, Color c, boolean[][] _pattern, double[][] _XY) {
		super(n, c, _pattern, _XY);
	}

	public StaircasePlot(String n, Color c, int _type, int _radius, double[][] _XY) {
		super(n, c, _type, _radius, _XY);
	}

	public StaircasePlot(String n, Color c, double[][] _XY) {
		super(n, c, _XY);
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!visible)
			return;

		//System.out.println(Array.toString(XY));
		
		draw.setColor(c);
		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);
		for (int i = 0; i < XY.length - 1; i++) {
			double[] begin = XY[i].clone();
			double[] end = XY[i + 1].clone();
			end[end.length - 1] = XY[i][end.length - 1];
			draw.drawLine(begin, end);
		}
		
		//System.out.println(Array.toString(XY));

		if (link) {
			for (int i = 0; i < XY.length - 2; i++) {
				double[] begin = XY[i+1].clone();
				double[] end = XY[i + 1].clone();
				begin[begin.length - 1] = XY[i][begin.length - 1];
				draw.drawLine(begin, end);
			}
		}
		//System.out.println(Array.toString(XY));
		
	}
	
	public static void main(String[] args) {
		Plot2DPanel p = new Plot2DPanel();

			double[] X = new double[10];
			double[] Y = new double[10];
			for (int j = 0; j < X.length; j++) {
				X[j] = j;
				Y[j] = Math.random();
			}
			p.addStaircasePlot("toto", X,Y);
		
		new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}