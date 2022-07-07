package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;
import static com.kimaita.monies.Utils.Utils.formatDate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kimaita.monies.R;
import com.kimaita.monies.adapters.SubjectNatureTotalAdapter;
import com.kimaita.monies.databinding.FragmentAnalysisDetailBinding;
import com.kimaita.monies.models.GraphEntry;
import com.kimaita.monies.viewmodels.AnalysisDetailViewModel;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnalysisDetailFragment extends Fragment {

    AnalysisDetailViewModel mViewModel;
    MoneyViewModel moneyViewModel;
    private String mCategory;
    FragmentAnalysisDetailBinding binding;
    SubjectNatureTotalAdapter adapter;


    public static AnalysisDetailFragment newInstance() {
        return new AnalysisDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = AnalysisDetailFragmentArgs.fromBundle(getArguments()).getCategory();
        if (requireActivity().findViewById(R.id.bottom_nav) != null)
            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (requireActivity().findViewById(R.id.bottom_nav) != null)
            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AnalysisDetailViewModel.class);
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        binding.analysisDetsToolBar.setNavigationOnClickListener(view1 -> Navigation.findNavController(view1).navigateUp());
        binding.analysisDetsToolBar.setTitle(mCategory);
        binding.analysisDetsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Calendar c = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.DAY_OF_MONTH, 1);
        c2.set(Calendar.HOUR_OF_DAY, 1);


        binding.analysisDetsPeriod.setOnClickListener(view12 -> {
            MaterialDatePicker<Pair<Long, Long>> datePicker = createMaterialDatePicker();
            datePicker.addOnPositiveButtonClickListener(selection -> mViewModel.setTimeRange(selection));
            datePicker.show(getParentFragmentManager(), "Date Range Picker");
        });

        moneyViewModel.getCategoryVolumeTransacted(c2.getTimeInMillis(), c.getTimeInMillis(), mCategory).observe(getViewLifecycleOwner(),
                aDouble -> {
                    if (aDouble != null) {
                        mViewModel.setSpend(aDouble);
                        binding.analysisDetsAmount.setText(formatAmountCurrency(aDouble));
                    }
                });

        moneyViewModel.getGraphPointsInPeriod(c2.getTimeInMillis(), c.getTimeInMillis(), mCategory).observe(getViewLifecycleOwner(),
                graphEntries -> {
                    setUpBarChart(binding.analysisDetsBarChart);
                    binding.analysisDetsBarChart.setData(createChartData(graphEntries));
                    binding.analysisDetsBarChart.invalidate();
                    binding.analysisDetsPeriod.setText(getString(R.string.target_period,
                            formatDate(c2.getTimeInMillis(), "dd MMM"), formatDate(c.getTimeInMillis(), "dd MMM")));

                });

        moneyViewModel.getNatureSubjectTotals(c2.getTimeInMillis(), c.getTimeInMillis(), mCategory).observe(getViewLifecycleOwner(), graphEntries -> {
            if (adapter != null) {
                adapter.submitList(graphEntries);
            }
        });

        mViewModel.getTimeRange().observe(getViewLifecycleOwner(), longLongPair -> {
            moneyViewModel.getCategoryVolumeTransacted(longLongPair.first, longLongPair.second, mCategory).observe(getViewLifecycleOwner(),
                    aDouble -> {
                        if (aDouble != null) {
                            mViewModel.setSpend(aDouble);
                            binding.analysisDetsAmount.setText(formatAmountCurrency(aDouble));
                        }
                    });

            moneyViewModel.getGraphPointsInPeriod(longLongPair.first, longLongPair.second, mCategory).observe(getViewLifecycleOwner(),
                    graphEntries -> {
                        setUpBarChart(binding.analysisDetsBarChart);
                        binding.analysisDetsBarChart.setData(createChartData(graphEntries));
                        binding.analysisDetsBarChart.invalidate();
                        binding.analysisDetsPeriod.setText(getString(R.string.target_period,
                                formatDate(longLongPair.first, "dd MMM"), formatDate(longLongPair.second, "dd MMM")));
                    });
            moneyViewModel.getNatureSubjectTotals(longLongPair.first, longLongPair.second, mCategory).observe(getViewLifecycleOwner(), graphEntries -> adapter.submitList(graphEntries));

        });

        mViewModel.getSpend().observe(getViewLifecycleOwner(), aDouble -> {
            adapter = new SubjectNatureTotalAdapter(new SubjectNatureTotalAdapter.SubjectTotalDiff(), requireContext(),
                    subject -> {
                        NavDirections action = AnalysisDetailFragmentDirections.actionAnalysisDetailFragmentToSubjectEntriesListFragment(subject);
                        Navigation.findNavController(requireView()).navigate(action);
                    }, aDouble);
            binding.analysisDetsRecyclerView.setAdapter(adapter);
        });
    }

    private void setUpBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        //chart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawBorders(false);
        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);

        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.text));
        leftAxis.setValueFormatter(chart.getDefaultValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        chart.getAxisRight().setEnabled(false);

        XAxis xLabels = chart.getXAxis();
        xLabels.setTextColor(getResources().getColor(R.color.text));
        xLabels.setDrawGridLines(false);
        xLabels.setDrawAxisLine(false);
        xLabels.mDecimals = 0;
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);
        Legend l = chart.getLegend();
        l.setTextColor(getResources().getColor(R.color.text));
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        chart.setFitBars(true);

    }

    private BarData createChartData(List<GraphEntry> transactions) {
        ArrayList<BarEntry> values = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (GraphEntry entry : transactions) {
            cal.setTime(entry.date);
            values.add(new BarEntry(cal.get(Calendar.DAY_OF_MONTH),
                    new float[]{(float) entry.amount, Float.parseFloat(entry.cost)}));
        }
        BarDataSet set1;
        set1 = new BarDataSet(values, "Category Spending");
        set1.setValues(values);
        set1.setDrawValues(false);
        set1.setDrawIcons(false);
        set1.setColors(getColors());
        set1.setStackLabels(new String[]{"Amount", "Transaction Cost"});

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        //data.setValueFormatter(new AxisValueFormatter());
        data.setValueTextColor(getResources().getColor(R.color.text));

        return data;
    }

    private int[] getColors() {

        // have as many colors as stack-values per entry
        int[] colors = new int[2];
        /*colors[0] = R.color.income;
        colors[1] = R.color.expenses;*/

        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 2);
        return colors;
    }

    private MaterialDatePicker createMaterialDatePicker() {
        return MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.define_period))
                .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                .build();

    }

}