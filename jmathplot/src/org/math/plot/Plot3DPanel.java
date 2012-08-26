package org.math.plot;

import java.awt.*;

import org.math.plot.canvas.*;
import org.math.plot.utils.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
/** class for ascending compatibility */
public class Plot3DPanel extends PlotPanel {

    private static final long serialVersionUID = 1L;

    public Plot3DPanel() {
        super(new Plot3DCanvas());
    }

    public Plot3DPanel(double[] min, double[] max, String[] axesScales, String[] axesLabels) {
        super(new Plot3DCanvas(min, max, axesScales, axesLabels));
    }

    public Plot3DPanel(PlotCanvas _canvas, String legendOrientation) {
        super(_canvas, legendOrientation);
    }

    public Plot3DPanel(PlotCanvas _canvas) {
        super(_canvas);
    }

    public Plot3DPanel(String legendOrientation) {
        super(new Plot3DCanvas(), legendOrientation);
    }

    /**
     * Adds a scatter plot (each data point is plotted as a single dot
     * marker) to the current plot panel.
     * @param name Name for the plot, which will be used in the legend.
     * (String)
     * @param color Plot color. (Color)
     * @param XY Array of triple double. For each triple, first one contains the X position
     * of the data points, second contains Y position, third contains Z position.
     * <br>
     * Each array of the triple
     * must be of the same length; if not a ArrayIndexOutOfBoundsException
     * exception will be thrown.
     * <br>
     * Each data set must come in 
     * array of <b>triple</b> of double; if not a ArrayIndexOutOfBoundsException exception
     * will be thrown.
     * @return the index of the plot in the panel (int).
     * @see #addLinePlot(String,Color,double[]...)
     * @see #addBarPlot(String, Color, double[]...)
     * @see #addBoxPlot(String, Color, double[][], double[][])
     * @see #addHistogramPlot(String, Color, double[][], double[])
     * @see #addStaircasePlot(String, Color, double[]...)
     */
    public int addScatterPlot(String name, Color color, double[][] XY) {
        return ((Plot3DCanvas) plotCanvas).addScatterPlot(name, color, XY);
    }

    public int addScatterPlot(String name, Color color, double[] X, double[] Y, double[] Z) {
        return ((Plot3DCanvas) plotCanvas).addScatterPlot(name, color, X, Y, Z);
    }

    public int addScatterPlot(String name, double[][] XY) {
        return addScatterPlot(name, getNewColor(), XY);
    }

    public int addScatterPlot(String name, double[] X, double[] Y, double[] Z) {
        return addScatterPlot(name, getNewColor(), X, Y, Z);
    }

    /**
     * Adds a line plot (each data point is connected to the next one by a
     * solid line) to the current plot panel.
     * @param name Name for the plot, which will be used in the legend.
     * (String)
     * @param color Plot color. (Color)
     * @param XY Array of triple double. For each triple, first one contains the X position
     * of the data points, second contains Y position, third contains Z position.
     * <br>
     * Each array of the triple
     * must be of the same length; if not a ArrayIndexOutOfBoundsException
     * exception will be thrown.
     * <br>
     * Each data set must come in 
     * array of <b>triple</b> of double; if not a ArrayIndexOutOfBoundsException exception
     * will be thrown.
     * @return the index of the plot in the panel (int).
     * @see #addScatterPlot(String,Color,double[]...)
     * @see #addBarPlot(String, Color, double[]...)
     * @see #addBoxPlot(String, Color, double[]... )
     * @see #addHistogramPlot(String, Color, double[]...)
     * @see #addStaircasePlot(String, Color, double[]...)
     */
    public int addLinePlot(String name, Color color, double[][] XY) {
        return ((Plot3DCanvas) plotCanvas).addLinePlot(name, color, XY);
    }

    public int addLinePlot(String name, Color color, double[] X, double[] Y, double[] Z) {
        return ((Plot3DCanvas) plotCanvas).addLinePlot(name, color, X, Y, Z);
    }

    public int addLinePlot(String name, double[][] XY) {
        return addLinePlot(name, getNewColor(), XY);
    }

    public int addLinePlot(String name, double[] X, double[] Y, double[] Z) {
        return addLinePlot(name, getNewColor(), X, Y, Z);
    }

    /**
     * Adds a bar plot (each data point is shown as a dot marker connected to
     * the horizontal axis by a vertical line) to the current plot panel.
     * @param name Name for the plot, which will be used in the legend.
     * (String)
     * @param color Plot color. (Color)
     * @param XY Array of triple double. For each triple, first one contains the X position
     * of the data points, second contains Y position, third contains Z position.
     * <br>
     * Each array of the triple
     * must be of the same length; if not a ArrayIndexOutOfBoundsException
     * exception will be thrown.
     * <br>
     * Each data set must come in 
     * array of <b>triple</b> of double; if not a ArrayIndexOutOfBoundsException exception
     * will be thrown.
     * @return the index of the plot in the panel (int).
     * @see #addScatterPlot(String,Color,double[]...)
     * @see #addLinePlot(String, Color, double[]...)
     * @see #addBoxPlot(String, Color, double[]... )
     * @see #addHistogramPlot(String, Color, double[]...)
     * @see #addStaircasePlot(String, Color, double[]...)
     */
    public int addBarPlot(String name, Color color, double[][] XY) {
        return ((Plot3DCanvas) plotCanvas).addBarPlot(name, color, XY);
    }

