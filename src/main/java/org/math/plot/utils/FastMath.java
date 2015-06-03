package org.math.plot.utils;

/*
 * Copyright 2012 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * =============================================================================
 * Notice of fdlibm package this program is partially derived from:
 *
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunSoft, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 * =============================================================================
 */

/**
 * Class providing math treatments that:
 * - are meant to be faster than those of java.lang.Math class (depending on
 *   JVM or JVM options, they might be slower),
 * - are still somehow accurate and robust (handling of NaN and such),
 * - do not (or not directly) generate objects at run time (no "new").
 * 
 * Other than optimized treatments, a valuable feature of this class is the
 * presence of angles normalization methods, derived from those used in
 * java.lang.Math (for which, sadly, no API is provided, letting everyone
 * with the terrible responsibility to write their own ones).
 * 
 * Non-redefined methods of java.lang.Math class are also available,
 * for easy replacement.
 * 
 * Use of look-up tables: around 1 Mo total, and initialized on class load.
 * 
 * - Methods with same signature than Math ones, are meant to return
 *   "good" approximations on all range.
 * - Methods terminating with "Fast" are meant to return "good" approximation
 *   on a reduced range only.
 * - Methods terminating with "Quick" are meant to be quick, but do not
 *   return a good approximation, and might only work on a reduced range.
 * 
 * Properties:
 * 
 * - jodk.fastmath.strict (boolean, default is true):
 *   If true, non-redefined Math methods which results could vary between Math and StrictMath,
 *   delegate to StrictMath, and if false, to Math.
 *   Default is true to ensure consistency across various architectures.
 *   
 * - jodk.fastmath.usejdk (boolean, default is false):
 *   If true, redefined Math methods, as well as their "Fast" or "Quick" terminated counterparts,
 *   delegate to StrictMath or Math, depending on jodk.fastmath.strict property.
 *   
 * - jodk.fastmath.fastlog (boolean, default is true):
 *   If true, using redefined log(double), if false using StrictMath.log(double) or
 *   Math.log(double), depending on jodk.fastmath.strict property.
 *   Default is true because jodk.fastmath.strict is true by default, and StrictMath.log(double)
 *   seems usually slow.
 *   
 * - jodk.fastmath.fastsqrt (boolean, default is false):
 *   If true, using redefined sqrt(double), if false using StrictMath.sqrt(double) or
 *   Math.sqrt(double), depending on jodk.fastmath.strict property.
 *   Default is false because StrictMath.sqrt(double) seems to usually delegate to hardware sqrt.
 * 
 * Unless jodk.fastmath.strict is false and jodk.fastmath.usejdk is true, these treatments
 * are consistent across various architectures, for constants and look-up tables are
 * computed with StrictMath, or exact Math methods.
 * 
 * --- words, words, words ---
 * 
 * "0x42BE0000 percents of the folks out there
 * are completely clueless about floating-point."
 * 
 * The difference between precision and accuracy:
 * "3.177777777777777 is a precise (16 digits)
 * but inaccurate (only correct up to the second digit)
 * approximation of PI=3.141592653589793(etc.)."
 */
