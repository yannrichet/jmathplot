package stackoverflow.how_to_set_axis_location_in_jmathplot;

import javax.swing.*;
import org.math.plot.*;
import org.math.plot.plotObjects.Base;

public class Answer {

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

        setAxisCenter(plot);
    }

    public static void setAxisCenter(PlotPanel plot) {
        plot.plotCanvas.base = new CenteredBase(new double[]{50,50},new double[]{100,100},new String[] { "lin", "lin" });
    }

public static class CenteredBase extends Base {
public CenteredBase(double[] Xmi, double[] Xma, String[] scales) {
super(Xmi, Xma, scales);
centerCoords();
}

private void centerCoords() {
		baseCoords = new double[dimension + 1][];
		for (int i = 0; i < baseCoords.length; i++) {
			baseCoords[i] = new double[]{50,50};
			if (i > 0)
				baseCoords[i][i - 1] = 100;
		}
	}
}
}