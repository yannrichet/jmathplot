# JMathPlot: interactive 2D and 3D plots #

Provides interactive 2D/3D plot (without openGL) :

    2D/3D scatter plot
    2D/3D line plot
    2D staircase plot
    2D/3D histogram plot
    2D/3D boxplot
    3D grid plot
    2D/3D quantiles on plots 

Note: for a true OpenGL java plot library, try the good jzy3d project

## Example Java code ##

```java
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

## Use it ##

Put https://github.com/yannrichet/jmathplot/blob/master/dist/jmathplot.jar in your java classpath

Or include maven dependency:
```xml
<dependencies>
...
    <dependency>
      <groupId>com.github.yannrichet</groupId>
      <artifactId>JMathPlot</artifactId>
      <version>1.0.1</version>
    </dependency>
...
</dependencies>
```

Then
- create a new PlotPanel instance: `PlotPanel plot = new Plot2DPanel();`
- add a plot inside `plot.addLinePlot("my plot", x, y);`
- use the PlotPanel as any Swing component (all PlotPanel extends JPanel, in fact) 

![Analytics](https://ga-beacon.appspot.com/UA-109580-20/jmathplot)
