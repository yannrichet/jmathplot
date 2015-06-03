package org.math.plot.canvas;

import java.awt.BasicStroke;
import java.awt.Stroke;
import org.math.plot.render.AWTDrawer;
import org.math.plot.utils.FastMath;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.math.plot.components.DataFrame;
import org.math.plot.components.LegendPanel;
import org.math.plot.components.ScalesFrame;
import org.math.plot.plotObjects.Base;
import org.math.plot.plotObjects.BaseDependant;
import org.math.plot.plotObjects.BasePlot;
import org.math.plot.plotObjects.Plotable;
import org.math.plot.plots.Plot;
import org.math.plot.render.AbstractDrawer;
import org.math.plot.utils.Array;

import static org.math.plot.plotObjects.Base.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 * Changed on 6/13/2014 by Jerry Dietrich 
 * Contact info ballooninternet@cox.net
 */
public abstract class PlotCanvas extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, BaseDependant, MouseWheelListener {

    //public int[] panelSize = new int[] { 400, 400 };
    public Base base;
    protected AbstractDrawer draw;
    protected BasePlot grid;
    public LegendPanel linkedLegendPanel;
    public LinkedList<Plot> plots;
    public LinkedList<Plotable> objects;

    // ///////////////////////////////////////////
    // ////// Constructor & inits ////////////////
    // ///////////////////////////////////////////
    public PlotCanvas() {
        initPanel();
        initBasenGrid();
        initDrawer();
    }

    public PlotCanvas(Base b, BasePlot bp) {
        initPanel();
        initBasenGrid(b, bp);
        initDrawer();
    }

    public PlotCanvas(double[] min, double[] max) {
        initPanel();
        initBasenGrid(min, max);
        initDrawer();
    }

    public PlotCanvas(double[] min, double[] max, String[] axesScales, String[] axesLabels) {
        initPanel();
        initBasenGrid(min, max, axesScales, axesLabels);
        initDrawer();
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }

    public void attachLegend(LegendPanel lp) {
        linkedLegendPanel = lp;
    }

