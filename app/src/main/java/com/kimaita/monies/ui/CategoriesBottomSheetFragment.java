package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Utils.populateChips;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.kimaita.monies.R;
import com.kimaita.monies.databinding.FragmentBottomSheetCategoriesBinding;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.Collections;
import java.util.List;

public class CategoriesBottomSheetFragment extends BottomSheetDialogFragment {

    final List<Message> messageList;
    FragmentBottomSheetCategoriesBinding binding;
    MoneyViewModel moneyViewModel;

    public CategoriesBottomSheetFragment(@NonNull List<Message> messages) {
        this.messageList = messages;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBottomSheetCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        moneyViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (!categories.isEmpty()) {
                Collections.sort(categories, (category, t1) -> Boolean.compare(category.isIn, t1.isIn));
                populateChips(categories, binding.categoriesChipGroup, this, requireContext());
            }
        });

        binding.categoriesChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            binding.categoriesBtnUpdate.setEnabled(!checkedIds.isEmpty());
            binding.categoriesBtnUpdate.setText(getString(R.string.update_ctg, messageList.size()));
        });

        binding.categoriesToolbar.setNavigationOnClickListener(view12 -> dismiss());

        binding.categoriesBtnUpdate.setOnClickListener(view1 -> {
            Chip chip = binding.categoriesChipGroup.findViewById(binding.categoriesChipGroup.getCheckedChipId());
            for (Message message : messageList) {
                Log.d("Assign Categories", "Entries: " + messageList.size());
                message.category = chip.getText().toString();
            }
            moneyViewModel.updateMessage(messageList.toArray(new Message[0]));
            dismiss();
        });
    }
}