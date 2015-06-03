package org.math.plot.plotObjects;

import org.math.plot.render.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public interface Editable {
	public double[] isSelected(int[] screenCoord, AbstractDrawer draw);

	public void edit(Object editParent);

	public void editnote(AbstractDrawer draw);

}
