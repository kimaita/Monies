package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Utils.copyToClipboard;
import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;
import static com.kimaita.monies.adapters.MessageAdapter.TIME_ARG;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.kimaita.monies.R;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.ui.CategoriesBottomSheetFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SubjectEntriesAdapter extends ListAdapter<Message, SubjectEntriesAdapter.SubjectEntriesViewHolder> {

    final Context mContext;
    final SimpleDateFormat sdf = new SimpleDateFormat();
    final FragmentManager fragmentManager;

    public SubjectEntriesAdapter(@NonNull DiffUtil.ItemCallback<Message> diffCallback, Context context, @NonNull FragmentManager fManager) {
        super(diffCallback);
        this.mContext = context;
        this.fragmentManager = fManager;
    }

    public static class MessageDiff extends DiffUtil.ItemCallback<Message> {

        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.id == newItem.id;
        }
    }

    @NonNull
    @Override
    public SubjectEntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_transaction, parent, false);
        return new SubjectEntriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectEntriesViewHolder holder, int position) {
        Message current = getItem(position);
        holder.bind(current);
    }


    public class SubjectEntriesViewHolder extends RecyclerView.ViewHolder {
        final TextView textAmount;
        final TextView textDate;
        final TextView textMessage;
        final TextView textCategory;
        final Chip chipTransactionCode;

        public SubjectEntriesViewHolder(@NonNull View itemView) {
            super(itemView);
            textAmount = itemView.findViewById(R.id.item_subject_amt);
            textDate = itemView.findViewById(R.id.item_subject_time);
            textMessage = itemView.findViewById(R.id.item_subject_msg);
            textCategory = itemView.findViewById(R.id.item_ctg);
            chipTransactionCode = itemView.findViewById(R.id.chip_subject_copyCode);
        }

        void bind(Message message) {
            if (message.isIn) {
                textAmount.setText(mContext.getString(R.string.in_format, formatAmountCurrency(message.amount)));
                ((CardView) itemView).setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.card_in_analysis_bg));
                chipTransactionCode.setChipBackgroundColorResource(R.color.card_in_analysis_bg);
            } else {
                textAmount.setText(mContext.getString(R.string.out_format, formatAmountCurrency(message.amount)));
                ((CardView) itemView).setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.card_out_analysis_bg));
                chipTransactionCode.setChipBackgroundColorResource(R.color.card_out_analysis_bg);
            }
            sdf.applyPattern(TIME_ARG);
            textDate.setText(sdf.format(message.forDay));
            textMessage.setText(message.msg);
            if (message.category != null) {
                textCategory.setText(message.category);
            } else {
                textCategory.setOnClickListener(view -> {
                    List<Message> messages = new ArrayList<>();
                    messages.add(message);
                    CategoriesBottomSheetFragment categoriesBottomSheet = new CategoriesBottomSheetFragment(messages);
                    categoriesBottomSheet.show(fragmentManager, "CategoriesSheet");
                });
            }

            chipTransactionCode.setOnClickListener(view -> {
                copyToClipboard(message.msg, "MPESA Transaction", mContext);
            });
            itemView.setOnClickListener(view -> {
                if (textMessage.getVisibility() == View.GONE) {
                    textMessage.setVisibility(View.VISIBLE);
                    chipTransactionCode.setVisibility(View.VISIBLE);
                } else {
                    textMessage.setVisibility(View.GONE);
                    chipTransactionCode.setVisibility(View.GONE);
                }
            });
        }
    }
}
