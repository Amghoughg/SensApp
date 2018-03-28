package it.cnr.iit.sensapp.controllers;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.utils.ChartData;

/**
 * Created by mattia on 27.01.18.
 */
public class UIController {

    public static void createPieChart(PieChart chart, Context context,
                                      Legend.LegendOrientation legendOrientation,
                                      Legend.LegendVerticalAlignment legendVerticalAlignment,
                                      Legend.LegendHorizontalAlignment legendHorizontalAlignment,
                                      List<ChartData.FrequencyData> dataFrequency,
                                      int...colors){

        chart.setTransparentCircleRadius(50f);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setRotationEnabled(false);

        chart.setExtraOffsets(50,0,0,0);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chart.getDescription().setEnabled(false);

        chart.setUsePercentValues(true);
        chart.setHoleRadius(40f);

        Legend l = chart.getLegend();
        l.setOrientation(legendOrientation);
        l.setVerticalAlignment(legendVerticalAlignment);
        l.setHorizontalAlignment(legendHorizontalAlignment);
        l.setStackSpace(100f);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTypeface(ResourcesCompat.getFont(context, R.font.myfont));
        l.setTextSize(12f);

        chart.offsetLeftAndRight(5);

        chart.setDrawEntryLabels(false);

        List<PieEntry> vals = new ArrayList<>();
        for(ChartData.FrequencyData tagFrequency : dataFrequency){
            vals.add(new PieEntry(tagFrequency.frequency, tagFrequency.tag));
        }

        PieDataSet dataSet = new PieDataSet(vals, null);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new CustomPercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }

    public static void createBarChart(BarChart chart, Context context, List<Integer> frequencyData){

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //chart.getDescription().setEnabled(false);

        Legend l = chart.getLegend();
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTypeface(ResourcesCompat.getFont(context, R.font.myfont));
        l.setTextSize(15f);

        chart.setDescription(null);    // Hide the description
        chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setDrawGridLines(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.setTouchEnabled(false);
        chart.getXAxis().setValueFormatter(new LabelFormatter(frequencyData));
        chart.getAxisLeft().setGranularity(1.0f);
        chart.getAxisLeft().setGranularityEnabled(true);
        if(frequencyData.size() != 0)
            chart.getAxisLeft().setAxisMaximum(Collections.max(frequencyData));
        chart.setDrawBorders(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getXAxis().setDrawAxisLine(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        List<BarEntry> entries = new ArrayList<>();
        for(int i=0; i<frequencyData.size(); i++) entries.add(new BarEntry(i, frequencyData.get(i)));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#DD6E42"));
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(1f);

        /*PieData data = new PieData(dataSet);
        data.setValueFormatter(new CustomPercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);*/

        chart.setData(data);
        chart.invalidate();
    }

    private static class CustomPercentFormatter implements IValueFormatter {


        private DecimalFormat mFormat;

        public CustomPercentFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0");
        }

        public CustomPercentFormatter(DecimalFormat format) {
            this.mFormat = format;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {

            if (value == 0.0f)
                return "";

            return mFormat.format(value) + " %";
        }
    }

    public static class LabelFormatter implements IAxisValueFormatter {
        private final List<Integer> mLabels;

        public LabelFormatter(List<Integer> labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return String.valueOf(mLabels.get((int)value));
        }
    }
}
