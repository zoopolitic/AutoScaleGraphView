package com.zoopolitic.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import com.zoopolitic.graphview.formatter.LabelFormatter;
import com.zoopolitic.graphview.formatter.SimpleFormatter;
import com.zoopolitic.graphview.formatter.SimpleLabelFormatter;
import com.zoopolitic.graphview.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import static com.zoopolitic.graphview.AndroidUtils.dpToPx;

/**
 * Created by zoopolitic
 */
public class AutoScaleGraphView extends View {

    private static final String TAG = "GraphView";

    private boolean snapEnabled;
    private boolean drawCentralLine;
    private boolean drawCentralLabel;

    /**
     * Scale animation duration
     */
    private int scaleDuration;

    /**
     * Snap animation duration
     */
    private int snapDuration;

    private Paint linePaint;
    private float graphLineWidth;

    private Paint pointsPaint;
    private int   pointRadius;
    private int   pointClickRadius;

    private int     focusedPointRadius;
    private Paint   focusedPointPaint;
    private int     focusedPointColor;
    private int     focusedPointStrokeWidth;
    private boolean drawFocusedPoints;

    private Paint centralLinePaint;
    private Path  centralLine;
    private int   centralLineColor;
    private int   centralLineWidth;
    private int   centralLineDashWidth;
    private int   centralLineGapWidth;

    private Paint yAxisPaint;
    private Paint yAxisLabelPaint;
    private Paint xAxisLabelPaint;
    private int   gridColor;
    private int   gridLabelColor;
    private int   yAxisLabelSeparation;
    private int   xAxisLabelSeparation;

    private int yAxisLabelMaxWidth;
    private int yAxisLabelHeight;
    private int xAxisLabelHeight;
    private int yAxisTextSize;
    private int xAxisTextSize;

    private Paint labelPaint;
    private Paint labelTextPaint;
    private int   labelBackgroundColor;
    private int   labelStrokeColor;
    private int   labelStrokeWidth;
    private int   labelTextColor;
    private int   labelCornerRadius;
    private int   labelTextSize;
    private int   labelPaddingLeft;
    private int   labelPaddingRight;
    private int   labelPaddingTop;
    private int   labelPaddingBottom;
    private int   labelCentralLineOffset;
    private Rect textBoundsBuffer = new Rect();

    private List<DataSet> dataSets = new ArrayList<>();

    private float[] graphLinesBuffer = new float[]{};

    private float[] axisYLinesBuffer     = new float[]{};
    private char[]  labelsBuffer         = new char[100];
    private float[] axisYPositionsBuffer = new float[]{};
    private float[] axisXPositionsBuffer = new float[]{};

    private int yAxisWidth;

    private ViewportManager viewportManager = new ViewportManager();

    private boolean computingScroll;

    private OverScroller   scroller;
    private VerticalScaler scaler;

    private GestureDetectorCompat gestureDetector;

    private ValueFormatter xAxisFormatter = new SimpleFormatter();
    private LabelFormatter labelFormatter = new SimpleLabelFormatter();

    public AutoScaleGraphView(Context context) {
        this(context, null, 0);
    }

    public AutoScaleGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScaleGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void d(String message) {
        Log.d(TAG, message);
    }

    private void w(String message) {
        Log.w(TAG, message);
    }

    private void i(String message) {
        Log.i(TAG, message);
    }