    public int addBarPlot(String name, Color color, double[] X, double[] Y, double[] Z) {
        return ((Plot3DCanvas) plotCanvas).addBarPlot(name, color, X, Y, Z);
    }

    public int addBarPlot(String name, double[][] XY) {
        return addBarPlot(name, getNewColor(), XY);
    }

    public int addBarPlot(String name, double[] X, double[] Y, double[] Z) {
        return addBarPlot(name, getNewColor(), X, Y, Z);
    }

    public int addBoxPlot(String name, Color c, double[][] XY, double[][] dX) {
        return ((Plot3DCanvas) plotCanvas).addBoxPlot(name, c, XY, dX);
    }

    public int addBoxPlot(String name, double[][] XY, double[][] dX) {
        return addBoxPlot(name, getNewColor(), XY, dX);
    }

    public int addBoxPlot(String name, Color c, double[][] XYdX) {
        return ((Plot3DCanvas) plotCanvas).addBoxPlot(name, c, Array.getColumnsRangeCopy(XYdX, 0, 2), Array.getColumnsRangeCopy(XYdX, 3, 5));
    }

    public int addBoxPlot(String name, double[][] XYdX) {
        return addBoxPlot(name, getNewColor(), XYdX);
    }

    public int addHistogramPlot(String name, Color c, double[][] XY, double[][] dX) {
        return ((Plot3DCanvas) plotCanvas).addHistogramPlot(name, c, XY, dX);
    }

    public int addHistogramPlot(String name, double[][] XY, double[][] dX) {
        return addHistogramPlot(name, getNewColor(), XY, dX);
    }

    public int addHistogramPlot(String name, Color c, double[][] XYdX) {
        return ((Plot3DCanvas) plotCanvas).addHistogramPlot(name, c, Array.getColumnsRangeCopy(XYdX, 0, 2), Array.getColumnsRangeCopy(XYdX, 3, 4));
    }

    public int addHistogramPlot(String name, double[][] XYdX) {
        return addHistogramPlot(name, getNewColor(), XYdX);
    }

    public int addHistogramPlot(String name, Color c, double[][] XY, int nX, int nY) {
        return ((Plot3DCanvas) plotCanvas).addHistogramPlot(name, c, XY, nX, nY);
    }

    public int addHistogramPlot(String name, double[][] XY, int nX, int nY) {
        return addHistogramPlot(name, getNewColor(), XY, nX, nY);
    }

    public int addHistogramPlot(String name, Color c, double[][] XY, double[] boundsX, double[] boundsY) {
        return ((Plot3DCanvas) plotCanvas).addHistogramPlot(name, c, XY, boundsX, boundsY);
    }

    public int addHistogramPlot(String name, double[][] XY, double[] boundsX, double[] boundsY) {
        return addHistogramPlot(name, getNewColor(), XY, boundsX, boundsY);
    }

    public int addHistogramPlot(String name, Color c, double[][] XY, double minX, double maxX, int nX, double minY, double maxY, int nY) {
        return ((Plot3DCanvas) plotCanvas).addHistogramPlot(name, c, XY, minX, maxX, nX, minY, maxY, nY);
    }

    public int addHistogramPlot(String name, double[][] XY, double minX, double maxX, int nX, double minY, double maxY, int nY) {
        return addHistogramPlot(name, getNewColor(), XY, minX, maxX, nX, minY, maxY, nY);
    }

    public int addGridPlot(String name, Color c, double[] X, double[] Y, double[][] Z) {
        return ((Plot3DCanvas) plotCanvas).addGridPlot(name, c, X, Y, Z);
    }

    public int addGridPlot(String name, double[] X, double[] Y, double[][] Z) {
        return addGridPlot(name, getNewColor(), X, Y, Z);
    }

    public int addGridPlot(String name, Color c, double[][] XYZMatrix) {
        return ((Plot3DCanvas) plotCanvas).addGridPlot(name, c, XYZMatrix);
    }

    public int addGridPlot(String name, double[][] XYZMatrix) {
        return addGridPlot(name, getNewColor(), XYZMatrix);
    }

    public int addCloudPlot(String name, Color color, double[][] sampleXYZ, int nX, int nY, int nZ) {
        return ((Plot3DCanvas) plotCanvas).addCloudPlot(name, color, sampleXYZ, nX, nY, nZ);
    }

    public int addCloudPlot(String name, double[][] sampleXYZ, int nX, int nY, int nZ) {
        return addCloudPlot(name, getNewColor(), sampleXYZ, nX, nY, nZ);
    }

    @Override
    public int addPlot(String type, String name, Color c, double[]... XY) {
        if (type.equalsIgnoreCase(SCATTER)) {
            return addScatterPlot(name, c, XY);
        } else if (type.equalsIgnoreCase(LINE)) {
            return addLinePlot(name, c, XY);
        } else if (type.equalsIgnoreCase(BAR)) {
            return addBarPlot(name, c, XY);
        } else if (type.equalsIgnoreCase(HISTOGRAM)) {
            return addHistogramPlot(name, c, XY);
        } else if (type.equalsIgnoreCase(BOX)) {
            return addBoxPlot(name, c, XY);
        } else if (type.equalsIgnoreCase(GRID)) {
            return addGridPlot(name, c, XY);
        } else {
            throw new IllegalArgumentException("Plot type is unknown : " + type);
        }
    }

    public void rotate(double theta, double phi) {
        ((Plot3DCanvas) plotCanvas).rotate(theta, phi);
        repaint();
    }
}
