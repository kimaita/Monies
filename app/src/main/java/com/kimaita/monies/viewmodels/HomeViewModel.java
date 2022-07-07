package com.kimaita.monies.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> selectedNature = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedPeriod = new MutableLiveData<>();

    public void setNature(@NonNull String nature) {
        selectedNature.setValue(nature);
    }

    public LiveData<String> getSelected() {
        return selectedNature;
    }

    public void setSelectedPeriod(int period) {
        selectedPeriod.setValue(period);
    }

    public LiveData<Integer> getSelectedPeriod() {
        return selectedPeriod;
    }

}
