/*
 * Created on 5 sept. 2005 by richet
 */
package org.math.plot.plotObjects;

import java.awt.*;


import org.math.plot.render.*;

public class PaintImage implements Plotable {

    public interface Paintable {

        public void paint(Graphics g);
    }
    protected Paintable source;
    protected Image img;
    protected double[] xyzSW, xyzSE, xyzNW;
    protected boolean visible = true;
    protected float alpha;

    public PaintImage(Paintable _source, float _alpha, double[] _xyzSW, double[] _xyzSE, double[] _xyzNW) {
        source = _source;

        xyzSW = _xyzSW;
        xyzSE = _xyzSE;
        xyzNW = _xyzNW;
        alpha = _alpha;
    }

    public void plot(AbstractDrawer draw) {
        if (!visible) {
            return;
        }

        if (img == null) {
            img = draw.canvas.createImage(draw.canvas.getWidth(), draw.canvas.getHeight());
            source.paint(img.getGraphics());
        }

        draw.drawImage(img, alpha, xyzSW, xyzSE, xyzNW);
    }

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setColor(Color c) {
        throw new IllegalArgumentException("method not available for this Object: PlotImage");
    }

    public Color getColor() {
        return null;
    }
}
