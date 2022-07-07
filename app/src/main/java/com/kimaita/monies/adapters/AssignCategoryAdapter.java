package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.kimaita.monies.R;
import com.kimaita.monies.models.Share;
import com.kimaita.monies.ui.AssignCategoriesFragmentDirections;

public class AssignCategoryAdapter extends ListAdapter<Share, AssignCategoryAdapter.MyViewHolder> {

    final OnItemClickListener listener;

    final Context mContext;

    public AssignCategoryAdapter(@NonNull DiffUtil.ItemCallback<Share> diffCallback, @NonNull Context mContext, @NonNull OnItemClickListener itemClickListener) {
        super(diffCallback);
        this.mContext = mContext;
        this.listener = itemClickListener;

    }

    public interface OnItemClickListener {
        void onItemClick(Share share);
    }

    public static class ShareDiff extends DiffUtil.ItemCallback<Share> {

        @Override
        public boolean areItemsTheSame(@NonNull Share oldItem, @NonNull Share newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Share oldItem, @NonNull Share newItem) {
            return oldItem.name.equals(newItem.name);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_assignctg, parent, false);
        return new AssignCategoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Share current = getItem(position);
        holder.bind(current);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView name;
        final MaterialTextView number;
        final MaterialTextView countTotal;
        final Button category;
        final Button viewEntries;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.assign_name);
            number = itemView.findViewById(R.id.assign_num);
            countTotal = itemView.findViewById(R.id.assign_count_share);
            category = itemView.findViewById(R.id.assign_ctg_btn);
            viewEntries = itemView.findViewById(R.id.view_entries_btn);
        }

        void bind(Share share) {

            if (share.isIn) {
                name.setTextColor(ContextCompat.getColor(mContext, R.color.income));
            } else {
                name.setTextColor(ContextCompat.getColor(mContext, R.color.expenses));
            }
            name.setText(share.name);
            if (share.amount != null) {
                number.setText(share.amount);
            } else {
                number.setText("--");
            }
            countTotal.setText(mContext.getString(R.string.assign_count_share, String.valueOf(share.count), formatAmountCurrency(share.share)));
            category.setOnClickListener(view -> listener.onItemClick(share));
            viewEntries.setOnClickListener(view -> {
                NavDirections action = AssignCategoriesFragmentDirections.actionAssignCategoriesFragmentToSubjectEntriesListFragment(share.name);
                Navigation.findNavController(itemView).navigate(action);
            });
        }
    }
}