    private void e(String message) {
        Log.e(TAG, message);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.AutoScaleGraphView, defStyleAttr, defStyleAttr);
        int visibleXRange;
        int xInterval;
        try {
            this.snapEnabled = array.getBoolean(R.styleable.AutoScaleGraphView_snapEnabled, true);
            this.scaleDuration = array.getInteger(R.styleable.AutoScaleGraphView_scaleDuration, 300);
            this.snapDuration = array.getInteger(R.styleable.AutoScaleGraphView_snapDuration, 300);
            this.focusedPointColor = array.getColor(R.styleable.AutoScaleGraphView_focusedPointColor, Color.BLACK);
            this.focusedPointStrokeWidth = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_focusedPointStrokeWidth, (int) dpToPx(2));
            this.drawFocusedPoints = array.getBoolean(R.styleable.AutoScaleGraphView_drawFocusedPoints, true);
            this.graphLineWidth = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_lineWidth, (int) dpToPx(2));
            this.pointRadius = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_pointRadius, (int) dpToPx(2));
            this.focusedPointRadius = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_focusedPointRadius, pointRadius * 2);
            this.pointClickRadius = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_pointClickRadius, (int) dpToPx(16));

            this.labelBackgroundColor = array.getColor(R.styleable.AutoScaleGraphView_labelBackgroundColor, Color.parseColor("#B3e0e0e0"));
            this.labelStrokeColor = array.getColor(R.styleable.AutoScaleGraphView_labelStrokeColor, Color.parseColor("#bdbdbd"));
            this.labelStrokeWidth = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelStrokeWidth, (int) dpToPx(1));
            this.labelTextColor = array.getColor(R.styleable.AutoScaleGraphView_labelTextColor, Color.BLACK);
            this.labelCornerRadius = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelCornerRadius, (int) dpToPx(2));
            this.labelTextSize = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelTextSize, (int) dpToPx(14));
            this.labelPaddingLeft = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelPaddingLeft, (int) dpToPx(8));
            this.labelPaddingTop = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelPaddingTop, (int) dpToPx(8));
            this.labelPaddingRight = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelPaddingRight, (int) dpToPx(8));
            this.labelPaddingBottom = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelPaddingBottom, (int) dpToPx(8));
            this.labelCentralLineOffset = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_labelCentralLineOffset, (int) dpToPx(6));

            this.centralLineWidth = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_centralLineWidth, (int) dpToPx(2));
            this.centralLineDashWidth = array.getInt(R.styleable.AutoScaleGraphView_centralLineDashWidth, 20);
            this.centralLineGapWidth = array.getInt(R.styleable.AutoScaleGraphView_centralLineGapWidth, 15);
            this.centralLineColor = array.getColor(R.styleable.AutoScaleGraphView_centralLineColor, Color.LTGRAY);
            this.drawCentralLabel = array.getBoolean(R.styleable.AutoScaleGraphView_drawCentralLabel, true);
            this.drawCentralLine = array.getBoolean(R.styleable.AutoScaleGraphView_drawCentralLine, true);

            this.gridColor = array.getColor(R.styleable.AutoScaleGraphView_gridColor, Color.parseColor("#e0e0e0"));
            this.gridLabelColor = array.getColor(R.styleable.AutoScaleGraphView_gridLabelColor, gridColor);
            this.yAxisLabelSeparation = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_gridLabelSeparation, (int) dpToPx(6));
            this.yAxisWidth = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_axisWidth, (int) dpToPx(1));
            this.yAxisTextSize = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_yAxisTextSize, (int) dpToPx(12));
            this.xAxisTextSize = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_xAxisTextSize, (int) dpToPx(12));
            this.yAxisLabelSeparation = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_yAxisLabelSeparation, (int) dpToPx(6));
            this.xAxisLabelSeparation = array.getDimensionPixelSize(R.styleable.AutoScaleGraphView_xAxisLabelSeparation, (int) dpToPx(12));
            visibleXRange = array.getInt(R.styleable.AutoScaleGraphView_visibleXRange, 7);
            xInterval = array.getInt(R.styleable.AutoScaleGraphView_xInterval, 1);
        } finally {
            array.recycle();
        }
        initPaints();

        scroller = new OverScroller(context, new AccelerateDecelerateInterpolator());
        scaler = new VerticalScaler(new FastOutSlowInInterpolator());
        scaler.setScaleDuration(scaleDuration);
        setVisibleXRange(visibleXRange);
        setXInterval(xInterval);

        gestureDetector = new GestureDetectorCompat(context, gestureListener);

        yAxisLabelHeight = (int) Math.abs(yAxisLabelPaint.getFontMetrics().top);
        yAxisLabelMaxWidth = (int) yAxisLabelPaint.measureText("0000");
    }

    public void setVisibleXRange(int visibleXRange) {
        viewportManager.setVisibleXRange(visibleXRange);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setXInterval(int xInterval) {
        viewportManager.setXInterval(xInterval);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    public void addDataSet(DataSet dataSet) {
        this.dataSets.add(dataSet);
        viewportManager.calculateMinMax(dataSet);
        viewportManager.computeClosestPoints();
        viewportManager.computeXAxis();
        scale();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void initPaints() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(graphLineWidth);
        linePaint.setAlpha((int) (0.75f * 255));

        pointsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointsPaint.setStyle(Paint.Style.FILL);
        pointsPaint.setStrokeCap(Paint.Cap.ROUND);
        pointsPaint.setStrokeWidth(pointRadius * 2);

        focusedPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusedPointPaint.setStyle(Paint.Style.STROKE);
        focusedPointPaint.setColor(focusedPointColor);
        focusedPointPaint.setStrokeCap(Paint.Cap.ROUND);
        focusedPointPaint.setStrokeWidth(focusedPointStrokeWidth);

        centralLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centralLinePaint.setStyle(Paint.Style.STROKE);
        centralLinePaint.setColor(centralLineColor);
        centralLinePaint.setStrokeCap(Paint.Cap.ROUND);
        centralLinePaint.setStrokeWidth(centralLineWidth);
        centralLinePaint.setPathEffect(new DashPathEffect(new float[]{centralLineDashWidth, centralLineGapWidth}, 0));

        yAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yAxisPaint.setStyle(Paint.Style.STROKE);
        yAxisPaint.setColor(gridColor);
        yAxisPaint.setStrokeWidth(yAxisWidth);

        yAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yAxisLabelPaint.setStyle(Paint.Style.STROKE);
        yAxisLabelPaint.setColor(gridLabelColor);
        yAxisLabelPaint.setTextSize(yAxisTextSize);
        yAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);

        xAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xAxisLabelPaint.setStyle(Paint.Style.STROKE);
        xAxisLabelPaint.setColor(gridLabelColor);
        xAxisLabelPaint.setTextSize(xAxisTextSize);
        xAxisLabelPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setPathEffect(new CornerPathEffect(labelCornerRadius));
        labelPaint.setStrokeWidth(labelStrokeWidth);

        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setStyle(Paint.Style.STROKE);
        labelTextPaint.setColor(labelTextColor);
        labelTextPaint.setTextSize(labelTextSize);
        labelTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewportManager.constrainDrawRect(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom()
        );
        if (snapEnabled) {
            snapToClosestPoint();
        }
        scale();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_chart_size);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minChartSize + getPaddingLeft() + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minChartSize + getPaddingTop() + getPaddingBottom(),
                                heightMeasureSpec)));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        viewportManager.computeClosestPoints();
        drawAxes(canvas);

        if (!dataSets.isEmpty() && drawCentralLine) {
            drawCentralLine(canvas);
        }

        for (DataSet dataSet : dataSets) {
            int lineCount = drawDataSet(canvas, dataSet);
            drawPoints(canvas, dataSet, lineCount);
        }
        if (drawFocusedPoints) {
            drawFocusedCircles(canvas);
        }

        if (drawCentralLabel) {
            drawDataLabel(canvas);
        }
    }

    private void drawPoints(Canvas canvas, DataSet dataSet, int lineCount) {
        int clipRestoreCount = canvas.save();
        canvas.clipRect(
                viewportManager.contentRect.left,
                viewportManager.contentRect.top,
                viewportManager.contentRect.right,
                viewportManager.contentRect.bottom - getGraphBottomOffset() + pointRadius
        );
        pointsPaint.setColor(dataSet.getPointColor());
        canvas.drawPoints(graphLinesBuffer, 0, lineCount * 4, pointsPaint);
        canvas.restoreToCount(clipRestoreCount);
    }

    private void drawAxes(Canvas canvas) {
        AxisStops yStops = viewportManager.yStops;
        AxisStops xStops = viewportManager.xStops;
        if (axisYLinesBuffer.length < yStops.numStops * 4) {
            axisYLinesBuffer = new float[yStops.numStops * 4];
        }

        if (axisYPositionsBuffer.length < yStops.numStops) {
            axisYPositionsBuffer = new float[yStops.numStops];
        }

        if (axisXPositionsBuffer.length < xStops.numStops) {
            axisXPositionsBuffer = new float[xStops.numStops];
        }

        for (int i = 0; i < yStops.numStops; i++) {
            axisYPositionsBuffer[i] = viewportManager.getDrawY(yStops.stops[i]);
        }

        for (int i = 0; i < xStops.numStops; i++) {
            axisXPositionsBuffer[i] = viewportManager.getDrawX(xStops.stops[i]);
        }

        int left = viewportManager.contentRect.left + yAxisLabelMaxWidth + yAxisLabelSeparation;
        for (int i = 0; i < yStops.numStops; i++) {
            //noinspection PointlessArithmeticExpression
            axisYLinesBuffer[i * 4 + 0] = left;
            axisYLinesBuffer[i * 4 + 1] = axisYPositionsBuffer[i];
            axisYLinesBuffer[i * 4 + 2] = viewportManager.contentRect.right;
            axisYLinesBuffer[i * 4 + 3] = axisYPositionsBuffer[i];
        }
        canvas.drawLines(axisYLinesBuffer, 0, yStops.numStops * 4, yAxisPaint);

        // draw Y labels
        int labelLength;
        int labelOffset;
        for (int i = 0; i < yStops.numStops; i++) {
            labelLength = AndroidUtils.formatFloat(labelsBuffer, yStops.stops[i], 0);
            labelOffset = labelsBuffer.length - labelLength;
            canvas.drawText(
                    labelsBuffer, labelOffset, labelLength,
                    viewportManager.contentRect.left + yAxisLabelMaxWidth,
                    axisYPositionsBuffer[i] + yAxisLabelHeight * 1f / 4,
                    yAxisLabelPaint);
        }

        boolean needInitialScale = xAxisLabelHeight == 0;
        // draw X labels
        for (int i = 0; i < xStops.numStops; i++) {
            String text = xAxisFormatter.getFormattedValue(xStops.stops[i]);
            if (text == null) {
                return;
            }
            float lineHeight = xAxisLabelPaint.descent() - xAxisLabelPaint.ascent();
            String[] lines = text.split("\n");
            float totalHeight = lineHeight * lines.length;
            xAxisLabelHeight = Math.max(xAxisLabelHeight, (int) totalHeight); // check for "tallest" label
            float y = viewportManager.contentRect.bottom - xAxisLabelSeparation - totalHeight / 2;
            for (String line : lines) {
                canvas.drawText(line, axisXPositionsBuffer[i], y, xAxisLabelPaint);
                y += lineHeight;
            }
        }
        // here label height is calculated
        if (needInitialScale) {
            scale();
        }
    }

    private float getGraphBottomOffset() {
        return xAxisLabelHeight + xAxisLabelSeparation * 2;
    }

    private void drawDataLabel(Canvas canvas) {
        float x = viewportManager.contentRect.centerX();
        float y = viewportManager.contentRect.centerY();
        String text = labelFormatter.getFormattedValue(
                viewportManager.closestPointsBuffer, viewportManager.closestPointsCount * 2);
        if (TextUtils.isEmpty(text)) {
            return;
        }

        String maxLine = null;
        String[] lines = text.split("\n");
        labelTextPaint.getTextBounds("A", 0, 1, textBoundsBuffer);
        float oneLineHeight = textBoundsBuffer.height();
        float divider = oneLineHeight * 0.5f;
        float totalHeight = oneLineHeight * lines.length;
        for (String line : lines) {
            if (maxLine == null || maxLine.length() < line.length()) {
                maxLine = line;
            }
        }
        totalHeight += divider * (lines.length - 1);

        float textWidth = labelTextPaint.measureText(maxLine);

        // draw background
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(labelBackgroundColor);
        float left = x + labelCentralLineOffset;
        float top = y - totalHeight / 2 - labelPaddingTop;
        float right = left + textWidth + labelPaddingRight + labelPaddingLeft;
        float bottom = y + totalHeight / 2 + labelPaddingBottom;
        canvas.drawRect(left, top, right, bottom, labelPaint);

        // draw stroke
        labelPaint.setStyle(Paint.Style.STROKE);
        labelPaint.setColor(labelStrokeColor);
        canvas.drawRect(left, top, right, bottom, labelPaint);

        // draw label
        float h = lines.length == 1 ? 0 : totalHeight / 2;
        float textY = y - h + (lines.length == 1 ? oneLineHeight / 2 : oneLineHeight);
        for (String line : lines) {
            canvas.drawText(line, x + labelCentralLineOffset + labelPaddingLeft, textY, labelTextPaint);
            textY += (oneLineHeight + divider);
        }
    }

    private void drawFocusedCircles(Canvas canvas) {
        int clipRestoreCount = canvas.save();
        canvas.clipRect(
                viewportManager.contentRect.left,
                viewportManager.contentRect.top,
                viewportManager.contentRect.right,
                viewportManager.contentRect.bottom - getGraphBottomOffset() + focusedPointRadius + focusedPointStrokeWidth
        );

        float centerX = viewportManager.contentRect.centerX();
        int size = viewportManager.closestPointsCount * 2;
        for (int i = 0; i < size; i += 2) {
            float x = viewportManager.getDrawX(viewportManager.closestPointsBuffer[i]);
            float y = viewportManager.getDrawY(viewportManager.closestPointsBuffer[i + 1]);
            float distance = Math.abs(Math.round(x - centerX));
            if (distance <= pointRadius) {
                float fraction = distance / pointRadius;
                float currRadius = (1 - fraction) * focusedPointRadius;
                currRadius = Math.max(pointRadius - focusedPointStrokeWidth, currRadius);
                canvas.drawCircle(x, y, currRadius, focusedPointPaint);
            }
        }

        canvas.restoreToCount(clipRestoreCount);
    }

    private void drawCentralLine(Canvas canvas) {
        if (centralLine == null) {
            float x = viewportManager.contentRect.centerX();
            centralLine = new Path();
            centralLine.moveTo(x, 0);
            centralLine.lineTo(x, viewportManager.contentRect.bottom);
        }
        int clipRestoreCount = canvas.save();
        canvas.clipRect(
                viewportManager.contentRect.left,
                viewportManager.contentRect.top,
                viewportManager.contentRect.right,
                viewportManager.contentRect.bottom - getGraphBottomOffset()
        );
        canvas.drawPath(centralLine, centralLinePaint);
        canvas.restoreToCount(clipRestoreCount);
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private int drawDataSet(Canvas canvas, DataSet dataSet) {
        List<DataPoint> points = dataSet.getPoints();
        int size = points.size();
        if (graphLinesBuffer.length < size * 4 - 4) {
            graphLinesBuffer = new float[size * 4 - 4];
        }

        int lineCount = 0;
        for (int i = 0, j = 0; i < size - 1; i++, j++, lineCount++) {
            DataPoint p1 = points.get(i);
            DataPoint p2 = points.get(i + 1);
            graphLinesBuffer[j * 4 + 0] = viewportManager.getDrawX(p1.x);
            graphLinesBuffer[j * 4 + 1] = viewportManager.getDrawY(p1.y);
            graphLinesBuffer[j * 4 + 2] = viewportManager.getDrawX(p2.x);
            graphLinesBuffer[j * 4 + 3] = viewportManager.getDrawY(p2.y);
        }

        int clipRestoreCount = canvas.save();
        canvas.clipRect(
                viewportManager.contentRect.left,
                viewportManager.contentRect.top,
                viewportManager.contentRect.right,
                viewportManager.contentRect.bottom - getGraphBottomOffset()
        );

        linePaint.setColor(dataSet.getLineColor());
        canvas.drawLines(graphLinesBuffer, 0, lineCount * 4, linePaint);

        canvas.restoreToCount(clipRestoreCount);

        return lineCount;
    }

    public void scale() {
        viewportManager.autoScale(scaler, getGraphBottomOffset(), dataSets);
    }

    public void snapToClosestPoint() {
        viewportManager.snapToClosestIndex(scroller, snapDuration);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Scrolling                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void computeScroll() {
        super.computeScroll();
        boolean needInvalidate = false;
        if (scroller.computeScrollOffset()) {
            computingScroll = true;

            viewportManager.computeXAxis();
            viewportManager.computeScrollSurfaceSize();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();

            // scroll finished
            if (x == scroller.getFinalX() && viewportManager.scrollInProgress) {
                scale();
            }
            viewportManager.move(viewportManager.currentXRange(x));
            needInvalidate = true;
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            // previous state was computing and current is not computing means that fling finished
            if (computingScroll && viewportManager.isFling()) {
                viewportManager.setFling(false);
                computingScroll = false;
                if (snapEnabled) {
                    snapToClosestPoint();
                } else {
                    scale();
                }
                needInvalidate = true;
            }
        }

        float newMinY = scaler.getCurrMinY();
        float newMaxY = scaler.getCurrMaxY();
        if (scaler.computeYScale() ||
                ((newMinY != 0 && viewportManager.top() != newMinY) // check to set final value for minY
                        || (newMaxY != 0 && viewportManager.bottom() != newMaxY))) {  // check to set final value for maxY
            viewportManager.setTop(newMinY);
            viewportManager.setBottom(newMaxY);
            needInvalidate = true;
        }
        if (needInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP && !retValue) {
            if (!viewportManager.clickedOnMarker(event.getX(), event.getY(), pointClickRadius, scroller, snapDuration)) {
                if (snapEnabled) {
                    snapToClosestPoint();
                } else {
                    scale();
                }
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
        return retValue || super.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            scroller.forceFinished(true);
            viewportManager.setFling(false);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            viewportManager.onScroll(distanceX, distanceY);
            ViewCompat.postInvalidateOnAnimation(AutoScaleGraphView.this);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            viewportManager.fling(scroller, (int) -velocityX, (int) -velocityY);
            ViewCompat.postInvalidateOnAnimation(AutoScaleGraphView.this);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            i("long press");
        }
    };

    public void scrollXBy(float distanceX, int duration) {
        viewportManager.scrollXBy(scroller, distanceX, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void moveStart() {
        viewportManager.moveStart();
        scale();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void moveEnd() {
        viewportManager.moveEnd();
        scale();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setXAxisFormatter(ValueFormatter xAxisFormatter) {
        this.xAxisFormatter = xAxisFormatter;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public boolean isDrawCentralLine() {
        return drawCentralLine;
    }

    public void setDrawCentralLine(boolean drawCentralLine) {
        this.drawCentralLine = drawCentralLine;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public boolean isDrawCentralLabel() {
        return drawCentralLabel;
    }

    public void setDrawCentralLabel(boolean drawCentralLabel) {
        this.drawCentralLabel = drawCentralLabel;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getSnapDuration() {
        return snapDuration;
    }

    public void setSnapDuration(int snapDuration) {
        this.snapDuration = snapDuration;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getScaleDuration() {
        return scaleDuration;
    }

    public void setScaleDuration(int scaleDuration) {
        this.scaleDuration = scaleDuration;
        this.scaler.setScaleDuration(scaleDuration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        ViewCompat.postInvalidateOnAnimation(this);
    }
}
