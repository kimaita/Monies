package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Constants.colors;
import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.kimaita.monies.R;
import com.kimaita.monies.models.GraphEntry;

import java.util.Random;

public class SubjectNatureTotalAdapter extends ListAdapter<GraphEntry, SubjectNatureTotalAdapter.SubjectTotalViewHolder> {

    final OnItemClickListener mListener;
    final Context mContext;
    private final double natureSpend;

    public interface OnItemClickListener {
        void onItemClick(@NonNull String subject);
    }

    public SubjectNatureTotalAdapter(@NonNull DiffUtil.ItemCallback<GraphEntry> diffCallback, Context context, OnItemClickListener listener, double spend) {
        super(diffCallback);
        this.mListener = listener;
        this.mContext = context;
        this.natureSpend = spend;
    }

    public static class SubjectTotalDiff extends DiffUtil.ItemCallback<GraphEntry> {
        @Override
        public boolean areItemsTheSame(@NonNull GraphEntry oldItem, @NonNull GraphEntry newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull GraphEntry oldItem, @NonNull GraphEntry newItem) {
            return oldItem.cost.equals(newItem.cost);
        }
    }


    @NonNull
    @Override
    public SubjectTotalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_category_subject, parent, false);
        return new SubjectTotalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectTotalViewHolder holder, int position) {
        GraphEntry currentEntry = getItem(position);
        holder.bind(currentEntry, natureSpend);
    }

    class SubjectTotalViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView total;
        final LinearProgressIndicator progressIndicator;

        public SubjectTotalViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.subject_name);
            total = itemView.findViewById(R.id.subject_spend);
            progressIndicator = itemView.findViewById(R.id.subject_progress);
        }

        void bind(GraphEntry item, double natureSpend) {
            name.setText(item.cost);
            total.setText(formatAmountCurrency(item.amount));
            progressIndicator.setProgress((int) item.amount);
            progressIndicator.setMax((int) natureSpend);
            progressIndicator.setIndicatorColor(getRandomColor());
            progressIndicator.invalidate();
            itemView.setOnClickListener(view -> mListener.onItemClick(item.cost));
        }

        int getRandomColor(){
            int i = new Random().nextInt(13);
            return ContextCompat.getColor(mContext, colors[i]);
        }
    }
}