public strictfp final class FastMath {

    /*
     * For trigonometric functions, use of look-up tables and Taylor-Lagrange formula
     * with 4 derivatives (more take longer to compute and don't add much accuracy,
     * less require larger tables (which use more memory, take more time to initialize,
     * and are slower to access (at least on the machine they were developed on))).
     * 
     * For angles reduction of cos/sin/tan functions:
     * - for small values, instead of reducing angles, and then computing the best index
     *   for look-up tables, we compute this index right away, and use it for reduction,
     * - for large values, treatments derived from fdlibm package are used, as done in
     *   java.lang.Math. They are faster but still "slow", so if you work with
     *   large numbers and need speed over accuracy for them, you might want to use
     *   normalizeXXXFast treatments before your function, or modify cos/sin/tan
     *   so that they call the fast normalization treatments instead of the accurate ones.
     *   NB: If an angle is huge (like PI*1e20), in double precision format its last digits
     *       are zeros, which most likely is not the case for the intended value, and doing
     *       an accurate reduction on a very inaccurate value is most likely pointless.
     *       But it gives some sort of coherence that could be needed in some cases.
     * 
     * Multiplication on double appears to be about as fast (or not much slower) than call
     * to <double_array>[<index>], and regrouping some doubles in a private class, to use
     * index only once, does not seem to speed things up, so:
     * - for uniformly tabulated values, to retrieve the parameter corresponding to
     *   an index, we recompute it rather than using an array to store it,
     * - for cos/sin, we recompute derivatives divided by (multiplied by inverse of)
     *   factorial each time, rather than storing them in arrays.
     * 
     * Lengths of look-up tables are usually of the form 2^n+1, for their values to be
     * of the form (<a_constant> * k/2^n, k in 0 .. 2^n), so that particular values
     * (PI/2, etc.) are "exactly" computed, as well as for other reasons.
     * 
     * Most math treatments I could find on the web, including "fast" ones,
     * usually take care of special cases (NaN, etc.) at the beginning, and
     * then deal with the general case, which adds a useless overhead for the
     * general (and common) case. In this class, special cases are only dealt
     * with when needed, and if the general case does not already handle them.
     */
    
public class DoubleWrapper {
    public double value;
    @Override
    public String toString() {
        return Double.toString(this.value);
    }
}

public class IntWrapper {
    public int value;
    @Override
    public String toString() {
        return Integer.toString(this.value);
    }
}
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean STRICT_MATH = false;//LangUtils.getBooleanProperty("jodk.fastmath.strict", true);

    private static final boolean USE_JDK_MATH = false;//LangUtils.getBooleanProperty("jodk.fastmath.usejdk", false);

    /**
     * Used for both log(double) and log10(double).
     */
    private static final boolean USE_REDEFINED_LOG = true;//LangUtils.getBooleanProperty("jodk.fastmath.fastlog", true);

    private static final boolean USE_REDEFINED_SQRT = true;//LangUtils.getBooleanProperty("jodk.fastmath.fastsqrt", false);

    // Set it to true if FastMath.sqrt(double) is slow (more tables, but less calls to FastMath.sqrt(double)).
    private static final boolean USE_POWTABS_FOR_ASIN = true;

    //--------------------------------------------------------------------------
    // GENERAL CONSTANTS
    //--------------------------------------------------------------------------

    /**
     * High approximation of PI, which is further from PI
     * than the low approximation Math.PI:
     *              PI ~= 3.14159265358979323846...
     *         Math.PI ~= 3.141592653589793
     * FastMath.PI_SUP ~= 3.1415926535897936
     */
    public static final double PI_SUP = Math.nextUp(Math.PI);

    private static final double ONE_DIV_F2 = 1/2.0;
    private static final double ONE_DIV_F3 = 1/6.0;
    private static final double ONE_DIV_F4 = 1/24.0;

    private static final double TWO_POW_24 = Double.longBitsToDouble(0x4170000000000000L);
    private static final double TWO_POW_N24 = Double.longBitsToDouble(0x3E70000000000000L);

    private static final double TWO_POW_26 = Double.longBitsToDouble(0x4190000000000000L);
    private static final double TWO_POW_N26 = Double.longBitsToDouble(0x3E50000000000000L);

    // First double value (from zero) such as (value+-1/value == value).
    private static final double TWO_POW_27 = Double.longBitsToDouble(0x41A0000000000000L);
    private static final double TWO_POW_N27 = Double.longBitsToDouble(0x3E40000000000000L);

    private static final double TWO_POW_N28 = Double.longBitsToDouble(0x3E30000000000000L);

    private static final double TWO_POW_52 = Double.longBitsToDouble(0x4330000000000000L);

    private static final double TWO_POW_N54 = Double.longBitsToDouble(0x3C90000000000000L);

    private static final double TWO_POW_N55 = Double.longBitsToDouble(0x3C80000000000000L);

    private static final double TWO_POW_66 = Double.longBitsToDouble(0x4410000000000000L);

    private static final double TWO_POW_450 = Double.longBitsToDouble(0x5C10000000000000L);
    private static final double TWO_POW_N450 = Double.longBitsToDouble(0x23D0000000000000L);

    private static final double TWO_POW_750 = Double.longBitsToDouble(0x6ED0000000000000L);
    private static final double TWO_POW_N750 = Double.longBitsToDouble(0x1110000000000000L);

    // Smallest double normal value.
    private static final double MIN_DOUBLE_NORMAL = Double.longBitsToDouble(0x0010000000000000L); // 2.2250738585072014E-308

    private static final int MIN_DOUBLE_EXPONENT = -1074;
    private static final int MAX_DOUBLE_EXPONENT = 1023;

    private static final int MAX_FLOAT_EXPONENT = 127;

    private static final double LOG_2 = StrictMath.log(2.0);
    private static final double LOG_TWO_POW_27 = StrictMath.log(TWO_POW_27);
    private static final double LOG_DOUBLE_MAX_VALUE = StrictMath.log(Double.MAX_VALUE);

    private static final double INV_LOG_10 = 1.0/StrictMath.log(10.0);

    private static final double DOUBLE_BEFORE_60 = Math.nextAfter(60.0, 0.0);

    //--------------------------------------------------------------------------
    // CONSTANTS FOR NORMALIZATIONS
    //--------------------------------------------------------------------------

    /*
     * Table of constants for 1/(2*PI), 282 Hex digits (enough for normalizing doubles).
     * 1/(2*PI) approximation = sum of ONE_OVER_TWOPI_TAB[i]*2^(-24*(i+1)).
     */
    private static final double ONE_OVER_TWOPI_TAB[] = {
        0x28BE60, 0xDB9391, 0x054A7F, 0x09D5F4, 0x7D4D37, 0x7036D8,
        0xA5664F, 0x10E410, 0x7F9458, 0xEAF7AE, 0xF1586D, 0xC91B8E,
        0x909374, 0xB80192, 0x4BBA82, 0x746487, 0x3F877A, 0xC72C4A,
        0x69CFBA, 0x208D7D, 0x4BAED1, 0x213A67, 0x1C09AD, 0x17DF90,
        0x4E6475, 0x8E60D4, 0xCE7D27, 0x2117E2, 0xEF7E4A, 0x0EC7FE,
        0x25FFF7, 0x816603, 0xFBCBC4, 0x62D682, 0x9B47DB, 0x4D9FB3,
        0xC9F2C2, 0x6DD3D1, 0x8FD9A7, 0x97FA8B, 0x5D49EE, 0xB1FAF9,
        0x7C5ECF, 0x41CE7D, 0xE294A4, 0xBA9AFE, 0xD7EC47};

    /*
     * Constants for 2*PI. Only the 23 most significant bits of each mantissa are used.
     * 2*PI approximation = sum of TWOPI_TAB<i>.
     */
    private static final double TWOPI_TAB0 = Double.longBitsToDouble(0x401921FB40000000L);
    private static final double TWOPI_TAB1 = Double.longBitsToDouble(0x3E94442D00000000L);
    private static final double TWOPI_TAB2 = Double.longBitsToDouble(0x3D18469880000000L);
    private static final double TWOPI_TAB3 = Double.longBitsToDouble(0x3B98CC5160000000L);
    private static final double TWOPI_TAB4 = Double.longBitsToDouble(0x3A101B8380000000L);

    private static final double INVPIO2 = Double.longBitsToDouble(0x3FE45F306DC9C883L); // 6.36619772367581382433e-01 53 bits of 2/pi
    private static final double PIO2_HI = Double.longBitsToDouble(0x3FF921FB54400000L); // 1.57079632673412561417e+00 first 33 bits of pi/2
    private static final double PIO2_LO = Double.longBitsToDouble(0x3DD0B4611A626331L); // 6.07710050650619224932e-11 pi/2 - PIO2_HI
    private static final double INVTWOPI = INVPIO2/4;
    private static final double TWOPI_HI = 4*PIO2_HI;
    private static final double TWOPI_LO = 4*PIO2_LO;

    // fdlibm uses 2^19*PI/2 here, but we normalize with % 2*PI instead of % PI/2,
    // and we can bear some more error.
    private static final double NORMALIZE_ANGLE_MAX_MEDIUM_DOUBLE = StrictMath.pow(2,20)*(2*Math.PI);

    /**
     * 2*Math.PI, normalized into [-PI,PI].
     * Computed using normalizeMinusPiPi(double).
     */
    private static final double TWO_MATH_PI_IN_MINUS_PI_PI = -2.449293598153844E-16;

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR COS, SIN
    //--------------------------------------------------------------------------

    private static final int SIN_COS_TABS_SIZE = (1<<getTabSizePower(11)) + 1;
    private static final double SIN_COS_DELTA_HI = TWOPI_HI/(SIN_COS_TABS_SIZE-1);
    private static final double SIN_COS_DELTA_LO = TWOPI_LO/(SIN_COS_TABS_SIZE-1);
    private static final double SIN_COS_INDEXER = 1/(SIN_COS_DELTA_HI+SIN_COS_DELTA_LO);
    private static final double[] sinTab = new double[SIN_COS_TABS_SIZE];
    private static final double[] cosTab = new double[SIN_COS_TABS_SIZE];

    // Max abs value for fast modulo, above which we use regular angle normalization.
    // This value must be < (Integer.MAX_VALUE / SIN_COS_INDEXER), to stay in range of int type.
    // The higher it is, the higher the error, but also the faster it is for lower values.
    // If you set it to ((Integer.MAX_VALUE / SIN_COS_INDEXER) * 0.99), worse accuracy on double range is about 1e-10.
    private static final double SIN_COS_MAX_VALUE_FOR_INT_MODULO = ((Integer.MAX_VALUE>>9) / SIN_COS_INDEXER) * 0.99;

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR TAN
    //--------------------------------------------------------------------------

    // We use the following formula:
    // 1) tan(-x) = -tan(x)
    // 2) tan(x) = 1/tan(PI/2-x)
    // ---> we only have to compute tan(x) on [0,A] with PI/4<=A<PI/2.

    // We use indexing past look-up tables, so that indexing information
    // allows for fast recomputation of angle in [0,PI/2] range.
    private static final int TAN_VIRTUAL_TABS_SIZE = (1<<getTabSizePower(12)) + 1;

    // Must be >= 45deg, and supposed to be >= 51.4deg, as fdlibm code is not
    // supposed to work with values inferior to that (51.4deg is about
    // (PI/2-Double.longBitsToDouble(0x3FE5942800000000L))).
    private static final double TAN_MAX_VALUE_FOR_TABS = Math.toRadians(77.0);

    private static final int TAN_TABS_SIZE = (int)((TAN_MAX_VALUE_FOR_TABS/(Math.PI/2)) * (TAN_VIRTUAL_TABS_SIZE-1)) + 1;
    private static final double TAN_DELTA_HI = PIO2_HI/(TAN_VIRTUAL_TABS_SIZE-1);
    private static final double TAN_DELTA_LO = PIO2_LO/(TAN_VIRTUAL_TABS_SIZE-1);
    private static final double TAN_INDEXER = 1/(TAN_DELTA_HI+TAN_DELTA_LO);
    private static final double[] tanTab = new double[TAN_TABS_SIZE];
    private static final double[] tanDer1DivF1Tab = new double[TAN_TABS_SIZE];
    private static final double[] tanDer2DivF2Tab = new double[TAN_TABS_SIZE];
    private static final double[] tanDer3DivF3Tab = new double[TAN_TABS_SIZE];
    private static final double[] tanDer4DivF4Tab = new double[TAN_TABS_SIZE];

    // Max abs value for fast modulo, above which we use regular angle normalization.
    // This value must be < (Integer.MAX_VALUE / TAN_INDEXER), to stay in range of int type.
    // The higher it is, the higher the error, but also the faster it is for lower values.
    private static final double TAN_MAX_VALUE_FOR_INT_MODULO = (((Integer.MAX_VALUE>>9) / TAN_INDEXER) * 0.99);

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR ACOS, ASIN
    //--------------------------------------------------------------------------

    // We use the following formula:
    // 1) acos(x) = PI/2 - asin(x)
    // 2) asin(-x) = -asin(x)
    // ---> we only have to compute asin(x) on [0,1].
    // For values not close to +-1, we use look-up tables;
    // for values near +-1, we use code derived from fdlibm.

    // Supposed to be >= sin(77.2deg), as fdlibm code is supposed to work with values > 0.975,
    // but seems to work well enough as long as value >= sin(25deg).
    private static final double ASIN_MAX_VALUE_FOR_TABS = StrictMath.sin(Math.toRadians(73.0));

    private static final int ASIN_TABS_SIZE = (1<<getTabSizePower(13)) + 1;
    private static final double ASIN_DELTA = ASIN_MAX_VALUE_FOR_TABS/(ASIN_TABS_SIZE - 1);
    private static final double ASIN_INDEXER = 1/ASIN_DELTA;
    private static final double[] asinTab = new double[ASIN_TABS_SIZE];
    private static final double[] asinDer1DivF1Tab = new double[ASIN_TABS_SIZE];
    private static final double[] asinDer2DivF2Tab = new double[ASIN_TABS_SIZE];
    private static final double[] asinDer3DivF3Tab = new double[ASIN_TABS_SIZE];
    private static final double[] asinDer4DivF4Tab = new double[ASIN_TABS_SIZE];

    private static final double ASIN_MAX_VALUE_FOR_POWTABS = StrictMath.sin(Math.toRadians(88.6));
    private static final int ASIN_POWTABS_POWER = 84;

    private static final double ASIN_POWTABS_ONE_DIV_MAX_VALUE = 1/ASIN_MAX_VALUE_FOR_POWTABS;
    private static final int ASIN_POWTABS_SIZE = USE_POWTABS_FOR_ASIN ? (1<<getTabSizePower(12)) + 1 : 0;
    private static final int ASIN_POWTABS_SIZE_MINUS_ONE = ASIN_POWTABS_SIZE - 1;
    private static final double[] asinParamPowTab = new double[ASIN_POWTABS_SIZE];
    private static final double[] asinPowTab = new double[ASIN_POWTABS_SIZE];
    private static final double[] asinDer1DivF1PowTab = new double[ASIN_POWTABS_SIZE];
    private static final double[] asinDer2DivF2PowTab = new double[ASIN_POWTABS_SIZE];
    private static final double[] asinDer3DivF3PowTab = new double[ASIN_POWTABS_SIZE];
    private static final double[] asinDer4DivF4PowTab = new double[ASIN_POWTABS_SIZE];

    private static final double ASIN_PIO2_HI = Double.longBitsToDouble(0x3FF921FB54442D18L); // 1.57079632679489655800e+00
    private static final double ASIN_PIO2_LO = Double.longBitsToDouble(0x3C91A62633145C07L); // 6.12323399573676603587e-17
    private static final double ASIN_PS0 = Double.longBitsToDouble(0x3fc5555555555555L); //  1.66666666666666657415e-01
    private static final double ASIN_PS1 = Double.longBitsToDouble(0xbfd4d61203eb6f7dL); // -3.25565818622400915405e-01
    private static final double ASIN_PS2 = Double.longBitsToDouble(0x3fc9c1550e884455L); //  2.01212532134862925881e-01
    private static final double ASIN_PS3 = Double.longBitsToDouble(0xbfa48228b5688f3bL); // -4.00555345006794114027e-02
    private static final double ASIN_PS4 = Double.longBitsToDouble(0x3f49efe07501b288L); //  7.91534994289814532176e-04
    private static final double ASIN_PS5 = Double.longBitsToDouble(0x3f023de10dfdf709L); //  3.47933107596021167570e-05
    private static final double ASIN_QS1 = Double.longBitsToDouble(0xc0033a271c8a2d4bL); // -2.40339491173441421878e+00
    private static final double ASIN_QS2 = Double.longBitsToDouble(0x40002ae59c598ac8L); //  2.02094576023350569471e+00
    private static final double ASIN_QS3 = Double.longBitsToDouble(0xbfe6066c1b8d0159L); // -6.88283971605453293030e-01
    private static final double ASIN_QS4 = Double.longBitsToDouble(0x3fb3b8c5b12e9282L); //  7.70381505559019352791e-02

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR ATAN
    //--------------------------------------------------------------------------

    // We use the formula atan(-x) = -atan(x)
    // ---> we only have to compute atan(x) on [0,+infinity[.
    // For values corresponding to angles not close to +-PI/2, we use look-up tables;
    // for values corresponding to angles near +-PI/2, we use code derived from fdlibm.

    // Supposed to be >= tan(67.7deg), as fdlibm code is supposed to work with values > 2.4375.
    private static final double ATAN_MAX_VALUE_FOR_TABS = StrictMath.tan(Math.toRadians(74.0));

    private static final int ATAN_TABS_SIZE = (1<<getTabSizePower(12)) + 1;
    private static final double ATAN_DELTA = ATAN_MAX_VALUE_FOR_TABS/(ATAN_TABS_SIZE - 1);
    private static final double ATAN_INDEXER = 1/ATAN_DELTA;
    private static final double[] atanTab = new double[ATAN_TABS_SIZE];
    private static final double[] atanDer1DivF1Tab = new double[ATAN_TABS_SIZE];
    private static final double[] atanDer2DivF2Tab = new double[ATAN_TABS_SIZE];
    private static final double[] atanDer3DivF3Tab = new double[ATAN_TABS_SIZE];
    private static final double[] atanDer4DivF4Tab = new double[ATAN_TABS_SIZE];

    private static final double ATAN_HI3 = Double.longBitsToDouble(0x3ff921fb54442d18L); // 1.57079632679489655800e+00 atan(inf)hi
    private static final double ATAN_LO3 = Double.longBitsToDouble(0x3c91a62633145c07L); // 6.12323399573676603587e-17 atan(inf)lo
    private static final double ATAN_AT0 = Double.longBitsToDouble(0x3fd555555555550dL); //  3.33333333333329318027e-01
    private static final double ATAN_AT1 = Double.longBitsToDouble(0xbfc999999998ebc4L); // -1.99999999998764832476e-01
    private static final double ATAN_AT2 = Double.longBitsToDouble(0x3fc24924920083ffL); //  1.42857142725034663711e-01
    private static final double ATAN_AT3 = Double.longBitsToDouble(0xbfbc71c6fe231671L); // -1.11111104054623557880e-01
    private static final double ATAN_AT4 = Double.longBitsToDouble(0x3fb745cdc54c206eL); //  9.09088713343650656196e-02
    private static final double ATAN_AT5 = Double.longBitsToDouble(0xbfb3b0f2af749a6dL); // -7.69187620504482999495e-02
    private static final double ATAN_AT6 = Double.longBitsToDouble(0x3fb10d66a0d03d51L); //  6.66107313738753120669e-02
    private static final double ATAN_AT7 = Double.longBitsToDouble(0xbfadde2d52defd9aL); // -5.83357013379057348645e-02
    private static final double ATAN_AT8 = Double.longBitsToDouble(0x3fa97b4b24760debL); //  4.97687799461593236017e-02
    private static final double ATAN_AT9 = Double.longBitsToDouble(0xbfa2b4442c6a6c2fL); // -3.65315727442169155270e-02
    private static final double ATAN_AT10 = Double.longBitsToDouble(0x3f90ad3ae322da11L); // 1.62858201153657823623e-02 

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR EXP AND EXPM1
    //--------------------------------------------------------------------------

    private static final double EXP_OVERFLOW_LIMIT = Double.longBitsToDouble(0x40862E42FEFA39EFL); // 7.09782712893383973096e+02
    private static final double EXP_UNDERFLOW_LIMIT = Double.longBitsToDouble(0xC0874910D52D3051L); // -7.45133219101941108420e+02
    private static final double EXP_MIN_INT_LIMIT = -705;
    private static final int EXP_LO_DISTANCE_TO_ZERO_POT = 0;
    private static final int EXP_LO_DISTANCE_TO_ZERO = (1<<EXP_LO_DISTANCE_TO_ZERO_POT);
    private static final int EXP_LO_TAB_SIZE_POT = getTabSizePower(11);
    private static final int EXP_LO_TAB_SIZE = (1<<EXP_LO_TAB_SIZE_POT)+1;
    private static final int EXP_LO_TAB_MID_INDEX = ((EXP_LO_TAB_SIZE-1)/2);
    private static final int EXP_LO_INDEXING = EXP_LO_TAB_MID_INDEX/EXP_LO_DISTANCE_TO_ZERO;
    private static final int EXP_LO_INDEXING_DIV_SHIFT = EXP_LO_TAB_SIZE_POT-1-EXP_LO_DISTANCE_TO_ZERO_POT;
    private static final double[] expHiTab = new double[1+(int)EXP_OVERFLOW_LIMIT];
    private static final double[] expHiInvTab = new double[1-(int)EXP_UNDERFLOW_LIMIT];
    private static final double[] expLoPosTab = new double[EXP_LO_TAB_SIZE];
    private static final double[] expLoNegTab = new double[EXP_LO_TAB_SIZE];

    //--------------------------------------------------------------------------
    // CONSTANTS FOR QUICK EXP
    //--------------------------------------------------------------------------

    private static final double EXP_QUICK_A = TWO_POW_52/LOG_2;
    private static final double EXP_QUICK_B = MAX_DOUBLE_EXPONENT * TWO_POW_52;
    private static final double EXP_QUICK_C = Math.ceil((StrictMath.log(LOG_2+2/Math.E) - LOG_2 - StrictMath.log(LOG_2)) * EXP_QUICK_A);

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR LOG AND LOG1P
    //--------------------------------------------------------------------------

    private static final int LOG_BITS = getTabSizePower(12);
    private static final int LOG_TAB_SIZE = (1<<LOG_BITS);
    private static final double[] logXLogTab = new double[LOG_TAB_SIZE];
    private static final double[] logXTab = new double[LOG_TAB_SIZE];
    private static final double[] logXInvTab = new double[LOG_TAB_SIZE];

    //--------------------------------------------------------------------------
    // TABLE FOR POWERS OF TWO
    //--------------------------------------------------------------------------

    private static final double[] twoPowTab = new double[(MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT)+1];

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR SQRT
    //--------------------------------------------------------------------------

    private static final int SQRT_LO_BITS = getTabSizePower(12);
    private static final int SQRT_LO_TAB_SIZE = (1<<SQRT_LO_BITS);
    private static final double[] sqrtXSqrtHiTab = new double[MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT+1];
    private static final double[] sqrtXSqrtLoTab = new double[SQRT_LO_TAB_SIZE];
    private static final double[] sqrtSlopeHiTab = new double[MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT+1];
    private static final double[] sqrtSlopeLoTab = new double[SQRT_LO_TAB_SIZE];

    //--------------------------------------------------------------------------
    // CONSTANTS AND TABLES FOR CBRT
    //--------------------------------------------------------------------------

    private static final int CBRT_LO_BITS = getTabSizePower(12);
    private static final int CBRT_LO_TAB_SIZE = (1<<CBRT_LO_BITS);
    // For CBRT_LO_BITS = 12:
    // cbrtXCbrtLoTab[0] = 1.0.
    // cbrtXCbrtLoTab[1] = cbrt(1. 000000000000 1111111111111111111111111111111111111111b)
    // cbrtXCbrtLoTab[2] = cbrt(1. 000000000001 1111111111111111111111111111111111111111b)
    // cbrtXCbrtLoTab[3] = cbrt(1. 000000000010 1111111111111111111111111111111111111111b)
    // cbrtXCbrtLoTab[4] = cbrt(1. 000000000011 1111111111111111111111111111111111111111b)
    // etc.
    private static final double[] cbrtXCbrtHiTab = new double[MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT+1];
    private static final double[] cbrtXCbrtLoTab = new double[CBRT_LO_TAB_SIZE];
    private static final double[] cbrtSlopeHiTab = new double[MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT+1];
    private static final double[] cbrtSlopeLoTab = new double[CBRT_LO_TAB_SIZE];

    //--------------------------------------------------------------------------
    // PUBLIC TREATMENTS
    //--------------------------------------------------------------------------

    /**
     * @param angle Angle in radians.
     * @return Angle cosine.
     */
    public static double cos(double angle) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.cos(angle) : Math.cos(angle);
        }
        angle = Math.abs(angle);
        if (angle > SIN_COS_MAX_VALUE_FOR_INT_MODULO) {
            // Faster than using normalizeZeroTwoPi.
            angle = remainderTwoPi(angle);
            if (angle < 0.0) {
                angle += 2*Math.PI;
            }
        }
        // index: possibly outside tables range.
        int index = (int)(angle * SIN_COS_INDEXER + 0.5);
        double delta = (angle - index * SIN_COS_DELTA_HI) - index * SIN_COS_DELTA_LO;
        // Making sure index is within tables range.
        // Last value of each table is the same than first, so we ignore it (tabs size minus one) for modulo.
        index &= (SIN_COS_TABS_SIZE-2); // index % (SIN_COS_TABS_SIZE-1)
        double indexCos = cosTab[index];
        double indexSin = sinTab[index];
        return indexCos + delta * (-indexSin + delta * (-indexCos * ONE_DIV_F2 + delta * (indexSin * ONE_DIV_F3 + delta * indexCos * ONE_DIV_F4)));
    }

    /**
     * Quick cosine, with accuracy of about 1.6e-3 (PI/<look-up tabs size>)
     * for |angle| < 6588397.0 (Integer.MAX_VALUE * (2*PI/<look-up tabs size>)),
     * and no accuracy at all for larger values.
     * 
     * @param angle Angle in radians.
     * @return Angle cosine.
     */
    public static double cosQuick(double angle) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.cos(angle) : Math.cos(angle);
        }
        return cosTab[((int)(Math.abs(angle) * SIN_COS_INDEXER + 0.5)) & (SIN_COS_TABS_SIZE-2)];
    }

    /**
     * @param angle Angle in radians.
     * @return Angle sine.
     */
    public static double sin(double angle) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.sin(angle) : Math.sin(angle);
        }
        boolean negateResult;
        if (angle < 0.0) {
            angle = -angle;
            negateResult = true;
        } else {
            negateResult = false;
        }
        if (angle > SIN_COS_MAX_VALUE_FOR_INT_MODULO) {
            // Faster than using normalizeZeroTwoPi.
            angle = remainderTwoPi(angle);
            if (angle < 0.0) {
                angle += 2*Math.PI;
            }
        }
        int index = (int)(angle * SIN_COS_INDEXER + 0.5);
        double delta = (angle - index * SIN_COS_DELTA_HI) - index * SIN_COS_DELTA_LO;
        index &= (SIN_COS_TABS_SIZE-2); // index % (SIN_COS_TABS_SIZE-1)
        double indexSin = sinTab[index];
        double indexCos = cosTab[index];
        double result = indexSin + delta * (indexCos + delta * (-indexSin * ONE_DIV_F2 + delta * (-indexCos * ONE_DIV_F3 + delta * indexSin * ONE_DIV_F4)));
        return negateResult ? -result : result;
    }

    /**
     * Quick sine, with accuracy of about 1.6e-3 (PI/<look-up tabs size>)
     * for |angle| < 6588397.0 (Integer.MAX_VALUE * (2*PI/<look-up tabs size>)),
     * and no accuracy at all for larger values.
     * 
     * @param angle Angle in radians.
     * @return Angle sine.
     */
    public static double sinQuick(double angle) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.sin(angle) : Math.sin(angle);
        }
        return cosTab[((int)(Math.abs(angle-Math.PI/2) * SIN_COS_INDEXER + 0.5)) & (SIN_COS_TABS_SIZE-2)];
    }

    /**
     * Computes sine and cosine together, at the cost of... a dependency of this class with DoubleWrapper.
     * 
     * @param angle Angle in radians.
     * @param sine Angle sine.
     * @param cosine Angle cosine.
     */
    public static void sinAndCos(double angle, DoubleWrapper sine, DoubleWrapper cosine) {
        if (USE_JDK_MATH) {
            sine.value = STRICT_MATH ? StrictMath.sin(angle) : Math.sin(angle);
            cosine.value = STRICT_MATH ? StrictMath.cos(angle) : Math.cos(angle);
            return;
        }
        // Using the same algorithm than sin(double) method, and computing also cosine at the end.
        boolean negateResult;
        if (angle < 0.0) {
            angle = -angle;
            negateResult = true;
        } else {
            negateResult = false;
        }
        if (angle > SIN_COS_MAX_VALUE_FOR_INT_MODULO) {
            // Faster than using normalizeZeroTwoPi.
            angle = remainderTwoPi(angle);
            if (angle < 0.0) {
                angle += 2*Math.PI;
            }
        }
        int index = (int)(angle * SIN_COS_INDEXER + 0.5);
        double delta = (angle - index * SIN_COS_DELTA_HI) - index * SIN_COS_DELTA_LO;
        index &= (SIN_COS_TABS_SIZE-2); // index % (SIN_COS_TABS_SIZE-1)
        double indexSin = sinTab[index];
        double indexCos = cosTab[index];
        double result = indexSin + delta * (indexCos + delta * (-indexSin * ONE_DIV_F2 + delta * (-indexCos * ONE_DIV_F3 + delta * indexSin * ONE_DIV_F4)));
        sine.value = negateResult ? -result : result;
        cosine.value = indexCos + delta * (-indexSin + delta * (-indexCos * ONE_DIV_F2 + delta * (indexSin * ONE_DIV_F3 + delta * indexCos * ONE_DIV_F4)));
    }

    /**
     * @param angle Angle in radians.
     * @return Angle tangent.
     */
    public static double tan(double angle) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.tan(angle) : Math.tan(angle);
        }
        if (Math.abs(angle) > TAN_MAX_VALUE_FOR_INT_MODULO) {
            // Faster than using normalizeMinusHalfPiHalfPi.
            angle = remainderTwoPi(angle);
            if (angle < -Math.PI/2) {
                angle += Math.PI;
            } else if (angle > Math.PI/2) {
                angle -= Math.PI;
            }
        }
        boolean negateResult;
        if (angle < 0.0) {
            angle = -angle;
            negateResult = true;
        } else {
            negateResult = false;
        }
        int index = (int)(angle * TAN_INDEXER + 0.5);
        double delta = (angle - index * TAN_DELTA_HI) - index * TAN_DELTA_LO;
        // index modulo PI, i.e. 2*(virtual tab size minus one). 
        index &= (2*(TAN_VIRTUAL_TABS_SIZE-1)-1); // index % (2*(TAN_VIRTUAL_TABS_SIZE-1))
        // Here, index is in [0,2*(TAN_VIRTUAL_TABS_SIZE-1)-1], i.e. indicates an angle in [0,PI[.
        if (index > (TAN_VIRTUAL_TABS_SIZE-1)) {
            index = (2*(TAN_VIRTUAL_TABS_SIZE-1)) - index;
            delta = -delta;
            negateResult = !negateResult;
        }
        double result;
        if (index < TAN_TABS_SIZE) {
            result = tanTab[index] + delta * (tanDer1DivF1Tab[index] + delta * (tanDer2DivF2Tab[index] + delta * (tanDer3DivF3Tab[index] + delta * tanDer4DivF4Tab[index])));
        } else { // angle in ]TAN_MAX_VALUE_FOR_TABS,TAN_MAX_VALUE_FOR_INT_MODULO], or angle is NaN
            // Using tan(angle) == 1/tan(PI/2-angle) formula: changing angle (index and delta), and inverting.
            index = (TAN_VIRTUAL_TABS_SIZE-1) - index;
            result = 1/(tanTab[index] - delta * (tanDer1DivF1Tab[index] - delta * (tanDer2DivF2Tab[index] - delta * (tanDer3DivF3Tab[index] - delta * tanDer4DivF4Tab[index]))));
        }
        return negateResult ? -result : result;
    }

    /**
     * @param value Value in [-1,1].
     * @return Value arccosine, in radians, in [0,PI].
     */
    public static double acos(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.acos(value) : Math.acos(value);
        }
        return Math.PI/2 - FastMath.asin(value);
    }

    /**
     * If value is not NaN and is outside [-1,1] range, closest value in this range is used.
     * 
     * @param value Value in [-1,1].
     * @return Value arccosine, in radians, in [0,PI].
     */
    public static double acosInRange(double value) {
        if (value <= -1) {
            return Math.PI;
        } else if (value >= 1) {
            return 0.0;
        } else {
            return FastMath.acos(value);
        }
    }

    /**
     * @param value Value in [-1,1].
     * @return Value arcsine, in radians, in [-PI/2,PI/2].
     */
    public static double asin(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.asin(value) : Math.asin(value);
        }
        boolean negateResult;
        if (value < 0.0) {
            value = -value;
            negateResult = true;
        } else {
            negateResult = false;
        }
        if (value <= ASIN_MAX_VALUE_FOR_TABS) {
            int index = (int)(value * ASIN_INDEXER + 0.5);
            double delta = value - index * ASIN_DELTA;
            double result = asinTab[index] + delta * (asinDer1DivF1Tab[index] + delta * (asinDer2DivF2Tab[index] + delta * (asinDer3DivF3Tab[index] + delta * asinDer4DivF4Tab[index])));
            return negateResult ? -result : result;
        } else if (USE_POWTABS_FOR_ASIN && (value <= ASIN_MAX_VALUE_FOR_POWTABS)) {
            int index = (int)(FastMath.powFast(value * ASIN_POWTABS_ONE_DIV_MAX_VALUE, ASIN_POWTABS_POWER) * ASIN_POWTABS_SIZE_MINUS_ONE + 0.5);
            double delta = value - asinParamPowTab[index];
            double result = asinPowTab[index] + delta * (asinDer1DivF1PowTab[index] + delta * (asinDer2DivF2PowTab[index] + delta * (asinDer3DivF3PowTab[index] + delta * asinDer4DivF4PowTab[index])));
            return negateResult ? -result : result;
        } else { // value > ASIN_MAX_VALUE_FOR_TABS, or value is NaN
            // This part is derived from fdlibm.
            if (value < 1.0) {
                double t = (1.0 - value)*0.5;
                double p = t*(ASIN_PS0+t*(ASIN_PS1+t*(ASIN_PS2+t*(ASIN_PS3+t*(ASIN_PS4+t*ASIN_PS5)))));
                double q = 1.0+t*(ASIN_QS1+t*(ASIN_QS2+t*(ASIN_QS3+t*ASIN_QS4)));
                double s = FastMath.sqrt(t);
                double z = s+s*(p/q);
                double result = ASIN_PIO2_HI-((z+z)-ASIN_PIO2_LO);
                return negateResult ? -result : result;
            } else { // value >= 1.0, or value is NaN
                if (value == 1.0) {
                    return negateResult ? -Math.PI/2 : Math.PI/2;
                } else {
                    return Double.NaN;
                }
            }
        }
    }

    /**
     * If value is not NaN and is outside [-1,1] range, closest value in this range is used.
     * 
     * @param value Value in [-1,1].
     * @return Value arcsine, in radians, in [-PI/2,PI/2].
     */
    public static double asinInRange(double value) {
        if (value <= -1) {
            return -Math.PI/2;
        } else if (value >= 1) {
            return Math.PI/2;
        } else {
            return FastMath.asin(value);
        }
    }

    /**
     * @param value A double value.
     * @return Value arctangent, in radians, in [-PI/2,PI/2].
     */
    public static double atan(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.atan(value) : Math.atan(value);
        }
        boolean negateResult;
        if (value < 0.0) {
            value = -value;
            negateResult = true;
        } else {
            negateResult = false;
        }
        if (value == 1.0) {
            // We want "exact" result for 1.0.
            return negateResult ? -Math.PI/4 : Math.PI/4;
        } else if (value <= ATAN_MAX_VALUE_FOR_TABS) {
            int index = (int)(value * ATAN_INDEXER + 0.5);
            double delta = value - index * ATAN_DELTA;
            double result = atanTab[index] + delta * (atanDer1DivF1Tab[index] + delta * (atanDer2DivF2Tab[index] + delta * (atanDer3DivF3Tab[index] + delta * atanDer4DivF4Tab[index])));
            return negateResult ? -result : result;
        } else { // value > ATAN_MAX_VALUE_FOR_TABS, or value is NaN
            // This part is derived from fdlibm.
            if (value < TWO_POW_66) {
                double x = -1/value;
                double x2 = x*x;
                double x4 = x2*x2;
                double s1 = x2*(ATAN_AT0+x4*(ATAN_AT2+x4*(ATAN_AT4+x4*(ATAN_AT6+x4*(ATAN_AT8+x4*ATAN_AT10)))));
                double s2 = x4*(ATAN_AT1+x4*(ATAN_AT3+x4*(ATAN_AT5+x4*(ATAN_AT7+x4*ATAN_AT9))));
                double result = ATAN_HI3-((x*(s1+s2)-ATAN_LO3)-x);
                return negateResult ? -result : result;
            } else { // value >= 2^66, or value is NaN
                if (Double.isNaN(value)) {
                    return Double.NaN;
                } else {
                    return negateResult ? -Math.PI/2 : Math.PI/2;
                }
            }
        }
    }

    /**
     * For special values for which multiple conventions could be adopted, behaves like Math.atan2(double,double).
     * 
     * @param y Coordinate on y axis.
     * @param x Coordinate on x axis.
     * @return Angle from x axis positive side to (x,y) position, in radians, in [-PI,PI].
     *         Angle measure is positive when going from x axis to y axis (positive sides).
     */
    public static double atan2(double y, double x) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.atan2(y,x) : Math.atan2(y,x);
        }
        if (x > 0.0) {
            if (y == 0.0) {
                return (1/y == Double.NEGATIVE_INFINITY) ? -0.0 : 0.0;
            }
            if (x == Double.POSITIVE_INFINITY) {
                if (y == Double.POSITIVE_INFINITY) {
                    return Math.PI/4;
                } else if (y == Double.NEGATIVE_INFINITY) {
                    return -Math.PI/4;
                } else if (y > 0.0) {
                    return 0.0;
                } else if (y < 0.0) {
                    return -0.0;
                } else {
                    return Double.NaN;
                }
            } else {
                return FastMath.atan(y/x);
            }
        } else if (x < 0.0) {
            if (y == 0.0) {
                return (1/y == Double.NEGATIVE_INFINITY) ? -Math.PI : Math.PI;
            }
            if (x == Double.NEGATIVE_INFINITY) {
                if (y == Double.POSITIVE_INFINITY) {
                    return 3*Math.PI/4;
                } else if (y == Double.NEGATIVE_INFINITY) {
                    return -3*Math.PI/4;
                } else if (y > 0.0) {
                    return Math.PI;
                } else if (y < 0.0) {
                    return -Math.PI;
                } else {
                    return Double.NaN;
                }
            } else if (y > 0.0) {
                return Math.PI/2 + FastMath.atan(-x/y);
            } else if (y < 0.0) {
                return -Math.PI/2 - FastMath.atan(x/y);
            } else {
                return Double.NaN;
            }
        } else if (x == 0.0) {
            if (y == 0.0) {
                if (1/x == Double.NEGATIVE_INFINITY) {
                    return (1/y == Double.NEGATIVE_INFINITY) ? -Math.PI : Math.PI;
                } else {
                    return (1/y == Double.NEGATIVE_INFINITY) ? -0.0 : 0.0;
                }
            }
            if (y > 0.0) {
                return Math.PI/2;
            } else if (y < 0.0) {
                return -Math.PI/2;
            } else {
                return Double.NaN;
            }
        } else {
            return Double.NaN;
        }
    }

    /**
     * @param value A double value.
     * @return Value hyperbolic cosine.
     */
    public static double cosh(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.cosh(value) : Math.cosh(value);
        }
        // cosh(x) = (exp(x)+exp(-x))/2
        if (value < 0.0) {
            value = -value;
        }
        if (value < LOG_TWO_POW_27) {
            if (value < TWO_POW_N27) {
                // cosh(x)
                // = (exp(x)+exp(-x))/2
                // = ((1+x+x^2/2!+...) + (1-x+x^2/2!-...))/2
                // = 1+x^2/2!+x^4/4!+...
                // For value of x small in magnitude, the sum of the terms does not add to 1.
                return 1;
            } else {
                double t = FastMath.exp(value);
                return 0.5 * (t+1/t);
            }
        } else if (value < LOG_DOUBLE_MAX_VALUE) {
            return 0.5 * FastMath.exp(value);
        } else {
            double t = FastMath.exp(value*0.5);
            return (0.5*t)*t;
        }
    }

    /**
     * @param value A double value.
     * @return Value hyperbolic sine.
     */
    public static double sinh(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.sinh(value) : Math.sinh(value);
        }
        // sinh(x) = (exp(x)-exp(-x))/2
        double h;
        if (value < 0.0) {
            value = -value;
            h = -0.5;
        } else {
            h = 0.5;
        }
        if (value < 22.0) {
            if (value < TWO_POW_N28) {
                return (h < 0.0) ? -value : value;
            } else {
                double t = FastMath.expm1(value);
                // Might be more accurate, if value < 1: return h*((t+t)-t*t/(t+1.0)).
                return h * (t + t/(t+1.0));
            }
        } else if (value < LOG_DOUBLE_MAX_VALUE) {
            return h * FastMath.exp(value);
        } else {
            double t = FastMath.exp(value*0.5);
            return (h*t)*t;
        }
    }

    /**
     * Computes hyperbolic sine and hyperbolic cosine together, at the cost of... a dependency of this class with DoubleWrapper.
     * 
     * @param value A double value.
     * @param hsine Value hyperbolic sine.
     * @param hcosine Value hyperbolic cosine.
     */
    public static void sinhAndCosh(double value, DoubleWrapper hsine, DoubleWrapper hcosine) {
        if (USE_JDK_MATH) {
            hsine.value = STRICT_MATH ? StrictMath.sinh(value) : Math.sinh(value);
            hcosine.value = STRICT_MATH ? StrictMath.cosh(value) : Math.cosh(value);
            return;
        }
        // Mixup of sinh and cosh treatments: if you modify them,
        // you might want to also modify this.
        double h;
        if (value < 0.0) {
            value = -value;
            h = -0.5;
        } else {
            h = 0.5;
        }
        // LOG_TWO_POW_27 = 18.714973875118524
        if (value < LOG_TWO_POW_27) { // test from cosh
            // sinh
            if (value < TWO_POW_N28) {
                hsine.value = (h < 0.0) ? -value : value;
            } else {
                double t = FastMath.expm1(value);
                hsine.value = h * (t + t/(t+1.0));
            }
            // cosh
            if (value < TWO_POW_N27) {
                hcosine.value = 1;
            } else {
                double t = FastMath.exp(value);
                hcosine.value = 0.5 * (t+1/t);
            }
        } else if (value < 22.0) { // test from sinh
            // Here, value is in [18.714973875118524,22.0[.
            double t = FastMath.expm1(value);
            hsine.value = h * (t + t/(t+1.0));
            hcosine.value = 0.5 * (t+1.0);
        } else {
            if (value < LOG_DOUBLE_MAX_VALUE) {
                hsine.value = h * FastMath.exp(value);
            } else {
                double t = FastMath.exp(value*0.5);
                hsine.value = (h*t)*t;
            }
            hcosine.value = Math.abs(hsine.value);
        }
    }

    /**
     * @param value A double value.
     * @return Value hyperbolic tangent.
     */
    public static double tanh(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.tanh(value) : Math.tanh(value);
        }
        // tanh(x) = sinh(x)/cosh(x)
        //         = (exp(x)-exp(-x))/(exp(x)+exp(-x))
        //         = (exp(2*x)-1)/(exp(2*x)+1)
        boolean negateResult;
        if (value < 0.0) {
            value = -value;
            negateResult = true;
        } else {
            negateResult = false;
        }
        double z;
        if (value < 22.0) {
            if (value < TWO_POW_N55) {
                return negateResult ? -value*(1.0-value) : value*(1.0+value);
            } else if (value >= 1) {
                z = 1.0-2.0/(FastMath.expm1(value+value)+2.0);
            } else {
                double t = FastMath.expm1(-(value+value));
                z = -t/(t+2.0);
            }
        } else {
            z = (value != value) ? Double.NaN : 1.0;
        }
        return negateResult ? -z : z;
    }

    /**
     * @param value A double value.
     * @return e^value.
     */
    public static double exp(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.exp(value) : Math.exp(value);
        }
        // exp(x) = exp([x])*exp(y)
        // with [x] the integer part of x, and y = x-[x]
        // ===>
        // We find an approximation of y, called z.
        // ===>
        // exp(x) = exp([x])*(exp(z)*exp(epsilon))
        // ===>
        // We have exp([x]) and exp(z) pre-computed in tables, we "just" have to compute exp(epsilon).
        //
        // We use the same indexing (cast to int) to compute x integer part and the
        // table index corresponding to z, to avoid two int casts.
        // Also, to optimize index multiplication and division, we use powers of two,
        // so that we can do it with bits shifts.
        if (value >= 0.0) {
            if (value > EXP_OVERFLOW_LIMIT) {
                return Double.POSITIVE_INFINITY;
            }
            int i = (int)(value*EXP_LO_INDEXING);
            int valueInt = (i>>EXP_LO_INDEXING_DIV_SHIFT);
            i -= (valueInt<<EXP_LO_INDEXING_DIV_SHIFT);
            double delta = (value-valueInt)-i*(1.0/EXP_LO_INDEXING);
            return expHiTab[valueInt] * (expLoPosTab[i+EXP_LO_TAB_MID_INDEX]*(1+delta*(1+delta*(1.0/2+delta*(1.0/6+delta*(1.0/24))))));
        } else { // value < 0.0, or value is NaN
            if (!(value >= EXP_UNDERFLOW_LIMIT)) { // value < EXP_UNDERFLOW_LIMIT, or value is NaN
                return (value < EXP_UNDERFLOW_LIMIT) ? 0.0 : Double.NaN;
            }
            // TODO JVM bug with -server option: test with values of all magnitudes
            // is very slow, if using (int)x instead of -(int)-x or (int)(long)x (which give the same result).
            // The guessed cause is that when the same expression is used to define "i" in
            // both sides of the above "else", some (desastrous) optimization is done which factorizes
            // it above the first "if" statement, making it computed all the time, without the protecting "sub-ifs".
            // Since cast from double to int with huge values is extremely slow,
            // this makes this whole treatment extremely slow for huge values.
            // The solution is therefore to modify a bit the expression for the "optimization" not to occur.
            int i = -(int)-(value*EXP_LO_INDEXING);
            int valueInt = -((-i)>>EXP_LO_INDEXING_DIV_SHIFT);
            i -= ((valueInt)<<EXP_LO_INDEXING_DIV_SHIFT);
            double delta = (value-valueInt)-i*(1.0/EXP_LO_INDEXING);
            double tmp = expHiInvTab[-valueInt] * (expLoPosTab[i+EXP_LO_TAB_MID_INDEX]*(1+delta*(1+delta*(1.0/2+delta*(1.0/6+delta*(1.0/24))))));
            // We took care not to compute with subnormal values.
            return (valueInt >= EXP_MIN_INT_LIMIT) ? tmp : tmp * TWO_POW_N54;
        }
    }

    /**
     * Quick exp, with a max relative error of about 3e-2 for |value| < 700.0 or so,
     * and no accuracy at all outside this range.
     * Derived from a note by Nicol N. Schraudolph, IDSIA, 1998.
     * 
     * @param value A double value.
     * @return e^value.
     */
    public static double expQuick(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.exp(value) : Math.exp(value);
        }
        if (false) {
            // Schraudolph's original method.
            return Double.longBitsToDouble((long)(EXP_QUICK_A * value + (EXP_QUICK_B - EXP_QUICK_C)));
        }
        /*
         * Cast of double values, even in long range, into long, is slower than
         * from double to int for values in int range, and then from int to long.
         * For that reason, we only work with integer values in int range (corresponding to the 32 first bits of the long,
         * containing sign, exponent, and highest significant bits of double's mantissa), and cast twice.
         */
        return Double.longBitsToDouble(((long)(int)(EXP_QUICK_A/(1L<<32) * value + (EXP_QUICK_B - EXP_QUICK_C)/(1L<<32)))<<32);
    }

    /**
     * Much more accurate than exp(value)-1, for values close to zero.
     * 
     * @param value A double value.
     * @return e^value-1.
     */
    public static double expm1(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.expm1(value) : Math.expm1(value);
        }
        // If value is far from zero, we use exp(value)-1.
        //
        // If value is close to zero, we use the following formula:
        // exp(value)-1
        // = exp(valueApprox)*exp(epsilon)-1
        // = exp(valueApprox)*(exp(epsilon)-exp(-valueApprox))
        // = exp(valueApprox)*(1+epsilon+epsilon^2/2!+...-exp(-valueApprox))
        // = exp(valueApprox)*((1-exp(-valueApprox))+epsilon+epsilon^2/2!+...)
        // exp(valueApprox) and exp(-valueApprox) being stored in tables.

        if (Math.abs(value) < EXP_LO_DISTANCE_TO_ZERO) {
            // Taking int part instead of rounding, which takes too long.
            int i = (int)(value*EXP_LO_INDEXING);
            double delta = value-i*(1.0/EXP_LO_INDEXING);
            return expLoPosTab[i+EXP_LO_TAB_MID_INDEX]*(expLoNegTab[i+EXP_LO_TAB_MID_INDEX]+delta*(1+delta*(1.0/2+delta*(1.0/6+delta*(1.0/24+delta*(1.0/120))))));
        } else {
            return FastMath.exp(value)-1;
        }
    }

    /**
     * @param value A double value.
     * @return Value logarithm (base e).
     */
    public static double log(double value) {
        if (USE_JDK_MATH || (!USE_REDEFINED_LOG)) {
            return STRICT_MATH ? StrictMath.log(value) : Math.log(value);
        } else {
            if (value > 0.0) {
                if (value == Double.POSITIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }

                // For normal values not close to 1.0, we use the following formula:
                // log(value)
                // = log(2^exponent*1.mantissa)
                // = log(2^exponent) + log(1.mantissa)
                // = exponent * log(2) + log(1.mantissa)
                // = exponent * log(2) + log(1.mantissaApprox) + log(1.mantissa/1.mantissaApprox)
                // = exponent * log(2) + log(1.mantissaApprox) + log(1+epsilon)
                // = exponent * log(2) + log(1.mantissaApprox) + epsilon-epsilon^2/2+epsilon^3/3-epsilon^4/4+...
                // with:
                // 1.mantissaApprox <= 1.mantissa,
                // log(1.mantissaApprox) in table,
                // epsilon = (1.mantissa/1.mantissaApprox)-1
                //
                // To avoid bad relative error for small results,
                // values close to 1.0 are treated aside, with the formula:
                // log(x) = z*(2+z^2*((2.0/3)+z^2*((2.0/5))+z^2*((2.0/7))+...)))
                // with z=(x-1)/(x+1)

                double h;
                if (value > 0.95) {
                    if (value < 1.14) {
                        double z = (value-1.0)/(value+1.0);
                        double z2 = z*z;
                        return z*(2+z2*((2.0/3)+z2*((2.0/5)+z2*((2.0/7)+z2*((2.0/9)+z2*((2.0/11)))))));
                    }
                    h = 0.0;
                } else if (value < MIN_DOUBLE_NORMAL) {
                    // Ensuring value is normal.
                    value *= TWO_POW_52;
                    // log(x*2^52)
                    // = log(x)-ln(2^52)
                    // = log(x)-52*ln(2)
                    h = -52*LOG_2;
                } else {
                    h = 0.0;
                }

                int valueBitsHi = (int)(Double.doubleToRawLongBits(value)>>32);
                int valueExp = (valueBitsHi>>20)-MAX_DOUBLE_EXPONENT;
                // Getting the first LOG_BITS bits of the mantissa.
                int xIndex = ((valueBitsHi<<12)>>>(32-LOG_BITS));

                // 1.mantissa/1.mantissaApprox - 1
                double z = (value * twoPowTab[-valueExp-MIN_DOUBLE_EXPONENT]) * logXInvTab[xIndex] - 1;

                z *= (1-z*((1.0/2)-z*((1.0/3))));

                return h + valueExp * LOG_2 + (logXLogTab[xIndex] + z);

            } else if (value == 0.0) {
                return Double.NEGATIVE_INFINITY;
            } else { // value < 0.0, or value is NaN
                return Double.NaN;
            }
        }
    }

    /**
     * Quick log, with a max relative error of about 2.8e-4
     * for values in ]0,+infinity[, and no accuracy at all
     * outside this range.
     */
    public static double logQuick(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.log(value) : Math.log(value);
        }
        /*
         * Inverse of Schraudolph's method for exp, is very inaccurate near 1,
         * and not that fast (even using floats), especially with added if's
         * to deal with values near 1, so we don't use it, and use a simplified
         * version of our log's redefined algorithm.
         */

        // Simplified version of log's redefined algorithm:
        // log(value) ~= exponent * log(2) + log(1.mantissaApprox)

        double h;
        if (value > 0.87) {
            if (value < 1.16) {
                return 2.0 * (value-1.0)/(value+1.0);
            }
            h = 0.0;
        } else if (value < MIN_DOUBLE_NORMAL) {
            value *= TWO_POW_52;
            h = -52*LOG_2;
        } else {
            h = 0.0;
        }

        int valueBitsHi = (int)(Double.doubleToRawLongBits(value)>>32);
        int valueExp = (valueBitsHi>>20)-MAX_DOUBLE_EXPONENT;
        int xIndex = ((valueBitsHi<<12)>>>(32-LOG_BITS));

        return h + valueExp * LOG_2 + logXLogTab[xIndex];
    }

    /**
     * @param value A double value.
     * @return Value logarithm (base 10).
     */
    public static double log10(double value) {
        if (USE_JDK_MATH || (!USE_REDEFINED_LOG)) {
            return STRICT_MATH ? StrictMath.log10(value) : Math.log10(value);
        } else {
            // INV_LOG_10 is < 1, but there is no risk of log(double)
            // overflow (positive or negative) while the end result shouldn't,
            // since log(Double.MIN_VALUE) and log(Double.MAX_VALUE) have
            // magnitudes of just a few hundreds.
            return log(value) * INV_LOG_10;
        }
    }

    /**
     * Much more accurate than log(1+value), for values close to zero.
     * 
     * @param value A double value.
     * @return Logarithm (base e) of (1+value).
     */
    public static double log1p(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.log1p(value) : Math.log1p(value);
        }
        if (false) {
            // This also works. Simpler but a bit slower.
            if (value == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }
            double valuePlusOne = 1+value;
            if (valuePlusOne == 1.0) {
                return value;
            } else {
                return FastMath.log(valuePlusOne)*(value/(valuePlusOne-1.0));
            }
        }
        if (value > -1.0) {
            if (value == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }

            // ln'(x) = 1/x
            // so
            // log(x+epsilon) ~= log(x) + epsilon/x
            // 
            // Let u be 1+value rounded:
            // 1+value = u+epsilon
            //
            // log(1+value)
            // = log(u+epsilon)
            // ~= log(u) + epsilon/value
            // We compute log(u) as done in log(double), and then add the corrective term.

            double valuePlusOne = 1.0+value;
            if (valuePlusOne == 1.0) {
                return value;
            } else if (Math.abs(value) < 0.15) {
                double z = value/(value+2.0);
                double z2 = z*z;
                return z*(2+z2*((2.0/3)+z2*((2.0/5)+z2*((2.0/7)+z2*((2.0/9)+z2*((2.0/11)))))));
            }

            int valuePlusOneBitsHi = (int)(Double.doubleToRawLongBits(valuePlusOne)>>32) & 0x7FFFFFFF;
            int valuePlusOneExp = (valuePlusOneBitsHi>>20)-MAX_DOUBLE_EXPONENT;
            // Getting the first LOG_BITS bits of the mantissa.
            int xIndex = ((valuePlusOneBitsHi<<12)>>>(32-LOG_BITS));

            // 1.mantissa/1.mantissaApprox - 1
            double z = (valuePlusOne * twoPowTab[-valuePlusOneExp-MIN_DOUBLE_EXPONENT]) * logXInvTab[xIndex] - 1;

            z *= (1-z*((1.0/2)-z*(1.0/3)));

            // Adding epsilon/valuePlusOne to z,
            // with
            // epsilon = value - (valuePlusOne-1)
            // (valuePlusOne + epsilon ~= 1+value (not rounded))

            return valuePlusOneExp * LOG_2 + logXLogTab[xIndex] + (z + (value - (valuePlusOne-1))/valuePlusOne);
        } else if (value == -1.0) {
            return Double.NEGATIVE_INFINITY;
        } else { // value < -1.0, or value is NaN
            return Double.NaN;
        }
    }

    /**
     * @param value An integer value in [1,Integer.MAX_VALUE].
     * @return The integer part of the logarithm, in base 2, of the specified value,
     *         i.e. a result in [0,30]
     * @throws IllegalArgumentException if the specified value is <= 0.
     */
    public static int log2(int value) {
        return NumbersUtils.log2(value);
    }

    /**
     * @param value An integer value in [1,Long.MAX_VALUE].
     * @return The integer part of the logarithm, in base 2, of the specified value,
     *         i.e. a result in [0,62]
     * @throws IllegalArgumentException if the specified value is <= 0.
     */
    public static int log2(long value) {
        return NumbersUtils.log2(value);
    }

    /**
     * 1e-13ish accuracy (or better) on whole double range.
     * 
     * @param value A double value.
     * @param power A power.
     * @return value^power.
     */
    public static double pow(double value, double power) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.pow(value,power) : Math.pow(value,power);
        }
        if (power == 0.0) {
            return 1.0;
        } else if (power == 1.0) {
            return value;
        }
        if (value <= 0.0) {
            // powerInfo: 0 if not integer, 1 if even integer, -1 if odd integer
            int powerInfo;
            if (Math.abs(power) >= (TWO_POW_52*2)) {
                // The binary digit just before comma is outside mantissa,
                // thus it is always 0: power is an even integer.
                powerInfo = 1;
            } else {
                // If power's magnitude permits, we cast into int instead of into long,
                // as it is faster.
                if (Math.abs(power) <= (double)Integer.MAX_VALUE) {
                    int powerAsInt = (int)power;
                    if (power == (double)powerAsInt) {
                        powerInfo = ((powerAsInt & 1) == 0) ? 1 : -1;
                    } else { // power is not an integer (and not NaN, due to test against Integer.MAX_VALUE)
                        powerInfo = 0;
                    }
                } else {
                    long powerAsLong = (long)power;
                    if (power == (double)powerAsLong) {
                        powerInfo = ((powerAsLong & 1) == 0) ? 1 : -1;
                    } else { // power is not an integer, or is NaN
                        if (power != power) {
                            return Double.NaN;
                        }
                        powerInfo = 0;
                    }
                }
            }

            if (value == 0.0) {
                if (power < 0.0) {
                    return (powerInfo < 0) ? 1/value : Double.POSITIVE_INFINITY;
                } else { // power > 0.0 (0 and NaN cases already treated)
                    return (powerInfo < 0) ? value : 0.0;
                }
            } else { // value < 0.0
                if (value == Double.NEGATIVE_INFINITY) {
                    if (powerInfo < 0) { // power odd integer
                        return (power < 0.0) ? -0.0 : Double.NEGATIVE_INFINITY;
                    } else { // power even integer, or not an integer
                        return (power < 0.0) ? 0.0 : Double.POSITIVE_INFINITY;
                    }
                } else {
                    return (powerInfo != 0) ? powerInfo * FastMath.exp(power*FastMath.log(-value)) : Double.NaN;
                }
            }
        } else { // value > 0.0, or value is NaN
            return FastMath.exp(power*FastMath.log(value));
        }
    }

    /**
     * Quick pow, with a max relative error of about 3.5e-2
     * for |a^b| < 1e10, of about 0.17 for |a^b| < 1e50,
     * and worse accuracy above.
     * 
     * @param value A double value, in ]0,+infinity[ (strictly positive and finite).
     * @param power A double value.
     * @return value^power.
     */
    public static double powQuick(double value, double power) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.pow(value,power) : Math.pow(value,power);
        }
        return FastMath.exp(power*FastMath.logQuick(value));
    }

    /**
     * This treatment is somehow accurate for low values of |power|,
     * and for |power*getExponent(value)| < 1023 or so (to stay away
     * from double extreme magnitudes (large and small)).
     * 
     * @param value A double value.
     * @param power A power.
     * @return value^power.
     */
    public static double powFast(double value, int power) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.pow(value,power) : Math.pow(value,power);
        }
        if (power > 5) { // Most common case first.
            double oddRemains = 1.0;
            do {
                // Test if power is odd.
                if ((power & 1) != 0) {
                    oddRemains *= value;
                }
                value *= value;
                power >>= 1; // power = power / 2
            } while (power > 5);
            // Here, power is in [3,5]: faster to finish outside the loop.
            if (power == 3) {
                return oddRemains * value * value * value;
            } else {
                double v2 = value * value;
                if (power == 4) {
                    return oddRemains * v2 * v2;
                } else { // power == 5
                    return oddRemains * v2 * v2 * value;
                }
            }
        } else if (power >= 0) { // power in [0,5]
            if (power < 3) { // power in [0,2]
                if (power == 2) { // Most common case first.
                    return value * value;
                } else if (power != 0) { // faster than == 1
                    return value;
                } else { // power == 0
                    return 1.0;
                }
            } else { // power in [3,5]
                if (power == 3) {
                    return value * value * value;
                } else { // power in [4,5]
                    double v2 = value * value;
                    if (power == 4) {
                        return v2 * v2;
                    } else { // power == 5
                        return v2 * v2 * value;
                    }
                }
            }
        } else { // power < 0
            // Opposite of Integer.MIN_VALUE does not exist as int.
            if (power == Integer.MIN_VALUE) {
                // Integer.MAX_VALUE = -(power+1)
                return 1.0/(FastMath.powFast(value,Integer.MAX_VALUE) * value);
            } else {
                return 1.0/FastMath.powFast(value,-power);
            }
        }
    }

    /**
     * Returns the exact result, provided it's in double range.
     * 
     * @param power A power.
     * @return 2^power.
     */
    public static double twoPow(int power) {
        /*
         * Using table, to go faster than NumbersUtils.twoPow(int).
         */
        if (power >= 0) {
            if (power <= MAX_DOUBLE_EXPONENT) {
                return twoPowTab[power-MIN_DOUBLE_EXPONENT];
            } else {
                // Overflow.
                return Double.POSITIVE_INFINITY;
            }
        } else {
            if (power >= MIN_DOUBLE_EXPONENT) {
                return twoPowTab[power-MIN_DOUBLE_EXPONENT];
            } else {
                // Underflow.
                return 0.0;
            }
        }
    }

    /**
     * @param value An int value.
     * @return value*value.
     */
    public static int pow2(int value) {
        return NumbersUtils.pow2(value);
    }

    /**
     * @param value A long value.
     * @return value*value.
     */
    public static long pow2(long value) {
        return NumbersUtils.pow2(value);
    }

    /**
     * @param value A float value.
     * @return value*value.
     */
    public static float pow2(float value) {
        return NumbersUtils.pow2(value);
    }

    /**
     * @param value A double value.
     * @return value*value.
     */
    public static double pow2(double value) {
        return NumbersUtils.pow2(value);
    }

    /**
     * @param value An int value.
     * @return value*value*value.
     */
    public static int pow3(int value) {
        return NumbersUtils.pow3(value);
    }

    /**
     * @param value A long value.
     * @return value*value*value.
     */
    public static long pow3(long value) {
        return NumbersUtils.pow3(value);
    }

    /**
     * @param value A float value.
     * @return value*value*value.
     */
    public static float pow3(float value) {
        return NumbersUtils.pow3(value);
    }

    /**
     * @param value A double value.
     * @return value*value*value.
     */
    public static double pow3(double value) {
        return NumbersUtils.pow3(value);
    }

    /**
     * @param value A double value.
     * @return Value square root.
     */
    public static double sqrt(double value) {
        if (USE_JDK_MATH || (!USE_REDEFINED_SQRT)) {
            return STRICT_MATH ? StrictMath.sqrt(value) : Math.sqrt(value);
        } else {
            // See cbrt for comments, sqrt uses the same ideas.

            if (!(value > 0.0)) { // value <= 0.0, or value is NaN
                return (value == 0.0) ? value : Double.NaN;
            } else if (value == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }

            double h;
            if (value < MIN_DOUBLE_NORMAL) {
                value *= TWO_POW_52;
                h = 2*TWO_POW_N26;
            } else {
                h = 2.0;
            }

            int valueBitsHi = (int)(Double.doubleToRawLongBits(value)>>32);
            int valueExponentIndex = (valueBitsHi>>20)+(-MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT);
            int xIndex = ((valueBitsHi<<12)>>>(32-SQRT_LO_BITS));

            double result = sqrtXSqrtHiTab[valueExponentIndex] * sqrtXSqrtLoTab[xIndex];
            double slope = sqrtSlopeHiTab[valueExponentIndex] * sqrtSlopeLoTab[xIndex];
            value *= 0.25;

            result += (value - result * result) * slope;
            result += (value - result * result) * slope;
            return h*(result + (value - result * result) * slope);
        }
    }

    /**
     * @param value A double value.
     * @return Value cubic root.
     */
    public static double cbrt(double value) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.cbrt(value) : Math.cbrt(value);
        }
        double h;
        if (value < 0.0) {
            if (value == Double.NEGATIVE_INFINITY) {
                return Double.NEGATIVE_INFINITY;
            }
            value = -value;
            // Making sure value is normal.
            if (value < MIN_DOUBLE_NORMAL) {
                value *= (TWO_POW_52*TWO_POW_26);
                // h = <result_sign> * <result_multiplicator_to_avoid_overflow> / <cbrt(value_multiplicator_to_avoid_subnormal)>
                h = -2*TWO_POW_N26;
            } else {
                h = -2.0;
            }
        } else {
            if (!(value < Double.POSITIVE_INFINITY)) { // value is +infinity, or value is NaN
                return value;
            }
            // Making sure value is normal.
            if (value < MIN_DOUBLE_NORMAL) {
                if (value == 0.0) {
                    // cbrt(0.0) = 0.0, cbrt(-0.0) = -0.0
                    return value;
                }
                value *= (TWO_POW_52*TWO_POW_26);
                h = 2*TWO_POW_N26;
            } else {
                h = 2.0;
            }
        }

        // Normal value is (2^<value exponent> * <a value in [1,2[>).
        // First member cubic root is computed, and multiplied with an approximation
        // of the cubic root of the second member, to end up with a good guess of
        // the result before using Newton's (or Archimedes's) method.
        // To compute the cubic root approximation, we use the formula "cbrt(value) = cbrt(x) * cbrt(value/x)",
        // choosing x as close to value as possible but inferior to it, so that cbrt(value/x) is close to 1
        // (we could iterate on this method, using value/x as new value for each iteration,
        // but finishing with Newton's method is faster).

        // Shift and cast into an int, which overall is faster than working with a long.
        int valueBitsHi = (int)(Double.doubleToRawLongBits(value)>>32);
        int valueExponentIndex = (valueBitsHi>>20)+(-MAX_DOUBLE_EXPONENT-MIN_DOUBLE_EXPONENT);
        // Getting the first CBRT_LO_BITS bits of the mantissa.
        int xIndex = ((valueBitsHi<<12)>>>(32-CBRT_LO_BITS));
        double result = cbrtXCbrtHiTab[valueExponentIndex] * cbrtXCbrtLoTab[xIndex];
        double slope = cbrtSlopeHiTab[valueExponentIndex] * cbrtSlopeLoTab[xIndex];

        // Lowering values to avoid overflows when using Newton's method
        // (we will then just have to return twice the result).
        // result^3 = value
        // (result/2)^3 = value/8
        value *= 0.125;
        // No need to divide result here, as division is factorized in result computation tables.
        // result *= 0.5;

        // Newton's method, looking for y = x^(1/p):
        // y(n) = y(n-1) + (x-y(n-1)^p) * slope(y(n-1))
        // y(n) = y(n-1) + (x-y(n-1)^p) * (1/p)*(x(n-1)^(1/p-1))
        // y(n) = y(n-1) + (x-y(n-1)^p) * (1/p)*(x(n-1)^((1-p)/p))
        // with x(n-1)=y(n-1)^p, i.e.:
        // y(n) = y(n-1) + (x-y(n-1)^p) * (1/p)*(y(n-1)^(1-p))
        //
        // For p=3:
        // y(n) = y(n-1) + (x-y(n-1)^3) * (1/(3*y(n-1)^2))

        // To save time, we don't recompute the slope between Newton's method steps,
        // as initial slope is good enough for a few iterations.
        //
        // NB: slope = 1/(3*trueResult*trueResult)
        //     As we have result = trueResult/2 (to avoid overflows), we have:
        //     slope = 4/(3*result*result)
        //           = (4/3)*resultInv*resultInv
        //     with newResultInv = 1/newResult
        //                       = 1/(oldResult+resultDelta)
        //                       = (oldResultInv)*1/(1+resultDelta/oldResult)
        //                       = (oldResultInv)*1/(1+resultDelta*oldResultInv)
        //                      ~= (oldResultInv)*(1-resultDelta*oldResultInv)
        //     ===> Successive slopes could be computed without division, if needed,
        //          by computing resultInv (instead of slope right away) and retrieving
        //          slopes from it.

        result += (value - result * result * result) * slope;
        result += (value - result * result * result) * slope;
        return h*(result + (value - result * result * result) * slope);
    }

    /**
     * Returns dividend - divisor * n, where n is the mathematical integer
     * closest to dividend/divisor.
     * If dividend/divisor is equally close to surrounding integers,
     * we choose n to be the integer of smallest magnitude, which makes
     * this treatment differ from Math.IEEEremainder(double,double),
     * where n is chosen to be the even integer.
     * Note that the choice of n is not done considering the double
     * approximation of dividend/divisor, because it could cause
     * result to be outside [-|divisor|/2,|divisor|/2] range.
     * The practical effect is that if multiple results would be possible,
     * we always choose the result that is the closest to (and has the same
     * sign as) the dividend.
     * Ex. :
     * - for (-3.0,2.0), this method returns -1.0,
     *   whereas Math.IEEEremainder returns 1.0.
     * - for (-5.0,2.0), both this method and Math.IEEEremainder return -1.0.
     * 
     * If the remainder is zero, its sign is the same as the sign of the first argument.
     * If either argument is NaN, or the first argument is infinite,
     * or the second argument is positive zero or negative zero,
     * then the result is NaN.
     * If the first argument is finite and the second argument is
     * infinite, then the result is the same as the first argument.
     * 
     * NB:
     * - Modulo operator (%) returns a value in ]-|divisor|,|divisor|[,
     *   which sign is the same as dividend.
     * - As for modulo operator, the sign of the divisor has no effect on the result.
     * 
     * @param dividend Dividend.
     * @param divisor Divisor.
     * @return Remainder of dividend/divisor, i.e. a value in [-|divisor|/2,|divisor|/2].
     */
    public static double remainder(double dividend, double divisor) {
        if (USE_JDK_MATH) {
            // no Math equivalent (differs from IEEEremainder(double,double))
        }
        if (Double.isInfinite(divisor)) {
            if (Double.isInfinite(dividend)) {
                return Double.NaN;
            } else {
                return dividend;
            }
        }
        double value = dividend % divisor;
        if (Math.abs(value+value) > Math.abs(divisor)) {
            return value + ((value > 0.0) ? -Math.abs(divisor) : Math.abs(divisor));
        } else {
            return value;
        }
    }

    /**
     * @param angle Angle in radians.
     * @return The same angle, in radians, but in [-Math.PI,Math.PI].
     */
    public static double normalizeMinusPiPi(double angle) {
        // Not modifying values in output range.
        if ((angle >= -Math.PI) && (angle <= Math.PI)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPi(angle);
        if (angleMinusPiPiOrSo < -Math.PI) {
            return -Math.PI;
        } else if (angleMinusPiPiOrSo > Math.PI) {
            return Math.PI;
        } else {
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * Not accurate for large values.
     * 
     * @param angle Angle in radians.
     * @return The same angle, in radians, but in [-Math.PI,Math.PI].
     */
    public static double normalizeMinusPiPiFast(double angle) {
        // Not modifying values in output range.
        if ((angle >= -Math.PI) && (angle <= Math.PI)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPiFast(angle);
        if (angleMinusPiPiOrSo < -Math.PI) {
            return -Math.PI;
        } else if (angleMinusPiPiOrSo > Math.PI) {
            return Math.PI;
        } else {
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * @param angle Angle in radians.
     * @return The same angle, in radians, but in [0,2*Math.PI].
     */
    public static double normalizeZeroTwoPi(double angle) {
        // Not modifying values in output range.
        if ((angle >= 0.0) && (angle <= 2*Math.PI)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPi(angle);
        if (angleMinusPiPiOrSo < 0.0) {
            // Not a problem if angle is slightly < -Math.PI,
            // since result ends up around PI, which is not near output range borders.
            return angleMinusPiPiOrSo + 2*Math.PI;
        } else {
            // Not a problem if angle is slightly > Math.PI,
            // since result ends up around PI, which is not near output range borders.
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * Not accurate for large values.
     * 
     * @param angle Angle in radians.
     * @return The same angle, in radians, but in [0,2*Math.PI].
     */
    public static double normalizeZeroTwoPiFast(double angle) {
        // Not modifying values in output range.
        if ((angle >= 0.0) && (angle <= 2*Math.PI)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPiFast(angle);
        if (angleMinusPiPiOrSo < 0.0) {
            // Not a problem if angle is slightly < -Math.PI,
            // since result ends up around PI, which is not near output range borders.
            return angleMinusPiPiOrSo + 2*Math.PI;
        } else {
            // Not a problem if angle is slightly > Math.PI,
            // since result ends up around PI, which is not near output range borders.
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * @param angle Angle in radians.
     * @return Angle value modulo PI, in radians, in [-Math.PI/2,Math.PI/2].
     */
    public static double normalizeMinusHalfPiHalfPi(double angle) {
        // Not modifying values in output range.
        if ((angle >= -Math.PI/2) && (angle <= Math.PI/2)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPi(angle);
        if (angleMinusPiPiOrSo < -Math.PI/2) {
            // Not a problem if angle is slightly < -Math.PI,
            // since result ends up around zero, which is not near output range borders.
            return angleMinusPiPiOrSo + Math.PI;
        } else if (angleMinusPiPiOrSo > Math.PI/2) {
            // Not a problem if angle is slightly > Math.PI,
            // since result ends up around zero, which is not near output range borders.
            return angleMinusPiPiOrSo - Math.PI;
        } else {
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * Not accurate for large values.
     * 
     * @param angle Angle in radians.
     * @return Angle value modulo PI, in radians, in [-Math.PI/2,Math.PI/2].
     */
    public static double normalizeMinusHalfPiHalfPiFast(double angle) {
        // Not modifying values in output range.
        if ((angle >= -Math.PI/2) && (angle <= Math.PI/2)) {
            return angle;
        }
        double angleMinusPiPiOrSo = remainderTwoPiFast(angle);
        if (angleMinusPiPiOrSo < -Math.PI/2) {
            // Not a problem if angle is slightly < -Math.PI,
            // since result ends up around zero, which is not near output range borders.
            return angleMinusPiPiOrSo + Math.PI;
        } else if (angleMinusPiPiOrSo > Math.PI/2) {
            // Not a problem if angle is slightly > Math.PI,
            // since result ends up around zero, which is not near output range borders.
            return angleMinusPiPiOrSo - Math.PI;
        } else {
            return angleMinusPiPiOrSo;
        }
    }

    /**
     * Returns sqrt(x^2+y^2) without intermediate overflow or underflow.
     */
    public static double hypot(double x, double y) {
        if (USE_JDK_MATH) {
            return STRICT_MATH ? StrictMath.hypot(x,y) : Math.hypot(x,y);
        }
        x = Math.abs(x);
        y = Math.abs(y);
        if (y < x) {
            double a = x;
            x = y;
            y = a;
        } else if (!(y >= x)) { // Testing if we have some NaN.
            if ((x == Double.POSITIVE_INFINITY) || (y == Double.POSITIVE_INFINITY)) {
                return Double.POSITIVE_INFINITY;
            } else {
                return Double.NaN;
            }
        }
        if (y-x == y) { // x too small to substract from y
            return y;
        } else {
            double factor;
            if (x > TWO_POW_450) { // 2^450 < x < y
                x *= TWO_POW_N750;
                y *= TWO_POW_N750;
                factor = TWO_POW_750;
            } else if (y < TWO_POW_N450) { // x < y < 2^-450
                x *= TWO_POW_750;
                y *= TWO_POW_750;
                factor = TWO_POW_N750;
            } else {
                factor = 1.0;
            }
            return factor * FastMath.sqrt(x*x+y*y);
        }
    }

    /**
     * @param value A float value.
     * @return Ceiling of value.
     */
    public static float ceil(float value) {
        if (USE_JDK_MATH) {
            // TODO use Math.ceil(float) if exists
            return (float)Math.ceil((double)value);
        }
        return -FastMath.floor(-value);
    }

    /**
     * Supposed to behave like Math.ceil(double), for safe interchangeability.
     * 
     * @param value A double value.
     * @return Ceiling of value.
     */
    public static double ceil(double value) {
        if (USE_JDK_MATH) {
            return Math.ceil(value);
        }
        return -FastMath.floor(-value);
    }

    /**
     * @param value A float value.
     * @return Floor of value.
     */
    public static float floor(float value) {
        if (USE_JDK_MATH) {
            // TODO use Math.floor(float) if exists
            return (float)Math.floor((double)value);
        }
        int exp = FastMath.getExponent(value);
        if (exp < 0) {
            if (value < 0.0f) {
                return -1.0f;
            } else { // value in [0.0f,1.0f[
                return 0.0f * value; // 0.0f, or -0.0f if value is -0.0f
            }
        } else {
            if (exp < 24) {
                int valueBits = Float.floatToRawIntBits(value);
                int anteCommaDigits = valueBits & (0xFF800000>>exp);
                if ((value < 0.0f) && (anteCommaDigits != valueBits)) {
                    return Float.intBitsToFloat(anteCommaDigits) - 1.0f;
                } else {
                    return Float.intBitsToFloat(anteCommaDigits);
                }
            } else {
                return value;
            }
        }
    }

    /**
     * Supposed to behave like Math.floor(double), for safe interchangeability.
     * 
     * @param value A double value.
     * @return Floor of value.
     */
    public static double floor(double value) {
        if (USE_JDK_MATH) {
            return Math.floor(value);
        }
        // Faster than to work directly on bits.
        if (Math.abs(value) <= (double)Integer.MAX_VALUE) {
            if (value > 0.0) {
                return (double)(int)value;
            } else if (value < 0.0) {
                double anteCommaDigits = (double)(int)value;
                if (value != anteCommaDigits) {
                    return anteCommaDigits - 1.0;
                } else {
                    return anteCommaDigits;
                }
            } else { // value is +-0.0 (not NaN due to test against Integer.MAX_VALUE)
                return value;
            }
        } else if (Math.abs(value) < TWO_POW_52) {
            // We split the value in two:
            // high part, which is a mathematical integer,
            // and the rest, for which we can get rid of the
            // post comma digits by casting into an int.
            double highPart = ((int)(value * TWO_POW_N26)) * TWO_POW_26;
            if (value > 0.0) {
                return highPart + (double)((int)(value - highPart));
            } else {
                double anteCommaDigits = highPart + (double)((int)(value - highPart));
                if (value != anteCommaDigits) {
                    return anteCommaDigits - 1.0;
                } else {
                    return anteCommaDigits;
                }
            }
        } else { // abs(value) >= 2^52, or value is NaN
            return value;
        }
    }

    /**
     * Supposed to behave like Math.round(float), for safe interchangeability.
     * 
     * @param value A double value.
     * @return Value rounded to nearest int.
     */
    public static int round(float value) {
        if (USE_JDK_MATH) {
            return Math.round(value);
        }
        // "return (int)FastMath.floor((float)(value+0.5));" would be more accurate for values in [8388609.0f,16777216.0f]
        // (i.e. [0x800001,0x1000000]), but would not give same results than Math.round(float).
        return (int)FastMath.floor(value+0.5f);
    }

    /**
     * Supposed to behave like Math.round(double), for safe interchangeability.
     * 
     * @param value A double value.
     * @return Value rounded to nearest long.
     */
    public static long round(double value) {
        if (USE_JDK_MATH) {
            return Math.round(value);
        }
        // Would be more coherent with rint, to call rint(double) instead of
        // floor(double), but that would not give same results than Math.round(double).
        double roundedValue = FastMath.floor(value+0.5);
        if (Math.abs(roundedValue) <= (double)Integer.MAX_VALUE) {
            // Faster with intermediary cast in int.
            return (long)(int)roundedValue;
        } else {
            return (long)roundedValue;
        }
    }

    /**
     * @param value A float value.
     * @return Value unbiased exponent.
     */
    public static int getExponent(float value) {
        if (USE_JDK_MATH) {
            return Math.getExponent(value);
        }
        return ((Float.floatToRawIntBits(value)>>23)&0xFF)-MAX_FLOAT_EXPONENT;
    }

    /**
     * @param value A double value.
     * @return Value unbiased exponent.
     */
    public static int getExponent(double value) {
        if (USE_JDK_MATH) {
            return Math.getExponent(value);
        }
        return (((int)(Double.doubleToRawLongBits(value)>>52))&0x7FF)-MAX_DOUBLE_EXPONENT;
    }

    /**
     * Gives same result as Math.toDegrees for some particular values
     * like Math.PI/2, Math.PI or 2*Math.PI, but is faster (no division).
     * 
     * @param angrad Angle value in radians.
     * @return Angle value in degrees.
     */
    public static double toDegrees(double angrad) {
        if (USE_JDK_MATH) {
            return Math.toDegrees(angrad);
        }
        return angrad * (180/Math.PI);
    }

    /**
     * Gives same result as Math.toRadians for some particular values
     * like 90.0, 180.0 or 360.0, but is faster (no division).
     * 
     * @param angdeg Angle value in degrees.
     * @return Angle value in radians.
     */
    public static double toRadians(double angdeg) {
        if (USE_JDK_MATH) {
            return Math.toRadians(angdeg);
        }
        return angdeg * (Math.PI/180);
    }

    /**
     * @param sign Sign of the angle: true for positive, false for negative.
     * @param degrees Degrees, in [0,180].
     * @param minutes Minutes, in [0,59].
     * @param seconds Seconds, in [0.0,60.0[.
     * @return Angle in radians.
     */
    public static double toRadians(boolean sign, int degrees, int minutes, double seconds) {
        return FastMath.toRadians(FastMath.toDegrees(sign, degrees, minutes, seconds));
    }

    /**
     * @param sign Sign of the angle: true for positive, false for negative.
     * @param degrees Degrees, in [0,180].
     * @param minutes Minutes, in [0,59].
     * @param seconds Seconds, in [0.0,60.0[.
     * @return Angle in degrees.
     */
    public static double toDegrees(boolean sign, int degrees, int minutes, double seconds) {
        double signFactor = sign ? 1.0 : -1.0;
        return signFactor * (degrees + (1.0/60)*(minutes + (1.0/60)*seconds));
    }

    /**
     * @param angrad Angle in radians.
     * @param degrees (out) Degrees, in [0,180].
     * @param minutes (out) Minutes, in [0,59].
     * @param seconds (out) Seconds, in [0.0,60.0[.
     * @return True if the resulting angle in [-180deg,180deg] is positive, false if it is negative.
     */
    public static boolean toDMS(double angrad, IntWrapper degrees, IntWrapper minutes, DoubleWrapper seconds) {
        // Computing longitude DMS.
        double tmp = FastMath.toDegrees(FastMath.normalizeMinusPiPi(angrad));
        boolean isNeg = (tmp < 0.0);
        if (isNeg) {
            tmp = -tmp;
        }
        degrees.value = (int)tmp;
        tmp = (tmp-degrees.value)*60.0;
        minutes.value = (int)tmp;
        seconds.value = Math.min((tmp-minutes.value)*60.0,DOUBLE_BEFORE_60);
        return !isNeg;
    }

    /**
     * @param value An int value.
     * @return The absolute value, except if value is Integer.MIN_VALUE, for which it returns Integer.MIN_VALUE.
     */
    public static int abs(int value) {
        if (USE_JDK_MATH) {
            return Math.abs(value);
        }
        return NumbersUtils.abs(value);
    }

    /**
     * @param value A long value.
     * @return The specified value as int.
     * @throws ArithmeticException if the specified value is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int toIntExact(long value) {
        return NumbersUtils.asInt(value);
    }

    /**
     * @param value A long value.
     * @return The closest int value in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int toInt(long value) {
        return NumbersUtils.toInt(value);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a+b.
     * @throws ArithmeticException if the mathematical result of a+b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int addExact(int a, int b) {
        return NumbersUtils.plusExact(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a+b.
     * @throws ArithmeticException if the mathematical result of a+b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long addExact(long a, long b) {
        return NumbersUtils.plusExact(a, b);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a+b.
     */
    public static int addBounded(int a, int b) {
        return NumbersUtils.plusBounded(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a+b.
     */
    public static long addBounded(long a, long b) {
        return NumbersUtils.plusBounded(a, b);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a-b.
     * @throws ArithmeticException if the mathematical result of a-b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int subtractExact(int a, int b) {
        return NumbersUtils.minusExact(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a-b.
     * @throws ArithmeticException if the mathematical result of a-b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long subtractExact(long a, long b) {
        return NumbersUtils.minusExact(a, b);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a-b.
     */
    public static int subtractBounded(int a, int b) {
        return NumbersUtils.minusBounded(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a-b.
     */
    public static long subtractBounded(long a, long b) {
        return NumbersUtils.minusBounded(a, b);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a*b.
     * @throws ArithmeticException if the mathematical result of a*b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int multiplyExact(int a, int b) {
        return NumbersUtils.timesExact(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a*b.
     * @throws ArithmeticException if the mathematical result of a*b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long multiplyExact(long a, long b) {
        return NumbersUtils.timesExact(a, b);
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a*b.
     */
    public static int multiplyBounded(int a, int b) {
        return NumbersUtils.timesBounded(a, b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a*b.
     */
    public static long multiplyBounded(long a, long b) {
        return NumbersUtils.timesBounded(a, b);
    }

    /**
     * @param minValue An int value.
     * @param maxValue An int value.
     * @param value An int value.
     * @return minValue if value < minValue, maxValue if value > maxValue, value otherwise.
     */
    public static int toRange(int minValue, int maxValue, int value) {
        return NumbersUtils.toRange(minValue, maxValue, value);
    }

    /**
     * @param minValue A long value.
     * @param maxValue A long value.
     * @param value A long value.
     * @return minValue if value < minValue, maxValue if value > maxValue, value otherwise.
     */
    public static long toRange(long minValue, long maxValue, long value) {
        return NumbersUtils.toRange(minValue, maxValue, value);
    }

    /**
     * @param minValue A float value.
     * @param maxValue A float value.
     * @param value A float value.
     * @return minValue if value < minValue, maxValue if value > maxValue, value otherwise.
     */
    public static float toRange(float minValue, float maxValue, float value) {
        return NumbersUtils.toRange(minValue, maxValue, value);
    }

    /**
     * @param minValue A double value.
     * @param maxValue A double value.
     * @param value A double value.
     * @return minValue if value < minValue, maxValue if value > maxValue, value otherwise.
     */
    public static double toRange(double minValue, double maxValue, double value) {
        return NumbersUtils.toRange(minValue, maxValue, value);
    }

    /**
     * NB: Since 2*Math.PI < 2*PI, a span of 2*Math.PI does not mean full angular range.
     * ex.: isInClockwiseDomain(0.0, 2*Math.PI, -1e-20) returns false.
     * ---> For full angular range, use a span > 2*Math.PI, like 2*PI_SUP constant of this class.
     * 
     * @param startAngRad An angle, in radians.
     * @param angSpanRad An angular span, >= 0.0, in radians.
     * @param angRad An angle, in radians.
     * @return True if angRad is in the clockwise angular domain going from startAngRad, over angSpanRad,
     *         extremities included, false otherwise.
     */
    public static boolean isInClockwiseDomain(double startAngRad, double angSpanRad, double angRad) {
        if (Math.abs(angRad) < -TWO_MATH_PI_IN_MINUS_PI_PI) {
            // special case for angular values of small magnitude
            if (angSpanRad < 0.0) {
                // empty domain
                return false;
            } else if (angSpanRad <= 2*Math.PI) { // angSpanRad is in [0.0,2*Math.PI]
                startAngRad = FastMath.normalizeMinusPiPi(startAngRad);
                double endAngRad = FastMath.normalizeMinusPiPi(startAngRad + angSpanRad);
                //
                if (startAngRad <= endAngRad) {
                    return (angRad >= startAngRad) && (angRad <= endAngRad);
                } else {
                    return (angRad >= startAngRad) || (angRad <= endAngRad);
                }
            } else if (angSpanRad != angSpanRad) { // angSpanRad is NaN
                return false;
            } else { // angSpanRad > 2*Math.PI
                // we know angRad is not NaN, due to a previous test
                return true;
            }
        } else {
            // general case
            return (FastMath.normalizeZeroTwoPi(angRad - startAngRad) <= angSpanRad);
        }
    }

    public static boolean isNaNOrInfinite(float value) {
        return NumbersUtils.isNaNOrInfinite(value);
    }

    public static boolean isNaNOrInfinite(double value) {
        return NumbersUtils.isNaNOrInfinite(value);
    }

    /*
     * 
     * Not-redefined java.lang.Math public values and treatments, for quick replacement of Math with FastMath.
     * 
     */

    public static final double E = Math.E;
    public static final double PI = Math.PI;
    public static double abs(double a) {
        return Math.abs(a);
    }
    public static float abs(float a) {
        return Math.abs(a);
    }
    public static long abs(long a) {
        return Math.abs(a);
    }
    public static double copySign(double magnitude, double sign) {
        return Math.copySign(magnitude,sign);
    }
    public static float copySign(float magnitude, float sign) {
        return Math.copySign(magnitude,sign);
    }
    public static double IEEEremainder(double f1, double f2) {
        return Math.IEEEremainder(f1,f2);
    }
    public static double max(double a, double b) {
        return Math.max(a,b);
    }
    public static float max(float a, float b) {
        return Math.max(a,b);
    }
    public static int max(int a, int b) {
        return Math.max(a,b);
    }
    public static long max(long a, long b) {
        return Math.max(a,b);
    }
    public static double min(double a, double b) {
        return Math.min(a,b);
    }
    public static float min(float a, float b) {
        return Math.min(a,b);
    }
    public static int min(int a, int b) {
        return Math.min(a,b);
    }
    public static long min(long a, long b) {
        return Math.min(a,b);
    }
    public static double nextAfter(double start, double direction) {
        return Math.nextAfter(start,direction);
    }
    public static float nextAfter(float start, float direction) {
        return Math.nextAfter(start,direction);
    }
    public static double nextUp(double d) {
        return Math.nextUp(d);
    }
    public static float nextUp(float f) {
        return Math.nextUp(f);
    }
    public static double random() {
        // StrictMath and Math use different RNG instances,
        // so their random() methods are not equivalent.
        return STRICT_MATH ? StrictMath.random() : Math.random();
    }
    public static double rint(double a) {
        return Math.rint(a);
    }
    public static double scalb(double d, int scaleFactor) {
        return Math.scalb(d,scaleFactor);
    }
    public static float scalb(float f, int scaleFactor) {
        return Math.scalb(f,scaleFactor);
    }
    public static double signum(double d) {
        return Math.signum(d);
    }
    public static float signum(float f) {
        return Math.signum(f);
    }
    public static double ulp(double d) {
        return Math.ulp(d);
    }
    public static float ulp(float f) {
        return Math.ulp(f);
    }

    //--------------------------------------------------------------------------
    //  PRIVATE TREATMENTS
    //--------------------------------------------------------------------------

    /**
     * FastMath is non-instantiable.
     */
    private FastMath() {
    }

    /**
     * Use look-up tables size power through this method,
     * to make sure is it small in case java.lang.Math
     * is directly used.
     */
    private static int getTabSizePower(int tabSizePower) {
        return USE_JDK_MATH ? Math.min(2, tabSizePower) : tabSizePower;
    }

    /**
     * Remainder using an accurate definition of PI.
     * Derived from a fdlibm treatment called __ieee754_rem_pio2.
     * 
     * This method can return values slightly (like one ULP or so) outside [-Math.PI,Math.PI] range.
     * 
     * @param angle Angle in radians.
     * @return Remainder of (angle % (2*PI)), which is in [-PI,PI] range.
     */
    private static double remainderTwoPi(double angle) {
        if (USE_JDK_MATH) {
            double y = STRICT_MATH ? StrictMath.sin(angle) : Math.sin(angle);
            double x = STRICT_MATH ? StrictMath.cos(angle) : Math.cos(angle);
            return STRICT_MATH ? StrictMath.atan2(y,x) : Math.atan2(y,x);
        }
        boolean negateResult;
        if (angle < 0.0) {
            negateResult = true;
            angle = -angle;
        } else {
            negateResult = false;
        }
        if (angle <= NORMALIZE_ANGLE_MAX_MEDIUM_DOUBLE) {
            double fn = (double)(int)(angle*INVTWOPI+0.5);
            double result = (angle - fn*TWOPI_HI) - fn*TWOPI_LO;
            return negateResult ? -result : result;
        } else if (angle < Double.POSITIVE_INFINITY) {
            // Reworking exponent to have a value < 2^24.
            long lx = Double.doubleToRawLongBits(angle);
            long exp = ((lx>>52)&0x7FF) - 1046;
            double z = Double.longBitsToDouble(lx - (exp<<52));

            double x0 = (double)((int)z);
            z = (z-x0)*TWO_POW_24;
            double x1 = (double)((int)z);
            double x2 = (z-x1)*TWO_POW_24;

            double result = subRemainderTwoPi(x0, x1, x2, (int)exp, (x2 == 0) ? 2 : 3);
            return negateResult ? -result : result;
        } else { // angle is +infinity or NaN
            return Double.NaN;
        }
    }

    /** 
     * Not accurate for large values.
     * 
     * This method can return values slightly (like one ULP or so) outside [-Math.PI,Math.PI] range.
     * 
     * @param angle Angle in radians.
     * @return Remainder of (angle % (2*PI)), which is in [-PI,PI] range.
     */
    private static double remainderTwoPiFast(double angle) {
        if (USE_JDK_MATH) {
            return remainderTwoPi(angle);
        }
        boolean negateResult;
        if (angle < 0.0) {
            negateResult = true;
            angle = -angle;
        } else {
            negateResult = false;
        }
        // - We don't bother with values higher than (2*PI*(2^52)),
        //   since they are spaced by 2*PI or more from each other.
        // - For large values, we don't use % because it might be very slow,
        //   and we split computation in two, because cast from double to int
        //   with large numbers might be very slow also.
        if (angle <= TWO_POW_26*(2*Math.PI)) {
            double fn = (double)(int)(angle*INVTWOPI+0.5);
            double result = (angle - fn*TWOPI_HI) - fn*TWOPI_LO;
            return negateResult ? -result : result;
        } else if (angle <= TWO_POW_52*(2*Math.PI)) {
            // 1) Computing remainder of angle modulo TWO_POW_26*(2*PI).
            double fn = (double)(int)(angle*(INVTWOPI/TWO_POW_26)+0.5);
            double result = (angle - fn*(TWOPI_HI*TWO_POW_26)) - fn*(TWOPI_LO*TWO_POW_26);
            // Here, result is in [-TWO_POW_26*Math.PI,TWO_POW_26*Math.PI].
            if (result < 0.0) {
                result = -result;
                negateResult = !negateResult;
            }
            // 2) Computing remainder of angle modulo 2*PI.
            fn = (double)(int)(result*INVTWOPI+0.5);
            result = (result - fn*TWOPI_HI) - fn*TWOPI_LO;
            return negateResult ? -result : result;
        } else if (angle < Double.POSITIVE_INFINITY) {
            return 0.0;
        } else { // angle is +infinity or NaN
            return Double.NaN;
        }
    }

    /**
     * Remainder using an accurate definition of PI.
     * Derived from a fdlibm treatment called __kernel_rem_pio2.
     * 
     * @param x0 Most significant part of the value, as an integer < 2^24, in double precision format. Must be >= 0.
     * @param x1 Following significant part of the value, as an integer < 2^24, in double precision format.
     * @param x2 Least significant part of the value, as an integer < 2^24, in double precision format.
     * @param e0 Exponent of x0 (value is (2^e0)*(x0+(2^-24)*(x1+(2^-24)*x2))). Must be >= -20.
     * @param nx Number of significant parts to take into account. Must be 2 or 3.
     * @return Remainder of (value % (2*PI)), which is in [-PI,PI] range.
     */
    private static double subRemainderTwoPi(double x0, double x1, double x2, int e0, int nx) {
        int ih;
        double z,fw;
        double f0,f1,f2,f3,f4,f5,f6 = 0.0,f7;
        double q0,q1,q2,q3,q4,q5;
        int iq0,iq1,iq2,iq3,iq4;

        final int jx = nx - 1; // jx in [1,2] (nx in [2,3])
        // Could use a table to avoid division, but the gain isn't worth it most likely...
        final int jv = (e0-3)/24; // We do not handle the case (e0-3 < -23).
        int q = e0-((jv<<4)+(jv<<3))-24; // e0-24*(jv+1)

        final int j = jv + 4;
        if (jx == 1) {
            f5 = (j >= 0) ? ONE_OVER_TWOPI_TAB[j]: 0.0;
            f4 = (j >= 1) ? ONE_OVER_TWOPI_TAB[j-1]: 0.0;
            f3 = (j >= 2) ? ONE_OVER_TWOPI_TAB[j-2]: 0.0;
            f2 = (j >= 3) ? ONE_OVER_TWOPI_TAB[j-3]: 0.0;
            f1 = (j >= 4) ? ONE_OVER_TWOPI_TAB[j-4]: 0.0;
            f0 = (j >= 5) ? ONE_OVER_TWOPI_TAB[j-5]: 0.0;

            q0 = x0*f1 + x1*f0;
            q1 = x0*f2 + x1*f1;
            q2 = x0*f3 + x1*f2;
            q3 = x0*f4 + x1*f3;
            q4 = x0*f5 + x1*f4;
        } else { // jx == 2
            f6 = (j >= 0) ? ONE_OVER_TWOPI_TAB[j]: 0.0;
            f5 = (j >= 1) ? ONE_OVER_TWOPI_TAB[j-1]: 0.0;
            f4 = (j >= 2) ? ONE_OVER_TWOPI_TAB[j-2]: 0.0;
            f3 = (j >= 3) ? ONE_OVER_TWOPI_TAB[j-3]: 0.0;
            f2 = (j >= 4) ? ONE_OVER_TWOPI_TAB[j-4]: 0.0;
            f1 = (j >= 5) ? ONE_OVER_TWOPI_TAB[j-5]: 0.0;
            f0 = (j >= 6) ? ONE_OVER_TWOPI_TAB[j-6]: 0.0;

            q0 = x0*f2 + x1*f1 + x2*f0;
            q1 = x0*f3 + x1*f2 + x2*f1;
            q2 = x0*f4 + x1*f3 + x2*f2;
            q3 = x0*f5 + x1*f4 + x2*f3;
            q4 = x0*f6 + x1*f5 + x2*f4;
        }

        z = q4;
        fw  = (double)((int)(TWO_POW_N24*z));
        iq0 = (int)(z-TWO_POW_24*fw);
        z   = q3+fw;
        fw  = (double)((int)(TWO_POW_N24*z));
        iq1 = (int)(z-TWO_POW_24*fw);
        z   = q2+fw;
        fw  = (double)((int)(TWO_POW_N24*z));
        iq2 = (int)(z-TWO_POW_24*fw);
        z   = q1+fw;
        fw  = (double)((int)(TWO_POW_N24*z));
        iq3 = (int)(z-TWO_POW_24*fw);
        z   = q0+fw;

        // Here, q is in [-25,2] range or so, so we can use the table right away.
        double twoPowQ = twoPowTab[q-MIN_DOUBLE_EXPONENT];

        z = (z*twoPowQ) % 8.0;
        z -= (double)((int)z);
        if (q > 0) {
            iq3 &= 0xFFFFFF>>q;
            ih = iq3>>(23-q);
        } else if (q == 0) {
            ih = iq3>>23;
        } else if (z >= 0.5) {
            ih = 2;
        } else {
            ih = 0;
        }
        if (ih > 0) {
            int carry;
            if (iq0 != 0) {
                carry = 1;
                iq0 = 0x1000000 - iq0;
                iq1 = 0x0FFFFFF - iq1;
                iq2 = 0x0FFFFFF - iq2;
                iq3 = 0x0FFFFFF - iq3;
            } else {
                if (iq1 != 0) {
                    carry = 1;
                    iq1 = 0x1000000 - iq1;
                    iq2 = 0x0FFFFFF - iq2;
                    iq3 = 0x0FFFFFF - iq3;
                } else {
                    if (iq2 != 0) {
                        carry = 1;
                        iq2 = 0x1000000 - iq2;
                        iq3 = 0x0FFFFFF - iq3;
                    } else {
                        if (iq3 != 0) {
                            carry = 1;
                            iq3 = 0x1000000 - iq3;
                        } else {
                            carry = 0;
                        }
                    }
                }
            }
            if (q > 0) {
                switch (q) {
                case 1:
                    iq3 &= 0x7FFFFF;
                    break;
                case 2:
                    iq3 &= 0x3FFFFF;
                    break;
                }
            }
            if (ih == 2) {
                z = 1.0 - z;
                if (carry != 0) {
                    z -= twoPowQ;
                }
            }
        }

        if (z == 0.0) {
            if (jx == 1) {
                f6 = ONE_OVER_TWOPI_TAB[jv+5];
                q5 = x0*f6 + x1*f5;
            } else { // jx == 2
                f7 = ONE_OVER_TWOPI_TAB[jv+5];
                q5 = x0*f7 + x1*f6 + x2*f5;
            }

            z = q5;
            fw  = (double)((int)(TWO_POW_N24*z));
            iq0 = (int)(z-TWO_POW_24*fw);
            z   = q4+fw;
            fw  = (double)((int)(TWO_POW_N24*z));
            iq1 = (int)(z-TWO_POW_24*fw);
            z   = q3+fw;
            fw  = (double)((int)(TWO_POW_N24*z));
            iq2 = (int)(z-TWO_POW_24*fw);
            z   = q2+fw;
            fw  = (double)((int)(TWO_POW_N24*z));
            iq3 = (int)(z-TWO_POW_24*fw);
            z   = q1+fw;
            fw  = (double)((int)(TWO_POW_N24*z));
            iq4 = (int)(z-TWO_POW_24*fw);
            z   = q0+fw;

            z = (z*twoPowQ) % 8.0;
            z -= (double)((int)z);
            if (q > 0) {
                // some parentheses for Eclipse formatter's weaknesses with bits shifts
                iq4 &= (0xFFFFFF>>q);
                ih = (iq4>>(23-q));
            } else if (q == 0) {
                ih = iq4>>23;
            } else if (z >= 0.5) {
                ih = 2;
            } else {
                ih = 0;
            }
            if (ih > 0) {
                if (iq0 != 0) {
                    iq0 = 0x1000000 - iq0;
                    iq1 = 0x0FFFFFF - iq1;
                    iq2 = 0x0FFFFFF - iq2;
                    iq3 = 0x0FFFFFF - iq3;
                    iq4 = 0x0FFFFFF - iq4;
                } else {
                    if (iq1 != 0) {
                        iq1 = 0x1000000 - iq1;
                        iq2 = 0x0FFFFFF - iq2;
                        iq3 = 0x0FFFFFF - iq3;
                        iq4 = 0x0FFFFFF - iq4;
                    } else {
                        if (iq2 != 0) {
                            iq2 = 0x1000000 - iq2;
                            iq3 = 0x0FFFFFF - iq3;
                            iq4 = 0x0FFFFFF - iq4;
                        } else {
                            if (iq3 != 0) {
                                iq3 = 0x1000000 - iq3;
                                iq4 = 0x0FFFFFF - iq4;
                            } else {
                                if (iq4 != 0) {
                                    iq4 = 0x1000000 - iq4;
                                }
                            }
                        }
                    }
                }
                if (q > 0) {
                    switch (q) {
                    case 1:
                        iq4 &= 0x7FFFFF;
                        break;
                    case 2:
                        iq4 &= 0x3FFFFF;
                        break;
                    }
                }
            }
            fw = twoPowQ * TWO_POW_N24; // q -= 24, so initializing fw with ((2^q)*(2^-24)=2^(q-24))
        } else {
            // Here, q is in [-25,-2] range or so, so we could use twoPow's table right away with
            // iq4 = (int)(z*twoPowTab[-q-TWO_POW_TAB_MIN_POW]);
            // but tests show using division is faster...
            iq4 = (int)(z/twoPowQ);
            fw = twoPowQ;
        }

        q4 = fw*(double)iq4;
        fw *= TWO_POW_N24;
        q3 = fw*(double)iq3;
        fw *= TWO_POW_N24;
        q2 = fw*(double)iq2;
        fw *= TWO_POW_N24;
        q1 = fw*(double)iq1;
        fw *= TWO_POW_N24;
        q0 = fw*(double)iq0;
        fw *= TWO_POW_N24;

        fw = TWOPI_TAB0*q4;
        fw += TWOPI_TAB0*q3 + TWOPI_TAB1*q4;
        fw += TWOPI_TAB0*q2 + TWOPI_TAB1*q3 + TWOPI_TAB2*q4;
        fw += TWOPI_TAB0*q1 + TWOPI_TAB1*q2 + TWOPI_TAB2*q3 + TWOPI_TAB3*q4;
        fw += TWOPI_TAB0*q0 + TWOPI_TAB1*q1 + TWOPI_TAB2*q2 + TWOPI_TAB3*q3 + TWOPI_TAB4*q4;

        return (ih == 0) ? fw : -fw;
    }

    //--------------------------------------------------------------------------
    // STATIC INITIALIZATIONS
    //--------------------------------------------------------------------------

    /**
     * Initializes look-up tables.
     * 
     * Might use some FastMath methods in there, not to spend
     * an hour in it, but must take care not to use methods
     * which look-up tables have not yet been initialized,
     * or that are not accurate enough.
     */
    static {

        // sin and cos

        final int SIN_COS_PI_INDEX = (SIN_COS_TABS_SIZE-1)/2;
        final int SIN_COS_PI_MUL_2_INDEX = 2*SIN_COS_PI_INDEX;
        final int SIN_COS_PI_MUL_0_5_INDEX = SIN_COS_PI_INDEX/2;
        final int SIN_COS_PI_MUL_1_5_INDEX = 3*SIN_COS_PI_INDEX/2;
        for (int i=0;i<SIN_COS_TABS_SIZE;i++) {
            // angle: in [0,2*PI].
            double angle = i * SIN_COS_DELTA_HI + i * SIN_COS_DELTA_LO;
            double sinAngle = StrictMath.sin(angle);
            double cosAngle = StrictMath.cos(angle);
            // For indexes corresponding to null cosine or sine, we make sure the value is zero
            // and not an epsilon. This allows for a much better accuracy for results close to zero.
            if (i == SIN_COS_PI_INDEX) {
                sinAngle = 0.0;
            } else if (i == SIN_COS_PI_MUL_2_INDEX) {
                sinAngle = 0.0;
            } else if (i == SIN_COS_PI_MUL_0_5_INDEX) {
                cosAngle = 0.0;
            } else if (i == SIN_COS_PI_MUL_1_5_INDEX) {
                cosAngle = 0.0;
            }
            sinTab[i] = sinAngle;
            cosTab[i] = cosAngle;
        }

        // tan

        for (int i=0;i<TAN_TABS_SIZE;i++) {
            // angle: in [0,TAN_MAX_VALUE_FOR_TABS].
            double angle = i * TAN_DELTA_HI + i * TAN_DELTA_LO;
            tanTab[i] = StrictMath.tan(angle);
            double cosAngle = StrictMath.cos(angle);
            double sinAngle = StrictMath.sin(angle);
            double cosAngleInv = 1/cosAngle;
            double cosAngleInv2 = cosAngleInv*cosAngleInv;
            double cosAngleInv3 = cosAngleInv2*cosAngleInv;
            double cosAngleInv4 = cosAngleInv2*cosAngleInv2;
            double cosAngleInv5 = cosAngleInv3*cosAngleInv2;
            tanDer1DivF1Tab[i] = cosAngleInv2;
            tanDer2DivF2Tab[i] = ((2*sinAngle)*cosAngleInv3) * ONE_DIV_F2;
            tanDer3DivF3Tab[i] = ((2*(1+2*sinAngle*sinAngle))*cosAngleInv4) * ONE_DIV_F3;
            tanDer4DivF4Tab[i] = ((8*sinAngle*(2+sinAngle*sinAngle))*cosAngleInv5) * ONE_DIV_F4;
        }

        // asin

        for (int i=0;i<ASIN_TABS_SIZE;i++) {
            // x: in [0,ASIN_MAX_VALUE_FOR_TABS].
            double x = i * ASIN_DELTA;
            asinTab[i] = StrictMath.asin(x);
            double oneMinusXSqInv = 1.0/(1-x*x);
            double oneMinusXSqInv0_5 = StrictMath.sqrt(oneMinusXSqInv);
            double oneMinusXSqInv1_5 = oneMinusXSqInv0_5*oneMinusXSqInv;
            double oneMinusXSqInv2_5 = oneMinusXSqInv1_5*oneMinusXSqInv;
            double oneMinusXSqInv3_5 = oneMinusXSqInv2_5*oneMinusXSqInv;
            asinDer1DivF1Tab[i] = oneMinusXSqInv0_5;
            asinDer2DivF2Tab[i] = (x*oneMinusXSqInv1_5) * ONE_DIV_F2;
            asinDer3DivF3Tab[i] = ((1+2*x*x)*oneMinusXSqInv2_5) * ONE_DIV_F3;
            asinDer4DivF4Tab[i] = ((5+2*x*(2+x*(5-2*x)))*oneMinusXSqInv3_5) * ONE_DIV_F4;
        }

        if (USE_POWTABS_FOR_ASIN) {
            for (int i=0;i<ASIN_POWTABS_SIZE;i++) {
                // x: in [0,ASIN_MAX_VALUE_FOR_POWTABS].
                double x = StrictMath.pow(i*(1.0/ASIN_POWTABS_SIZE_MINUS_ONE), 1.0/ASIN_POWTABS_POWER) * ASIN_MAX_VALUE_FOR_POWTABS;
                asinParamPowTab[i] = x;
                asinPowTab[i] = StrictMath.asin(x);
                double oneMinusXSqInv = 1.0/(1-x*x);
                double oneMinusXSqInv0_5 = StrictMath.sqrt(oneMinusXSqInv);
                double oneMinusXSqInv1_5 = oneMinusXSqInv0_5*oneMinusXSqInv;
                double oneMinusXSqInv2_5 = oneMinusXSqInv1_5*oneMinusXSqInv;
                double oneMinusXSqInv3_5 = oneMinusXSqInv2_5*oneMinusXSqInv;
                asinDer1DivF1PowTab[i] = oneMinusXSqInv0_5;
                asinDer2DivF2PowTab[i] = (x*oneMinusXSqInv1_5) * ONE_DIV_F2;
                asinDer3DivF3PowTab[i] = ((1+2*x*x)*oneMinusXSqInv2_5) * ONE_DIV_F3;
                asinDer4DivF4PowTab[i] = ((5+2*x*(2+x*(5-2*x)))*oneMinusXSqInv3_5) * ONE_DIV_F4;
            }
        }

        // atan

        for (int i=0;i<ATAN_TABS_SIZE;i++) {
            // x: in [0,ATAN_MAX_VALUE_FOR_TABS].
            double x = i * ATAN_DELTA;
            double onePlusXSqInv = 1.0/(1+x*x);
            double onePlusXSqInv2 = onePlusXSqInv*onePlusXSqInv;
            double onePlusXSqInv3 = onePlusXSqInv2*onePlusXSqInv;
            double onePlusXSqInv4 = onePlusXSqInv2*onePlusXSqInv2;
            atanTab[i] = StrictMath.atan(x);
            atanDer1DivF1Tab[i] = onePlusXSqInv;
            atanDer2DivF2Tab[i] = (-2*x*onePlusXSqInv2) * ONE_DIV_F2;
            atanDer3DivF3Tab[i] = ((-2+6*x*x)*onePlusXSqInv3) * ONE_DIV_F3;
            atanDer4DivF4Tab[i] = ((24*x*(1-x*x))*onePlusXSqInv4) * ONE_DIV_F4;
        }

        // exp

        for (int i=0;i<EXP_LO_TAB_SIZE;i++) {
            // x: in [-EXPM1_DISTANCE_TO_ZERO,EXPM1_DISTANCE_TO_ZERO].
            double x = -EXP_LO_DISTANCE_TO_ZERO + i/(double)EXP_LO_INDEXING;
            // exp(x)
            expLoPosTab[i] = StrictMath.exp(x);
            // 1-exp(-x), accurately computed
            expLoNegTab[i] = -StrictMath.expm1(-x);
        }
        for (int i=0;i<=(int)EXP_OVERFLOW_LIMIT;i++) {
            expHiTab[i] = StrictMath.exp(i);
        }
        for (int i=0;i<=-(int)EXP_UNDERFLOW_LIMIT;i++) {
            // We take care not to compute with subnormal values.
            if ((double)-i >= EXP_MIN_INT_LIMIT) {
                expHiInvTab[i] = StrictMath.exp(-i);
            } else {
                expHiInvTab[i] = StrictMath.exp(54*LOG_2-i);
            }
        }

        // log

        for (int i=0;i<LOG_TAB_SIZE;i++) {
            // Exact to use inverse of tab size, since it is a power of two.
            double x = 1+i*(1.0/LOG_TAB_SIZE);
            logXLogTab[i] = StrictMath.log(x);
            logXTab[i] = x;
            logXInvTab[i] = 1/x;
        }

        // twoPow

        for (int i=MIN_DOUBLE_EXPONENT;i<=MAX_DOUBLE_EXPONENT;i++) {
            twoPowTab[i-MIN_DOUBLE_EXPONENT] = StrictMath.pow(2.0,i);
        }

        // sqrt

        for (int i=MIN_DOUBLE_EXPONENT;i<=MAX_DOUBLE_EXPONENT;i++) {
            double twoPowExpDiv2 = StrictMath.pow(2.0,i*0.5);
            sqrtXSqrtHiTab[i-MIN_DOUBLE_EXPONENT] = twoPowExpDiv2 * 0.5; // Half sqrt, to avoid overflows.
            sqrtSlopeHiTab[i-MIN_DOUBLE_EXPONENT] = 1/twoPowExpDiv2;
        }
        sqrtXSqrtLoTab[0] = 1.0;
        sqrtSlopeLoTab[0] = 1.0;
        final long SQRT_LO_MASK = (0x3FF0000000000000L | (0x000FFFFFFFFFFFFFL>>SQRT_LO_BITS));
        for (int i=1;i<SQRT_LO_TAB_SIZE;i++) {
            long xBits = SQRT_LO_MASK | (((long)(i-1))<<(52-SQRT_LO_BITS));
            double sqrtX = StrictMath.sqrt(Double.longBitsToDouble(xBits));
            sqrtXSqrtLoTab[i] = sqrtX;
            sqrtSlopeLoTab[i] = 1/sqrtX;
        }

        // cbrt

        for (int i=MIN_DOUBLE_EXPONENT;i<=MAX_DOUBLE_EXPONENT;i++) {
            double twoPowExpDiv3 = StrictMath.pow(2.0,i/3.0);
            cbrtXCbrtHiTab[i-MIN_DOUBLE_EXPONENT] = twoPowExpDiv3 * 0.5; // Half cbrt, to avoid overflows.
            double tmp = 1/twoPowExpDiv3;
            cbrtSlopeHiTab[i-MIN_DOUBLE_EXPONENT] = (4.0/3)*tmp*tmp;
        }
        cbrtXCbrtLoTab[0] = 1.0;
        cbrtSlopeLoTab[0] = 1.0;
        final long CBRT_LO_MASK = (0x3FF0000000000000L | (0x000FFFFFFFFFFFFFL>>CBRT_LO_BITS));
        for (int i=1;i<CBRT_LO_TAB_SIZE;i++) {
            long xBits = CBRT_LO_MASK | (((long)(i-1))<<(52-CBRT_LO_BITS));
            double cbrtX = StrictMath.cbrt(Double.longBitsToDouble(xBits));
            cbrtXCbrtLoTab[i] = cbrtX;
            cbrtSlopeLoTab[i] = 1/(cbrtX*cbrtX);
        }
    }
}