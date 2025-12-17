package com.example.afinal;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for creating and styling MPAndroidChart charts
 * according to Nomadstay design system.
 */
public class ChartHelper {
    
    // Nomadstay colors
    private static final int COLOR_PRIMARY = Color.parseColor("#F97316");
    private static final int COLOR_PRIMARY_VARIANT = Color.parseColor("#FB923C");
    private static final int COLOR_SUCCESS = Color.parseColor("#16A34A");
    private static final int COLOR_ERROR = Color.parseColor("#DC2626");
    private static final int COLOR_TEXT_PRIMARY = Color.parseColor("#0F172A");
    private static final int COLOR_TEXT_SECONDARY = Color.parseColor("#64748B");
    private static final int COLOR_SURFACE = Color.parseColor("#FFFFFF");
    private static final int COLOR_OUTLINE = Color.parseColor("#E5E7EB");
    
    /**
     * Setup a LineChart for score history over days
     * @param chart The LineChart view
     * @param scores List of scores (y values)
     * @param labels List of day labels (x values)
     */
    public static void setupScoreLineChart(LineChart chart, List<Float> scores, List<String> labels) {
        if (chart == null || scores == null || scores.isEmpty()) {
            return;
        }
        
        // Create entries
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            entries.add(new Entry(i, scores.get(i)));
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Điểm");
        dataSet.setColor(COLOR_PRIMARY);
        dataSet.setCircleColor(COLOR_PRIMARY);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleColor(COLOR_SURFACE);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(COLOR_PRIMARY);
        dataSet.setFillAlpha(30);
        
        // Configure chart
        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setExtraOffsets(8, 8, 8, 8);
        
        // Configure X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(COLOR_TEXT_SECONDARY);
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);
        if (labels != null && !labels.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }
        
        // Configure Y axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(COLOR_OUTLINE);
        leftAxis.setTextColor(COLOR_TEXT_SECONDARY);
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        
        chart.getAxisRight().setEnabled(false);
        
        // Configure legend
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        
        chart.animateX(500);
        chart.invalidate();
    }
    
    /**
     * Setup a BarChart for topic accuracy percentages
     * @param chart The BarChart view
     * @param topicAccuracies Map of topic names to accuracy percentages
     */
    public static void setupTopicBarChart(BarChart chart, Map<String, Float> topicAccuracies) {
        if (chart == null || topicAccuracies == null || topicAccuracies.isEmpty()) {
            return;
        }
        
        // Create entries
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : topicAccuracies.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "% Đúng");
        dataSet.setColor(COLOR_PRIMARY);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(COLOR_TEXT_PRIMARY);
        dataSet.setValueTextSize(10f);
        
        // Configure chart
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setExtraOffsets(8, 8, 8, 8);
        chart.setFitBars(true);
        
        // Configure X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(COLOR_TEXT_SECONDARY);
        xAxis.setTextSize(9f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45f);
        
        // Configure Y axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(COLOR_OUTLINE);
        leftAxis.setTextColor(COLOR_TEXT_SECONDARY);
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        
        chart.getAxisRight().setEnabled(false);
        
        // Configure legend
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        
        chart.animateY(500);
        chart.invalidate();
    }
    
    /**
     * Setup a PieChart for correct/incorrect ratio
     * @param chart The PieChart view
     * @param correct Number of correct answers
     * @param incorrect Number of incorrect answers
     */
    public static void setupCorrectIncorrectPieChart(PieChart chart, int correct, int incorrect) {
        if (chart == null) {
            return;
        }
        
        // Handle empty data
        if (correct == 0 && incorrect == 0) {
            chart.setNoDataText("Chưa có dữ liệu");
            chart.setNoDataTextColor(COLOR_TEXT_SECONDARY);
            chart.invalidate();
            return;
        }
        
        // Create entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (correct > 0) {
            entries.add(new PieEntry(correct, "Đúng"));
        }
        if (incorrect > 0) {
            entries.add(new PieEntry(incorrect, "Sai"));
        }
        
        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        if (correct > 0) {
            colors.add(COLOR_SUCCESS);
        }
        if (incorrect > 0) {
            colors.add(COLOR_ERROR);
        }
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);
        
        // Configure chart
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(COLOR_SURFACE);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setDrawCenterText(true);
        
        // Calculate percentage
        int total = correct + incorrect;
        float percentage = total > 0 ? (correct * 100f / total) : 0f;
        chart.setCenterText(String.format("%.0f%%", percentage));
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(COLOR_TEXT_PRIMARY);
        
        chart.setTouchEnabled(false);
        chart.setRotationEnabled(false);
        chart.setExtraOffsets(8, 8, 8, 8);
        
        // Configure legend
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(COLOR_TEXT_SECONDARY);
        legend.setTextSize(11f);
        legend.setDrawInside(false);
        
        chart.animateY(500);
        chart.invalidate();
    }
    
    /**
     * Setup empty/placeholder state for a LineChart
     */
    public static void setupEmptyLineChart(LineChart chart) {
        if (chart == null) return;
        chart.setNoDataText("Chưa có dữ liệu thống kê");
        chart.setNoDataTextColor(COLOR_TEXT_SECONDARY);
        chart.invalidate();
    }
    
    /**
     * Setup empty/placeholder state for a BarChart
     */
    public static void setupEmptyBarChart(BarChart chart) {
        if (chart == null) return;
        chart.setNoDataText("Chưa có dữ liệu thống kê");
        chart.setNoDataTextColor(COLOR_TEXT_SECONDARY);
        chart.invalidate();
    }
}

