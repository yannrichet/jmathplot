package org.math.plot.plotObjects;

import org.math.plot.utils.FastMath;

/**
 * BSD License
 * 
 * @author Yann RICHET
 * Changed on 6/13/2014 by Jerry Dietrich 
 * Contact info ballooninternet@cox.net
 */
public class Base {

	public final static String STRINGS = "str";
	
	public final static String LINEAR = "lin";

	public final static String LOGARITHM = "log";

	public double[][] baseCoords;

	protected double[] precisionUnit;

	public double[] roundXmin;

	public double[] roundXmax;

	protected double[] trueXmin;

	protected double[] trueXmax;

	public int dimension;

	public String[] axesScales;

	public Base(double[] Xmi, double[] Xma, String[] scales) {
		init(Xmi.length);
		trueXmin = Xmi;
		trueXmax = Xma;
		dimension = trueXmin.length;
		axesScales = scales;
		setFixedBounds(Xmi, Xma);
	}

	private void init(int d) {
		precisionUnit = new double[d];
		roundXmin = new double[d];
		roundXmax = new double[d];
		trueXmin = new double[d];
		trueXmax = new double[d];
	}

	private void resetCoords() {
		baseCoords = new double[dimension + 1][];
		for (int i = 0; i < baseCoords.length; i++) {
			baseCoords[i] = (double[]) (roundXmin.clone());
			if (i > 0)
				baseCoords[i][i - 1] = roundXmax[i - 1];
		}
	}

	/*
	 * protected void setPrecisionUnit(double[] Xmi,double[] Xma) {
	 * precisionUnit = new double[Xmi.length]; for (int i = 0; i <
	 * precisionUnit.length; i++) { setPrecisionUnit(Xmi[i],Xma[i], i); } }
	 */

	private void setPrecisionUnit(int i, double Xmi, double Xma) {
		if (Xma - Xmi > 0) {
			precisionUnit[i] = FastMath.pow(10, FastMath.floor(FastMath.log(Xma - Xmi) / FastMath.log(10)));
		} else {
			precisionUnit[i] = 1;
		}
		// System.out.println("precisionUnit["+i+"] = "+precisionUnit[i]);
	}

	public void setAxesScales(String[] scales) {
		axesScales = scales;
		setRoundBounds(trueXmin, trueXmax);
		resetCoords();
	}

	public void setAxesScales(int i, String scale) {
		axesScales[i] = scale;
		setRoundBounds(trueXmin, trueXmax);
		resetCoords();
	}

	public double[][] getCoords() {
		return baseCoords;
	}

	/*
	 * public int getDimension() { return dimension; }
	 */

	public String[] getAxesScales() {
		return axesScales;
	}

	public String getAxeScale(int i) {
		return axesScales[i];
	}

	public double[] getMinBounds() {
		return roundXmin;
	}

	public double[] getMaxBounds() {
		return roundXmax;
	}

	public double[] getPrecisionUnit() {
		return precisionUnit;
	}

	// ///////////////////////////////////////////
	// ////// bounds methods /////////////////////
	// ///////////////////////////////////////////

	private void setBounds(int i, double Xmi, double Xma) {
		if ((Xmi <= 0) && (axesScales[i].equalsIgnoreCase(LOGARITHM))) {
			Xmi = 1.0;
		}
		if ((Xma <= 0) && (axesScales[i].equalsIgnoreCase(LOGARITHM))) {
			Xma = 1.0;
		}
		if (Xmi == Xma) {
			Xmi = Xma - 1;
		}
		if (Xmi > Xma) {
			throw new IllegalArgumentException("Error while bounding dimension " + (i + 1) + " : min " + Xmi + " must be < to max " + Xma);
		}
		roundXmin[i] = Xmi;
		roundXmax[i] = Xma;
		resetCoords();
	}

	/*
	 * private void setBounds(double[] Xmi, double[] Xma) { for (int i = 0; i <
	 * Xmi.length; i++) { setBounds(i, Xmi[i], Xma[i]); } }
	 */

	public void setFixedBounds(int i, double Xmi, double Xma) {
		setPrecisionUnit(i, Xmi, Xma);
		setBounds(i, Xmi, Xma);
	}

	public void setFixedBounds(double[] Xmi, double[] Xma) {
		for (int i = 0; i < Xmi.length; i++) {
			setFixedBounds(i, Xmi[i], Xma[i]);
		}
	}

