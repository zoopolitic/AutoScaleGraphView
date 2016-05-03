package com.zoopolitic.graphview.formatter;

import android.support.annotation.Nullable;

/**
 * Created by zoopolitic on 27 Апрель 2016 13:40.
 */
public interface ValueFormatter {

    @Nullable String getFormattedValue(float index);
}
