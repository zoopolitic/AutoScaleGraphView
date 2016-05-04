package com.zoopolitic.graphview;

import android.content.res.Resources;

/**
 * Created by zoopolitic
 */
class AndroidUtils {

    public static float dpToPx(float dp) {
        return Resources.getSystem().getDisplayMetrics().density * dp;
    }

    private static final int POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000};

    /**
     * Formats a float value to the given number of decimals. Returns the length of the string.
     * The string begins at out.length - [return value].
     */
    static int formatFloat(final char[] out, float val, int digits) {
        boolean negative = false;
        if (val == 0) {
            out[0] = '0';
            return 1;
        }
        if (val < 0) {
            negative = true;
            val = -val;
        }
        if (digits >= POW10.length) {
            digits = POW10.length - 1;
        }
        val *= POW10[digits];
        long lval = Math.round(val);
        int index = out.length - 1;
        int charCount = 0;
        while (lval != 0 || charCount < (digits + 1)) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            out[index--] = (char) (digit + '0');
            charCount++;
            if (charCount == digits) {
                out[index--] = '.';
                charCount++;
            }
        }
        if (negative) {
            out[index] = '-';
            charCount++;
        }
        return charCount;
    }

    /**
     * Rounds the given number to the given number of significant digits. Based on an answer on
     * <a href="http://stackoverflow.com/questions/202302">Stack Overflow</a>.
     */
    private static float roundToOneSignificantFigure(double num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        final int power = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    public static double roundToDecimalPlaces(double value, int decimalPlaces) {
        double shift = Math.pow(10, decimalPlaces);
        return Math.round(value * shift) / shift;
    }
}
