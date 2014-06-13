package examples;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.GridLayout;
import org.math.plot.*;

/**
 * Copyright : BSD License
 * @author Yann RICHET
 */
public class TestApplet extends Applet {

    @Override
    public void init() {
        // Data definition
        int n = 10;
        double[][] datas1 = new double[n][3];
        double[][] datas2 = new double[n][3];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) {
                datas1[i][j] = Math.random();
                datas2[i][j] = Math.random();
            }
        }

        // PlotPanel construction
        Plot3DPanel plotpanel = new Plot3DPanel();
        plotpanel.addLegend("SOUTH");

        // Data plots addition
        plotpanel.addScatterPlot("datas1", datas1);
        plotpanel.addBarPlot("datas2", datas2);

        plotpanel.setSize(600, 600);
        plotpanel.setPreferredSize(new Dimension(600, 600));

        // include plot in applet
        //setLayout(new GridLayout(1,1));
        setSize(600, 600);
        setPreferredSize(new Dimension(600, 600));

        add(plotpanel);
    }
}