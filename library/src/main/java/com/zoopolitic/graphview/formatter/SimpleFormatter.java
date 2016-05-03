package com.zoopolitic.graphview.formatter;

import android.support.annotation.Nullable;

/**
 * Created by zoopolitic on 27 Апрель 2016 13:43.
 */
public class SimpleFormatter implements ValueFormatter {

    @Nullable
    @Override
    public String getFormattedValue(float index) {
        return String.valueOf(index);
    }
}
