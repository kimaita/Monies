package com.kimaita.monies.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kimaita.monies.R;

public class CreateCategoryDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    CreateCategoryDialogListener listener;
    final boolean isIn;

    public interface CreateCategoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, boolean isIn, String ctg);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    public CreateCategoryDialogFragment(boolean mIsIn) {
        isIn = mIsIn;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Define Category");

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View viewInflated = inflater.inflate(R.layout.dialog_new_ctg, null);
        // Set up the input
        final TextInputEditText inputEditText = viewInflated.findViewById(R.id.edittext_category_name);
        final TextInputLayout textInputLayout = viewInflated.findViewById(R.id.textField_ctg_name);

        builder.setView(viewInflated)
                // Add action buttons
                .setPositiveButton(R.string.create, (dialog, id) -> {
                    if (!TextUtils.isEmpty(inputEditText.getText())) {
                        String categoryName = inputEditText.getText().toString();
                        listener.onDialogPositiveClick(CreateCategoryDialogFragment.this, isIn, categoryName);
                    } else {
                        textInputLayout.setError(getString(R.string.empty_name));
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> listener.onDialogNegativeClick(CreateCategoryDialogFragment.this));
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (CreateCategoryDialogListener) getParentFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getParentFragment().toString()
                    + " must implement DialogListener");
        }
    }

}