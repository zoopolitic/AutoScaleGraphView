package com.zoopolitic.graphview.formatter;

/**
 * Created by zoopolitic
 */
public interface LabelFormatter {

    /**
     * Formats central label output.
     * Do not perform heavy operations here it will cause performance problems.
     * Keep as light as you can
     *
     * @param points points buffer
     * @param count  points count. Important: iterate through points while i less then count
     * @return formatted label string
     */
    String getFormattedValue(float[] points, int count);
}
