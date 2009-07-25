package org.math.plot.render;

import org.math.plot.canvas.*;

public class AWTDrawer2D extends AWTDrawer {

	public AWTDrawer2D(PlotCanvas _canvas) {
		super(_canvas);
		projection = new Projection2D(this);
	}

	/*// More efficient method for orthogonal display of images
	public void drawImage(Image img,float alpha, double[] _xyzSW, double[] _xyzSE,double[] _xyzNW) {		
		int[] cornerNW = projection.screenProjection(_xyzNW);
		int[] cornerSE = projection.screenProjection(_xyzSE);
		int[] cornerSW = projection.screenProjection(_xyzSW);
		
		AffineTransform transform = new AffineTransform();
		transform.translate(cornerNW[0],cornerNW[1]);
		transform.scale((-cornerSW[0]+cornerSE[0])/(double)img.getWidth(canvas),(-cornerNW[1]+cornerSW[1])/(double)img.getHeight(canvas));
		
		Composite cs = comp2D.getComposite();
		comp2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		comp2D.drawImage(img, transform,canvas);
		comp2D.setComposite(cs);		
	}*/
	
}
