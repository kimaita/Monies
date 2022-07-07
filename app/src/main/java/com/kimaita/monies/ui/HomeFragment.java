package com.kimaita.monies.ui;

import static android.view.View.INVISIBLE;
import static com.kimaita.monies.MainActivity.loaderCallbacks;
import static com.kimaita.monies.Utils.Constants.AIRTIME;
import static com.kimaita.monies.Utils.Constants.CASH_DEPOSIT;
import static com.kimaita.monies.Utils.Constants.CASH_WITHDRAWAL;
import static com.kimaita.monies.Utils.Constants.FULIZA;
import static com.kimaita.monies.Utils.Constants.MSHWARI;
import static com.kimaita.monies.Utils.Constants.PERIOD_DAY;
import static com.kimaita.monies.Utils.Constants.PERIOD_MONTH;
import static com.kimaita.monies.Utils.Constants.PERIOD_WEEK;
import static com.kimaita.monies.Utils.Constants.RECEIVED;
import static com.kimaita.monies.Utils.Constants.SENT;
import static com.kimaita.monies.Utils.Constants.TILLS_PAYBILLS;
import static com.kimaita.monies.Utils.MessageUtils.tranBalance;
import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;
import static com.kimaita.monies.Utils.Utils.getListItems;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kimaita.monies.R;
import com.kimaita.monies.Utils.PrefManager;
import com.kimaita.monies.adapters.MessageAdapter;
import com.kimaita.monies.databinding.FragmentHomeBinding;
import com.kimaita.monies.models.ListItem;
import com.kimaita.monies.viewmodels.HomeViewModel;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.text.DecimalFormat;
import java.util.List;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    public static final int LOADER = 0;
    private static final String TAG = "Bottom Sheet Behaviour";
    MoneyViewModel moneyViewModel;
    HomeViewModel homeViewModel;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    String selectedNature;
    MessageAdapter mAdapter;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetTransactions.bottomSheetTransactions);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                bottomSheetBehavior.setPeekHeight(getBottomSheetHeight(view));
            }
        });
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        mAdapter = new MessageAdapter(new MessageAdapter.ListItemDiff(), requireContext(), getParentFragmentManager());
        binding.bottomSheetTransactions.recyclerHomeRecent.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.bottomSheetTransactions.recyclerHomeRecent.setLayoutManager(linearLayoutManager);
        binding.homeToolBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_define_ctg) {
                Navigation.findNavController(requireView()).navigate(R.id.categoryDefineFragment);
            }
            if (item.getItemId() == R.id.menu_assign_ctg) {
                Navigation.findNavController(requireView()).navigate(R.id.assignCategoriesFragment);
            }
            return false;
        });

        periodSwitch(binding.homeChipGroupPeriod.getCheckedChipId());
        binding.homeChipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                periodSwitch(checkedIds.get(0));
            }
        });

        moneyViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), messages -> {
            if (!messages.isEmpty())
                binding.textCurrentBal.setText(formatAmountCurrency(tranBalance(messages.get(0).msg)));
            List<ListItem> messageList = getListItems(messages);
            mAdapter.submitList(messageList);
        });

        homeViewModel.getSelected().observe(getViewLifecycleOwner(), s -> {
            selectedNature = s;
            moneyViewModel.getRecentNatureTransactions(s).observe(getViewLifecycleOwner(), messages -> {
                List<ListItem> messageList = getListItems(messages);
                mAdapter.submitList(messageList);
            });
            binding.bottomSheetTransactions.transactionsLabel.setText(getString(R.string.recent_type_transactions, s));

            homeViewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), integer -> {
                moneyViewModel.getTransactionCount(s, integer).observe(getViewLifecycleOwner(), aDouble ->
                        binding.mpesaDets.textTransactionCount.setText(String.valueOf(aDouble)));
                moneyViewModel.getTransactionVolume(s, integer).observe(getViewLifecycleOwner(), aDouble -> {
                    if (aDouble != null)
                        binding.mpesaDets.textTransactionVol.setText(formatAmountCurrency(aDouble));
                    else binding.mpesaDets.textTransactionVol.setText("--");
                });
                moneyViewModel.getTopSubject(s, integer).observe(getViewLifecycleOwner(), s1 ->
                        binding.mpesaDets.textTopSubject.setText(s1));

                moneyViewModel.getGrowth(s, integer).observe(getViewLifecycleOwner(),
                        aDouble -> moneyViewModel.getGrowthPercent(s, integer).observe(getViewLifecycleOwner(),
                                aDouble1 -> setGrowthText(aDouble, aDouble1)));
            });
        });
        if (selectedNature == null) {
            homeViewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), integer -> {
                moneyViewModel.getGrowth(null, integer).observe(getViewLifecycleOwner(),
                        aDouble -> moneyViewModel.getGrowthPercent(null, integer).observe(getViewLifecycleOwner(),
                                aDouble1 -> setGrowthText(aDouble, aDouble1)));
                moneyViewModel.getTransactionCount(null, integer).observe(getViewLifecycleOwner(), aDouble ->
                        binding.mpesaDets.textTransactionCount.setText(String.valueOf(aDouble)));
                moneyViewModel.getTransactionVolume(null, integer).observe(getViewLifecycleOwner(), aDouble -> {
                    if (aDouble != null)
                        binding.mpesaDets.textTransactionVol.setText(formatAmountCurrency(aDouble));
                    else binding.mpesaDets.textTransactionVol.setText("--");
                });
                moneyViewModel.getTopSubject(null, integer).observe(getViewLifecycleOwner(), s1 ->
                        binding.mpesaDets.textTopSubject.setText(s1));
            });
        }
        binding.mpesaCards.cardAirtime.setOnClickListener(view1 -> homeViewModel.setNature(AIRTIME));
        binding.mpesaCards.cardDeposits.setOnClickListener(view12 -> homeViewModel.setNature(CASH_DEPOSIT));
        binding.mpesaCards.cardFuliza.setOnClickListener(view13 -> homeViewModel.setNature(FULIZA));
        binding.mpesaCards.cardReceived.setOnClickListener(view14 -> homeViewModel.setNature(RECEIVED));
        binding.mpesaCards.cardMshwari.setOnClickListener(view15 -> homeViewModel.setNature(MSHWARI));
        binding.mpesaCards.cardTillsPaybills.setOnClickListener(view16 -> homeViewModel.setNature(TILLS_PAYBILLS));
        binding.mpesaCards.cardWithdrawal.setOnClickListener(view17 -> homeViewModel.setNature(CASH_WITHDRAWAL));
        binding.mpesaCards.cardSent.setOnClickListener(view18 -> homeViewModel.setNature(SENT));

        bottomSheetBehavior.addBottomSheetCallback(createBottomSheetBehavior());

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                LoaderManager.getInstance(requireActivity()).initLoader(LOADER, null, loaderCallbacks(requireContext(), moneyViewModel, new PrefManager(requireContext())));
            }
        });
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.income));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setGrowthText(Double aDouble, Double aDouble1) {
        if (aDouble != null) {
            if (aDouble < 0) {
                binding.mpesaDets.iconTrend.setImageResource(R.drawable.ic_home_neg_growth);
            } else {
                binding.mpesaDets.iconTrend.setImageResource(R.drawable.ic_home_pos_growth);
            }
            DecimalFormat df = new DecimalFormat("#.#%");
            binding.mpesaDets.textTrend.setText(getString(R.string.growth_format, formatAmountCurrency(aDouble), df.format(aDouble1)));
        } else {
            binding.mpesaDets.textTrend.setText("--");
        }
    }

    private void periodSwitch(int period) {
        switch (period) {
            case R.id.chip_past_day:
                homeViewModel.setSelectedPeriod(PERIOD_DAY);
                break;
            case R.id.chip_past_week:
                homeViewModel.setSelectedPeriod(PERIOD_WEEK);
                break;
            default:
                homeViewModel.setSelectedPeriod(PERIOD_MONTH);
                break;
        }
    }

    private BottomSheetBehavior.BottomSheetCallback createBottomSheetBehavior() {
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (requireActivity().findViewById(R.id.bottom_nav).getVisibility() == View.INVISIBLE)
                            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        requireActivity().findViewById(R.id.bottom_nav).setVisibility(INVISIBLE);

                        if (selectedNature != null)
                            moneyViewModel.getAllNatureTransactions(selectedNature).observe(getViewLifecycleOwner(),
                                    messages -> {
                                        List<ListItem> messageList = getListItems(messages);
                                        mAdapter.submitList(messageList);
                                    });
                        else
                            moneyViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), messages -> {
                                List<ListItem> messageList = getListItems(messages);
                                mAdapter.submitList(messageList);
                            });

                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        };
    }

    int getBottomSheetHeight(View v) {
        return getResources().getDisplayMetrics().heightPixels - getOccupiedHeight(v) - 64;
    }

    private int getOccupiedHeight(View view) {
        return view.findViewById(R.id.text_current_bal).getMeasuredHeight() +
                view.findViewById(R.id.text_bal_label).getMeasuredHeight() +
                view.findViewById(R.id.home_app_bar).getMeasuredHeight() +
                view.findViewById(R.id.mpesa_cards).getMeasuredHeight() +
                view.findViewById(R.id.mpesa_dets).getMeasuredHeight() +
                view.findViewById(R.id.home_layoutChips).getMeasuredHeight();

    }

}






