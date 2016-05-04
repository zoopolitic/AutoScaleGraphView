# AutoScaleGraphView
Auto Scale Graph View allows create GoogleFit-like autoscaling graph

<img src="https://github.com/zoopolitic/AutoScaleGraphView/blob/master/demo1.gif?raw=true" width="50%"/> 

## HowTo

### XML:
```xml
<com.zoopolitic.graphview.AutoScaleGraphView
    android:id="@+id/graphView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_centerVertical="true"
    android:paddingTop="16dp"
    custom:axisWidth="0.5dp"
    custom:centralLineColor="#bdbdbd"
    custom:centralLineDashWidth="20"
    custom:centralLineGapWidth="15"
    custom:centralLineWidth="2dp"
    custom:drawCentralLabel="true"
    custom:drawCentralLine="true"
    custom:drawFocusedPoints="true"
    custom:focusedPointColor="@android:color/holo_red_dark"
    custom:focusedPointRadius="10dp"
    custom:focusedPointStrokeWidth="2dp"
    custom:gridColor="#bdbdbd"
    custom:gridLabelColor="#bdbdbd"
    custom:gridLabelSeparation="6dp"
    custom:labelBackgroundColor="#B3e0e0e0"
    custom:labelCentralLineOffset="6dp"
    custom:labelCornerRadius="1dp"
    custom:labelPaddingBottom="6dp"
    custom:labelPaddingLeft="6dp"
    custom:labelPaddingRight="6dp"
    custom:labelPaddingTop="6dp"
    custom:labelStrokeColor="#bdbdbd"
    custom:labelStrokeWidth="1dp"
    custom:labelTextColor="@android:color/black"
    custom:labelTextSize="14sp"
    custom:lineWidth="4dp"
    custom:pointClickRadius="16dp"
    custom:pointRadius="5dp"
    custom:scaleDuration="200"
    custom:snapDuration="200"
    custom:snapEnabled="true"
    custom:visibleXRange="7"
    custom:xAxisLabelSeparation="12dp"
    custom:xAxisTextSize="12sp"
    custom:xInterval="1"
    custom:yAxisLabelSeparation="12dp"
    custom:yAxisTextSize="12sp"
    />
```

### Usage

```java
    List<DataPoint> points = new ArrayList<>();
        
    points.add(new DataPoint(0, 10));
    points.add(new DataPoint(0, 15));
    points.add(new DataPoint(0, 20));
    points.add(new DataPoint(0, 18));
        
    graphView.addDataSet(new DataSet(lineColor, pointsColor, points));
```

## Gradle

```groovy
compile 'com.zoopolitic:auto-scale-graphview:0.1'
```

**Maven:**
```xml
<dependency>
  <groupId>com.zoopolitic</groupId>
  <artifactId>auto-scale-graphview</artifactId>
  <version>0.1</version>
  <type>pom</type>
</dependency>
```

## License

```
AutoScaleGraphView library for Android
Copyright (c) 2016 Alex Perevozchykov (http://github.com/zoopolitic).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
