```java

import javax.swing.*;
 
import org.math.plot.*;
 
import static java.lang.Math.*;
 
import static org.math.array.DoubleArray.*;
 
public class GridPlotsExample {
        public static void main(String[] args) {
 
                // define your data
                double[] x = increment(0.0, 0.1, 1.0); // x = 0.0:0.1:1.0
                double[] y = increment(0.0, 0.05, 1.0);// y = 0.0:0.05:1.0
                double[][] z1 = f1(x, y);
                double[][] z2 = f2(x, y);
 
                // create your PlotPanel (you can use it as a JPanel) with a legend at SOUTH
                Plot3DPanel plot = new Plot3DPanel("SOUTH");
 
                // add grid plot to the PlotPanel
                plot.addGridPlot("z=cos(PI*x)*sin(PI*y)", x, y, z1);
                plot.addGridPlot("z=sin(PI*x)*cos(PI*y)", x, y, z2);
 
                // put the PlotPanel in a JFrame like a JPanel
                JFrame frame = new JFrame("a plot panel");
                frame.setSize(600, 600);
                frame.setContentPane(plot);
                frame.setVisible(true);
 
        }
 
        // function definition: z=cos(PI*x)*sin(PI*y)
        public static double f1(double x, double y) {
                double z = cos(x * PI) * sin(y * PI);
                return z;
        }
 
        // grid version of the function
        public static double[][] f1(double[] x, double[] y) {
                double[][] z = new double[y.length][x.length];
                for (int i = 0; i < x.length; i++)
                        for (int j = 0; j < y.length; j++)
                                z[j][i] = f1(x[i], y[j]);
                return z;
        }
 
        // another function definition: z=sin(PI*x)*cos(PI*y)
        public static double f2(double x, double y) {
                double z = sin(x * PI) * cos(y * PI);
                return z;
        }
 
        // grid version of the function
        public static double[][] f2(double[] x, double[] y) {
                double[][] z = new double[y.length][x.length];
                for (int i = 0; i < x.length; i++)
                        for (int j = 0; j < y.length; j++)
                                z[j][i] = f2(x[i], y[j]);
                return z;
        }
}

```
