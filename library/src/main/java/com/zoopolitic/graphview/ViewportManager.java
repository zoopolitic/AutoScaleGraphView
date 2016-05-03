package com.zoopolitic.graphview;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.OverScroller;

import java.util.List;

/**
 * Created by zoopolitic
 */
class ViewportManager {

    private static final String TAG = "ViewportManager";

    /**
     * Rect that represents current visible part of graph.
     * This rect works with values not pixels
     */
    private RectF viewportRect = new RectF();

    /**
     * Rect for drawing on canvas
     */
    public Rect contentRect = new Rect();

    /**
     * Buffer for points within visible X-range
     */
    public float[] xRangePoints = new float[100];

    /**
     * Number of points within visible X-range
     */
    public int pointsCountWithinXRange;

    /**
     * Buffer for closest to central line points
     */
    public float[] closestPointsBuffer = new float[20]; // 10 points

    /**
     * Number of closestPoints
     */
    public int closestPointsCount;

    /**
     * Size of area that we can scroll
     * X - how much we can scroll horizontally
     * Y - how much we can scroll vertically
     */
    public Point surfaceSize = new Point();

    /**
     * True of scroller is scrolling at the moment
     */
    public boolean scrollInProgress;

    /**
     * Indicates was fling performed or not
     */
    private boolean fling;

    /**
     * Visible range of X values
     */
    public int visibleXRange = 7;

    /**
     * Interval of X axis values
     */
    public int xInterval = 1;

    /**
     * Stops of Y axis
     */
    public AxisStops yStops = new AxisStops();

    /**
     * Stops of X axis
     */
    public AxisStops xStops = new AxisStops();

    /**
     * Min/Max values of X/Y of the viewport
     */
    public float maxX = Float.MIN_VALUE;
    public float minX = Float.MAX_VALUE;
    public float maxY = Float.MIN_VALUE;
    public float minY = Float.MAX_VALUE;

    /**
     * Compute points within horizontal X range. If point's Y value is out of viewportRect top/bottom
     * range but X within left/right range - point will be added to xRange array
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    public void computePointsWithinXRange(List<DataSet> dataSets) {
        pointsCountWithinXRange = 0;
        for (DataSet dataSet : dataSets) {
            List<DataPoint> points = dataSet.getPoints();
            int size = points.size();
            for (int i = 0; i < size; i++) {
                DataPoint p = points.get(i);
                if (p.x > viewportRect.left && p.x < viewportRect.right) {
                    int length = xRangePoints.length;
                    if (length < pointsCountWithinXRange * 2 + 2) {
                        xRangePoints = new float[Math.round(length * 3 / 2)];
                    }
                    xRangePoints[pointsCountWithinXRange * 2 + 0] = p.x;
                    xRangePoints[pointsCountWithinXRange * 2 + 1] = p.y;
                    pointsCountWithinXRange++;
                }
            }
        }
    }

    /**
     * Set left/top/right/bottom values of drawing rect (area where graph will draw)
     */
    public void constrainDrawRect(int left, int top, int right, int bottom) {
        contentRect.set(left, top, right, bottom);
    }

    /**
     * Set left/top/right/bottom values of viewport rect
     */
    public void constrainViewportRect(float left, float top, float right, float bottom) {
        viewportRect.set(left, top, right, bottom);
    }

    /**
     * Calculates min,max X/Y-values from the provided dataSet
     */
    public void calculateMinMax(DataSet dataSet) {
        if (dataSet.getMaxY() > maxY) {
            maxY = dataSet.getMaxY();
        }
        if (dataSet.getMinY() < minY) {
            minY = dataSet.getMinY();
        }
        if (dataSet.getMaxX() > maxX) {
            maxX = dataSet.getMaxX();
        }
        if (dataSet.getMinX() < minX) {
            minX = dataSet.getMinX();
        }
        if (viewportRect.isEmpty()) {
            constrainViewportRect(maxX - visibleXRange, minY, maxX, maxY);
        }
    }

