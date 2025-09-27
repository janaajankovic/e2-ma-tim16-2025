package com.example.habittrackerrpg.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
    private FragmentStatisticsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        statisticsViewModel.getTasksByStatus().observe(getViewLifecycleOwner(), this::setupPieChart);
        statisticsViewModel.getCompletedTasksByCategory().observe(getViewLifecycleOwner(), this::setupBarChart);
        statisticsViewModel.getLongestStreak().observe(getViewLifecycleOwner(), streak -> binding.textViewLongestStreak.setText(String.format(Locale.getDefault(), "%d days", streak)));
        statisticsViewModel.getActiveDays().observe(getViewLifecycleOwner(), days -> binding.textViewActiveDays.setText(String.format(Locale.getDefault(), "%d days", days)));
        statisticsViewModel.getDailyTotalXp().observe(getViewLifecycleOwner(), entries -> setupLineChart(binding.lineChartXpLast7Days, entries, "Total XP"));
        statisticsViewModel.getDailyAverageXp().observe(getViewLifecycleOwner(), entries -> setupLineChart(binding.lineChartAvgDifficulty, entries, "Average XP per Day"));
        statisticsViewModel.getAverageDifficultyDescription().observe(getViewLifecycleOwner(), result -> binding.textViewAvgDifficultyDescription.setText(result));
        statisticsViewModel.getSpecialMissions().observe(getViewLifecycleOwner(), text -> binding.textViewSpecialMissions.setText(text));
    }

    private void setupPieChart(Map<TaskStatus, Integer> statusMap) {
        if (statusMap == null || statusMap.isEmpty()) return;
        PieChart pieChart = binding.pieChartTasksByStatus;
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(58f);
        pieChart.setDrawEntryLabels(false);

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<TaskStatus, Integer> entry : statusMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey().toString()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private void setupBarChart(Map<String, Integer> categoryMap) {
        if (categoryMap == null || categoryMap.isEmpty()) {
            binding.barChartTasksByCategory.clear();
            binding.barChartTasksByCategory.invalidate();
            return;
        }

        BarChart barChart = binding.barChartTasksByCategory;
        barChart.getDescription().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completed Tasks");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.animateY(1000);
        barChart.invalidate();
    }


    private void setupLineChart(LineChart chart, List<Entry> entries, String label) {
        if (entries == null || entries.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }
        chart.getDescription().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelRotationAngle(-45);
        xAxis.setGranularity(1f);

        xAxis.setValueFormatter(new ValueFormatter() {
            private final java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("dd MMM", Locale.US);

            @Override
            public String getFormattedValue(float value) {
                java.time.LocalDate date = java.time.LocalDate.ofEpochDay((long) value);
                return date.format(formatter);
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(ColorTemplate.getHoloBlue());
        dataSet.setCircleColor(Color.BLACK);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}