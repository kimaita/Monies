package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Constants.PERIOD_DAY;
import static com.kimaita.monies.Utils.Constants.PERIOD_MONTH;
import static com.kimaita.monies.Utils.Constants.PERIOD_WEEK;
import static com.kimaita.monies.Utils.Utils.getListItems;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kimaita.monies.R;
import com.kimaita.monies.adapters.MessageAdapter;
import com.kimaita.monies.databinding.FragmentTransactionsBinding;
import com.kimaita.monies.models.ListItem;
import com.kimaita.monies.viewmodels.HomeViewModel;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.List;


public class TransactionsFragment extends Fragment {

    MessageAdapter mAdapter;
    FragmentTransactionsBinding binding;
    MoneyViewModel moneyViewModel;
    HomeViewModel homeViewModel;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        homeViewModel.getSelected().observe(getViewLifecycleOwner(), s -> {
            binding.chipType.setText(s);
            moneyViewModel.getRecentNatureTransactions(s).observe(getViewLifecycleOwner(), messages -> {
                List<ListItem> messageList = getListItems(messages);
                mAdapter.submitList(messageList);
                mAdapter = new MessageAdapter(new MessageAdapter.ListItemDiff(), requireContext(), getParentFragmentManager());
                binding.transactionsRecyclerView.setAdapter(mAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                binding.transactionsRecyclerView.setLayoutManager(linearLayoutManager);
            });
        });
        homeViewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), integer -> binding.chipPeriod.setText(getPeriod(integer)));

        binding.chipPeriod.setOnClickListener(view1 -> {

        });
        binding.chipType.setOnClickListener(view2 -> {

        });
        binding.chipSort.setOnClickListener(view3 -> {

        });
        binding.chipViewSince.setOnClickListener(view4 -> {
            if (!binding.chipViewSince.isChecked()) {
                TypeListDialogFragment.newInstance().show(getParentFragmentManager(), "TypeListDialog");
            }
        });
        binding.chipViewSince.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.transactionsRecyclerView.setVisibility(View.GONE);
                binding.transactionsSinceLayout.transactionsSinceLayout.setVisibility(View.VISIBLE);

            } else {
                binding.transactionsRecyclerView.setVisibility(View.VISIBLE);
                binding.transactionsSinceLayout.transactionsSinceLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private String getPeriod(int p) {
        switch (p) {
            case PERIOD_DAY:
                return getString(R.string.past_day);
            case PERIOD_WEEK:
                return getString(R.string.past_week);
            case PERIOD_MONTH:
                return getString(R.string.past_month);
            default:
                return "Custom Period";
        }
    }



}