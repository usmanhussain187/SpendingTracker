package com.example.SpendingTracker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Graph_Activity2 extends AppCompatActivity {

    private PieChart pieChart;
    private List<Transaction> transactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph2);

        transactions = (List<Transaction>) getIntent().getSerializableExtra("transactions");

        pieChart = findViewById(R.id.pieChart);
        setupPieChart();
        loadChartData();

        Button btnViewChart = findViewById(R.id.btn_view_chart);
        btnViewChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Graph_Activity2
                Intent intent = new Intent(Graph_Activity2.this, Graph_Activity.class);
                intent.putExtra("transactions", (Serializable) transactions);
                startActivity(intent);
            }
        });

    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f);

        pieChart.setCenterText("Income");
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setCenterTextSize(12f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(34f);

        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setRotationAngle(0f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);

        pieChart.setExtraOffsets(20f, 0f, 20f, 0f);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    }

    private void loadChartData() {
        if (transactions == null || transactions.isEmpty()) {
            // Handle the case when transactions is null or empty
            return;
        }

        Map<String, Float> categoryAmounts = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("income")) {
                String category = transaction.getCategory();
                float amount = (float) transaction.getAmount();

                // Add the amount to the existing category or create a new entry
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    categoryAmounts.put(category, categoryAmounts.getOrDefault(category, 0f) + amount);
                }
            }
        }

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            String category = entry.getKey();
            float amount = entry.getValue();
            entries.add(new PieEntry(Math.abs(amount), category));
        }

        int[] colors = new int[]{
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
                Color.MAGENTA, Color.LTGRAY, Color.DKGRAY, Color.BLACK, Color.WHITE,
                Color.parseColor("#FF4081"), Color.parseColor("#536DFE"), Color.parseColor("#FF5722"),
                Color.parseColor("#FFC107"), Color.parseColor("#4CAF50"), Color.parseColor("#03A9F4"),
                Color.parseColor("#9C27B0"), Color.parseColor("#FF9800"), Color.parseColor("#009688"),
                Color.parseColor("#E91E63")
        };

        PieDataSet dataSet = new PieDataSet(entries, ""); // Empty label
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        // Hide the legend
        pieChart.getLegend().setEnabled(false);



    // Create a custom legend below the pie chart
        Legend legend = pieChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setFormSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }



}