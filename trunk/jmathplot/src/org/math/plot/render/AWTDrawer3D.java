package org.math.plot.render;

import org.math.plot.canvas.*;

public class AWTDrawer3D extends AWTDrawer {

    public AWTDrawer3D(PlotCanvas _canvas) {
        super(_canvas);
        projection = new Projection3D(this);
    }

    public void rotate(int[] t, int[] panelSize) {
        ((Projection3D) projection).rotate(t, panelSize);
    }

    public void dilate(int[] screenOrigin, double[] screenRatio) {
        super.dilate(screenOrigin, screenRatio);
        ((Projection3D) projection).updateCoordsCenterScreen();
        canvas.repaint();
    }
    
    public void translate(int... t) {
        super.translate(t);
         ((Projection3D) projection).updateCoordsCenterScreen();
        canvas.repaint();
    }
}
