package com.zoopolitic.graphview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoopolitic on 27 Апрель 2016 16:06.
 */
public class DataSet {

    private float maxX = Float.MIN_VALUE;
    private float minX = Float.MAX_VALUE;

    private float maxY = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE;

    private int lineColor;
    private int pointColor;

    private List<DataPoint> points;

    public DataSet(int lineColor, int pointColor) {
        this.lineColor = lineColor;
        this.pointColor = pointColor;
        this.points = new ArrayList<>();
    }

    public DataSet(int lineColor, int pointColor, List<DataPoint> points) {
        this.lineColor = lineColor;
        this.pointColor = pointColor;
        this.points = points;
        int size = points.size();
        for (int i = 0; i < size; i++) {
            calculateMinMax(points.get(i));
        }
    }

    public float getLevelWidth() {
        return maxX - minX;
    }

    public float getLevelHeight() {
        return maxY - minY;
    }

    public void addPoint(DataPoint point) {
        this.points.add(point);
        calculateMinMax(point);
    }

    public void removePoint(DataPoint point) {
        boolean removed = this.points.remove(point);
        if (removed) {
            if (this.maxX == point.x || this.minX == point.x
                    || this.maxY == point.y || this.minY == point.y) {
                int size = this.points.size();
                for (int i = 0; i < size; i++) {
                    calculateMinMax(this.points.get(i));
                }
            }
        }
    }

    private void calculateMinMax(DataPoint point) {
        if (point.x > maxX) {
            maxX = point.x;
        }
        if (point.x < minX) {
            minX = point.x;
        }
        if (point.y > maxY) {
            maxY = point.y;
        }
        if (point.y < minY) {
            minY = point.y;
        }
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMinY() {
        return minY;
    }

    public int getPointColor() {
        return pointColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public List<DataPoint> getPoints() {
        return points;
    }
}
