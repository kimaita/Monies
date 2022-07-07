package com.kimaita.monies.Utils;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.kimaita.monies.R;
import com.kimaita.monies.models.Category;
import com.kimaita.monies.models.DateListHeader;
import com.kimaita.monies.models.ListItem;
import com.kimaita.monies.models.Message;

import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Utils {

    public static void populateChips(String[] texts, ChipGroup chipGroup, Fragment fragment) {
        for (String text : texts) {
            Chip mChip = (Chip) fragment.getLayoutInflater().inflate(R.layout.chip, chipGroup, false);
            mChip.setText(text);
            chipGroup.addView(mChip);
        }
    }

    public static void populateChips(List<Category> categories, ChipGroup chipGroup, Fragment fragment, Context context) {
        for (Category category : categories) {
            Chip mChip = (Chip) fragment.getLayoutInflater().inflate(R.layout.chip, chipGroup, false);
            mChip.setText(category.categoryName);
            mChip.setTextColor((category.isIn) ? ContextCompat.getColor(context, R.color.income) : ContextCompat.getColor(context, R.color.expenses));
            mChip.setChipStrokeColorResource((category.isIn) ? R.color.income : R.color.expenses);
            chipGroup.addView(mChip);
        }
    }

    public static void addChip(String s, ChipGroup chipGroup, Fragment fragment) {
        Chip mChip = (Chip) fragment.getLayoutInflater().inflate(R.layout.chip, chipGroup, false);
        mChip.setText(s);
        mChip.setChecked(true);
        chipGroup.addView(mChip);
    }


    public static String formatAmountCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        Currency c = Currency.getInstance("KSH");
        numberFormat.setCurrency(c);
        return numberFormat.format(amount);
    }

    @NonNull
    public static SpannableString formatWithQuoteSpan(@NonNull String text, Context c) {
        SpannableString spannableString = new SpannableString(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            spannableString.setSpan(new QuoteSpan(c.getColor(R.color.income), 10, 10), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    public static String formatDate(@NonNull Date date, String pattern) {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        String timeArg = (pattern == null) ? "dd MMM, yyyy" : pattern;
        sdf.applyPattern(timeArg);
        return sdf.format(date);
    }

    public static String formatDate(@NonNull Long date, String pattern) {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        String timeArg = (pattern == null) ? "dd MMM, yyyy" : pattern;
        sdf.applyPattern(timeArg);
        return sdf.format(new Date(date));
    }

    @NonNull
    public static SpannableString formatWithColorSpan(@NonNull String text, int color) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    //create a map with date values as the key for lists of messages
    public static Map<String, List<Message>> toMap(@NonNull List<Message> messages) {
        Map<String, List<Message>> mMessageMap = new HashMap<>();
        for (Message message : messages) {
            String mDate = new Date(message.forDay).toString();
            if (mMessageMap.containsKey(mDate)) {
                mMessageMap.get(mDate).add(message);
            } else {
                ArrayList<Message> value = new ArrayList<>();
                value.add(message);
                mMessageMap.put(mDate, value);
            }
        }
        return mMessageMap;
    }

    //create a list with dates and messages
    public static List<ListItem> getListItems(@NonNull Map<String, List<Message>> mMessageMap) {
        // TreeMap to store values of HashMap
        // Copy all data from hashMap into TreeMap
        TreeMap<String, List<Message>> sorted = new TreeMap<>(mMessageMap);
        ArrayList<ListItem> mItems = new ArrayList<>();
        for (String date : sorted.descendingMap().keySet()) {
            DateListHeader header = new DateListHeader(Date.valueOf(date));
            mItems.add(header);
            mItems.addAll(mMessageMap.get(date));
        }
        return mItems;
    }

    public static List<ListItem> getListItems(@NonNull List<Message> messages) {
        ArrayList<ListItem> messagesList = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ArrayList<ListItem>> result = executor.submit(() -> {
            ArrayList<ListItem> mMessages;
            mMessages = (ArrayList<ListItem>) getListItems(toMap(messages));
            return mMessages;
        });
        try {
            messagesList = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        return messagesList;
    }

    public static String getInitials(@NonNull String subject) {
        StringBuilder initials = new StringBuilder();
        for (String s : subject.split(" ")) {
            if (s.length() > 0)
                initials.append(s.charAt(0));
        }
        return (initials.length() > 1) ? initials.substring(0, 2) : initials.toString();
    }

    public static void copyToClipboard(@NonNull String string, @Nullable String label, @NonNull Context mContext){
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (label == null) label = "Transaction";
        ClipData clip = ClipData.newPlainText(label, string);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, "Copied to Clipboard", LENGTH_SHORT).show();
    }
}