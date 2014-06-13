package org.math.plot.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.math.plot.DataPanel;
import org.math.plot.MatrixTablePanel;
import org.math.plot.canvas.PlotCanvas;
import org.math.plot.plotObjects.Editable;
import org.math.plot.plotObjects.Noteable;
import org.math.plot.plotObjects.Plotable;
import org.math.plot.render.AbstractDrawer;
import org.math.plot.utils.Array;

public abstract class Plot implements Plotable, Noteable, Editable {

    public String name;
    public Color color;
    public boolean visible = true;
    public LinkedList<LayerPlot> layers;
    public boolean noted = false;
    public double[] coordNoted;
    //public boolean forcenoted = false;
    public int note_precision = 5;

    public Plot(String n, Color c) {
        name = n;
        color = c;
        layers = new LinkedList<LayerPlot>();

    }

    public void clearLayers() {
        layers.clear();
    }

    public void addLayer(LayerPlot q) {
        layers.add(q);
    }

    public void addQuantile(QuantileLayerPlot q) {
        layers.add(q);
    }

    public void addQuantile(int a, double r, double[] q, boolean symetric) {
        layers.add(new QuantileLayerPlot(this, a, q, r, symetric));
    }

    public void addQuantile(int a, double r, double q, boolean symetric) {
        layers.add(new QuantileLayerPlot(this, a, q, r, symetric));
    }

    public void addQuantiles(int a, double[][] q) {
        layers.add(new DensityLayerPlot(this, a, q));
    }

    public void addQuantiles(int a, double[] q) {
        layers.add(new DensityLayerPlot(this, a, q));
    }

    public void addGaussQuantiles(int a, double[] s) {
        layers.add(new GaussianDensityLayerPlot(this, a, s));
    }

    public void addGaussQuantiles(int a, double s) {
        layers.add(new GaussianDensityLayerPlot(this, a, s));
    }

    /*public void addQuantiles(double[][][] q,boolean _symetric) {
    for (int i = 0; i < q[0].length; i++) {
    addQuantile(i, Array.getColumnCopy(q, i, 0),_symetric);
    addQuantile(i, Array.getColumnCopy(q, i, 1),_symetric);
    }
    }*/

    /*public void addQuantiles(double[][] q,boolean _symetric) {
    for (int i = 0; i < q[0].length; i++) {
    addQuantile(i, Array.getColumnCopy(q, i),_symetric);
    }
    }*/
    public void addVector(double[][] v) {
        layers.add(new VectorLayerPlot(this, v));
    }

    public abstract void setData(double[][] d);

    public abstract double[][] getData();

    public double[] getBounds(int axis) {
        return Array.getColumnCopy(getBounds(), axis);
    }

    /**This method should be abstract, but for backward compatibility, here is a basic impl.*/
    public double[][] getBounds() {
        return Array.mergeRows(Array.min(getData()), Array.max(getData()));
    }

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    /*
     * public String getType() { return type; }
     */
    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    public abstract double[] isSelected(int[] screenCoordTest, AbstractDrawer draw);

    public void note(AbstractDrawer draw) {
        plot(draw, PlotCanvas.NOTE_COLOR);
        plotLayerPlots(draw, PlotCanvas.NOTE_COLOR);
    }

    public void noteCoord(AbstractDrawer draw, double[] coordNoted) {
        if (coordNoted == null) {
            return;
        }

        draw.setColor(PlotCanvas.NOTE_COLOR);
        draw.drawCoordinate(coordNoted);
        draw.drawShadowedText(Array.cat("\n", draw.canvas.reverseMapedData(coordNoted)), .5f, coordNoted);
    }

    public abstract void plot(AbstractDrawer draw, Color c);

    public void plot(AbstractDrawer draw) {
        //if (layers.size() > 0)
        plotLayerPlots(draw, color);
        //else
        plot(draw, color);
    }

    public void plotLayerPlots(AbstractDrawer draw, Color c) {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).plot(draw, c);
        }

    }

    public void edit(Object src) {
        ((PlotCanvas) src).displayDataFrame(((PlotCanvas) src).getPlotIndex(this));
    }

    public void editnote(AbstractDrawer draw) {
        plot(draw, PlotCanvas.EDIT_COLOR);
        plotLayerPlots(draw, PlotCanvas.EDIT_COLOR);
    }
    public DataPanel datapanel = null;
    public PlotCanvas plotCanvas;

    public DataPanel getDataPanel(PlotCanvas plotCanvas) {
        this.plotCanvas = plotCanvas;
        if (datapanel == null) {
            datapanel = new DefaultDataPanel(this);
        }
        return datapanel;
    }

    public class DefaultDataPanel extends DataPanel {

        private static final long serialVersionUID = 1L;
        MatrixTablePanel XY;
        JCheckBox visible;
        JButton color;
        JPanel plottoolspanel;
        Plot plot;
        //DataFrame dframe;

        public DefaultDataPanel(/*DataFrame _dframe,*/Plot _plot) {
            plot = _plot;
            //dframe = _dframe;
            visible = new JCheckBox("Visible");
            visible.setSelected(plot.getVisible());
            color = new JButton();
            color.setBackground(plot.getColor());
            XY = new MatrixTablePanel(plotCanvas.reverseMapedData(plot.getData()));

            visible.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    if (visible.isSelected()) {
                        plot.setVisible(true);
                    } else {
                        plot.setVisible(false);
                    }
                    plotCanvas.linkedLegendPanel.updateLegends();
                    /*dframe.*/ plotCanvas.repaint();
                }
            });
            color.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Color c = JColorChooser.showDialog(plotCanvas, "Choose plot color", plot.getColor());
                    color.setBackground(c);
                    plot.setColor(c);
                    plotCanvas.linkedLegendPanel.updateLegends();
                    /*dframe.*/ plotCanvas.linkedLegendPanel.repaint();
                    /*dframe.*/ plotCanvas.repaint();
                }
            });

            this.setLayout(new BorderLayout());
            plottoolspanel = new JPanel();
            plottoolspanel.add(visible);
            plottoolspanel.add(color);
            this.add(plottoolspanel, BorderLayout.NORTH);
            this.add(XY, BorderLayout.CENTER);
        }

        @Override
        protected void toWindow() {
            XY.toWindow();
        }

        @Override
        public void toClipBoard() {
            XY.toClipBoard();
        }

        @Override
        public void toASCIIFile(File file) {
            XY.toASCIIFile(file);
        }

        public String getText() {
            return XY.getText();
        }
    }
}