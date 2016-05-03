package com.zoopolitic.graphview;

import android.os.SystemClock;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by zoopolitic
 */
class VerticalScaler {

    /**
     * The interpolator, used for making scale animate 'naturally.'
     */
    private Interpolator interpolator;

    /**
     * The total animation duration for a scale.
     */
    private long animationDurationMillis = 300;

    /**
     * Whether or not the current scaling has finished.
     */
    private boolean finished = true;

    /**
     * The time the scale started, computed using {@link android.os.SystemClock#elapsedRealtime()}.
     */
    private long startRTC;

    /**
     * The current maxY value; computed by {@link #computeYScale()}.
     */
    private float currentMaxY;

    /**
     * The destination maxY value.
     */
    private float endMaxY;

    /**
     * The current minY value; computed by {@link #computeYScale()}.
     */
    private float currentMinY;

    /**
     * The destination maxY value.
     */
    private float endMinY;

    private float startMaxY;
    private float startMinY;

    public VerticalScaler() {
        this(null);
    }

    public VerticalScaler(Interpolator interpolator) {
        this.interpolator = interpolator == null ? new FastOutSlowInInterpolator() : interpolator;
    }

    public void setScaleDuration(long duration) {
        this.animationDurationMillis = duration;
    }

    /**
     * Forces the scale finished state to the given value. Unlike {@link #abortAnimation()}, the
     * current scale value isn't set to the ending value.
     *
     * @see android.widget.Scroller#forceFinished(boolean)
     */
    public void forceFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Aborts the animation, setting the current scale value to the ending value.
     *
     * @see android.widget.Scroller#abortAnimation()
     */
    public void abortAnimation() {
        finished = true;
        currentMaxY = endMaxY;
    }

    /**
     * Starts scaling from current vales to end values
     *
     * @see android.widget.Scroller#startScroll(int, int, int, int)
     */
    public void startScalingY(float currentMaxY, float currentMinY, float endMaxY, float endMinY) {
        startRTC = SystemClock.elapsedRealtime();

        this.endMaxY = endMaxY;
        this.endMinY = endMinY;

        this.startMaxY = this.currentMaxY = currentMaxY;
        this.startMinY = this.currentMinY = currentMinY;

        finished = false;
    }

    /**
     * Computes the current maxY, returning true if scaling is still active and false if the
     * scaling has finished.
     *
     * @see android.widget.Scroller#computeScrollOffset()
     */
    public boolean computeYScale() {
        if (finished) {
            return false;
        }

        long tRTC = SystemClock.elapsedRealtime() - startRTC;
        if (tRTC >= animationDurationMillis) {
            finished = true;
            currentMaxY = endMaxY;
            currentMinY = endMinY;
            return false;
        }

        float fraction = tRTC * 1f / animationDurationMillis;
        float value = interpolator.getInterpolation(fraction);

        currentMaxY = startMaxY + value * (endMaxY - startMaxY);
        currentMinY = startMinY + value * (endMinY - startMinY);
        return true;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Returns the current maxY.
     *
     * @see android.widget.Scroller#getCurrY()
     */
    public float getCurrMaxY() {
        return currentMaxY;
    }

    /**
     * Returns the current minY.
     *
     * @see android.widget.Scroller#getCurrY()
     */
    public float getCurrMinY() {
        return currentMinY;
    }
}