    private void initPanel() {
        objects = new LinkedList<Plotable>();
        plots = new LinkedList<Plot>();

        setDoubleBuffered(true);

        //setSize(panelSize[0], panelSize[1]);
        //setPreferredSize(new Dimension(panelSize[0], panelSize[1]));
        setBackground(Color.white);

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public abstract void initDrawer();

    public void initBasenGrid(double[] min, double[] max, String[] axesScales, String[] axesLabels) {
        base = new Base(min, max, axesScales);
        grid = new BasePlot(base, axesLabels);
        // grid.getAxe(0).getDarkLabel().setCorner(0.5,-1);
        // grid.getAxe(1).getDarkLabel().setCorner(0,-0.5);
    }

    public abstract void initBasenGrid(double[] min, double[] max);

    public abstract void initBasenGrid();

    public void initBasenGrid(Base b, BasePlot bp) {
        base = b;
        grid = bp;

    }

    // ///////////////////////////////////////////
    // ////// set actions ////////////////////////
    // ///////////////////////////////////////////
    public void setActionMode(int am) {
        ActionMode = am;
    }

    public void setNoteCoords(boolean b) {
        allowNoteCoord = b;
    }

    public void setEditable(boolean b) {
        allowEdit = b;
    }

    public boolean getEditable() {
        return allowEdit;
    }

    public void setNotable(boolean b) {
        allowNote = b;
    }

    public boolean getNotable() {
        return allowNote;
    }

    // ///////////////////////////////////////////
    // ////// set/get elements ///////////////////
    // ///////////////////////////////////////////
    public LinkedList<Plot> getPlots() {
        return plots;
    }

    public Plot getPlot(int i) {
        return (Plot) plots.get(i);
    }

    public int getPlotIndex(Plot p) {
        for (int i = 0; i < plots.size(); i++) {
            if (getPlot(i) == p) {
                return i;
            }
        }
        return -1;
    }

    public LinkedList<Plotable> getPlotables() {
        return objects;
    }

    public Plotable getPlotable(int i) {
        return (Plotable) objects.get(i);
    }

    public BasePlot getGrid() {
        return grid;
    }

    public String[] getAxisScales() {
        return base.getAxesScales();
    }

    public void setAxisLabels(String... labels) {
        grid.setLegend(labels);
        repaint();
    }

    public void setAxisLabel(int axe, String label) {
        grid.setLegend(axe, label);
        repaint();
    }

    public void setAxisScales(String... scales) {
        base.setAxesScales(scales);
        setAutoBounds();
    }

    public void setAxiScale(int axe, String scale) {
        base.setAxesScales(axe, scale);
        setAutoBounds(axe);
    }

    public void setFixedBounds(double[] min, double[] max) {
        base.setFixedBounds(min, max);
        resetBase();
        repaint();
    }

    public void setFixedBounds(int axe, double min, double max) {
        base.setFixedBounds(axe, min, max);
        resetBase();
        repaint();
    }

    public void includeInBounds(double... into) {
        boolean changed = base.includeInBounds(into);
        if (!changed) {
            return;
        }
        grid.resetBase();
        repaint();
    }

    public void includeInBounds(Plot plot) {
        boolean changed = base.includeInBounds(Array.min(plot.getBounds()));
        changed = changed | base.includeInBounds(Array.max(plot.getBounds()));
        if (!changed) {
            return;
        }
        resetBase();
        repaint();
    }

    public void setAutoBounds() {
        if (plots.size() > 0) {
            Plot plot0 = this.getPlot(0);
            base.setRoundBounds(Array.min(plot0.getBounds()), Array.max(plot0.getBounds()));
        } else { // build default min and max bounds
            double[] min = new double[base.dimension];
            double[] max = new double[base.dimension];
            for (int i = 0; i < base.dimension; i++) {
                if (base.getAxeScale(i).equalsIgnoreCase(LINEAR)) {
                    min[i] = 0.0;
                    max[i] = 1.0;
                } else if (base.getAxeScale(i).equalsIgnoreCase(LOGARITHM)) {
                    min[i] = 1.0;
                    max[i] = 10.0;
                }
            }
            base.setRoundBounds(min, max);
        }
        for (int i = 1; i < plots.size(); i++) {
            Plot ploti = this.getPlot(i);
            base.includeInBounds(Array.min(ploti.getBounds()));
            base.includeInBounds(Array.max(ploti.getBounds()));
        }
        resetBase();
        repaint();
    }

    public void setAutoBounds(int axe) {
        if (plots.size() > 0) {
            Plot plot0 = this.getPlot(0);
            base.setRoundBounds(axe, Array.min(plot0.getBounds())[axe], Array.max(plot0.getBounds())[axe]);
        } else { // build default min and max bounds
            double min = 0.0;
            double max = 0.0;
            if (base.getAxeScale(axe).equalsIgnoreCase(LINEAR) | base.getAxeScale(axe).equalsIgnoreCase(STRINGS)) {
                min = 0.0;
                max = 1.0;
            } else if (base.getAxeScale(axe).equalsIgnoreCase(LOGARITHM)) {
                min = 1.0;
                max = 10.0;
            }
            base.setRoundBounds(axe, min, max);
        }

        for (int i = 1; i < plots.size(); i++) {
            Plot ploti = this.getPlot(i);
            base.includeInBounds(axe, Array.min(ploti.getBounds())[axe]);
            base.includeInBounds(axe, Array.max(ploti.getBounds())[axe]);
        }
        resetBase();
        repaint();
    }

    public void resetBase() {
        // System.out.println("PlotCanvas.resetBase");
        draw.resetBaseProjection();
        grid.resetBase();

        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i) instanceof BaseDependant) {
                ((BaseDependant) (objects.get(i))).resetBase();
            }
        }
        repaint();
    }

    // ///////////////////////////////////////////
    // ////// add/remove elements ////////////////
    // ///////////////////////////////////////////
    public void addLabel(String text, Color c, double... where) {
        addPlotable(new org.math.plot.plotObjects.Label(text, c, where));
    }

    public void addBaseLabel(String text, Color c, double... where) {
        addPlotable(new org.math.plot.plotObjects.BaseLabel(text, c, where));
    }

    public void addPlotable(Plotable p) {
        objects.add(p);
        // resetBase();
        repaint();
    }

    public void removePlotable(Plotable p) {
        objects.remove(p);
        repaint();
    }

    public void removePlotable(int i) {
        objects.remove(i);
        repaint();
    }

    public void removeAllPlotables() {
        objects.clear();
        repaint();
    }
    boolean adjustBounds = true;

    public void setAdjustBounds(boolean adjust) {
        adjustBounds = adjust;
    }

    public boolean getAdjustBounds() {
        return adjustBounds;
    }

    public int addPlot(Plot newPlot) {
        plots.add(newPlot);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        if (plots.size() == 1) {
            setAutoBounds();
        } else {
            if (adjustBounds) {
                includeInBounds(newPlot);
            } else {
                repaint();
            }
        }
        return plots.size() - 1;
    }

    public void setPlot(int I, Plot p) {
        plots.set(I, p);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        repaint();
    }

    public void changePlotData(int I, double[]... XY) {
        getPlot(I).setData(XY);
        if (adjustBounds) {
            includeInBounds(getPlot(I));
        } else {
            repaint();
        }
    }

    public void changePlotName(int I, String name) {
        getPlot(I).setName(name);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        repaint();
    }

    public void changePlotColor(int I, Color c) {
        getPlot(I).setColor(c);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        repaint();
    }

    public void removePlot(int I) {
        plots.remove(I);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        if (plots.size() != 0) {
            if (adjustBounds) {
                setAutoBounds();
            } else {
                repaint();
            }
        }

    }

    public void removePlot(Plot p) {
        plots.remove(p);
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        if (plots.size() != 0) {
            if (adjustBounds) {
                setAutoBounds();
            }
        }

    }

    public void removeAllPlots() {
        plots.clear();
        if (linkedLegendPanel != null) {
            linkedLegendPanel.updateLegends();
        }
        clearNotes();
    }

    public void addVectortoPlot(int numPlot, double[][] v) {
        getPlot(numPlot).addVector(v);
    }

    /*public void addQuantiletoPlot(int numPlot, boolean _symetric, double[]... q) {
    getPlot(numPlot).addQuantiles(q, _symetric);
    }*/
    public void addQuantiletoPlot(int numPlot, int numAxe, double rate, boolean symetric, double[] q) {
        getPlot(numPlot).addQuantile(numAxe, rate, q, symetric);
    }

    public void addQuantiletoPlot(int numPlot, int numAxe, double rate, boolean symetric, double q) {
        getPlot(numPlot).addQuantile(numAxe, rate, q, symetric);
    }

    public void addQuantilestoPlot(int numPlot, int numAxe, double[][] q) {
        getPlot(numPlot).addQuantiles(numAxe, q);
    }

    public void addQuantilestoPlot(int numPlot, int numAxe, double[] q) {
        getPlot(numPlot).addQuantiles(numAxe, q);
    }

    public void addGaussQuantilestoPlot(int numPlot, int numAxe, double[] s) {
        getPlot(numPlot).addGaussQuantiles(numAxe, s);
    }

    public void addGaussQuantilestoPlot(int numPlot, int numAxe, double s) {
        getPlot(numPlot).addGaussQuantiles(numAxe, s);
    }

    // ///////////////////////////////////////////
    // ////// call for toolbar actions ///////////
    // ///////////////////////////////////////////
    public void toGraphicFile(File file) throws IOException {

        Image image = createImage(getWidth(), getHeight());
        paint(image.getGraphics());
        image = new ImageIcon(image).getImage();

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();

        try {
            ImageIO.write((RenderedImage) bufferedImage, "PNG", file);
        } catch (IllegalArgumentException ex) {
        }
    }
    JFrame scalesFrame = new ScalesFrame(this);

    public void setScalesFrame(JFrame scalesFrame) {
        this.scalesFrame = scalesFrame;
    }

    public void displayScalesFrame() {
        scalesFrame.setVisible(true);
    }
    DataFrame dataFrame = new DataFrame(this);

    public void setDataFrame(DataFrame dataFrame) {
        this.dataFrame = dataFrame;
    }

    public void displayDataFrame(int i) {
        dataFrame.selectIndex(i);
    }

    public void displayDataFrame() {
        displayDataFrame(0);
    }
    boolean mapset = false;

    public void resetMapData() {
        for (int i = 0; i < grid.getAxis().length; i++) {
            grid.getAxis()[i].setStringMap(null);
            setAxiScale(i, Base.LINEAR);
        }
        mapset = false;
    }

    public double[][] mapData(Object[][] data) {
        //System.out.println("mapData:" + Array.cat(data));

        double[][] mapeddata = new double[data.length][data[0].length];

        if (!mapset) {
            for (int j = 0; j < data[0].length; j++) {
                if (!Array.isDouble(data[0][j].toString())) {
                    //System.out.println(data[0][j].toString() + " is not a double");
                    setAxiScale(j, Base.STRINGS);

                    ArrayList<String> string_array_j = new ArrayList<String>(data.length);
                    for (int i = 0; i < data.length; i++) {
                        string_array_j.add(data[i][j].toString());
                    }

                    grid.getAxis(j).setStringMap(Array.mapStringArray(string_array_j));
                    grid.getAxis(j).init();

                    for (int i = 0; i < data.length; i++) {
                        mapeddata[i][j] = grid.getAxis(j).getStringMap().get(data[i][j].toString());
                    }

                    //System.out.println("Axe " + j + ":" + Array.toString(grid.getAxe(j).getStringMap()));
                    initReverseMap(j);
                } else {
                    //System.out.println(data[0][j].toString() + " is a double");
                    //System.out.println("Axe " + j + ": double[]");
                    for (int i = 0; i < data.length; i++) {
                        mapeddata[i][j] = Double.valueOf(data[i][j].toString());
                    }
                }
            }
            mapset = true;
        } else {
            for (int j = 0; j < data[0].length; j++) {
                if (!Array.isDouble(data[0][j].toString())) {
                    //System.out.println(data[0][j].toString() + " is not a double");
                    if (base.getAxeScale(j).equals(Base.STRINGS)) {
                        for (int i = 0; i < data.length; i++) {
                            if (!grid.getAxis(j).getStringMap().containsKey(data[i][j].toString())) {
                                Set<String> s = grid.getAxis(j).getStringMap().keySet();
                                ArrayList<String> string_array_j = new ArrayList<String>(s.size() + 1);
                                string_array_j.addAll(s);
                                string_array_j.add(data[i][j].toString());
                                grid.getAxis(j).setStringMap(Array.mapStringArray(string_array_j));

                                //System.out.println("Axe " + j + ":" + Array.toString(grid.getAxe(j).getStringMap()));
                                initReverseMap(j);
                            }
                            mapeddata[i][j] = grid.getAxis(j).getStringMap().get(data[i][j].toString());
                        }
                    } else {
                        throw new IllegalArgumentException("The mapping of this PlotPanel was not set on axis " + j);
                    }
                } else {
                    //System.out.println(data[0][j].toString() + " is a double");
                    //System.out.println("Axe " + j + ": double[]");
                    for (int i = 0; i < data.length; i++) {
                        mapeddata[i][j] = Double.valueOf(data[i][j].toString());
                    }
                }
            }
        }
        return mapeddata;
    }

    public Object[][] reverseMapedData(double[][] mapeddata) {
        Object[][] stringdata = new Object[mapeddata.length][mapeddata[0].length];

        for (int i = 0; i < mapeddata.length; i++) {
            stringdata[i] = reverseMapedData(mapeddata[i]);
        }

        return stringdata;
    }

    public Object[] reverseMapedData(double[] mapeddata) {
        Object[] stringdata = new Object[mapeddata.length];

        if (reversedMaps == null) {
            reversedMaps = new HashMap[grid.getAxis().length];
        }

        for (int j = 0; j < mapeddata.length; j++) {
            if (reversedMaps[j] != null) {
                stringdata[j] = reversedMaps[j].get((Double) (mapeddata[j]));
            } else {
                stringdata[j] = (Double) (mapeddata[j]);
            }
        }

        return stringdata;
    }
    HashMap<Double, String>[] reversedMaps;

    private void initReverseMap(int j) {
        if (reversedMaps == null) {
            reversedMaps = new HashMap[grid.getAxis().length];
        }

        if (grid.getAxis(j) != null) {
            reversedMaps[j] = Array.reverseStringMap(grid.getAxis(j).getStringMap());
        }
    }
    // ///////////////////////////////////////////
    // ////// Paint method ///////////////////////
    // ///////////////////////////////////////////
    // anti-aliasing constant
    final protected static RenderingHints AALIAS = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    public static Color NOTE_COLOR = Color.DARK_GRAY;
    public static Color EDIT_COLOR = Color.BLACK;
    public boolean allowEdit = true;
    public boolean allowNote = true;
    public boolean allowNoteCoord = true;

    public void paint(Graphics gcomp) {
        // System.out.println("PlotCanvas.paint");

        Graphics2D gcomp2D = (Graphics2D) gcomp;

        // anti-aliasing methods
        gcomp2D.addRenderingHints(AALIAS);
        gcomp2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        gcomp2D.setColor(getBackground());
        gcomp2D.fillRect(0, 0, getSize().width, getSize().height);

        draw.initGraphics(gcomp2D);

        // draw plot
        grid.plot(draw);

        for (int i = 0; i < plots.size(); i++) {
            getPlot(i).plot(draw);
            if (linkedLegendPanel != null) {
                linkedLegendPanel.nonote(i);
            }
        }

        for (int i = 0; i < objects.size(); i++) {
            getPlotable(i).plot(draw);
        }

        if (drawRect != null) {
            gcomp2D.setColor(Color.black);
            gcomp2D.setStroke(rectStroke);
            gcomp2D.drawRect(drawRect[0], drawRect[1], drawRect[2], drawRect[3]);
        }

        // draw note
        if (allowNote) {
            /*if (allowNoteCoord && coordNoted != null) {
            draw.setColor(NOTE_COLOR);
            draw.drawCoordinate(coordNoted);
            draw.drawText(Array.cat(reverseMapedData(coordNoted)), coordNoted);
            }*/
            for (int i = 0; i < plots.size(); i++) {
                if (getPlot(i).noted) {
                    if (linkedLegendPanel != null) {
                        linkedLegendPanel.note(i);
                    }
                    getPlot(i).note(draw);
                    //return;
                }
                if (allowNoteCoord && getPlot(i).coordNoted != null) {
                    getPlot(i).noteCoord(draw, getPlot(i).coordNoted);
                }
            }
        }
    }
    // ///////////////////////////////////////////
    // ////// Listeners //////////////////////////
    // ///////////////////////////////////////////
    public final static int ZOOM = 0;
    public final static int TRANSLATION = 1;
    public int ActionMode;
    protected boolean dragging = false;
    protected int[] mouseCurent = new int[2];
    protected int[] mouseClick = new int[2];

    public void clearNotes() {
        for (int i = 0; i < plots.size(); i++) {
            getPlot(i).coordNoted = null;
        }
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        //System.out.println("PlotCanvas.mousePressed");
		/*
         * System.out.println("PlotCanvas.mousePressed"); System.out.println("
         * mouseClick = [" + mouseClick[0] + " " + mouseClick[1] + "]");
         * System.out.println(" mouseCurent = [" + mouseCurent[0] + " " +
         * mouseCurent[1] + "]");
         */
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        mouseClick[0] = mouseCurent[0];
        mouseClick[1] = mouseCurent[1];
    }
    int[] drawRect = null;
    final Stroke rectStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

    public void mouseDragged(MouseEvent e) {
        //System.out.println("PlotCanvas.mouseDragged");

        dragging = true;
        /*
         * System.out.println("PlotCanvas.mouseDragged"); System.out.println("
         * mouseClick = [" + mouseClick[0] + " " + mouseClick[1] + "]");
         * System.out.println(" mouseCurent = [" + mouseCurent[0] + " " +
         * mouseCurent[1] + "]");
         */
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        switch (ActionMode) {
            case TRANSLATION:
                draw.translate(mouseCurent[0] - mouseClick[0], mouseCurent[1] - mouseClick[1]);
                mouseClick[0] = mouseCurent[0];
                mouseClick[1] = mouseCurent[1];
                repaint();
                break;
            case ZOOM:
                int x = FastMath.min(mouseClick[0], mouseCurent[0]);
                int y = FastMath.min(mouseClick[1], mouseCurent[1]);
                int w = FastMath.abs(mouseCurent[0] - mouseClick[0]);
                int h = FastMath.abs(mouseCurent[1] - mouseClick[1]);
                if (drawRect == null) {
                    drawRect = new int[4];
                }
                drawRect[0] = x;
                drawRect[1] = y;
                drawRect[2] = w;
                drawRect[3] = h;
                repaint();  //repaint(x - 1, y - 1, w + 2, h + 2);
                break;
        }
        //repaint();
    }

    public void mouseReleased(MouseEvent e) {
        //System.out.println("PlotCanvas.mouseReleased");

        /*
         * System.out.println("PlotCanvas.mouseReleased"); System.out.println("
         * mouseClick = [" + mouseClick[0] + " " + mouseClick[1] + "]");
         * System.out.println(" mouseCurent = [" + mouseCurent[0] + " " +
         * mouseCurent[1] + "]");
         */
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        switch (ActionMode) {
            case ZOOM:
                if (FastMath.abs(mouseCurent[0] - mouseClick[0]) > 10 && FastMath.abs(mouseCurent[1] - mouseClick[1]) > 10) {
                    int[] origin = {FastMath.min(mouseClick[0], mouseCurent[0]), FastMath.min(mouseClick[1], mouseCurent[1])};
                    double[] ratio = {FastMath.abs((double) (mouseCurent[0] - mouseClick[0]) / (double) getWidth()),
                                      FastMath.abs((double) (mouseCurent[1] - mouseClick[1]) / (double) getHeight())
                    };
                    draw.dilate(origin, ratio);
                    drawRect = null;
                    repaint();
                } else {
                    drawRect = null;
                    repaint();
                }
                break;
        }
        //repaint();
        dragging = false;
    }

    public void mouseClicked(MouseEvent e) {
        //System.out.println("PlotCanvas.mouseClicked");

        /*
         * System.out.println("PlotCanvas.mouseClicked"); System.out.println("
         * mouseClick = [" + mouseClick[0] + " " + mouseClick[1] + "]");
         * System.out.println(" mouseCurent = [" + mouseCurent[0] + " " +
         * mouseCurent[1] + "]");
         */
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        mouseClick[0] = mouseCurent[0];
        mouseClick[1] = mouseCurent[1];

        if (allowEdit) {
            if (e.getModifiers() == MouseEvent.BUTTON1_MASK && e.getClickCount() > 1) {
                for (int i = 0; i < grid.getAxis().length; i++) {
                    if (grid.getAxis(i).isSelected(mouseClick, draw) != null) {
                        grid.getAxis(i).edit(this);
                        return;
                    }
                }

                for (int i = 0; i < plots.size(); i++) {
                    if (getPlot(i).isSelected(mouseClick, draw) != null) {
                        getPlot(i).edit(this);
                        return;
                    }
                }
            }
        }

        if (!dragging && allowNote) {
            for (int i = 0; i < plots.size(); i++) {
                double[] _coordNoted = getPlot(i).isSelected(mouseClick, draw);
                if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                    if (_coordNoted != null) {
                        getPlot(i).noted = !getPlot(i).noted;
                    } else {
                        getPlot(i).noted = false;
                    }
                } else if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                    if (_coordNoted != null) {
                        if (getPlot(i).coordNoted != null) {
                            boolean alreadyNoted = true;
                            for (int j = 0; j < _coordNoted.length; j++) {
                                alreadyNoted = alreadyNoted && _coordNoted[j] == getPlot(i).coordNoted[j];
                            }
                            if (alreadyNoted) {
                                getPlot(i).coordNoted = null;
                            } else {
                                getPlot(i).coordNoted = _coordNoted;
                            }
                        } else {
                            getPlot(i).coordNoted = _coordNoted;
                        }
                    }
                }
            }
            repaint();
        } else {
            dragging = false;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        	for (int i = 0; i < plots.size(); i++) {
        		if (getPlot(i).noted) {
        			double[] _coordNoted = getPlot(i).isSelected(mouseCurent, draw);           
        			if (_coordNoted != null) {
        				getPlot(i).coordNoted = _coordNoted;
        	        	repaint();
        			}
        		}
        	}
        
        //System.out.println("PlotCanvas.mouseMoved");
		/*
         * System.out.println("PlotCanvas.mouseClicked"); System.out.println("
         * mouseClick = [" + mouseClick[0] + " " + mouseClick[1] + "]");
         * System.out.println(" mouseCurent = [" + mouseCurent[0] + " " +
         * mouseCurent[1] + "]");
         */
        /*mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        if (allowNote) {
        for (int i = 0; i < plots.size(); i++) {
        double[] _coordNoted = getPlot(i).isSelected(mouseCurent, draw);
        if (_coordNoted != null) {
        getPlot(i).noted = !getPlot(i).noted;
        } else {
        getPlot(i).noted = false;
        }
        }
        repaint();
        }*/
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        //System.out.println("PlotCanvas.mouseWheelMoved");
		/*
         * System.out.println("PlotCanvas.mouseWheelMoved");
         * System.out.println(" mouseClick = [" + mouseClick[0] + " " +
         * mouseClick[1] + "]"); System.out.println(" mouseCurent = [" +
         * mouseCurent[0] + " " + mouseCurent[1] + "]");
         */
        mouseCurent[0] = e.getX();
        mouseCurent[1] = e.getY();
        e.consume();
        int[] origin;
        double[] ratio;
        // double factor = 1.5;
        //switch (ActionMode) {
        //    case ZOOM:
        if (e.getWheelRotation() == -1) {
            if (Array.max(((AWTDrawer) draw).projection.totalScreenRatio) > .01) {
                origin = new int[]{(int) (mouseCurent[0] - getWidth() / 3/* (2*factor) */),
                                   (int) (mouseCurent[1] - getHeight() / 3/* (2*factor) */)};
                ratio = new double[]{0.666/* 1/factor, 1/factor */, 0.666};
                draw.dilate(origin, ratio);
            }
        } else {
            if (Array.max(((AWTDrawer) draw).projection.totalScreenRatio) < 1) {
                origin = new int[]{(int) (mouseCurent[0] - getWidth() / 1.333/* (2/factor) */),
                                   (int) (mouseCurent[1] - getHeight() / 1.333/* (2/factor) */)
                };
                ratio = new double[]{1.5, 1.5 /* factor, factor */};
                draw.dilate(origin, ratio);
            } else /* (Array.max(((AWTDrawer) draw).projection.totalScreenRatio) >= 1)*/ {
                ((AWTDrawer) draw).projection.initBaseCoordsProjection(true);
            }
        }
        repaint();
        //       break;
        //}
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        //System.out.println("PlotCanvas.componentResized");
        //panelSize = new int[] { (int) (getSize().getWidth()), (int) (getSize().getHeight()) };
        if (draw != null) {
            draw.resetBaseProjection();
        }
        //System.out.println("PlotCanvas : "+panelSize[0]+" x "+panelSize[1]);
        repaint();
        if (linkedLegendPanel != null) {
            linkedLegendPanel.componentResized(e);
        }
    }

    public void componentShown(ComponentEvent e) {
    }
}
