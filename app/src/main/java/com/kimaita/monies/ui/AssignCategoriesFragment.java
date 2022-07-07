package com.kimaita.monies.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kimaita.monies.adapters.AssignCategoryAdapter;
import com.kimaita.monies.databinding.FragmentAssignCategoriesBinding;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.ArrayList;
import java.util.List;

public class AssignCategoriesFragment extends Fragment {

    private FragmentAssignCategoriesBinding binding;
    private MoneyViewModel defineViewModel;

    public AssignCategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAssignCategoriesBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defineViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        AssignCategoryAdapter mAdapter = new AssignCategoryAdapter(new AssignCategoryAdapter.ShareDiff(), requireContext(),
                share -> {
                    List<Message> messages = new ArrayList<>();
                    defineViewModel.getSubjectUncategorizedMsg(share.name).observe(getViewLifecycleOwner(), messages::addAll);
                    defineViewModel.getSubjectUncategorizedMsg(share.name).removeObservers(getViewLifecycleOwner());
                    CategoriesBottomSheetFragment categoriesBottomSheet = new CategoriesBottomSheetFragment(messages);
                    categoriesBottomSheet.show(getParentFragmentManager(), "CategoriesSheet");
                });

        binding.recyclerAssignCtg.setAdapter(mAdapter);
        binding.recyclerAssignCtg.setLayoutManager(new LinearLayoutManager(getContext()));
        defineViewModel.getUncategorizedMsgList().observe(getViewLifecycleOwner(), mAdapter::submitList);
        binding.topAppBar.setNavigationOnClickListener(view1 -> Navigation.findNavController(view1).navigateUp());

    }
}
