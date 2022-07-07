package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Utils.copyToClipboard;
import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;
import static com.kimaita.monies.Utils.Utils.formatWithQuoteSpan;
import static com.kimaita.monies.Utils.Utils.getInitials;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.kimaita.monies.R;
import com.kimaita.monies.models.DateListHeader;
import com.kimaita.monies.models.ListItem;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.ui.CategoriesBottomSheetFragment;
import com.kimaita.monies.ui.HomeFragmentDirections;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageAdapter extends ListAdapter<ListItem, RecyclerView.ViewHolder> {

    final SimpleDateFormat sdf = new SimpleDateFormat();
    public static final String TIME_ARG = "h:mm a";
    static final String DATE_ARG = "dd MMM, yyyy";
    final Context context;
    final FragmentManager fragmentManager;

    public MessageAdapter(@NonNull DiffUtil.ItemCallback<ListItem> diffCallback, Context mContext, FragmentManager fManager) {
        super(diffCallback);
        this.context = mContext;
        this.fragmentManager = fManager;
    }

    public static class ListItemDiff extends DiffUtil.ItemCallback<ListItem> {

        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem.getType() == 0 ?
                    Objects.equals(((DateListHeader) oldItem).getDate().toString(), ((DateListHeader) newItem).getDate().toString()) :
                    ((Message) oldItem).id == ((Message) newItem).id;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch (i) {
            case ListItem.TYPE_DETAIL:
                view = inflater.inflate(R.layout.item_mpesa_message, viewGroup, false);
                return new MessageViewHolder(view);

            case ListItem.TYPE_HEADER:
                view = inflater.inflate(R.layout.item_date, viewGroup, false);
                return new DateViewHolder(view);

            default:
                throw new IllegalStateException("Unexpected value: " + i);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int view = getItemViewType(position);
        if (view == ListItem.TYPE_HEADER) {
            DateViewHolder dateViewHolder = (DateViewHolder) holder;
            dateViewHolder.bindDate((DateListHeader) getCurrentList().get(position));
        } else if (view == ListItem.TYPE_DETAIL) {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            messageViewHolder.bindMessage((Message) getCurrentList().get(position));
        }
    }


    @Override
    public int getItemViewType(int position) {
        return getCurrentList().get(position).getType();
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView amount;
        final MaterialTextView time;
        final MaterialTextView msg;
        final MaterialTextView category;
        final MaterialTextView subject;
        final TextView initials;
        final ImageView copy;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.item_mpesa_time);
            category = itemView.findViewById(R.id.item_ctg);
            msg = itemView.findViewById(R.id.item_mpesa_msg);
            amount = itemView.findViewById(R.id.item_mpesa_amt);
            subject = itemView.findViewById(R.id.item_mpesa_reci_num);
            initials = itemView.findViewById(R.id.item_mpesa_initials);
            copy = itemView.findViewById(R.id.item_mpesa_copy);
        }

        void bindMessage(Message message) {
            if (message.isIn) {
                amount.setText(context.getString(R.string.in_format, formatAmountCurrency(message.amount)));
                amount.setTextColor(ContextCompat.getColor(context, R.color.income));
            } else {
                amount.setText(context.getString(R.string.out_format, formatAmountCurrency(message.amount)));
                amount.setTextColor(ContextCompat.getColor(context, R.color.expenses));
            }
            subject.setText(message.subject);
            sdf.applyPattern(TIME_ARG);
            time.setText(sdf.format(message.forDay));
            initials.setText(getInitials(message.subject));
            if (message.category == null) {
                category.setVisibility(View.VISIBLE);
                category.setText("Uncategorised");
                category.setTypeface(null, Typeface.ITALIC);
                category.setTextColor(ContextCompat.getColor(context, R.color.in_out));
            } else {
                if (message.category.equals(message.transactionType) || message.category.contains(message.transactionType)) {
                    category.setVisibility(View.GONE);
                } else {
                    category.setText(message.category);
                }
            }
            itemView.setOnClickListener(view -> {
                if (msg.getVisibility() == View.GONE) {
                    msg.setText(formatWithQuoteSpan(message.msg, context));
                    msg.setVisibility(View.VISIBLE);
                    copy.setVisibility(View.VISIBLE);
                } else {
                    msg.setVisibility(View.GONE);
                    copy.setVisibility(View.GONE);
                }
            });

            category.setOnClickListener(view1 -> {
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                CategoriesBottomSheetFragment categoriesBottomSheet = new CategoriesBottomSheetFragment(messages);
                categoriesBottomSheet.show(fragmentManager, "CategoriesSheet");
            });

            initials.setOnClickListener(v -> {
                NavDirections action = HomeFragmentDirections.actionNavigationHomeToSubjectEntriesListFragment(message.subject);
                Navigation.findNavController(v).navigate(action);
            });
            copy.setOnClickListener(v1 -> copyToClipboard(message.msg, "MPESA Transaction", context));

        }
    }


    private class DateViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView dateView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.text_date_header);
        }

        void bindDate(DateListHeader date) {
            sdf.applyPattern(DATE_ARG);
            dateView.setText(sdf.format(date.getDate()));
        }
    }

}