	public void roundBounds(int i) {
		setPrecisionUnit(i, trueXmin[i], trueXmax[i]);
		if (axesScales[i].equalsIgnoreCase(LOGARITHM)) {
			setBounds(i, FastMath.pow(10, FastMath.floor(FastMath.log(trueXmin[i]) / FastMath.log(10))), FastMath.pow(10, FastMath.ceil(FastMath.log(trueXmax[i]) / FastMath.log(10))));
		} else if (axesScales[i].equalsIgnoreCase(LINEAR)||axesScales[i].equalsIgnoreCase(STRINGS)) {
			if (roundXmin[i] <= roundXmax[i])
			  setBounds(i, precisionUnit[i] * (FastMath.floor(trueXmin[i] / precisionUnit[i])), precisionUnit[i] * (FastMath.ceil(trueXmax[i] / precisionUnit[i])));
			else
			  setBounds(i, precisionUnit[i] * (FastMath.ceil(trueXmax[i] / precisionUnit[i])), precisionUnit[i] * (FastMath.floor(trueXmin[i] / precisionUnit[i])));
		}

		/*
		 * System.out.println("precisionUnit[" + i + "]=" + precisionUnit[i]);
		 * System.out.println("trueXmin["+i+"]="+trueXmin[i]);
		 * System.out.println("trueXmax["+i+"]="+trueXmax[i]);
		 * System.out.println("roundXmin["+i+"]="+roundXmin[i]);
		 * System.out.println("roundXmax["+i+"]="+roundXmax[i]);
		 * 
		 * System.out.println("Xmi=" + trueXmin[i] + " Xma=" + trueXmax[i]);
		 * System.out.println( " -> precisionUnit[i] * (Math.floor(Xmi /
		 * precisionUnit[i]))=" + precisionUnit[i] * (Math.floor(trueXmin[i] /
		 * precisionUnit[i]))); System.out.println( " -> precisionUnit[i] *
		 * (Math.ceil(Xma / precisionUnit[i]))=" + precisionUnit[i] *
		 * (ceil(trueXmax[i] / precisionUnit[i])));
		 */

	}

	public void setRoundBounds(int i, double Xmi, double Xma) {
		trueXmin[i] = Xmi;
		trueXmax[i] = Xma;
		roundBounds(i);
	}

	public void setRoundBounds(double[] Xmi, double[] Xma) {
		for (int i = 0; i < Xmi.length; i++) {
			trueXmin[i] = Xmi[i];
			trueXmax[i] = Xma[i];
			roundBounds(i);
		}
	}

    public boolean includeInBounds(int dim, double XY) {
        boolean changed = false;
        for (int i = 0; i < roundXmin.length; i++) {
            if (i == dim) {
                if (XY < trueXmin[i]) {
                    trueXmin[i] = XY;
                    changed = true;
                }
            }
        }
        for (int i = 0; i < roundXmax.length; i++) {
            if (i == dim) {
                if (XY > trueXmax[i]) {
                    trueXmax[i] = XY;
                    changed = true;
                }
            }
        }
        if (changed) {
            roundBounds(dim);
        }
        return changed;
    }

    public boolean includeInBounds(double[] XY) {
        boolean changed = false;
        for (int i = 0; i < roundXmin.length; i++) {
            if (XY[i] < trueXmin[i]) {
                trueXmin[i] = XY[i];
                changed = true;
            }
        }
        for (int i = 0; i < roundXmax.length; i++) {
            if (XY[i] > trueXmax[i]) {
                trueXmax[i] = XY[i];
                changed = true;
            }
        }
        if (changed) {
            setRoundBounds(trueXmin, trueXmax);
        }
        return changed;
    }

	// ///////////////////////////////////////////
	// ////// other public methods ///////////////
	// ///////////////////////////////////////////

	public boolean authorizedLogScale(int i) {
		// System.out.println("Xmin[" + i + "] = " + roundXmin[i]);
		if (roundXmin[i] > 0) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < baseCoords.length; i++) {
			s.append("[");
			for (int j = 0; j < baseCoords[i].length; j++)
				s.append(baseCoords[i][j] + ",");
			s.deleteCharAt(s.length() - 1);
			s.append("]");
		}
		return s.toString();
	}
}