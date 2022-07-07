package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Utils.addChip;
import static com.kimaita.monies.Utils.Utils.populateChips;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.kimaita.monies.R;
import com.kimaita.monies.databinding.FragmentDefineCategoryBinding;
import com.kimaita.monies.models.Category;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryDefineFragment extends Fragment implements CreateCategoryDialogFragment.CreateCategoryDialogListener {

    private FragmentDefineCategoryBinding binding;
    private MoneyViewModel defineViewModel;

    public CategoryDefineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity().findViewById(R.id.bottom_nav) != null)
            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDefineCategoryBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defineViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        populateChips(getResources().getStringArray(R.array.def_categories), binding.chipGroupDefineExp, this);
        populateChips(getResources().getStringArray(R.array.def_incomes), binding.chipGroupDefineInc, this);

        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_define_done) {
                getInsertCtgSelections();
                return true;
            }
            return false;
        });

        binding.topAppBar.setNavigationOnClickListener(view1 -> Navigation.findNavController(view1).navigateUp());

        binding.textDefineExp.setOnClickListener(view12 -> showNoticeDialog(false));
        binding.textDefineInc.setOnClickListener(view12 -> showNoticeDialog(true));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        binding = null;
    }

    private void getInsertCtgSelections() {

        List<Integer> checkedChipsExp = binding.chipGroupDefineExp.getCheckedChipIds();
        List<Integer> checkedChipsIn = binding.chipGroupDefineInc.getCheckedChipIds();
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        for (int chipID : checkedChipsExp) {
            Chip chip = binding.chipGroupDefineExp.findViewById(chipID);
            Category category = new Category();
            category.categoryName = chip.getText().toString();
            category.isIn = false;
            categoryArrayList.add(category);
        }
        for (int chipID : checkedChipsIn) {
            Chip chip = binding.chipGroupDefineInc.findViewById(chipID);
            Category category = new Category();
            category.categoryName = chip.getText().toString();
            category.isIn = true;
            categoryArrayList.add(category);
        }
        try {
            defineViewModel.insertCategory(categoryArrayList.toArray(new Category[0]));

            Snackbar.make(requireView(), getString(R.string.success_add), Snackbar.LENGTH_LONG)
                    .setAction(R.string.done, view1 -> Navigation.findNavController(view1).navigate(R.id.navigation_home))
                    .show();
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.failed_add, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", view12 -> getInsertCtgSelections())
                    .show();
            Log.e("CategoryDefineFragment", "Failed to insert into Database");
        }

    }

    public void showNoticeDialog(boolean isIn) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CreateCategoryDialogFragment(isIn);
        dialog.show(getChildFragmentManager(), "CreateCategoryDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, boolean isIn, String ctg) {
        if (ctg != null) {
            if (isIn)
                addChip(ctg, binding.chipGroupDefineInc, this);
            else
                addChip(ctg, binding.chipGroupDefineExp, this);
        }
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }
}

