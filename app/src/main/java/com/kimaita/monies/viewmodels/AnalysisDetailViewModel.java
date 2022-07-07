package com.kimaita.monies.viewmodels;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnalysisDetailViewModel extends ViewModel {

    private final MutableLiveData<Pair<Long, Long>> timeRange = new MutableLiveData<>();
    private final MutableLiveData<Double> spend = new MutableLiveData<>();

    public MutableLiveData<Pair<Long, Long>> getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(@NonNull Pair<Long, Long> mTimeRange) {
        timeRange.setValue(mTimeRange);
    }

    public MutableLiveData<Double> getSpend() {
        return spend;
    }

    public void setSpend(Double mSpend) {
        spend.setValue(mSpend);
    }
}