package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.kimaita.monies.R;
import com.kimaita.monies.models.Share;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

public class ShareAdapter extends ListAdapter<Share, ShareAdapter.MyViewHolder> {

    final DecimalFormat df = new DecimalFormat("#.#%");
    final NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
    final Currency c = Currency.getInstance("KES");
    final OnItemClickListener listener;
    private final Context mContext;

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public ShareAdapter(@NonNull DiffUtil.ItemCallback<Share> diffCallback, @NonNull Context mContext, @NonNull OnItemClickListener listener) {
        super(diffCallback);
        this.listener = listener;
        this.mContext = mContext;
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mpesa_message, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Share current = getItem(position);
        holder.bind(current, position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView category, amount, percentage, count;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            //category = itemView.findViewById(R.id.textViewHomeCategory);
            //amount = itemView.findViewById(R.id.textViewHomeAmount);
            //percentage = itemView.findViewById(R.id.textViewHomeShare);
            //count  = itemView.findViewById(R.id.textViewHomeCount);
        }
        void bind(Share share, int position) {
            numberFormat.setCurrency(c);
            category.setText(share.name);
            count.setText(mContext.getString(R.string.entries_count,share.count));
            percentage.setText(df.format(share.share));
            SpannableString spannableString =new SpannableString(formatAmountCurrency(Double.parseDouble(share.amount)));
            spannableString.setSpan(new RelativeSizeSpan(0.25f), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //amount.setText(spannableString);
            itemView.setOnClickListener(v -> {
                listener.onItemClick(position);
            });
        }
    }
}
