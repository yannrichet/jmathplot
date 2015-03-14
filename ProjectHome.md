| **Professional support is now available at https://sites.google.com/site/mulabsltd/products/jmathplot**|
|:|

# JMathPlot: interactive 2D and 3D plots #
Provides interactive 2D/3D plot (without openGL) :
  * 2D/3D scatter plot
  * 2D/3D line plot
  * 2D staircase plot
  * 2D/3D histogram plot
  * 2D/3D boxplot
  * 3D grid plot
  * 2D/3D quantiles on plots

**_Note: for a true OpenGL java plot library, try the good [jzy3d](http://code.google.com/p/jzy3d) project_**

# Example Java code #
```
import org.math.plot.*;
...
  
  double[] x = ...
  double[] y = ...
 
  // create your PlotPanel (you can use it as a JPanel)
  Plot2DPanel plot = new Plot2DPanel();
 
  // add a line plot to the PlotPanel
  plot.addLinePlot("my plot", x, y);
 
  // put the PlotPanel in a JFrame, as a JPanel
  JFrame frame = new JFrame("a plot panel");
  frame.setContentPane(plot);
  frame.setVisible(true);
```
# Use it #
  1. put [jmathplot.jar](http://jmathplot.googlecode.com/svn/trunk/jmathplot/dist/jmathplot.jar) in your java classpath
  1. create a new PlotPanel instance: `Plot2DPanel plot = new Plot2DPanel();`
  1. add a plot inside `plot.addLinePlot("my plot", x, y);`
  1. use the PlotPanel as any Swing component (all PlotPanel extends JPanel, in fact)