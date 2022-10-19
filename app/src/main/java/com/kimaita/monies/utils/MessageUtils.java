package com.kimaita.monies.utils;

import static com.kimaita.monies.utils.Constants.BOUGHT;
import static com.kimaita.monies.utils.Constants.FULIZA_IN;
import static com.kimaita.monies.utils.Constants.FULIZA_OUT;
import static com.kimaita.monies.utils.Constants.GIVE;
import static com.kimaita.monies.utils.Constants.PAID_TO;
import static com.kimaita.monies.utils.Constants.RECEIVED_FROM;
import static com.kimaita.monies.utils.Constants.SENT_TO;
import static com.kimaita.monies.utils.Constants.TRANSFERRED;
import static com.kimaita.monies.utils.Constants.WITHDRAW;
import static com.kimaita.monies.utils.Constants.WITHDRAW_ATM;
import static com.kimaita.monies.utils.MessageExtractor.matched;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class MessageUtils {

    private MessageUtils() {
        throw new IllegalStateException("Utility class");
    }
    //check if message is transactional type
    private static boolean isValid(@NonNull String mess) {
        if (mess.contains("Confirmed") || mess.contains("confirmed"))
            return mess.contains(PAID_TO) || mess.contains(SENT_TO) || mess.contains(BOUGHT) ||
                    mess.contains(RECEIVED_FROM) || mess.contains(WITHDRAW) || mess.contains(WITHDRAW_ATM) || mess.contains(TRANSFERRED)
                    || mess.contains(GIVE) || mess.contains(FULIZA_OUT) || mess.contains(FULIZA_IN);
        return false;
    }

    public static CursorLoader defineCursorLoader(@NonNull Context context, long lastID) {
        Uri inbox = Uri.parse("content://sms/inbox");
        String[] projection = {"_id", "body", "date"};
        String[] selectionArgs = new String[2];
        String searchAddress = "MPESA";
        String searchID = String.valueOf(lastID);
        String selectionClause = "address = ? AND _id > ? ";
        String sortOrder = BaseColumns._ID + " ASC";
        selectionArgs[0] = searchAddress;
        selectionArgs[1] = searchID;

        return new CursorLoader(context, inbox, projection, selectionClause, selectionArgs, sortOrder);

    }

    public static List<String> loadMpesaTransactions(@NonNull Cursor c, @NonNull PrefManager prefManager) {

        ArrayList<String> messageList = new ArrayList<>();
        int totalSMS = c.getCount();
        long id = 0;
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                String mess = c.getString(c.getColumnIndexOrThrow("body"));
                if (isValid(mess)) {
                    String str = "id: " +
                            c.getLong(c.getColumnIndexOrThrow("_id")) +
                            "body: " +
                            mess +
                            "time: " +
                            c.getLong(c.getColumnIndexOrThrow("date"));
                    messageList.add(str);
                }
                c.moveToNext();
            }
            prefManager.setMessageId(id);
        }
        c.close();
        return messageList;
    }

    //extract balance
    public static double getBalanceAfterTransaction(@NonNull String mess) {
        double bal = 0;
        String pt = "New M-PESA balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)|M-PESA balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            bal = (matcher.group(1) == null) ?
                    Double.parseDouble(Objects.requireNonNull(matcher.group(2)).replace(",", "")) :
                    Double.parseDouble(Objects.requireNonNull(matcher.group(1)).replace(",", ""));
        }
        return bal;
    }

    //extract M-Shwari Balance
    public static double getMShwariBalance(@NonNull String mess) {
        double bal = 0;
        String pt = "New M-Shwari saving account balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)|M-Shwari balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            bal = (matcher.group(1) == null) ?
                    Double.parseDouble(Objects.requireNonNull(matcher.group(2)).replace(",", "")) :
                    Double.parseDouble(Objects.requireNonNull(matcher.group(1)).replace(",", ""));

        }
        return bal;
    }

    public static double getOutstandingFuliza(@NonNull String mess) {
        double bal = 0;
        String pt = "outstanding amount is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            bal = (matcher.group(1) != null) ?
                    Double.parseDouble(Objects.requireNonNull(matcher.group(1)).replace(",", "")) :
                    -1;

        }
        return bal;
    }

}