    /**
     * Move viewport to new X, Y
     *
     * @param newX new value of X (rect left)
     */
    public void move(float newX) {
        float currWidth = viewportRect.width();

        /**
         * +/- currWidth / 2 for centralLine to be able to reach the most left/right points
         * and another currWidth / 2 is for overScroll (see {@link #fling(int, int)}.
         * So currWidth / 2 + currWidth / 2 = curWidth, so subtract and add currWidth to each side
         */
        // maxX - currWidth is available for scroll area
        newX = Math.max(minX - currWidth, Math.min(newX, maxX - currWidth + currWidth));

        viewportRect.left = newX;
        viewportRect.right = newX + currWidth;
    }

    public void moveStart() {
        float left = minX - visibleXRange * 1f / 2;
        viewportRect.left = left;
        viewportRect.right = left + visibleXRange;
        computeClosestPoints();
        computeXAxis();
    }

    public void moveEnd() {
        float right = maxX + visibleXRange * 1f / 2;
        viewportRect.left = right - visibleXRange;
        viewportRect.right = right;
        computeClosestPoints();
        computeXAxis();
    }

    /**
     * Computes the current scrollable surface size, in pixels. For example, if the entire graph
     * area is visible, this is simply the current size of {@link #contentRect}. If the graph
     * is zoomed in 200% in both directions, the returned size will be twice as large horizontally
     * and vertically.
     */
    public void computeScrollSurfaceSize() {
        surfaceSize.set(
                (int) ((maxX - minX) / viewportRect.width() * contentRect.width()),
                (int) ((maxY - minY) / viewportRect.height() * contentRect.height())
        );
    }

    /**
     * Check if some graph point was clicked and if yes - scroll to it
     *
     * @param x                x value of click
     * @param y                y value of click
     * @param pointClickRadius additional radius of click to be triggered
     * @param scroller         scroller to scroll
     * @return true if any marker was clicked, false otherwise
     */
    public boolean clickedOnMarker(float x, float y, float pointClickRadius, OverScroller scroller, int duration) {
        for (int i = 0; i < pointsCountWithinXRange * 2; i += 2) {
            float pointX = getDrawX(xRangePoints[i]);
            float pointY = getDrawY(xRangePoints[i + 1]);
            float xDiff = Math.abs(x - pointX);
            float yDiff = Math.abs(y - pointY);
            if (xDiff < pointClickRadius && yDiff < pointClickRadius) {
                scrollXTo(scroller, pointX, duration);
                return true;
            }
        }

        return false;
    }

    /**
     * Scrolls to provided X position
     *
     * @param scroller scroller to scroll
     * @param x        x value to scroll
     */
    private void scrollXTo(OverScroller scroller, float x, int duration) {
        computeScrollSurfaceSize();

        float dx = x - contentRect.centerX();

        if (dx != 0) {
            scrollInProgress = true;
            scroller.startScroll(startX(), startY(), (int) dx, 0, duration);
        }
    }

    /**
     * Scrolls by provided X distance
     *
     * @param scroller scroller to scroll
     * @param dx       X distance to scroll
     */
    public void scrollXBy(OverScroller scroller, float dx, int duration) {
        // prevent scroll beyond right edge point
        if (viewportRect.left + dx > maxX) {
            dx = (maxX - visibleXRange * 1f / 2) - viewportRect.left;
        }
        // prevent scroll beyond left edge point
        if (viewportRect.left + dx < minX) {
            dx = (minX + visibleXRange * 1f / 2) - viewportRect.right;
        }
        float distance = dx / viewportRect.width() * contentRect.width();
        if (distance != 0) {
            computeScrollSurfaceSize();
            scrollInProgress = true;
            scroller.startScroll(startX(), startY(), (int) distance, 0, duration);
        }
    }

