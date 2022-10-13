package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Constants.types;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kimaita.monies.databinding.FragmentTypeListDialogBinding;
import com.kimaita.monies.viewmodels.HomeViewModel;

/**
 * <p>A fragment that shows a radio group as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     TypeListDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class TypeListDialogFragment extends BottomSheetDialogFragment {

    private FragmentTypeListDialogBinding binding;


    public static TypeListDialogFragment newInstance() {
        return new TypeListDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTypeListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        for (String type : types) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(type);
            binding.radioGroup.addView(rb);
        }

        binding.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton btn = (RadioButton) binding.radioGroup.findViewById(i);
            homeViewModel.setSelectedType(btn.getText().toString());
            dismiss();
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}