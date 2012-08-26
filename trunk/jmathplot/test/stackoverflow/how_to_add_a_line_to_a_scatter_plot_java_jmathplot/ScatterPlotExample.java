package stackoverflow.how_to_add_a_line_to_a_scatter_plot_java_jmathplot;

import java.awt.Color;
import javax.swing.*;
import org.math.plot.*;
import org.math.plot.plotObjects.Line;

public class ScatterPlotExample {

    public static void main(String[] args) {

        double[] x = new double[]{60};
        double[] y = new double[]{50};

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel

        plot.addScatterPlot("teeeeest", x, y);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("a plot panel");
        frame.setSize(600, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);

        makeAxis1to100(plot);
        drawHorizontalLine(plot);
    }

    public static void makeAxis1to100(PlotPanel plot) {
        plot.setFixedBounds(0, 1, 100);
    }

    public static void drawHorizontalLine(PlotPanel plot) {
        plot.addPlotable(new Line(Color.red, new double[]{plot.plotCanvas.base.getMinBounds()[0],49.5},new double[]{plot.plotCanvas.base.getMaxBounds()[0],49.5}));
    }
}