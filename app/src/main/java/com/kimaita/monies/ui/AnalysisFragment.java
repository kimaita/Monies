package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.kimaita.monies.R;
import com.kimaita.monies.adapters.AnalysisCategoryListAdapter;
import com.kimaita.monies.adapters.ShareAdapter;
import com.kimaita.monies.databinding.FragmentAnalysisBinding;
import com.kimaita.monies.models.GraphEntry;
import com.kimaita.monies.models.Share;
import com.kimaita.monies.viewmodels.AnalysisViewModel;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.ArrayList;
import java.util.List;


public class AnalysisFragment extends Fragment {

    private FragmentAnalysisBinding binding;
    MoneyViewModel moneyViewModel;
    AnalysisViewModel analysisViewModel;

    public static Fragment newInstance() {
        return new AnalysisFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);
        analysisViewModel = new ViewModelProvider(this).get(AnalysisViewModel.class);
        MaterialButton btn = (MaterialButton) binding.toggleGridLinear;

        AnalysisCategoryListAdapter adapter = new AnalysisCategoryListAdapter(new ShareAdapter.ShareDiff(), requireContext());
        binding.analysisRecyclerCategories.setAdapter(adapter);
        binding.analysisRecyclerCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        analysisViewModel.setIsGrid(true);
        binding.toggleGridLinear.setOnClickListener(view13 -> analysisViewModel.setIsGrid(Boolean.FALSE.equals(analysisViewModel.getIsGrid().getValue())));

        analysisViewModel.getIsGrid().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.analysisRecyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
                btn.setIconResource(R.drawable.ic_sharp_grid_on_24);
            } else {
                binding.analysisRecyclerCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                btn.setIconResource(R.drawable.ic_round_view_list_24);
            }
        });

        analysisViewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), integer -> {

        });

        moneyViewModel.getInVolume().observe(getViewLifecycleOwner(), aDouble ->
                binding.inAmt.setText(formatAmountCurrency(aDouble)));
        moneyViewModel.getOutVolume().observe(getViewLifecycleOwner(), aDouble ->
                binding.outAmt.setText(formatAmountCurrency(aDouble)));

        moneyViewModel.getMpesaNatureShares().observe(getViewLifecycleOwner(), shares -> {
            adapter.submitList(shares);
            if (!shares.isEmpty()) {
                setUpPieChart(binding.analysisPiechart);
                binding.analysisPiechart.setData(createPieData(shares));
                binding.analysisPiechart.invalidate();
            }
        });

        binding.cardIn.setOnClickListener(view1 -> moneyViewModel.getMpesaInShares().observe(getViewLifecycleOwner(), shares -> {
            if (!shares.isEmpty()) {
                setUpPieChart(binding.analysisPiechart);
                binding.analysisPiechart.setData(createPieData(shares));
                binding.analysisPiechart.invalidate();
            }
        }));

        binding.cardOut.setOnClickListener(view12 -> moneyViewModel.getMpesaOutShares().observe(getViewLifecycleOwner(), shares -> {
            if (!shares.isEmpty()) {
                setUpPieChart(binding.analysisPiechart);
                binding.analysisPiechart.setData(createPieData(shares));
                binding.analysisPiechart.invalidate();
            }
        }));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }


    void setUpPieChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(getResources().getColor(R.color.pie_hole));
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(65f);
        chart.setTransparentCircleRadius(69f);
        chart.setDrawCenterText(false);
        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        // add a selection listener
        //chart.setOnChartValueSelectedListener(this);
        chart.setDrawEntryLabels(false);

        chart.animateY(1400, Easing.EaseInOutQuad);
        chart.spin(2000, 0, 360, Easing.EaseInCirc);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(getResources().getColor(R.color.text));
        l.setWordWrapEnabled(true);
        l.setDrawInside(false);
        l.setXEntrySpace(8f);
        l.setYEntrySpace(0f);
        l.setYOffset(2f);


       /* // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);*/

    }

    private LineData createGraphData(List<GraphEntry> data) {
        List<Entry> values = new ArrayList<>();
        float amount = 0;
        for (GraphEntry p : data) {
            amount += p.amount;
            Entry entry = new Entry((float) p.date.getTime(), amount);
            values.add(entry);
        }
        LineDataSet lineDataSet = new LineDataSet(values, "Entries");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.5f);
        lineDataSet.setDrawFilled(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setFillFormatter(null);
        List<ILineDataSet> datasets = new ArrayList<>();
        datasets.add(lineDataSet);

        return new LineData(datasets);
    }

    PieData createPieData(List<Share> categoryShares) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Share share : categoryShares) {
            entries.add(new PieEntry(Float.parseFloat(share.amount), share.name));
        }

        PieDataSet set = new PieDataSet(entries, "Category Spending");

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        set.setColors(colors);
        set.setSliceSpace(1f);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter());
        return data;
    }

}