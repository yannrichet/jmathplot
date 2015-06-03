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

/**
 * Class containing various basic utility methods to deal with numbers.
 * This class is meant to be light (no big look-up tables or such).
 * 
 * Check methods return boolean if success,
 * for it allows to use them in assertions.
 * 
 * toString methods use capital letters, unlike JDK's toStrings, for it is more
 * readable (especially, "l" and "1" can easily be confused with one another).
 * 
 * Some methods have an int version additionally to the long version,
 * even though long version could be used instead, for performance reasons,
 * either for the methods themselves (if they do computations with ints
 * instead of longs), or to be used in an int use case (like methods
 * checking whether or not a signed int can fit in such number of bits).
 */
public strictfp final class NumbersUtils {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean ASSERTIONS = false;
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final int MIN_DOUBLE_EXPONENT = -1074;
    private static final int MAX_DOUBLE_EXPONENT = 1023;

    /**
     * All possible upper case chars for representing a number as a String.
     */
    private final static char[] CHAR_BY_DIGIT;
    static {
        final char minDecimal = '0';
        final char maxDecimal = '9';
        final int n1 = maxDecimal - minDecimal + 1;
        final char minLetter = 'A';
        final char maxLetter = 'Z';
        final int n2 = maxLetter - minLetter + 1;
        CHAR_BY_DIGIT = new char[n1+n2];
        int i=0;
        for (char c=minDecimal;c<=maxDecimal;c++) {
            CHAR_BY_DIGIT[i++] = c;
        }
        for (char c=minLetter;c<=maxLetter;c++) {
            CHAR_BY_DIGIT[i++] = c;
        }
    }

    /**
     * For power-of-two radixes only.
     */
    private static final int[] DIV_SHIFT_BY_RADIX;
    static {
        DIV_SHIFT_BY_RADIX = new int[32+1];
        int shift=1;
        for (int radix=2;radix<=32;radix*=2) {
            DIV_SHIFT_BY_RADIX[radix] = shift++;
        }
    }

    private final static int[] MAX_NBR_OF_NEG_INT_DIGITS_BY_RADIX = new int[Character.MAX_RADIX+1];
    private final static int[] MAX_NBR_OF_NEG_LONG_DIGITS_BY_RADIX = new int[Character.MAX_RADIX+1];
    static {
        for (int radix=Character.MIN_RADIX;radix<=Character.MAX_RADIX;radix++) {
            /*
             * Brutal but works.
             * -1 for the sign.
             */
            MAX_NBR_OF_NEG_INT_DIGITS_BY_RADIX[radix] = Integer.toString(Integer.MIN_VALUE, radix).length()-1;
            MAX_NBR_OF_NEG_LONG_DIGITS_BY_RADIX[radix] = Long.toString(Long.MIN_VALUE, radix).length()-1;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @return True if the specified values are equal or both NaN, false otherwise.
     */
    public static boolean equal(float a, float b) {
        // Only does one test if a == b.
        return (a == b) ? true : ((a != a) && (b != b));
    }

    /**
     * @return True if the specified values are equal or both NaN, false otherwise.
     */
    public static boolean equal(double a, double b) {
        // Only does one test if a == b.
        return (a == b) ? true : ((a != a) && (b != b));
    }

    /*
     * min/max ranges
     */

    /**
     * @return True if the specified value is in the specified range (inclusive), false otherwise.
     */
    public static boolean isInRange(int min, int max, int a) {
        return (min <= a) && (a <= max);
    }

    /**
     * @return True if the specified value is in the specified range (inclusive), false otherwise.
     */
    public static boolean isInRange(long min, long max, long a) {
        return (min <= a) && (a <= max);
    }

    /**
     * Returns false if any value is NaN.
     * 
     * @return True if the specified value is in the specified range (inclusive), false otherwise.
     */
    public static boolean isInRange(float min, float max, float a) {
        return (min <= a) && (a <= max);
    }

    /**
     * Returns false if any value is NaN.
     * 
     * @return True if the specified value is in the specified range (inclusive), false otherwise.
     */
    public static boolean isInRange(double min, double max, double a) {
        return (min <= a) && (a <= max);
    }

    /*
     * 
     */

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value is not in the specified range (inclusive).
     */
    public static boolean checkIsInRange(int min, int max, int a) {
        if (!isInRange(min, max, a)) {
            throw new IllegalArgumentException(a+" not in ["+min+","+max+"]");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value is not in the specified range (inclusive).
     */
    public static boolean checkIsInRange(long min, long max, long a) {
        if (!isInRange(min, max, a)) {
            throw new IllegalArgumentException(a+" not in ["+min+","+max+"]");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value is not in the specified range (inclusive)
     *         or any parameter is NaN.
     */
    public static boolean checkIsInRange(float min, float max, float a) {
        if (!isInRange(min, max, a)) {
            throw new IllegalArgumentException(a+" not in ["+min+","+max+"]");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value is not in the specified range (inclusive)
     *         or any parameter is NaN.
     */
    public static boolean checkIsInRange(double min, double max, double a) {
        if (!isInRange(min, max, a)) {
            throw new IllegalArgumentException(a+" not in ["+min+","+max+"]");
        }
        return true;
    }

    /*
     * bitwise ranges
     */

    /**
     * @param bitSize A number of bits, in [1,32].
     * @return True if the specified value fits as a signed integer
     *         over the specified number of bits, false otherwise.
     * @throws IllegalArgumentException if the specified number of bits is not in [1,32].
     */
    public static boolean isInRangeSigned(int a, int bitSize) {
        checkBitSizeForSignedInt(bitSize);
        return (minSignedIntForBitSize_noCheck(bitSize) <= a) && (a <= maxSignedIntForBitSize_noCheck(bitSize));
    }

    /**
     * @param bitSize A number of bits, in [1,64].
     * @return True if the specified value fits as a signed integer
     *         over the specified number of bits, false otherwise.
     * @throws IllegalArgumentException if the specified number of bits is not in [1,64].
     */
    public static boolean isInRangeSigned(long a, int bitSize) {
        checkBitSizeForSignedLong(bitSize);
        return (minSignedLongForBitSize_noCheck(bitSize) <= a) && (a <= maxSignedLongForBitSize_noCheck(bitSize));
    }

    /**
     * @param bitSize A number of bits, in [1,31].
     * @return True if the specified value fits as an unsigned integer
     *         over the specified number of bits, false otherwise.
     * @throws IllegalArgumentException if the specified number of bits is not in [1,31].
     */
    public static boolean isInRangeUnsigned(int a, int bitSize) {
        return isInRange(0, maxUnsignedIntForBitSize(bitSize), a);
    }

    /**
     * @param bitSize A number of bits, in [1,63].
     * @return True if the specified value fits as an unsigned integer
     *         over the specified number of bits, false otherwise.
     * @throws IllegalArgumentException if the specified number of bits is not in [1,63].
     */
    public static boolean isInRangeUnsigned(long a, int bitSize) {
        return isInRange(0, maxUnsignedLongForBitSize(bitSize), a);
    }

    /*
     * 
     */

    /**
     * @param bitSize A number of bits, in [1,32].
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value does not fit
     *         as a signed integer over the specified number of bits.
     */
    public static boolean checkIsInRangeSigned(int a, int bitSize) {
        if (!isInRangeSigned(a, bitSize)) {
            throw new IllegalArgumentException(a+" does not fit as a signed value over "+bitSize+" bits");
        }
        return true;
    }

    /**
     * @param bitSize A number of bits, in [1,64].
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value does not fit
     *         as a signed integer over the specified number of bits.
     */
    public static boolean checkIsInRangeSigned(long a, int bitSize) {
        if (!isInRangeSigned(a, bitSize)) {
            throw new IllegalArgumentException(a+" does not fit as a signed value over "+bitSize+" bits");
        }
        return true;
    }

    /**
     * @param bitSize A number of bits, in [1,31].
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value does not fit
     *         as an unsigned integer over the specified number of bits.
     */
    public static boolean checkIsInRangeUnsigned(int a, int bitSize) {
        if (!isInRangeUnsigned(a, bitSize)) {
            throw new IllegalArgumentException(a+" does not fit as an unsigned value over "+bitSize+" bits");
        }
        return true;
    }

    /**
     * @param bitSize A number of bits, in [1,63].
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified value does not fit
     *         as an unsigned integer over the specified number of bits.
     */
    public static boolean checkIsInRangeUnsigned(long a, int bitSize) {
        if (!isInRangeUnsigned(a, bitSize)) {
            throw new IllegalArgumentException(a+" does not fit as an unsigned value over "+bitSize+" bits");
        }
        return true;
    }

    /*
     * masks (int)
     */

    /**
     * @param bitSize A number of bits, in [0,32].
     * @return Mask with the specified number of left bits set with 0,
     *         and other bits set with 1.
     */
    public static int intMaskMSBits0(int bitSize) {
        checkIsInRange(0, 32, bitSize);
        // Shifting in two times, for >>> doesn't work for full bit size (<< as well).
        final int halfish = (bitSize>>1);
        return ((-1)>>>halfish)>>>(bitSize-halfish);
    }

    /**
     * @param bitSize A number of bits, in [0,32].
     * @return Mask with the specified number of left bits set with 1,
     *         and other bits set with 0.
     */
    public static int intMaskMSBits1(int bitSize) {
        return ~intMaskMSBits0(bitSize);
    }

    /**
     * @param bitSize A number of bits, in [0,32].
     * @return Mask with the specified number of right bits set with 0,
     *         and other bits set with 1.
     */
    public static int intMaskLSBits0(int bitSize) {
        return ~intMaskMSBits0(32-bitSize);
    }

    /**
     * @param bitSize A number of bits, in [0,32].
     * @return Mask with the specified number of right bits set with 1,
     *         and other bits set with 0.
     */
    public static int intMaskLSBits1(int bitSize) {
        return intMaskMSBits0(32-bitSize);
    }

    /*
     * masks (long)
     */

    /**
     * @param bitSize A number of bits, in [0,64].
     * @return Mask with the specified number of left bits set with 0,
     *         and other bits set with 1.
     */
    public static long longMaskMSBits0(int bitSize) {
        checkIsInRange(0, 64, bitSize);
        // Shifting in two times, for >>> doesn't work for full bit size (<< as well).
        final int halfish = (bitSize>>1);
        return ((-1L)>>>halfish)>>>(bitSize-halfish);
    }

    /**
     * @param bitSize A number of bits, in [0,64].
     * @return Mask with the specified number of left bits set with 1,
     *         and other bits set with 0.
     */
    public static long longMaskMSBits1(int bitSize) {
        return ~longMaskMSBits0(bitSize);
    }

    /**
     * @param bitSize A number of bits, in [0,64].
     * @return Mask with the specified number of right bits set with 0,
     *         and other bits set with 1.
     */
    public static long longMaskLSBits0(int bitSize) {
        return ~longMaskMSBits0(64-bitSize);
    }

    /**
     * @param bitSize A number of bits, in [0,64].
     * @return Mask with the specified number of right bits set with 1,
     *         and other bits set with 0.
     */
    public static long longMaskLSBits1(int bitSize) {
        return longMaskMSBits0(64-bitSize);
    }

    /*
     * signed/unsigned
     */

    /**
     * @return Unsigned value corresponding to bits of the specified byte.
     */
    public static short byteAsUnsigned(byte value) {
        return (short)(((short)value) & 0xFF);
    }

    /**
     * @return Unsigned value corresponding to bits of the specified short.
     */
    public static int shortAsUnsigned(short value) {
        return ((int)value) & 0xFFFF;
    }

    /**
     * @return Unsigned value corresponding to bits of the specified int.
     */
    public static long intAsUnsigned(int value) {
        return ((long)value) & 0xFFFFFFFF;
    }

    /*
     * bitwise ranges
     */

    /**
     * @return True if a signed int value can be read over the specified number of bits,
     *         i.e. if it is in [1,32], false otherwise.
     */
    public static boolean isValidBitSizeForSignedInt(int bitSize) {
        return (bitSize > 0) && (bitSize <= 32);
    }

    /**
     * @return True if a signed long value can be read over the specified number of bits,
     *         i.e. if it is in [1,64], false otherwise.
     */
    public static boolean isValidBitSizeForSignedLong(int bitSize) {
        return (bitSize > 0) && (bitSize <= 64);
    }

    /**
     * @return True if an unsigned int value can be read over the specified number of bits,
     *         i.e. if it is in [1,31], false otherwise.
     */
    public static boolean isValidBitSizeForUnsignedInt(int bitSize) {
        return (bitSize > 0) && (bitSize < 32);
    }

    /**
     * @return True if an unsigned long value can be read over the specified number of bits,
     *         i.e. if it is in [1,63], false otherwise.
     */
    public static boolean isValidBitSizeForUnsignedLong(int bitSize) {
        return (bitSize > 0) && (bitSize < 64);
    }

    /*
     * 
     */

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if a signed int value can't be read over the
     *         specified number of bits, i.e. if it is not in [1,32].
     */
    public static boolean checkBitSizeForSignedInt(int bitSize) {
        if (!isValidBitSizeForSignedInt(bitSize)) {
            throw new IllegalArgumentException("bit size ["+bitSize+"] must be in [1,32] for signed int values");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if a signed long value can't be read over the
     *         specified number of bits, i.e. if it is not in [1,64].
     */
    public static boolean checkBitSizeForSignedLong(int bitSize) {
        if (!isValidBitSizeForSignedLong(bitSize)) {
            throw new IllegalArgumentException("bit size ["+bitSize+"] must be in [1,64] for signed long values");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if an unsigned int value can't be read over the
     *         specified number of bits, i.e. if it is not in [1,31].
     */
    public static boolean checkBitSizeForUnsignedInt(int bitSize) {
        if (!isValidBitSizeForUnsignedInt(bitSize)) {
            throw new IllegalArgumentException("bit size ["+bitSize+"] must be in [1,31] for unsigned int values");
        }
        return true;
    }

    /**
     * @return True if does not throw.
     * @throws IllegalArgumentException if an unsigned long value can't be read over the
     *         specified number of bits, i.e. if it is not in [1,63].
     */
    public static boolean checkBitSizeForUnsignedLong(int bitSize) {
        if (!isValidBitSizeForUnsignedLong(bitSize)) {
            throw new IllegalArgumentException("bit size ["+bitSize+"] must be in [1,63] for unsigned long values");
        }
        return true;
    }

    /*
     * 
     */

    /**
     * @param bitSize A number of bits in [1,32].
     * @return The min signed int value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static int minSignedIntForBitSize(int bitSize) {
        checkBitSizeForSignedInt(bitSize);
        return minSignedIntForBitSize_noCheck(bitSize);
    }

    /**
     * @param bitSize A number of bits in [1,64].
     * @return The min signed long value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static long minSignedLongForBitSize(int bitSize) {
        checkBitSizeForSignedLong(bitSize);
        return minSignedLongForBitSize_noCheck(bitSize);
    }

    /**
     * @param bitSize A number of bits in [1,32].
     * @return The max signed int value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static int maxSignedIntForBitSize(int bitSize) {
        checkBitSizeForSignedInt(bitSize);
        return maxSignedIntForBitSize_noCheck(bitSize);
    }

    /**
     * @param bitSize A number of bits in [1,64].
     * @return The max signed long value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static long maxSignedLongForBitSize(int bitSize) {
        checkBitSizeForSignedLong(bitSize);
        return maxSignedLongForBitSize_noCheck(bitSize);
    }

    /**
     * @param bitSize A number of bits in [1,31].
     * @return The max unsigned int value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static int maxUnsignedIntForBitSize(int bitSize) {
        checkBitSizeForUnsignedInt(bitSize);
        // i.e. (1<<bitSize)-1
        return (Integer.MAX_VALUE>>(31-bitSize));
    }

    /**
     * @param bitSize A number of bits in [1,63].
     * @return The max unsigned long value that can be stored over the specified number of bits.
     * @throws IllegalArgumentException if the specified number of bits is out of range.
     */
    public static long maxUnsignedLongForBitSize(int bitSize) {
        checkBitSizeForUnsignedLong(bitSize);
        // i.e. (1L<<bitSize)-1
        return (Long.MAX_VALUE>>(63-bitSize));
    }

    /*
     * 
     */

    /**
     * @return The number of bits required to store the specified value as a signed integer,
     *         i.e. a result in [1,32].
     */
    public static int bitSizeForSignedValue(int value) {
        if (value > 0) {
            return 33-Integer.numberOfLeadingZeros(value);
        } else if (value == 0) {
            return 1;
        } else {
            // Works for Integer.MIN_VALUE as well.
            return 33-Integer.numberOfLeadingZeros(-value-1);
        }
    }

    /**
     * @return The number of bits required to store the specified value as a signed integer,
     *         i.e. a result in [1,64].
     */
    public static int bitSizeForSignedValue(long value) {
        if (value > 0) {
            return 65-Long.numberOfLeadingZeros(value);
        } else if (value == 0) {
            return 1;
        } else {
            // Works for Long.MIN_VALUE as well.
            return 65-Long.numberOfLeadingZeros(-value-1);
        }
    }

    /**
     * @param value An integer value in [0,Integer.MAX_VALUE].
     * @return The number of bits required to store the specified value as an unsigned integer,
     *         i.e. a result in [1,31].
     * @throws IllegalArgumentException if the specified value is < 0.
     */
    public static int bitSizeForUnsignedValue(int value) {
        if (value > 0) {
            return 32-Integer.numberOfLeadingZeros(value);
        } else {
            if (value == 0) {
                return 1;
            } else {
                throw new IllegalArgumentException("unsigned value ["+value+"] must be >= 0");
            }
        }
    }

    /**
     * @param value An integer value in [0,Long.MAX_VALUE].
     * @return The number of bits required to store the specified value as an unsigned integer,
     *         i.e. a result in [1,63].
     * @throws IllegalArgumentException if the specified value is < 0.
     */
    public static int bitSizeForUnsignedValue(long value) {
        if (value > 0) {
            return 64-Long.numberOfLeadingZeros(value);
        } else {
            if (value == 0) {
                return 1;
            } else {
                throw new IllegalArgumentException("unsigned value ["+value+"] must be >= 0");
            }
        }
    }

    /*
     * integer functions
     */
    
    /**
     * @return 1 if the specified value is > 0, 0 if it is 0, -1 otherwise.
     */
    public static int signum(int a) {
        return (a < 0) ? -1 : ((a == 0) ? 0 : 1);
    }

    /**
     * @return 1 if the specified value is > 0, 0 if it is 0, -1 otherwise.
     */
    public static int signum(long a) {
        return (a < 0) ? -1 : ((a == 0) ? 0 : 1);
    }

    /**
     * @return True if the specified value is even, false otherwise.
     */
    public static boolean isEven(int a) {
        return ((a&1) == 0);
    }

    /**
     * @return True if the specified value is even, false otherwise.
     */
    public static boolean isEven(long a) {
        // faster to work on ints
        return isEven((int)a);
    }

    /**
     * @return True if the specified value is odd, false otherwise.
     */
    public static boolean isOdd(int a) {
        return ((a&1) != 0);
    }

    /**
     * @return True if the specified value is odd, false otherwise.
     */
    public static boolean isOdd(long a) {
        // faster to work on ints
        return isOdd((int)a);
    }

    /**
     * @return True if the specified values are both even or both odd, false otherwise.
     */
    public static boolean haveSameEvenness(int a, int b) {
        return (((a^b)&1) == 0);
    }

    /**
     * @return True if the specified values are both even or both odd, false otherwise.
     */
    public static boolean haveSameEvenness(long a, long b) {
        // faster to work on ints
        return haveSameEvenness((int)a, (int)b);
    }

    /**
     * @return True if the specified values are both >= 0 or both < 0, false otherwise.
     */
    public static boolean haveSameSign(int a, int b) {
        return ((a^b) >= 0);
    }

    /**
     * @return True if the specified values are both >= 0 or both < 0, false otherwise.
     */
    public static boolean haveSameSign(long a, long b) {
        return ((a^b) >= 0);
    }

    /**
     * @return True if the specified value is a power of two,
     *         i.e. a value of the form 2^k, with k >= 0.
     */
    public static boolean isPowerOfTwo(int a) {
        if (a <= 0) {
            return false;
        }
        if (false) {
            // also works
            return (a & -a) == a;
        }
        return (a & (a-1)) == 0;
    }

    /**
     * @return True if the specified value is a power of two,
     *         i.e. a value of the form 2^k, with k >= 0.
     */
    public static boolean isPowerOfTwo(long a) {
        if (a <= 0) {
            return false;
        }
        if (false) {
            // also works
            return (a & -a) == a;
        }
        return (a & (a-1)) == 0;
    }

    /**
     * @return True if the specified value is a signed power of two,
     *         i.e. a value of the form +-2^k, with k >= 0.
     */
    public static boolean isSignedPowerOfTwo(int a) {
        if (a > 0) {
            return (a & (a-1)) == 0;
        } else {
            if (a == -a) {
                // a is 0 or Integer.MIN_VALUE
                return (a != 0);
            }
            return ((-a) & (-a-1)) == 0;
        }
    }

    /**
     * @return True if the specified value is a signed power of two,
     *         i.e. a value of the form +-2^k, with k >= 0.
     */
    public static boolean isSignedPowerOfTwo(long a) {
        if (a > 0) {
            return (a & (a-1)) == 0;
        } else {
            if (a == -a) {
                // a is 0 or Long.MIN_VALUE
                return (a != 0);
            }
            return ((-a) & (-a-1)) == 0;
        }
    }

    /**
     * @param a A value in [1,Integer.MAX_VALUE].
     * @return The highest power of two <= a.
     */
    public static int floorPowerOfTwo(int a) {
        if (a <= 0) {
            throw new IllegalArgumentException("a ["+a+"] must be > 0");
        }
        return 1 << (31 - Integer.numberOfLeadingZeros(a));
    }

    /**
     * @param a A value in [1,Long.MAX_VALUE].
     * @return The highest power of two <= a.
     */
    public static long floorPowerOfTwo(long a) {
        if (a <= 0) {
            throw new IllegalArgumentException("a ["+a+"] must be > 0");
        }
        return 1L << (63 - Long.numberOfLeadingZeros(a));
    }

    /**
     * @param a A value in [0,2^30].
     * @return The lowest power of two >= a.
     */
    public static int ceilingPowerOfTwo(int a) {
        checkIsInRange(0, (1<<30), a);
        // From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (32 - Integer.numberOfLeadingZeros(a - 1));
    }

    /**
     * @param a A value in [0,2^62].
     * @return The lowest power of two >= a.
     */
    public static long ceilingPowerOfTwo(long a) {
        checkIsInRange(0L, (1L<<62), a);
        return 1L << (64 - Long.numberOfLeadingZeros(a - 1));
    }

    /**
     * @return Mean without overflow, rounded to the lowest value (i.e. mathematical floor((a+b)/2), using floating point division).
     */
    public static int meanLow(int a, int b) {
        return (a & b) + ((a ^ b) >> 1);
    }

    /**
     * @return Mean without overflow, rounded to the lowest value (i.e. mathematical floor((a+b)/2), using floating point division).
     */
    public static long meanLow(long a, long b) {
        return (a & b) + ((a ^ b) >> 1);
    }

    /**
     * @return Mean without overflow, rounded to the value of smallest magnitude (i.e. mathematical (a+b)/2, using integer division).
     */
    public static int meanSml(int a, int b) {
        int result = meanLow(a,b);
        if (!haveSameEvenness(a, b)) {
            // inexact
            if (((a&b) < 0) || (((a|b) < 0) && (a+b < 0))) {
                // both < 0, or only one is < 0 and it has the largest magnitude
                result++;
            }
        }
        return result;
    }

    /**
     * @return Mean without overflow, rounded to the value of smallest magnitude (i.e. mathematical (a+b)/2, using integer division).
     */
    public static long meanSml(long a, long b) {
        long result = meanLow(a,b);
        if (!haveSameEvenness(a, b)) {
            // inexact
            if (((a&b) < 0) || (((a|b) < 0) && (a+b < 0))) {
                // both < 0, or only one is < 0 and it has the largest magnitude
                result++;
            }
        }
        return result;
    }

    /**
     * Useful because a positive int value could not represent half the width
     * of full int range width, which is mathematically Integer.MAX_VALUE+1.
     * 
     * @return Minus half the range width (inclusive, and rounded to the value of smaller magnitude)
     *         between the specified bounds.
     * @throws IllegalArgumentException if min > max.
     */
    public static int negHalfWidth(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min ["+min+"] must be <= max ["+max+"]");
        }
        int mean = meanLow(min, max);
        return min - mean - ((min^max)&1);
    }

    /**
     * Useful because a positive long value could not represent half the width
     * of full long range width, which is mathematically Long.MAX_VALUE+1.
     * 
     * @return Minus half the range width (inclusive, and rounded to the value of smaller magnitude)
     *         between the specified bounds.
     * @throws IllegalArgumentException if min > max.
     */
    public static long negHalfWidth(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min ["+min+"] must be <= max ["+max+"]");
        }
        long mean = meanLow(min, max);
        return min - mean - ((min^max)&1);
    }

    /**
     * This treatment being designed for optimization, the fact that spot
     * is a signed power of two is only checked if assertions are enabled.
     * 
     * @param value A value.
     * @param spot A signed power of two (i.e. a value of the form +-2^k, k >= 0).
     * @return value % spot, i.e. a value in ]-|spot|,|spot|[.
     */
    public static int moduloSignedPowerOfTwo(int value, int spot) {
        if(ASSERTIONS)assert(isSignedPowerOfTwo(spot));
        if (spot == Integer.MIN_VALUE) {
            return (value != Integer.MIN_VALUE) ? value : 0;
        } else {
            int s = (value>>31);
            return ((((value+s) ^ s) & (abs(spot)-1)) + s) ^ s;
        }
    }

    /**
     * This treatment being designed for optimization, the fact that spot
     * is a signed power of two is only checked if assertions are enabled.
     * 
     * @param value A value.
     * @param spot A signed power of two (i.e. a value of the form +-2^k, k >= 0).
     * @return value % spot, i.e. a value in ]-|spot|,|spot|[.
     */
    public static long moduloSignedPowerOfTwo(long value, long spot) {
        if(ASSERTIONS)assert(isSignedPowerOfTwo(spot));
        if (spot == Long.MIN_VALUE) {
            return (value != Long.MIN_VALUE) ? value : 0;
        } else {
            long s = (value>>63);
            return ((((value+s) ^ s) & (abs(spot)-1)) + s) ^ s;
        }
    }

    /**
     * @param value An integer value > 0.
     * @return The integer part of the logarithm, in base 2, of the specified value,
     *         i.e. a result in [0,30]
     * @throws IllegalArgumentException if the specified value is <= 0.
     */
    public static int log2(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value ["+value+"] must be > 0");
        }
        return 31-Integer.numberOfLeadingZeros(value);
    }

    /**
     * @param value An integer value > 0.
     * @return The integer part of the logarithm, in base 2, of the specified value,
     *         i.e. a result in [0,62]
     * @throws IllegalArgumentException if the specified value is <= 0.
     */
    public static int log2(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value ["+value+"] must be > 0");
        }
        return 63-Long.numberOfLeadingZeros(value);
    }

    /**
     * Possibly faster than java.lang.Math.abs(int).
     * 
     * @return The absolute value, except if value is Integer.MIN_VALUE, for which it returns Integer.MIN_VALUE.
     */
    public static int abs(int a) {
        return (a^(a>>31))-(a>>31);
    }

    /**
     * Possibly faster than java.lang.Math.abs(long).
     * 
     * @return The absolute value, except if value is Long.MIN_VALUE, for which it returns Long.MIN_VALUE.
     */
    public static long abs(long a) {
        return (a^(a>>63))-(a>>63);
    }

    /**
     * net.jodk.FastMath class has a typically faster version of this method,
     * using look-up tables.
     * 
     * Returns the exact result, provided it's in double range,
     * i.e. if power is in [-1074,1023].
     * 
     * @param power A power.
     * @return 2^power.
     */
    public static double twoPow(int power) {
        if (power <= -MAX_DOUBLE_EXPONENT) { // Not normal.
            if (power >= MIN_DOUBLE_EXPONENT) { // Subnormal.
                return Double.longBitsToDouble(0x0008000000000000L>>(-(power+MAX_DOUBLE_EXPONENT)));
            } else { // Underflow.
                return 0.0;
            }
        } else if (power > MAX_DOUBLE_EXPONENT) { // Overflow.
            return Double.POSITIVE_INFINITY;
        } else { // Normal.
            return Double.longBitsToDouble(((long)(power+MAX_DOUBLE_EXPONENT))<<52);
        }
    }

    /**
     * If the specified value is in int range, the returned value is identical.
     * 
     * @return An int hash of the specified value.
     */
    public static int intHash(long a) {
        if (false) {
            // also works
            int hash = ((int)(a>>32)) ^ ((int)a);
            if (a < 0) {
                hash = -hash-1;
            }
            return hash;
        }
        int hash = ((int)(a>>32)) + ((int)a);
        if (a < 0) {
            hash++;
        }
        return hash;
    }

    /**
     * Not defining an asByte(long) method, since asByte((int)aLong) works.
     * 
     * @param a An int value.
     * @return The specified value as byte.
     * @throws ArithmeticException if the specified value is not in [Byte.MIN_VALUE,Byte.MAX_VALUE] range.
     */
    public static byte asByte(int a) {
        if (a != (byte)a) {
            throw new ArithmeticException("overflow: "+a);
        }
        return (byte)a;
    }

    /**
     * @param a A long value.
     * @return The specified value as int.
     * @throws ArithmeticException if the specified value is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int asInt(long a) {
        if (a != (int)a) {
            throw new ArithmeticException("overflow: "+a);
        }
        return (int)a;
    }

    /**
     * @param a A long value.
     * @return The closest int value in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int toInt(long a) {
        if (a != (int)a) {
            return (a < (long)Integer.MIN_VALUE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return (int)a;
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a+b.
     * @throws ArithmeticException if the mathematical result of a+b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int plusExact(int a, int b) {
        if ((a^b) < 0) { // test if a and b signs are different
            return a + b;
        } else {
            int sum = a + b;
            if ((a^sum) < 0) {
                throw new ArithmeticException("overflow: "+a+"+"+b);
            } else {
                return sum;
            }
        }
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a+b.
     * @throws ArithmeticException if the mathematical result of a+b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long plusExact(long a, long b) {
        if ((a^b) < 0) { // test if a and b signs are different
            return a + b;
        } else {
            long sum = a + b;
            if ((a^sum) < 0) {
                throw new ArithmeticException("overflow: "+a+"+"+b);
            } else {
                return sum;
            }
        }
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a+b.
     */
    public static int plusBounded(int a, int b) {
        return toInt(((long)a) + ((long)b));
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a+b.
     */
    public static long plusBounded(long a, long b) {
        if ((a^b) < 0) { // test if a and b signs are different
            return a + b;
        } else {
            long sum = a + b;
            if ((a^sum) < 0) {
                return (sum >= 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
            } else {
                return sum;
            }
        }
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a-b.
     * @throws ArithmeticException if the mathematical result of a-b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int minusExact(int a, int b) {
        if ((a^b) >= 0) { // test if a and b signs are identical
            return a - b;
        } else {
            int diff = a - b;
            if ((a^diff) < 0) {
                throw new ArithmeticException("overflow: "+a+"-"+b);
            } else {
                return diff;
            }
        }
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a-b.
     * @throws ArithmeticException if the mathematical result of a-b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long minusExact(long a, long b) {
        if ((a^b) >= 0) { // test if a and b signs are identical
            return a - b;
        } else {
            long diff = a - b;
            if ((a^diff) < 0) {
                throw new ArithmeticException("overflow: "+a+"-"+b);
            } else {
                return diff;
            }
        }
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a-b.
     */
    public static int minusBounded(int a, int b) {
        return toInt(((long)a) - ((long)b));
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a-b.
     */
    public static long minusBounded(long a, long b) {
        if ((a^b) >= 0) { // test if a and b signs are identical
            return a - b;
        } else {
            long diff = a - b;
            if ((a^diff) < 0) {
                return (diff >= 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
            } else {
                return diff;
            }
        }
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The mathematical result of a*b.
     * @throws ArithmeticException if the mathematical result of a*b is not in [Integer.MIN_VALUE,Integer.MAX_VALUE] range.
     */
    public static int timesExact(int a, int b) {
        long product = a * (long)b;
        if (product != (int)product) {
            throw new ArithmeticException("overflow: "+a+"*"+b);
        }
        return (int)product;
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The mathematical result of a*b.
     * @throws ArithmeticException if the mathematical result of a*b is not in [Long.MIN_VALUE,Long.MAX_VALUE] range.
     */
    public static long timesExact(long a, long b) {
        long r = a * b;
        long absA = abs(a);
        long absB = abs(b);
        if (((absA|absB)>>>31) != 0) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if (((b != 0) && (r/b != a)) ||
                    ((a == Long.MIN_VALUE) && (b == -1))) {
                throw new ArithmeticException("overflow: "+a+"*"+b);
            }
        }
        return r;
    }

    /**
     * @param a An int value.
     * @param b An int value.
     * @return The int value of [Integer.MIN_VALUE,Integer.MAX_VALUE] range which is the closest to mathematical result of a*b.
     */
    public static int timesBounded(int a, int b) {
        return (int)(a * (double)b);
    }

    /**
     * @param a A long value.
     * @param b A long value.
     * @return The long value of [Long.MIN_VALUE,Long.MAX_VALUE] range which is the closest to mathematical result of a*b.
     */
    public static long timesBounded(long a, long b) {
        long r = a * b;
        long absA = abs(a);
        long absB = abs(b);
        if (((absA|absB)>>>31) != 0) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if (((b != 0) && (r/b != a)) ||
                    ((a == Long.MIN_VALUE) && (b == -1))) {
                return ((a^b) >= 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
            }
        }
        return r;
    }

    /*
     * integer and floating point functions
     */

    /**
     * @return True if the specified value is NaN, positive of negative infinity, false otherwise.
     */
    public static boolean isNaNOrInfinite(float a) {
        // a-a is not equal to 0.0f (and is NaN) <-> a is NaN or +-infinity
        return !(a-a == 0.0f);
    }

    /**
     * @return True if the specified value is NaN, positive of negative infinity, false otherwise.
     */
    public static boolean isNaNOrInfinite(double a) {
        // a-a is not equal to 0.0 (and is NaN) <-> a is NaN or +-infinity
        return !(a-a == 0.0);
    }

    /**
     * @param a A value.
     * @return a*a.
     */
    public static int pow2(int a) {
        return a*a;
    }

    /**
     * @param a A value.
     * @return a*a.
     */
    public static long pow2(long a) {
        return a*a;
    }

    /**
     * @param a A value.
     * @return a*a.
     */
    public static float pow2(float a) {
        return a*a;
    }

    /**
     * @param a A value.
     * @return a*a.
     */
    public static double pow2(double a) {
        return a*a;
    }

    /**
     * @param a A value.
     * @return a*a*a.
     */
    public static int pow3(int a) {
        return a*a*a;
    }

    /**
     * @param a A value.
     * @return a*a*a.
     */
    public static long pow3(long a) {
        return a*a*a;
    }

    /**
     * @param a A value.
     * @return a*a*a.
     */
    public static float pow3(float a) {
        return a*a*a;
    }

    /**
     * @param a A value.
     * @return a*a*a.
     */
    public static double pow3(double a) {
        return a*a*a;
    }

    /*
     * 
     */

    /**
     * @param min A value.
     * @param max A value.
     * @param a A value.
     * @return min if a <= min, else max if a >= max, else a.
     */
    public static int toRange(int min, int max, int a) {
        if (a <= min) {
            return min;
        } else if (a >= max) {
            return max;
        } else {
            return a;
        }
    }

    /**
     * @param min A value.
     * @param max A value.
     * @param a A value.
     * @return min if a <= min, else max if a >= max, else a.
     */
    public static long toRange(long min, long max, long a) {
        if (a <= min) {
            return min;
        } else if (a >= max) {
            return max;
        } else {
            return a;
        }
    }

    /**
     * @param min A value.
     * @param max A value.
     * @param a A value.
     * @return min if a <= min, else max if a >= max, else a.
     */
    public static float toRange(float min, float max, float a) {
        if (a <= min) {
            return min;
        } else if (a >= max) {
            return max;
        } else {
            return a;
        }
    }

    /**
     * @param min A value.
     * @param max A value.
     * @param a A value.
     * @return min if a <= min, else max if a >= max, else a.
     */
    public static double toRange(double min, double max, double a) {
        if (a <= min) {
            return min;
        } else if (a >= max) {
            return max;
        } else {
            return a;
        }
    }

    /*
     * toString (radix)
     */

    /**
     * @param radix Radix to be checked.
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified radix is not in [2,36].
     */
    public static boolean checkRadix(int radix) {
        if (!isInRange(Character.MIN_RADIX, Character.MAX_RADIX, radix)) {
            throw new IllegalArgumentException("radix ["+radix+"] must be in ["+Character.MIN_RADIX+","+Character.MAX_RADIX+"]");
        }
        return true;
    }

    /**
     * @param radix A radix in [2,36].
     * @return Number of characters (minus sign included)
     *         to represent the specified value in the specified radix.
     */
    public static int computeNbrOfChars(int value, int radix) {
        if (value < 0) {
            // 1 for sign
            return 1 + computeNbrOfDigits_negValue(value, radix);
        } else {
            return computeNbrOfDigits_negValue(-value, radix);
        }
    }

    /**
     * @param radix A radix in [2,36].
     * @return Number of characters (minus sign included)
     *         to represent the specified value in the specified radix.
     */
    public static int computeNbrOfChars(long value, int radix) {
        if (value < 0) {
            // 1 for sign
            return 1 + computeNbrOfDigits_negValue(value, radix);
        } else {
            return computeNbrOfDigits_negValue(-value, radix);
        }
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return Number of characters (minus sign included)
     *         to represent the specified value in the specified radix.
     */
    public static int computeNbrOfChars(int value, int radix, int paddingUpTo) {
        if (value < 0) {
            // 1 for sign
            return 1 + Math.max(paddingUpTo, computeNbrOfDigits_negValue(value, radix));
        } else {
            return Math.max(paddingUpTo, computeNbrOfDigits_negValue(-value, radix));
        }
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return Number of characters (minus sign included)
     *         to represent the specified value in the specified radix.
     */
    public static int computeNbrOfChars(long value, int radix, int paddingUpTo) {
        if (value < 0) {
            // 1 for sign
            return 1 + Math.max(paddingUpTo, computeNbrOfDigits_negValue(value, radix));
        } else {
            return Math.max(paddingUpTo, computeNbrOfDigits_negValue(-value, radix));
        }
    }

    /**
     * @param radix A radix in [2,36].
     * @return Number of digits of the specified value in the specified radix.
     */
    public static int computeNbrOfDigits(int value, int radix) {
        return computeNbrOfDigits_negValue(-abs(value), radix);
    }

    /**
     * @param radix A radix in [2,36].
     * @return Number of digits of the specified value in the specified radix.
     */
    public static int computeNbrOfDigits(long value, int radix) {
        return computeNbrOfDigits_negValue(-abs(value), radix);
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return Number of digits of the specified value in the specified radix,
     *         including the specified padding.
     */
    public static int computeNbrOfDigits(int value, int radix, int paddingUpTo) {
        return Math.max(paddingUpTo,computeNbrOfDigits(value, radix));
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return Number of digits of the specified value in the specified radix,
     *         including the specified padding.
     */
    public static int computeNbrOfDigits(long value, int radix, int paddingUpTo) {
        return Math.max(paddingUpTo,computeNbrOfDigits(value, radix));
    }

    /**
     * This method just delegates to Integer.toString(int),
     * but is defined here to complete the API.
     * 
     * @return String representation of the specified value in base 10.
     */
    public static String toString(int value) {
        return Integer.toString(value);
    }

    /**
     * This method just delegates to Long.toString(long),
     * but is defined here to complete the API.
     * 
     * @return String representation of the specified value in base 10.
     */
    public static String toString(long value) {
        return Long.toString(value);
    }

    /**
     * @param radix A radix in [2,36].
     * @return String representation of the specified value in the specified radix.
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    public static String toString(int value, int radix) {
        return toString(value, radix, 0);
    }

    /**
     * @param radix A radix in [2,36].
     * @return String representation of the specified value in the specified radix.
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    public static String toString(long value, int radix) {
        return toString(value, radix, 0);
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return String representation of the specified value in the specified radix.
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    public static String toString(int value, int radix, int paddingUpTo) {
        // Only one test if radix+paddingUpTo != 10.
        if ((radix+paddingUpTo == 10) && (paddingUpTo == 0)) {
            // Using JDK's optimized algorithm.
            return Integer.toString(value);
        }
        
        int negValue;
        final int signSize;
        final boolean negative = (value < 0);
        if (negative) {
            negValue = value;
            signSize = 1;
        } else {
            negValue = -value;
            signSize = 0;
        }
        // Faster if we just use max possible number of characters (33),
        // but we prefer to take care of garbage's memory footprint.
        // Checks radix.
        final int nbrOfChars = signSize + Math.max(paddingUpTo, computeNbrOfDigits_negValue(negValue, radix));

        final char[] chars = new char[nbrOfChars];

        int charPos = nbrOfChars;

        final boolean radixIsPowerOfTwo = ((radix & (radix-1)) == 0);
        // Not allowing Integer.MIN_VALUE so it can be negated.
        if (radixIsPowerOfTwo && (negValue != Integer.MIN_VALUE)) {
            final int mask = radix-1;
            final int divShift = DIV_SHIFT_BY_RADIX[radix];
            while (negValue <= -radix) {
                chars[--charPos] = CHAR_BY_DIGIT[(int)((-negValue) & mask)];
                negValue = -((-negValue) >> divShift);
            }
        } else {
            while (negValue <= -radix) {
                chars[--charPos] = CHAR_BY_DIGIT[(int)(-(negValue % radix))];
                negValue /= radix;
            }
        }
        chars[--charPos] = CHAR_BY_DIGIT[(int)(-negValue)];

        while (charPos > signSize) {
            chars[--charPos] = '0';
        }

        if (negative) {
            chars[0] = '-';
        }

        return new String(chars);
    }

    /**
     * @param radix A radix in [2,36].
     * @param paddingUpTo Number of digits (sign excluded) up to which left-padding with zeros is done.
     * @return String representation of the specified value in the specified radix.
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    public static String toString(long value, int radix, int paddingUpTo) {
        // Only one test if radix+paddingUpTo != 10.
        if ((radix+paddingUpTo == 10) && (paddingUpTo == 0)) {
            // Using JDK's optimized algorithm.
            return Long.toString(value);
        }
        
        long negValue;
        final int signSize;
        final boolean negative = (value < 0);
        if (negative) {
            negValue = value;
            signSize = 1;
        } else {
            negValue = -value;
            signSize = 0;
        }
        // Checks radix.
        final int nbrOfChars = signSize + Math.max(paddingUpTo, computeNbrOfDigits_negValue(negValue, radix));

        final char[] chars = new char[nbrOfChars];

        int charPos = nbrOfChars;

        final boolean radixIsPowerOfTwo = ((radix & (radix-1)) == 0);
        // Not allowing Long.MIN_VALUE so it can be negated.
        if (radixIsPowerOfTwo && (negValue != Long.MIN_VALUE)) {
            final int mask = radix-1;
            final int divShift = DIV_SHIFT_BY_RADIX[radix];
            while (negValue <= -radix) {
                chars[--charPos] = CHAR_BY_DIGIT[(int)((-negValue) & mask)];
                negValue = -((-negValue) >> divShift);
            }
        } else {
            while (negValue <= -radix) {
                chars[--charPos] = CHAR_BY_DIGIT[(int)(-(negValue % radix))];
                negValue /= radix;
            }
        }
        chars[--charPos] = CHAR_BY_DIGIT[(int)(-negValue)];

        while (charPos > signSize) {
            chars[--charPos] = '0';
        }

        if (negative) {
            chars[0] = '-';
        }

        return new String(chars);
    }

    /*
     * toString (bits)
     */

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified bit range does not fit in a byte.
     */
    public static boolean checkBitPositionsByte(int firstBitPos, int lastBitPosExcl) {
        return checkBitPositions(firstBitPos, lastBitPosExcl, 8);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified bit range does not fit in a short.
     */
    public static boolean checkBitPositionsShort(int firstBitPos, int lastBitPosExcl) {
        return checkBitPositions(firstBitPos, lastBitPosExcl, 16);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified bit range does not fit in an int.
     */
    public static boolean checkBitPositionsInt(int firstBitPos, int lastBitPosExcl) {
        return checkBitPositions(firstBitPos, lastBitPosExcl, 32);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @return True if does not throw.
     * @throws IllegalArgumentException if the specified bit range does not fit in a long.
     */
    public static boolean checkBitPositionsLong(int firstBitPos, int lastBitPosExcl) {
        return checkBitPositions(firstBitPos, lastBitPosExcl, 64);
    }

    /**
     * @return String representation of specified bits, in big endian.
     */
    public static String toStringBits(byte bits) {
        final char[] chars = new char[8];
        int bitIndex = 8;
        while (--bitIndex >= 0) {
            chars[7-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
        }
        return new String(chars);
    }

    /**
     * @return String representation of specified bits, in big endian.
     */
    public static String toStringBits(short bits) {
        final char[] chars = new char[16];
        int bitIndex = 16;
        while (--bitIndex >= 0) {
            chars[15-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
        }
        return new String(chars);
    }

    /**
     * @return String representation of specified bits, in big endian.
     */
    public static String toStringBits(int bits) {
        final char[] chars = new char[32];
        int bitIndex = 32;
        while (--bitIndex >= 0) {
            chars[31-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
        }
        return new String(chars);
    }

    /**
     * @return String representation of specified bits, in big endian.
     */
    public static String toStringBits(long bits) {
        final char[] chars = new char[64];
        int bitIndex = 64;
        while (--bitIndex >= 0) {
            chars[63-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
        }
        return new String(chars);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @param bigEndian True for bits to be added in big endian order (MSBit to LSBit)
     *                  false for little endian order.
     * @param padding True if underscores must be added instead of out-of-range bits,
     *                false to just add characters corresponding to in-range bits.
     * @return String representation of specified bits.
     */
    public static String toStringBits(
            byte bits,
            int firstBitPos,
            int lastBitPosExcl,
            boolean bigEndian,
            boolean padding) {
        checkBitPositionsByte(firstBitPos, lastBitPosExcl);
        return toStringBits_0_32_bitPosAlreadyChecked(8,bits, firstBitPos, lastBitPosExcl, bigEndian, padding);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @param bigEndian True for bits to be added in big endian order (MSBit to LSBit)
     *                  false for little endian order.
     * @param padding True if underscores must be added instead of out-of-range bits,
     *                false to just add characters corresponding to in-range bits.
     * @return String representation of specified bits.
     */
    public static String toStringBits(
            short bits,
            int firstBitPos,
            int lastBitPosExcl,
            boolean bigEndian,
            boolean padding) {
        checkBitPositionsShort(firstBitPos, lastBitPosExcl);
        return toStringBits_0_32_bitPosAlreadyChecked(16,bits, firstBitPos, lastBitPosExcl, bigEndian, padding);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @param bigEndian True for bits to be added in big endian order (MSBit to LSBit)
     *                  false for little endian order.
     * @param padding True if underscores must be added instead of out-of-range bits,
     *                false to just add characters corresponding to in-range bits.
     * @return String representation of specified bits.
     */
    public static String toStringBits(
            int bits,
            int firstBitPos,
            int lastBitPosExcl,
            boolean bigEndian,
            boolean padding) {
        checkBitPositionsInt(firstBitPos, lastBitPosExcl);
        return toStringBits_0_32_bitPosAlreadyChecked(32,bits, firstBitPos, lastBitPosExcl, bigEndian, padding);
    }

    /**
     * @param firstBitPos First bit position (inclusive).
     * @param lastBitPosExcl Last bit position (exclusive).
     * @param bigEndian True for bits to be added in big endian order (MSBit to LSBit)
     *                  false for little endian order.
     * @param padding True if underscores must be added instead of out-of-range bits,
     *                false to just add characters corresponding to in-range bits.
     * @return String representation of specified bits.
     */
    public static String toStringBits(
            long bits,
            int firstBitPos,
            int lastBitPosExcl,
            boolean bigEndian,
            boolean padding) {
        checkBitPositionsLong(firstBitPos, lastBitPosExcl);
        final int bitSize = 64;
        final int bitSizeM1 = bitSize-1;
        final int lastBitPos = lastBitPosExcl-1;
        if (padding) {
            final int nbrOfChars = bitSize;
            final char[] chars = new char[nbrOfChars];
            int bitIndex = bitSizeM1;
            if (bigEndian) {
                final int firstBitIndex = bitSizeM1-lastBitPos;
                final int lastBitIndex = bitSizeM1-firstBitPos;
                while (bitIndex > lastBitIndex) {
                    chars[bitSizeM1-bitIndex] = '_';
                    --bitIndex;
                }
                while (bitIndex >= firstBitIndex) {
                    chars[bitSizeM1-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
                while (bitIndex >= 0) {
                    chars[bitSizeM1-bitIndex] = '_';
                    --bitIndex;
                }
            } else {
                while (bitIndex > lastBitPos) {
                    chars[bitIndex] = '_';
                    --bitIndex;
                }
                while (bitIndex >= firstBitPos) {
                    chars[bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
                while (bitIndex >= 0) {
                    chars[bitIndex] = '_';
                    --bitIndex;
                }
            }
            return new String(chars);
        } else {
            final int nbrOfChars = (lastBitPosExcl - firstBitPos);
            final char[] chars = new char[nbrOfChars];
            if (bigEndian) {
                final int firstBitIndex = bitSizeM1-lastBitPos;
                final int lastBitIndex = bitSizeM1-firstBitPos;
                int bitIndex = lastBitIndex;
                while (bitIndex >= firstBitIndex) {
                    chars[lastBitIndex-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
            } else {
                int bitIndex = lastBitPos;
                while (bitIndex >= firstBitPos) {
                    chars[bitIndex-firstBitPos] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
            }
            return new String(chars);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Had such isInXXX methods, and corresponding checkXXX methods,
     * but they seem actually slower in practice, so just keeping this
     * code here in case some day it becomes faster than regular isInXXX.
     * 
     * Only works for non-empty ranges, i.e. such as min <= max.
     * This treatment being designed for optimization, min <= max
     * is only checked is assertions are enabled.
     * 
     * @return True if the specified value is in the specified range (inclusive), false otherwise.
     */
    private static boolean dontUseMe_isInNonEmptyRange_(int min, int max, int a) {
        if(ASSERTIONS)assert(min <= max);
        // Using modulo arithmetic.
        return (Integer.MIN_VALUE+(a-min) <= Integer.MIN_VALUE+(max-min));
    }

    /*
     * 
     */
    
    private static int minSignedIntForBitSize_noCheck(int bitSize) {
        // i.e. (-1<<(bitSize-1))
        return (Integer.MIN_VALUE>>(32-bitSize));
    }

    private static long minSignedLongForBitSize_noCheck(int bitSize) {
        // i.e. (-1L<<(bitSize-1))
        return (Long.MIN_VALUE>>(64-bitSize));
    }

    private static int maxSignedIntForBitSize_noCheck(int bitSize) {
        // i.e. (1<<(bitSize-1))-1
        return (Integer.MAX_VALUE>>(32-bitSize));
    }

    private static long maxSignedLongForBitSize_noCheck(int bitSize) {
        // i.e. (1L<<(bitSize-1))-1
        return (Long.MAX_VALUE>>(64-bitSize));
    }

    /*
     * 
     */

    /**
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    private static int computeNbrOfDigits_negValue(int negValue, int radix) {
        checkRadix(radix);
        final int maxNbrOfDigits = MAX_NBR_OF_NEG_INT_DIGITS_BY_RADIX[radix];
        int p = radix;
        for (int i=1;i<maxNbrOfDigits;i++) {
            if (negValue > -p) {
                return i;
            }
            p *= radix;
        }
        return maxNbrOfDigits;
    }

    /**
     * @throws IllegalArgumentException if the specified radix is out of range.
     */
    private static int computeNbrOfDigits_negValue(long negValue, int radix) {
        checkRadix(radix);
        final int maxNbrOfDigits = MAX_NBR_OF_NEG_LONG_DIGITS_BY_RADIX[radix];
        long p = radix;
        for (int i=1;i<maxNbrOfDigits;i++) {
            if (negValue > -p) {
                return i;
            }
            p *= radix;
        }
        return maxNbrOfDigits;
    }

    /*
     * 
     */

    private static boolean checkBitPositions(int firstBitPos, int lastBitPosExcl, int bitSize) {
        if ((firstBitPos < 0) || (firstBitPos > lastBitPosExcl) || (lastBitPosExcl > bitSize)) {
            throw new IllegalArgumentException(
                    "bit positions (first="+firstBitPos+",lastExcl="+lastBitPosExcl
                    +") must verify 0 <= first <= lastExcl <= "+bitSize);
        }
        return true;
    }

    /**
     * Common method for byte, short and int.
     * Could be a bit faster to have specific methods for byte and short,
     * but not much, and that would also make more messy (byte-)code.
     * 
     * @param bitSize Must be in [0,32].
     */
    private static String toStringBits_0_32_bitPosAlreadyChecked(
            int bitSize,
            int bits,
            int firstBitPos,
            int lastBitPosExcl,
            boolean bigEndian,
            boolean padding) {
        if(ASSERTIONS)assert((bitSize >= 0) && (bitSize <= 32));
        final int bitSizeM1 = bitSize-1;
        final int lastBitPos = lastBitPosExcl-1;
        if (padding) {
            final int nbrOfChars = bitSize;
            final char[] chars = new char[nbrOfChars];
            int bitIndex = bitSizeM1;
            if (bigEndian) {
                final int firstBitIndex = bitSizeM1-lastBitPos;
                final int lastBitIndex = bitSizeM1-firstBitPos;
                while (bitIndex > lastBitIndex) {
                    chars[bitSizeM1-bitIndex] = '_';
                    --bitIndex;
                }
                while (bitIndex >= firstBitIndex) {
                    chars[bitSizeM1-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
                while (bitIndex >= 0) {
                    chars[bitSizeM1-bitIndex] = '_';
                    --bitIndex;
                }
            } else {
                while (bitIndex > lastBitPos) {
                    chars[bitIndex] = '_';
                    --bitIndex;
                }
                while (bitIndex >= firstBitPos) {
                    chars[bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
                while (bitIndex >= 0) {
                    chars[bitIndex] = '_';
                    --bitIndex;
                }
            }
            return new String(chars);
        } else {
            final int nbrOfChars = (lastBitPosExcl - firstBitPos);
            final char[] chars = new char[nbrOfChars];
            if (bigEndian) {
                final int firstBitIndex = bitSizeM1-lastBitPos;
                final int lastBitIndex = bitSizeM1-firstBitPos;
                int bitIndex = lastBitIndex;
                while (bitIndex >= firstBitIndex) {
                    chars[lastBitIndex-bitIndex] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
            } else {
                int bitIndex = lastBitPos;
                while (bitIndex >= firstBitPos) {
                    chars[bitIndex-firstBitPos] = (char)('0'+((bits>>bitIndex)&1));
                    --bitIndex;
                }
            }
            return new String(chars);
        }
    }
}