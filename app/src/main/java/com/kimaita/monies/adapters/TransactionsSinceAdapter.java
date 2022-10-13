package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.MessageUtils.transactionTime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kimaita.monies.R;

import java.text.SimpleDateFormat;

public class TransactionsSinceAdapter extends ListAdapter<String, RecyclerView.ViewHolder> {

    private final Context context;

    public TransactionsSinceAdapter(@NonNull DiffUtil.ItemCallback<String> diffCallback, Context mContext) {
        super(diffCallback);
        this.context = mContext;
    }

    public static class MessageBodyDiff extends DiffUtil.ItemCallback<String> {

        @Override
        public boolean areItemsTheSame(@NonNull String s, @NonNull String t1) {
            return s.equals(t1);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String s, @NonNull String t1) {
            return s.equals(t1);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycleritem_since, viewGroup, false);
        return new TransactionSinceViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    class TransactionSinceViewHolder extends RecyclerView.ViewHolder {

        final TextView dateTime;
        final TextView amtName;
        final TextView balance;
        final TextView message;


        TransactionSinceViewHolder(@NonNull final View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.text_dateTime);
            amtName = itemView.findViewById(R.id.text_amtSubject);
            balance = itemView.findViewById(R.id.text_balance);
            message = itemView.findViewById(R.id.text_msg);

        }

        void bind(String msgBody) {
            SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("h:mm a ddd, MMM yyyy");

            dateTime.setText(dateTimeFormatter.format(transactionTime(msgBody)));

            message.setText(msgBody);

            itemView.setOnClickListener(view -> {
                if(message.getVisibility()==View.VISIBLE)
                    message.setVisibility(View.GONE);
                else if(message.getVisibility()==View.GONE)
                    message.setVisibility(View.VISIBLE);
            });

        }
    }
}
