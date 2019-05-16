
import java.io.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.Scanner;        
import java.text.DecimalFormat;  
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;  
import org.math.plot.*;
import org.math.plot.plotObjects.*;
import java.util.Arrays;


public class magneticPendulumStudents
{
	//constants
    public static final double DT = 1.e-3;    					//	time interval in s
    public static final double Tmax = 50.;   					//	max time in s
	public static final double mass = 0.1; 						//	mass of the stone
	public static final double cLift = 10; 						//	coefficient of lift
	public static final double cFriction = 1; 					//	coefficient of friction
	public static final double rAir = 1.225; 					//	density of air
	public static final double rWater = 1000; 					//	desity of water
	public static final double cDrag =  0.5;					//	coefficient of drag
	public static final double gravity = 9.8;					//	gravity
	public static final double radius = 0.04;					//	radius of stone
	public static final double height = 0.01;					// 	height of stone
	public static final double aStone = 2 * Math.PI * radius * radius + 2 * Math.PI * height;  		// area of stone
    // Start main method
    public static void main(String[] args)
    {

		double xInitial, yInitial;

		xInitial = 0.;
		yInitial = 1.5;

		// Calculate length of arrays
		int imax = (int)(Tmax/DT);					// Maximal index

		// Declare main variables
		double[] t = new double[imax];				// time in sec

		double[] x1 = new double[imax];  			// x-position in m
		double[] vx1 = new double[imax];			// x-velocity in m/s
		double[] ax1 = new double[imax];			// x-acceleration in m/s/s

		double[] y1 = new double[imax];				// y-position in m
		double[] vy1 = new double[imax];			// y-velocity in m/s
		double[] ay1 = new double[imax];			// y-acceleration in m/s/s
		
		double[] speed = new double[imax];			// velocity in m/s 
		double[] angleBeta = new double[imax];  	// angle of attack


		// Initialize the first step in time;
		t[0] = 0;

		x1[0] = xInitial;
		y1[0] = yInitial;

		vx1[0] = 20.;
		vy1[0] = -2.;
		speed[0] = Math.sqrt(vx1[0] * vx1[0] + vy1[0] * vy1[0]);
		angleBeta[0] = Math.atan(vy1[0]/vx1[0]);

		ax1[0] = accelerationx1(angleBeta[0],speed[0]);
		ay1[0] = accelerationy1(angleBeta[0],speed[0]);

		x1[1] = x1[0] + vx1[0]*DT + 1/2 * ax1[0] * DT * DT;
		vx1[1] = vx1[0]+ax1[0]*DT;
		y1[1] = y1[0] + vy1[0]*DT + 1/2 * ay1[0] * DT * DT;
		vy1[1] = vy1[0]+ay1[0]*DT;
		
		angleBeta[1] = Math.atan(vy1[1]/vx1[1]);
		speed[0] = Math.sqrt(vx1[1] * vx1[1] + vy1[1] * vy1[1]);
		
		ax1[1] = accelerationx1(angleBeta[1],speed[1]);
		ay1[1] = accelerationy1(angleBeta[1],speed[1]);

		
		for(int i = 2; i < imax;i++)
		{
			if (vx1[i-1] < 0.){
				System.out.println("rock has stopped moving");
				System.out.println(i * DT);
				double[] xRealPosition = new double[i];
				double[] yRealPosition = new double[i];

				xRealPosition = Arrays.copyOfRange(x1, 0, i);
				yRealPosition = Arrays.copyOfRange(y1, 0, i);

				Plot2DPanel myPlot = new Plot2DPanel();

   				myPlot.addLinePlot("trajectory", Color.RED, xRealPosition, yRealPosition);
   				new FrameView(myPlot).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   				while(true){
   					// do nothing;
   				}

				//System.exit(0);
			}
			else{
			//System.out.println("X Speed : " + vx)

			if (y1[i-1] > 0){
			x1[i] = x1[i-1] + vx1[i-1] * DT + 1/2 * ax1[i-1] * DT * DT;
			y1[i] = y1[i-1] + vy1[i-1] * DT + 1/2 * ay1[i-1] * DT * DT;

			// Update velocity

			vx1[i] = vx1[i-1] + ax1[i-1]*DT; 
			vy1[i] = vy1[i-1] + ay1[i-1] * DT; 

			angleBeta[i] = Math.atan(vy1[i]/vx1[i]);
			speed[i] = Math.sqrt(vx1[i] * vx1[i] + vy1[i] * vy1[i]);

			// Update acceleration
			ax1[i] = accelerationx1(angleBeta[i],speed[i]);
			ay1[i] = accelerationy1(angleBeta[i],speed[i]);
						
			//System.out.println("x position: " + x1[i]);
			//System.out.println("y position:" + y1[i]);
			//System.out.println("x speed:" + vx1[i]);
			//System.out.println("y speed:" + vy1[i]);
			}
			else 
			{
			//System.out.println("rock is below");
			x1[i] = x1[i-1] + vx1[i-1] * DT + 1/2 * ax1[i-1]*DT*DT ;
			y1[i] = y1[i-1] + vy1[i-1] * DT + 1/2 * ay1[i-1] * DT * DT;

			// Update velocity

			vx1[i] = vx1[i-1] + ax1[i-1] * DT; 
			vy1[i] = vy1[i-1] + ay1[i-1] * DT; 


			angleBeta[i] = Math.atan(vy1[i]/vx1[i]);
			speed[i] = Math.sqrt(vx1[i] * vx1[i] + vy1[i] * vy1[i]);

			// Update acceleration
			ax1[i] = accelerationx2(0.3,speed[i],y1[i]);
			ay1[i] = accelerationy2(0.3,speed[i],y1[i]);
						
			//System.out.println("x position: " + x1[i]);
			//System.out.println("y position:" + y1[i]);
			//System.out.println("x speed:" + vx1[i]);
			//System.out.println("y speed:" + vy1[i]);
			}
		}

	}


} // end main

