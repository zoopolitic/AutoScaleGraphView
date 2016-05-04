package com.zoopolitic.graphview.formatter;

/**
 * Created by zoopolitic on 28 Апрель 2016 17:43.
 */
public class SimpleLabelFormatter implements LabelFormatter {

    StringBuffer buffer = new StringBuffer();

    @Override
    public String getFormattedValue(float[] points, int count) {
        buffer.delete(0, buffer.length());
        for (int i = 0; i < count; i += 2) {
            buffer.append(points[i + 1]);
            buffer.append(" kg");
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
