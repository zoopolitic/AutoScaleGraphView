package com.zoopolitic.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zoopolitic.graphview.AutoScaleGraphView;
import com.zoopolitic.graphview.DataPoint;
import com.zoopolitic.graphview.DataSet;
import com.zoopolitic.graphview.formatter.SimpleFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private Random rnd = new Random();
    private AutoScaleGraphView graphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = Calendar.getInstance();
        graphView = (AutoScaleGraphView) findViewById(R.id.graphView);
        graphView.setXAxisFormatter(new SimpleFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float index) {
                calendar.set(Calendar.DAY_OF_YEAR, (int) index);
                return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()).toUpperCase()
                        + "\n" + calendar.get(Calendar.DAY_OF_MONTH);
            }
        });
        addRandomDataSet();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_enable_disable_snap).setChecked(graphView.isSnapEnabled());
        menu.findItem(R.id.action_show_hide_central_label).setChecked(graphView.isDrawCentralLabel());
        menu.findItem(R.id.action_show_hide_central_line).setChecked(graphView.isDrawCentralLine());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean checked = item.isChecked();
        switch (item.getItemId()) {
            case R.id.action_add_data_set:
                addRandomDataSet();
                return true;
            case R.id.action_enable_disable_snap:
                graphView.setSnapEnabled(!checked);
                item.setChecked(!checked);
                return true;
            case R.id.action_move_end:
                graphView.moveEnd();
                return true;
            case R.id.action_move_start:
                graphView.moveStart();
                return true;
            case R.id.action_pan_left:
                graphView.scrollXBy(-5, 200);
                return true;
            case R.id.action_pan_right:
                graphView.scrollXBy(5, 200);
                return true;
            case R.id.action_show_hide_central_label:
                graphView.setDrawCentralLabel(!checked);
                item.setChecked(!checked);
                return true;
            case R.id.action_show_hide_central_line:
                graphView.setDrawCentralLine(!checked);
                item.setChecked(!checked);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addRandomDataSet() {
        int lineColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        int pointsColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        List<DataPoint> points = new ArrayList<>();
        int observingDaysCount = 110;
        Random r = new Random();
        calendar.add(Calendar.DAY_OF_YEAR, -observingDaysCount);
        for (int i = 1; i < observingDaysCount; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            int rndVal = r.nextInt(4 - 1 + 1) + 1;
            if (i % rndVal == 0) {
                float val = (r.nextFloat() * (75 - 66) + 66);
                points.add(new DataPoint(
                        calendar.get(Calendar.DAY_OF_YEAR),
                        (float) roundToDecimalPlaces(val, 1)
                ));
            }
        }
        graphView.addDataSet(new DataSet(lineColor, pointsColor, points));
    }

    public static double roundToDecimalPlaces(double value, int decimalPlaces) {
        double shift = Math.pow(10, decimalPlaces);
        return Math.round(value * shift) / shift;
    }
}
