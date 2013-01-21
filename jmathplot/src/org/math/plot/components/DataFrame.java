package org.math.plot.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.math.plot.*;
import org.math.plot.canvas.*;
import org.math.plot.plots.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class DataFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private PlotCanvas plotCanvas;
    private LegendPanel legend;
    private JTabbedPane panels;

    public DataFrame(PlotCanvas p, LegendPanel l) {
        super("Data");
        plotCanvas = p;
        legend = l;
        JPanel panel = new JPanel();
        panels = new JTabbedPane();

        panel.add(panels);
        setContentPane(panel);
        //setVisible(true);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            setPanel();
        }
        super.setVisible(b);
    }

    private void setPanel() {
        panels.removeAll();
        for (Plot plot : plotCanvas.getPlots()) {
            panels.add(new DataPanel(/*this, */plot), plot.getName());
        }
        pack();
    }

    public void selectIndex(int i) {
        setVisible(true);
        if (panels.getTabCount() > i) {
            panels.setSelectedIndex(i);
        }
    }

    public class DataPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        MatrixTablePanel XY;
        JCheckBox visible;
        JButton color;
        JPanel plottoolspanel;
        Plot plot;
        DataFrame dframe;

        public DataPanel(/*DataFrame _dframe,*/Plot _plot) {
            plot = _plot;
            //dframe = _dframe;
            visible = new JCheckBox("visible");
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
                    /*dframe.*/ plotCanvas.repaint();
                }
            });
            color.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Color c = JColorChooser.showDialog(plotCanvas, "Choose plot color", plot.getColor());
                    color.setBackground(c);
                    plot.setColor(c);
                    legend.updateLegends();
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
    }
}