    /**
     * Auto scales graph
     *
     * @param scaler       scaler to perform scale
     * @param bottomOffset bottom offset of gr
     *                     aph
     * @param dataSets     point dataSets
     */
    public void autoScale(VerticalScaler scaler, float bottomOffset, List<DataSet> dataSets) {
        scrollInProgress = false;
        computePointsWithinXRange(dataSets);

        // no points within current viewport
        if (pointsCountWithinXRange == 0) {
            return;
        }

        float topPoint = Float.MIN_VALUE;
        float bottomPoint = Float.MAX_VALUE;
        for (int i = 0; i < pointsCountWithinXRange * 2; i += 2) {
            float y = xRangePoints[i + 1];
            if (y > topPoint) {
                topPoint = y;
            }
            if (y < bottomPoint) {
                bottomPoint = y;
            }
        }
        float currMaxY = viewportRect.bottom;
        float currMinY = viewportRect.top;
        computeYAxisStops(bottomPoint, topPoint);

        float topAxisY = yStops.stops[yStops.numStops - 1];
        float bottomAxisY = yStops.stops[0];

        float bottomOffsetFraction = bottomOffset / contentRect.height();

        float newMinY = (bottomAxisY - bottomOffsetFraction * topAxisY) / (1 - bottomOffsetFraction);

        float minDiff = Math.abs(currMinY - newMinY);
        float maxDiff = Math.abs(currMaxY - topAxisY);
        if (minDiff > 0 || maxDiff > 0) {
            scaler.startScalingY(currMaxY, currMinY, topAxisY, newMinY);
        }
    }

    /**
     * Snap to closest x index
     *
     * @param scroller scroller to scroll
     */
    public void snapToClosestIndex(OverScroller scroller, int duration) {
        if (xStops.stops.length == 0) {
            computeXAxis();
        }
        float closestX = Float.MAX_VALUE;
        float centerX = contentRect.centerX();
        for (int i = 0; i < xStops.stops.length; i++) {
            float x = getDrawX(xStops.stops[i]);
            if (Math.abs(x - centerX) <= Math.abs(closestX - centerX)) {
                closestX = x;
            }
        }
        scrollXTo(scroller, closestX, duration);
    }

    /**
     * Compute closest to central line points. Several points will be written to array only if
     * they have same X values
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    public void computeClosestPoints() {
        float centerX = contentRect.centerX();
        float closestDistance = Float.MAX_VALUE;
        for (int i = 0; i < pointsCountWithinXRange * 2; i += 2) {
            float x = getDrawX(xRangePoints[i]);
            float distance = Math.abs(x - centerX);
            if (distance <= closestDistance) {
                // if point has same distance - increment points count
                if (distance == closestDistance) {
                    closestPointsCount++;
                } else {
                    closestPointsCount = 0;
                }
                closestDistance = distance;
                if (closestPointsBuffer.length < (closestPointsCount * 2 + 2)) {
                    closestPointsBuffer = new float[closestPointsBuffer.length * 2];
                }
                closestPointsBuffer[closestPointsCount * 2 + 0] = xRangePoints[i];
                closestPointsBuffer[closestPointsCount * 2 + 1] = xRangePoints[i + 1];
            }
        }
        if (pointsCountWithinXRange > 0) {
            closestPointsCount++; // increment because we have at least 1 point
        }
    }

    /**
     * Compute stops for Y Axis
     *
     * @param minY min Y value (bottom value constraint)
     * @param maxY max Y value (top value constraint)
     */
    private void computeYAxisStops(float minY, float maxY) {
        int start = (int) minY;
        int end = (int) Math.ceil(maxY);
        float range = end - start;
        if (range < 0) {
            return;
        }

        int stepsCount = 2;
        if (range == 0) {
            start -= stepsCount;
            end += stepsCount;
        }
        int interval = range == 0 ? stepsCount : (int) Math.ceil(range / stepsCount);
        while (start + interval * stepsCount <= end) {
            stepsCount++;
            interval = range == 0 ? stepsCount : (int) Math.ceil(range / stepsCount);
        }

        float value = start;
        int n = 1;
        while (value <= end) {
            value += interval;
            n++;
        }
        yStops.numStops = n;
        if (yStops.stops.length < n) {
            yStops.stops = new float[n];
        }
        value = start;
        for (int i = 0; i < n; i++, value += interval) {
            yStops.stops[i] = value;
        }
    }

