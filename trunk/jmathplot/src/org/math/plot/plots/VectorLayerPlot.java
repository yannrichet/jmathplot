package org.math.plot.plots;

import java.awt.*;

import javax.swing.*;

import org.math.plot.*;
import org.math.plot.render.*;
import org.math.plot.utils.*;

/**
 * @author Yann RICHET
 * @version 1.0
 */

/**Layer to add a vector field to an existing Plot*/
public class VectorLayerPlot extends LayerPlot {

	public static int RADIUS = 5;

	double[][] V;

	/**Create a vector fiels based on data of a plot
	  @param p Base plot to support vector field
	  @param v Vector field of same lenght that p data */
	public VectorLayerPlot(Plot p, double[][] v) {
		super("Vector of " + p.name, p);
		if (v != null) {
			Array.checkRowDimension(v, p.getData().length);
			Array.checkColumnDimension(v, p.getData()[0].length);
		}
		V = v;

	}

	@Override
	public void setData(double[][] v) {
		V = v;
	}

	@Override
	public double[][] getData() {
		return V;
	}

	public void plot(AbstractDrawer draw, Color c) {
		if (!plot.visible)
			return;

		draw.setColor(c);

		draw.setLineType(AbstractDrawer.CONTINOUS_LINE);

		for (int i = 0; i < plot.getData().length; i++) {
			double[] d = Array.getRowCopy(plot.getData(), i);
			for (int j = 0; j < d.length; j++) {
				d[j] += V[i][j];
			}
			draw.drawLine(plot.getData()[i], d);
			//TODO: draw arrow at position d

		}

	}

	public static void main(String[] args) {
		Plot2DPanel p2 = new Plot2DPanel();
		double[][] XYZ = new double[100][2];
		double[][] dXYZ = new double[100][2];

		for (int j = 0; j < XYZ.length; j++) {
			XYZ[j][0] = Math.random()*10;
			XYZ[j][1] = Math.random()*10;
			dXYZ[j][0] = 1.0/Math.sqrt(1+Math.log(XYZ[j][0])*Math.log(XYZ[j][0]));
			dXYZ[j][1] = Math.log(XYZ[j][0])/Math.sqrt(1+Math.log(XYZ[j][0])*Math.log(XYZ[j][0]));
		}
		p2.addScatterPlot("toto", XYZ);

		p2.addVectortoPlot(0, dXYZ);
		new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}