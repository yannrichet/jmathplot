package org.math.plot.plots;

import java.awt.*;

import javax.swing.*;

import org.math.plot.*;
import org.math.plot.canvas.PlotCanvas;
import org.math.plot.render.*;
import org.math.plot.utils.Array;

public class ScatterPlot extends Plot {

    private int type;
    private int radius;
    private boolean[][] pattern;
    private boolean use_pattern;
    double[][] XY;
    private String[] tags;

    public ScatterPlot(String n, Color c, boolean[][] _pattern, double[][] _XY) {
        super(n, c);
        XY = _XY;
        use_pattern = true;
        pattern = _pattern;
    }

    public ScatterPlot(String n, Color c, int _type, int _radius, double[][] _XY) {
        super(n, c);
        XY = _XY;
        use_pattern = false;
        type = _type;
        radius = _radius;
    }

    public ScatterPlot(String n, Color c, double[][] _XY) {
        this(n, c, AbstractDrawer.ROUND_DOT, AbstractDrawer.DEFAULT_DOT_RADIUS, _XY);
    }

    public void plot(AbstractDrawer draw, Color c) {
        if (!visible) {
            return;
        }

        draw.setColor(c);
        if (use_pattern) {
            draw.setDotType(AbstractDrawer.PATTERN_DOT);
            draw.setDotPattern(pattern);
        } else {
            draw.setDotRadius(radius);
            if (type == AbstractDrawer.CROSS_DOT) {
                draw.setDotType(AbstractDrawer.CROSS_DOT);
            } else {
                draw.setDotType(AbstractDrawer.ROUND_DOT);
            }
        }

        for (int i = 0; i < XY.length; i++) {
            draw.drawDot(XY[i]);
        }
    }

    public void setDotPattern(int t) {
        type = t;
        use_pattern = false;
    }

    public void setDotPattern(boolean[][] t) {
        use_pattern = true;
        pattern = t;
    }

    @Override
    public void setData(double[][] d) {
        XY = d;
    }

    @Override
    public double[][] getData() {
        return XY;
    }

    public double[] isSelected(int[] screenCoordTest, AbstractDrawer draw) {
        for (int i = 0; i < XY.length; i++) {
            int[] screenCoord = draw.project(XY[i]);

            if ((screenCoord[0] + note_precision > screenCoordTest[0]) && (screenCoord[0] - note_precision < screenCoordTest[0]) && (screenCoord[1] + note_precision > screenCoordTest[1]) && (screenCoord[1] - note_precision < screenCoordTest[1])) {
                return XY[i];
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Plot2DPanel p2 = new Plot2DPanel();
        for (int i = 0; i < 3; i++) {
            double[][] XYZ = new double[10][2];
            for (int j = 0; j < XYZ.length; j++) {
                XYZ[j][0] = /*1 + */ Math.random();
                XYZ[j][1] = /*100 * */ Math.random();
            }
            p2.addScatterPlot("toto" + i, XYZ);
        }

        p2.setLegendOrientation(PlotPanel.SOUTH);
        new FrameView(p2).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Plot3DPanel p = new Plot3DPanel();
        String[] tags = null;
        for (int i = 0; i < 3; i++) {
            double[][] XYZ = new double[10][3];
             tags = new String[10];
            for (int j = 0; j < XYZ.length; j++) {
                XYZ[j][0] = /*1 +*/ Math.random();
                XYZ[j][1] = /*100 **/ Math.random();
                XYZ[j][2] = /*0.0001 **/ Math.random();
                tags[j] = "tags "+ j;
            }
            p.addScatterPlot("toto" + i, XYZ);
        }
        ((ScatterPlot)p.getPlot(0)).setTags(tags);

        p.setLegendOrientation(PlotPanel.SOUTH);
        new FrameView(p).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @Override
    public void noteCoord(AbstractDrawer draw, double[] coordNoted) {
        if (coordNoted == null) {
            return;
        }

        if (tags == null) {
            super.noteCoord(draw, coordNoted);
        } else {
            draw.setColor(PlotCanvas.NOTE_COLOR);
            for (int i = 0; i < XY.length; i++) {
                if (tags.length > i) {
                    if (Array.equals(XY[i], coordNoted)) {
                        draw.drawText(tags[i], coordNoted);
                    }
                }
            }
        }
        //draw.drawCoordinate(coordNoted);
        //draw.drawText(Array.cat(draw.canvas.reverseMapedData(coordNoted)), coordNoted);
    }
}