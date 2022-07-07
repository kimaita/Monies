package com.kimaita.monies.adapters;

import static com.kimaita.monies.Utils.Utils.formatAmountCurrency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kimaita.monies.R;
import com.kimaita.monies.models.Share;
import com.kimaita.monies.ui.AnalysisFragmentDirections;

import java.text.DecimalFormat;

public class AnalysisCategoryListAdapter extends ListAdapter<Share, AnalysisCategoryListAdapter.CategoryViewHolder> {

    final DecimalFormat df = new DecimalFormat("#.##%");
    final Context mContext;

    public AnalysisCategoryListAdapter(@NonNull DiffUtil.ItemCallback<Share> diffCallback, Context context) {
        super(diffCallback);
        mContext = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_analysis_card, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Share current = getItem(position);
        holder.bind(current);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView categoryName;
        final TextView categorySpend;
        final TextView categoryGrowth;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.analysis_card_ctg);
            categorySpend = itemView.findViewById(R.id.analysis_card_amt);
            categoryGrowth = itemView.findViewById(R.id.analysis_card_growth);
        }

        public void bind(Share current) {
            categoryName.setText(current.name);
            categorySpend.setText(formatAmountCurrency(Double.parseDouble(current.amount)));
            if (current.share > 0)
                //TODO: add color span for positive and negative growth
                categoryGrowth.setText(mContext.getString(R.string.pos_growth_format_ctg, df.format(current.share)));
            else
                categoryGrowth.setText(mContext.getString(R.string.neg_growth_format_ctg, df.format(current.share)));
            itemView.setOnClickListener(view -> {
                NavDirections action = AnalysisFragmentDirections.actionNavigationAnalysisToAnalysisDetailFragment(current.name);
                Navigation.findNavController(itemView).navigate(action);
            });
        }
    }
}
