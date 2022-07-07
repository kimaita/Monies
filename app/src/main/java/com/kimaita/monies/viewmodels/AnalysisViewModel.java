package com.kimaita.monies.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnalysisViewModel extends ViewModel {

    private final MutableLiveData<Integer> selectedPeriod = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGrid = new MutableLiveData<>();

    public void setSelectedPeriod(int period) {
        selectedPeriod.setValue(period);
    }

    public LiveData<Integer> getSelectedPeriod() {
        return selectedPeriod;
    }

    public MutableLiveData<Boolean> getIsGrid() {
        return isGrid;
    }
    public void setIsGrid(boolean isGridOn){
        isGrid.setValue(isGridOn);
    }
}
