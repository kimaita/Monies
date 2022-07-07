package com.kimaita.monies.ui;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

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

import com.kimaita.monies.R;
import com.kimaita.monies.adapters.SubjectEntriesAdapter;
import com.kimaita.monies.databinding.FragmentSubjectEntriesListBinding;
import com.kimaita.monies.viewmodels.MoneyViewModel;

public class SubjectEntriesListFragment extends Fragment {

    FragmentSubjectEntriesListBinding binding;
    private MoneyViewModel moneyViewModel;
    String subject;

    public SubjectEntriesListFragment() {
        // Required empty public constructor
    }

    public static SubjectEntriesListFragment newInstance() {
        return new SubjectEntriesListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subject = SubjectEntriesListFragmentArgs.fromBundle(getArguments()).getSubject();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubjectEntriesListBinding.inflate(inflater, container, false);
        if (requireActivity().findViewById(R.id.bottom_nav) != null)
            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        binding.appBarSubject.subjectTopAppBar.setTitle(subject);
        binding.appBarSubject.subjectTopAppBar.setNavigationOnClickListener(view13 -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
        moneyViewModel.getSubjectTransactionStats(subject, true).observe(getViewLifecycleOwner(), subjectStats -> {
            binding.subjectTextViewIn.setText(getString(R.string.subject_in, subjectStats.count, formatAmountCurrency(subjectStats.amt)));
            if(subjectStats.count==0)
                    binding.subjectCardIn.setVisibility(View.GONE);
        });
        moneyViewModel.getSubjectTransactionStats(subject, false).observe(getViewLifecycleOwner(), subjectStats -> {
            binding.subjectTextViewOut.setText(getString(R.string.subject_out, subjectStats.count, formatAmountCurrency(subjectStats.amt)));
            if(subjectStats.count==0)
                binding.subjectCardOut.setVisibility(View.GONE);
        });
        SubjectEntriesAdapter entriesAdapter = new SubjectEntriesAdapter(new SubjectEntriesAdapter.MessageDiff(), requireContext(), getParentFragmentManager());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerviewEntries.setLayoutManager(layoutManager);
        binding.recyclerviewEntries.setAdapter(entriesAdapter);
        moneyViewModel.getSubjectTransactions(subject).observe(getViewLifecycleOwner(), messages -> {
            entriesAdapter.submitList(messages);
            binding.appBarSubject.subjectTopAppBar.setSubtitle(messages.get(0).numSubject);
            String type = messages.get(0).transactionType;
        });

        binding.subjectCardIn.setOnClickListener(view1 ->
                moneyViewModel.getFilteredSubjectTransactions(subject, true).observe(getViewLifecycleOwner(), entriesAdapter::submitList));
        binding.subjectCardOut.setOnClickListener(view12 ->
                moneyViewModel.getFilteredSubjectTransactions(subject, false).observe(getViewLifecycleOwner(), entriesAdapter::submitList));


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (requireActivity().findViewById(R.id.bottom_nav) != null)
            requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
    }


}