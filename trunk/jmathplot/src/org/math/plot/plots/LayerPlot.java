/*
 * Created on 5 juil. 07 by richet
 */
package org.math.plot.plots;

import org.math.plot.render.*;

public abstract class LayerPlot extends Plot {

	Plot plot;

	public LayerPlot(String name, Plot p) {
		super(name, p.color);
		plot = p;
	}

	public double[] isSelected(int[] screenCoordTest, AbstractDrawer draw) {
		return null;
	}
}