    public static double accelerationx1(double angleBeta, double speed)
    {
		double accx;
		
		accx = (-1 * cDrag * rAir * speed * speed * aStone * Math.cos(angleBeta))/(2 * mass);
		
		return accx;
    }
	
	public static double accelerationx2(double angleTheta, double speed, double height)
    {
		double accx;
		double shortcut = (1 - Math.abs(height)/(radius * Math.sin(angleTheta)));
		double aStoneSubmerged = radius * radius * (Math.acos(shortcut) - shortcut * Math.sqrt(1 - shortcut * shortcut));
		accx = -1 * ((cLift * Math.sin(angleTheta) + cFriction * Math.cos(angleTheta)) * rWater * speed * speed * aStoneSubmerged)/(2 * mass);
		//System.out.println("x acceleration 2:" + accx);

		return accx;
    }


    public static double accelerationy1(double angleBeta, double speed)
    {
		double accy;
		
		accy = -1 * gravity * mass + (-1 * cDrag * rAir * speed * speed * aStone * Math.sin(angleBeta))/(2 * mass);
		
		return accy;
    }
	
	///////////////////////////////////////////////////////////////////////////////////
    public static double accelerationy2(double angleTheta, double speed, double height)
    {
    	//System.out.println("speed: " + speed);
		double accy;
		double shortcut = (1 - Math.abs(height)/(radius * Math.sin(angleTheta)));
		//System.out.println("shortcut: " + shortcut);
		double aStoneSubmerged = radius * radius * (Math.acos(shortcut) - shortcut * Math.sqrt(1 - shortcut * shortcut));
		double stuff = ((cLift * Math.cos(angleTheta) + cFriction * Math.sin(angleTheta)) * rWater * speed * speed * aStoneSubmerged)/(2 * mass);
		accy = gravity + stuff;
		/*System.out.println("stuff: " + stuff);
		System.out.println("y acceleration 2:" + accy);
		System.out.println("area stone submerged:" + aStoneSubmerged);
		*/


		return accy;
    }
} 
