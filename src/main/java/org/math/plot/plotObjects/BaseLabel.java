package org.math.plot.plotObjects;

import java.awt.*;

import org.math.plot.*;
import org.math.plot.canvas.*;
import org.math.plot.render.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */

public class BaseLabel extends Label /* implements BaseDependant */{

	public BaseLabel(String l, Color c, double... rC) {
		super(l, c, rC);
	}

	/*
	 * public void resetBase() { System.out.println("BaseLabel.resetBase"); }
	 */

	public static void main(String[] args) {
		Plot3DCanvas p3d = new Plot3DCanvas(new double[] { 0, 0, 0 }, new double[] { 10, 10, 10 }, new String[] { "lin", "lin", "lin" }, new String[] { "x",
				"y", "z" });
		new FrameView(p3d);
		// p3d.addPlot(DoubleArray.random(10, 3), "plot", "SCATTER");
		p3d.addPlotable(new BaseLabel("label", Color.RED, new double[] { -0.1, 0.5, 0.5 }));
	}

	public void plot(AbstractDrawer draw) {
		draw.setColor(color);
		draw.setFont(font);
		draw.setTextAngle(angle);
		draw.setTextOffset(cornerE, cornerN);
		draw.drawTextBase(label, coord);
	}

}