    /**
     * Compute stops for X Axis
     */
    public void computeXAxis() {
        xStops.numStops = visibleXRange;
        if (xStops.stops.length < visibleXRange) {
            xStops.stops = new float[visibleXRange];
        }
        int value = (int) Math.ceil(viewportRect.left);
        for (int i = 0; i < visibleXRange; i++, value += xInterval) {
            xStops.stops[i] = value;
        }
    }

    /**
     * Reacts on fling from GestureDetector's fling hook
     *
     * @param scroller  scroller to start fling
     * @param velocityX X fling velocity
     * @param velocityY Y fling velocity
     */
    public void fling(OverScroller scroller, int velocityX, int velocityY) {
        fling = true;
        // +/- contentRect.width() / 2 is to be able to scroll half of the screen left/right
        // (for centralLine to be able to reach the most left/right point)
        int minX = 0 - contentRect.width() / 2;
        int maxX = (surfaceSize.x - contentRect.width()) + contentRect.width() / 2;
        scroller.forceFinished(true);
        scroller.fling(
                startX(),
                startY(),
                velocityX,
                velocityY,
                minX, maxX,
                0, surfaceSize.y - contentRect.height(),
                contentRect.width() / 2,
                contentRect.height() / 2
        );
    }

    /**
     * Reacts on scroll from GestureDetector's scroll hook
     *
     * @param distanceX scrolled X distance in pixels
     * @param distanceY scrolled Y distance in pixels
     */
    public void onScroll(float distanceX, float distanceY) {
        // transform drawing X/Y to viewport's X/Y
        float viewportXOffset = distanceX / contentRect.width() * viewportRect.width();
        // compute how much we can scroll
        computeScrollSurfaceSize();
        // compute X-axis values
        computeXAxis();
        // set new viewports left
        move(viewportRect.left + viewportXOffset);
    }

    /**
     * Returns start X value to perform scroll or fling
     */
    public int startX() {
        return (int) (surfaceSize.x * (viewportRect.left - minX) / (maxX - minX));
    }

    /**
     * Returns start Y value to perform scroll or fling
     */
    public int startY() {
        return (int) (surfaceSize.y * (maxY - viewportRect.bottom) / (maxY - minY));
    }

    /**
     * @param x current X value in pixels
     * @return current X value of the viewport
     */
    public float currentXRange(float x) {
        return minX + (maxX - minX) * (x / surfaceSize.x);
    }

    /**
     * @param y current Y value in pixels
     * @return current Y value of the viewport
     */
    public float currentYRange(float y) {
        return maxY - (maxY - minY) * (y / surfaceSize.y);
    }

    /**
     * Transforms viewport's X value to X value for drawing on canvas (pixels)
     *
     * @param x viewport's X value
     * @return draw X value
     */
    public float getDrawX(float x) {
        return contentRect.left + contentRect.width() * (x - viewportRect.left) / viewportRect.width();
    }

    /**
     * Transforms viewport's Y value to Y value for drawing on canvas (pixels)
     *
     * @param y viewport's Y value
     * @return Y value for drawing on canvas
     */
    public float getDrawY(float y) {
        return contentRect.bottom - contentRect.height() * (y - viewportRect.top) / viewportRect.height();
    }

    public void setVisibleXRange(int visibleXRange) {
        this.visibleXRange = visibleXRange;
    }

    public void setXInterval(int xInterval) {
        this.xInterval = xInterval;
    }

    public boolean isFling() {
        return fling;
    }

    public void setFling(boolean fling) {
        this.fling = fling;
    }

    public float top() {
        return viewportRect.top;
    }

    public void setTop(float top) {
        viewportRect.top = top;
    }

    public float bottom() {
        return viewportRect.bottom;
    }

    public void setBottom(float bottom) {
        viewportRect.bottom = bottom;
    }
}
