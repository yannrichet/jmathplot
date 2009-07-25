package org.math.plot.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class Array {

    public static HashMap<Double, String> reverseStringMap(HashMap<String, Double> map) {
        if (map == null) {
            return null;
        }
        HashMap<Double, String> reverseMap = new HashMap<Double, String>();
        for (String key : map.keySet()) {
            reverseMap.put(map.get(key), key);
        }
        return reverseMap;
    }

    private static HashMap<String, Double> mapStringArray(double minvalue, double step, List<String> array) {
        if (array == null) {
            return null;
        }
        Collections.sort(array);
        HashMap<String, Double> map = new HashMap<String, Double>(array.size());
        double v = minvalue;
        for (String string : array) {
            if (!map.containsKey(string)) {
                map.put(string, v);
                v += step;
            }
        }
        return map;
    }

    public static boolean equals(double[] x, double[] y) {
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < y.length; i++) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static String toString(HashMap hash) {
        StringBuffer sb = new StringBuffer();
        for (Object key : hash.keySet()) {
            sb.append(key + " > " + hash.get(key) + "\n");
        }
        return sb.toString();
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException ne) {
            return false;
        }
        return true;
    }

    public static HashMap<String, Double> mapStringArray(List<String> array) {
        return mapStringArray(0, 1, array);
    }

    // Create methods
    public static String cat(Object[] array) {
        return cat(" ", array);
    }

    public static String cat(String separator, Object[] array) {
        String o = "";
        for (int i = 0; i < array.length - 1; i++) {
            o += array[i].toString() + separator;
        }
        o += array[array.length - 1].toString();
        return o;
    }

    public static String cat(String columnsSeparator, String rowsSeparator, Object[][] array) {
        String o = "";
        for (int i = 0; i < array.length - 1; i++) {
            o += cat(columnsSeparator, array[i]) + rowsSeparator;
        }
        o += cat(columnsSeparator, array[array.length - 1]);
        return o;
    }

    public static String cat(Object[][] array) {
        return cat(" ", "\n", array);
    }

    /*public static String cat(List<Object> array, String rowsSeparator) {
    String o = "";
    for (int i = 0; i < array.size() - 1; i++)
    o += array.get(i) + rowsSeparator;
    o += array.get(array.size() - 1);
    return o;
    }

    public static String cat(List <Object>array) {
    return cat(array, " ");
    }*/
    public static String[] duplicate(int m, String c) {
        String[] o = new String[m];
        for (int i = 0; i < o.length; i++) {
            o[i] = c;
        }
        return o;
    }

    public static int[] duplicate(int m, int c) {
        int[] o = new int[m];
        for (int i = 0; i < o.length; i++) {
            o[i] = c;
        }
        return o;
    }

    public static double[][] one(int m, int n) {
        return one(m, n, 1.0);
    }

    public static double[][] one(int m, int n, double c) {
        double[][] o = new double[m][n];
        for (int i = 0; i < o.length; i++) {
            for (int j = 0; j < o[i].length; j++) {
                o[i][j] = c;
            }
        }
        return o;
    }

    public static double[] one(int m) {
        return one(m, 1.0);
    }

    public static double[] one(int m, double c) {
        double[] o = new double[m];
        for (int i = 0; i < o.length; i++) {
            o[i] = c;
        }
        return o;
    }

    public static double[][] increment(int m, int n, double begin, double pitch) {
        double[][] array = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                array[i][j] = begin + i * pitch;
            }
        }
        return array;
    }

    public static double[] increment(int m, double begin, double pitch) {
        double[] array = new double[m];
        for (int i = 0; i < m; i++) {
            array[i] = begin + i * pitch;
        }
        return array;
    }

    // Modify rows & colmumns methods
    public static double[] copy(double[] M) {
        double[] array = new double[M.length];
        System.arraycopy(M, 0, array, 0, M.length);
        return array;
    }

    public static double[][] copy(double[][] M) {
        double[][] array = new double[M.length][M[0].length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(M[i], 0, array[i], 0, M[i].length);
        }
        return array;
    }

    public static double[][] getSubMatrixRangeCopy(double[][] M, int i1, int i2, int j1, int j2) {
        double[][] array = new double[i2 - i1 + 1][j2 - j1 + 1];
        for (int i = 0; i < i2 - i1 + 1; i++) {
            System.arraycopy(M[i + i1], j1, array[i], 0, j2 - j1 + 1);
        }
        ;
        return array;
    }

    public static double[][] getColumnsRangeCopy(double[][] M, int j1, int j2) {
        double[][] array = new double[M.length][j2 - j1 + 1];
        for (int i = 0; i < M.length; i++) {
            System.arraycopy(M[i], j1, array[i], 0, j2 - j1 + 1);
        }
        return array;
    }

    public static double[][] getColumnsCopy(double[][] M, int... J) {
        double[][] array = new double[M.length][J.length];
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < J.length; j++) {
                array[i][j] = M[i][J[j]];
            }
        }
        return array;
    }

    public static double[] getColumnCopy(double[][] M, int j) {
        double[] array = new double[M.length];
        for (int i = 0; i < M.length; i++) {
            array[i] = M[i][j];
        }
        return array;
    }

    public static double[] getColumnCopy(double[][][] M, int j, int k) {
        double[] array = new double[M.length];
        for (int i = 0; i < M.length; i++) {
            array[i] = M[i][j][k];
        }
        return array;
    }

    public static double[][] getRowsCopy(double[][] M, int... I) {
        double[][] array = new double[I.length][M[0].length];
        for (int i = 0; i < I.length; i++) {
            System.arraycopy(M[I[i]], 0, array[i], 0, M[I[i]].length);
        }
        return array;
    }

    public static double[] getRowCopy(double[][] M, int i) {
        double[] array = new double[M[0].length];
        System.arraycopy(M[i], 0, array, 0, M[i].length);
        return array;
    }

    public static double[][] getRowsRangeCopy(double[][] M, int i1, int i2) {
        double[][] array = new double[i2 - i1 + 1][M[0].length];
        for (int i = 0; i < i2 - i1 + 1; i++) {
            System.arraycopy(M[i + i1], 0, array[i], 0, M[i + i1].length);
        }
        return array;
    }

    public static double[] getRangeCopy(double[] M, int j1, int j2) {
        double[] array = new double[j2 - j1 + 1];
        System.arraycopy(M, j1, array, 0, j2 - j1 + 1);
        return array;
    }

    public static double[] getCopy(double[] M, int... I) {
        double[] array = new double[I.length];
        for (int i = 0; i < I.length; i++) {
            array[i] = M[I[i]];
        }
        return array;
    }

    public static int getColumnDimension(double[][] M, int i) {
        return M[i].length;
    }

    public static double[][] mergeRows(double[]... x) {
        double[][] array = new double[x.length][];
        for (int i = 0; i < array.length; i++) {
            array[i] = new double[x[i].length];
            System.arraycopy(x[i], 0, array[i], 0, array[i].length);
        }
        return array;
    }

    public static double[][] mergeColumns(double[]... x) {
        double[][] array = new double[x[0].length][x.length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = x[j][i];
            }
        }
        return array;
    }

    public static double[] merge(double[]... x) {
        int[] xlength_array = new int[x.length];
        xlength_array[0] = x[0].length;
        for (int i = 1; i < x.length; i++) {
            xlength_array[i] = x[i].length + xlength_array[i - 1];
        }
        double[] array = new double[xlength_array[x.length - 1]];
        System.arraycopy(x[0], 0, array, 0, x[0].length);
        for (int i = 1; i < x.length; i++) {
            System.arraycopy(x[i], 0, array, xlength_array[i - 1], x[i].length);
        }
        return array;
    }

    public static double[][] insertColumns(double[][] x, double[][] y, int J) {
        double[][] array = new double[x.length][x[0].length + y[0].length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(x[i], 0, array[i], 0, J);
            System.arraycopy(y[i], 0, array[i], J, y[i].length);
            System.arraycopy(x[i], J, array[i], J + y[i].length, x[i].length - J);
        }
        return array;
    }

    public static double[][] insertColumn(double[][] x, double[] y, int J) {
        double[][] array = new double[x.length][x[0].length + 1];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(x[i], 0, array[i], 0, J);
            array[i][J] = y[i];
            System.arraycopy(x[i], J, array[i], J + 1, x[i].length - J);
        }
        return array;
    }

    public static double[][] insertRows(double[][] x, double[][] y, int I) {
        double[][] array = new double[x.length + y.length][x[0].length];
        for (int i = 0; i < I; i++) {
            System.arraycopy(x[i], 0, array[i], 0, x[i].length);
        }
        for (int i = 0; i < y.length; i++) {
            System.arraycopy(y[i], 0, array[i + I], 0, y[i].length);
        }
        for (int i = 0; i < x.length - I; i++) {
            System.arraycopy(x[i + I], 0, array[i + I + y.length], 0, x[i].length);
        }
        return array;
    }

    public static double[][] insertRow(double[][] x, double[] y, int I) {
        double[][] array = new double[x.length + 1][x[0].length];
        for (int i = 0; i < I; i++) {
            System.arraycopy(x[i], 0, array[i], 0, x[i].length);
        }
        System.arraycopy(y, 0, array[I], 0, y.length);
        for (int i = 0; i < x.length - I; i++) {
            System.arraycopy(x[i + I], 0, array[i + I + 1], 0, x[i].length);
        }
        return array;
    }

    public static double[] insert(double[] x, int I, double... y) {
        double[] array = new double[x.length + y.length];
        System.arraycopy(x, 0, array, 0, I);
        System.arraycopy(y, 0, array, I, y.length);
        System.arraycopy(x, I, array, I + y.length, x.length - I);
        return array;
    }

    public static double[][] deleteColumnsRange(double[][] x, int J1, int J2) {
        double[][] array = new double[x.length][x[0].length - (J2 - J1 + 1)];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(x[i], 0, array[i], 0, J1);
            System.arraycopy(x[i], J2 + 1, array[i], J1, x[i].length - (J2 + 1));
        }
        return array;
    }

    public static double[][] deleteColumns(double[][] x, int... J) {
        double[][] array = new double[x.length][x[0].length - J.length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(x[i], 0, array[i], 0, J[0]);
            for (int j = 0; j < J.length - 1; j++) {
                System.arraycopy(x[i], J[j] + 1, array[i], J[j] - j, J[j + 1] - J[j] - 1);
            }
            System.arraycopy(x[i], J[J.length - 1] + 1, array[i], J[J.length - 1] - J.length + 1, x[i].length - J[J.length - 1] - 1);
        }
        return array;
    }

    public static double[][] deleteRowsRange(double[][] x, int I1, int I2) {
        double[][] array = new double[x.length - (I2 - I1 + 1)][x[0].length];
        for (int i = 0; i < I1; i++) {
            System.arraycopy(x[i], 0, array[i], 0, x[i].length);
        }
        for (int i = 0; i < x.length - I2 - 1; i++) {
            System.arraycopy(x[i + I2 + 1], 0, array[i + I1], 0, x[i].length);
        }
        return array;
    }

    public static double[][] deleteRows(double[][] x, int... I) {
        double[][] array = new double[x.length - I.length][x[0].length];
        for (int i = 0; i < I[0]; i++) {
            System.arraycopy(x[i], 0, array[i], 0, x[i].length);
        }
        for (int j = 0; j < I.length - 1; j++) {
            for (int i = I[j] + 1; i < I[j + 1]; i++) {
                System.arraycopy(x[i], 0, array[i - j], 0, x[i].length);
            }
        }
        for (int i = I[I.length - 1] + 1; i < x.length; i++) {
            System.arraycopy(x[i], 0, array[i - I.length], 0, x[i].length);
        }
        return array;
    }

    public static double[] deleteRange(double[] x, int J1, int J2) {
        double[] array = new double[x.length - (J2 - J1 + 1)];
        System.arraycopy(x, 0, array, 0, J1);
        System.arraycopy(x, J2 + 1, array, J1, x.length - (J2 + 1));
        return array;
    }

    public static double[] delete(double[] x, int... J) {
        double[] array = new double[x.length - J.length];
        System.arraycopy(x, 0, array, 0, J[0]);
        for (int j = 0; j < J.length - 1; j++) {
            System.arraycopy(x, J[j] + 1, array, J[j] - j, J[j + 1] - J[j] - 1);
        }
        System.arraycopy(x, J[J.length - 1] + 1, array, J[J.length - 1] - J.length + 1, x.length - J[J.length - 1] - 1);
        return array;
    }

    public static double[][] buildXY(double Xmin, double Xmax, double[] Y) {
        int n = Y.length;
        double[][] XY = new double[n][2];
        for (int i = 0; i < n; i++) {
            XY[i][0] = Xmin + (Xmax - Xmin) * (double) i / (double) (n - 1);
            XY[i][1] = Y[i];
        }
        return XY;
    }

    public static double[][] buildXY(double[] X, double[] Y) {
        return mergeColumns(X, Y);
    }

    // min/max methods
    public static double[] min(double[][] M) {
        double[] min = new double[M[0].length];
        for (int j = 0; j < min.length; j++) {
            min[j] = M[0][j];
            for (int i = 1; i < M.length; i++) {
                min[j] = Math.min(min[j], M[i][j]);
            }
        }
        return min;
    }

    public static int min(int... M) {
        int min = M[0];
        for (int i = 1; i < M.length; i++) {
            min = Math.min(min, M[i]);
        }
        return min;
    }

    public static int max(int... M) {
        int max = M[0];
        for (int i = 1; i < M.length; i++) {
            max = Math.max(max, M[i]);
        }
        return max;
    }

    public static double min(double... M) {
        double min = M[0];
        for (int i = 1; i < M.length; i++) {
            min = Math.min(min, M[i]);
        }
        return min;
    }

    public static double[] max(double[][] M) {
        double[] max = new double[M[0].length];
        for (int j = 0; j < max.length; j++) {
            max[j] = M[0][j];
            for (int i = 1; i < M.length; i++) {
                max[j] = Math.max(max[j], M[i][j]);
            }
        }
        return max;
    }

    public static double max(double... M) {
        double max = M[0];
        for (int i = 1; i < M.length; i++) {
            max = Math.max(max, M[i]);
        }
        return max;
    }

    public static int[] minIndex(double[][] M) {
        int[] minI = new int[M[0].length];
        for (int j = 0; j < minI.length; j++) {
            minI[j] = 0;
            for (int i = 1; i < M.length; i++) {
                if (M[i][j] < M[minI[j]][j]) {
                    minI[j] = i;
                }
            }

        }
        return minI;
    }

    public static int minIndex(double... M) {
        int minI = 0;
        for (int i = 1; i < M.length; i++) {
            if (M[i] < M[minI]) {
                minI = i;
            }
        }
        return minI;
    }

    public static int[] maxIndex(double[][] M) {
        int[] maxI = new int[M[0].length];
        for (int j = 0; j < maxI.length; j++) {
            maxI[j] = 0;
            for (int i = 1; i < M.length; i++) {
                if (M[i][j] > M[maxI[j]][j]) {
                    maxI[j] = i;
                }
            }
        }
        return maxI;
    }

    public static int maxIndex(double... M) {
        int maxI = 0;
        for (int i = 1; i < M.length; i++) {
            if (M[i] > M[maxI]) {
                maxI = i;
            }
        }
        return maxI;
    }

    // print methods
    public static String toString(double[]... v) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                str.append(v[i][j] + " ");
            }
            if (i < v.length - 1) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    // check methods
    public static void throwError(String msg) {
        throw new IllegalArgumentException(msg);
    }

    public static void checkColumnDimension(double[][] M, int n) {
        for (int i = 0; i < M.length; i++) {
            if (M[i].length != n) {
                throwError("row " + i + " have " + M[i].length + " columns instead of " + n + " columns expected.");
            }
        }
    }

    public static boolean isColumnDimension(double[][] M, int n) {
        for (int i = 0; i < M.length; i++) {
            if (M[i].length != n) {
                return false;
            }
        }
        return true;
    }

    public static void checkRowDimension(double[][] M, int m) {
        if (M.length != m) {
            throwError("columns have " + M.length + " rows instead of " + m + " rows expected.");
        }
    }

    public static boolean isRowDimension(double[][] M, int m) {
        if (M.length != m) {
            return false;
        }
        return true;
    }

    public static void checkLength(double[] M, int n) {
        if (M.length != n) {
            throwError("row have " + M.length + " elements instead of " + n + " elements expected.");
        }
    }

    public static boolean isLength(double[] M, int n) {
        if (M.length != n) {
            return false;
        }
        return true;
    }
}
