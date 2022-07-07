package com.kimaita.monies.Utils;

import static com.kimaita.monies.Utils.Constants.AIRTIME;
import static com.kimaita.monies.Utils.Constants.CASH_DEPOSIT;
import static com.kimaita.monies.Utils.Constants.CASH_WITHDRAWAL;
import static com.kimaita.monies.Utils.Constants.FULIZA;
import static com.kimaita.monies.Utils.Constants.MSHWARI;
import static com.kimaita.monies.Utils.Constants.MSHWARI_DEPOSIT;
import static com.kimaita.monies.Utils.Constants.MSHWARI_WITHDRAWAL;
import static com.kimaita.monies.Utils.Constants.PAYBILLS;
import static com.kimaita.monies.Utils.Constants.RECEIVED;
import static com.kimaita.monies.Utils.Constants.SENT;
import static com.kimaita.monies.Utils.Constants.TILL;
import static com.kimaita.monies.Utils.Constants.TILLS_PAYBILLS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import com.kimaita.monies.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("h:mm add/MM/yy");
    private static final String PAID_TO = "paid to";
    private static final String SENT_TO = "sent to";
    static final String BOUGHT = "bought";
    static final String RECEIVED_FROM = "received";
    static final String WITHDRAW = "Withdraw";
    static final String TRANSFERRED = "transferred";
    static final String FULIZA_OUT = "from your M-PESA has been used to";
    static final String FULIZA_IN = "Fuliza M-Pesa amount is";
    static final String GIVE = "Give Ksh";
    static final String FOR_ACCOUNT = "for account";
    static final String OTHER_AIRTIME = "Other Airtime";
    static final String POCHI = "Pochi La Biashara";
    private static final String FULIZA_LOAN = "Fuliza Loan";
    private static final String FULIZA_PAYMENT = "Fuliza Repayment";
    private static final String UNCLASSIFIED = "Unclassified";

    //check if message is transactional type
    public static boolean isValid(@NonNull String mess) {
        return (mess.contains("Confirmed") || mess.contains("confirmed")) &&
                (mess.contains(PAID_TO) || mess.contains(SENT_TO) || mess.contains(BOUGHT) ||
                        mess.contains(RECEIVED_FROM) || mess.contains(WITHDRAW) || mess.contains(TRANSFERRED)
                        || mess.contains("Give") || mess.contains(FULIZA_OUT) || mess.contains(FULIZA_IN));
    }

    public static String getMessageContent(@NonNull String mess) {
        String message;
        String pt;
        if (mess.contains(GIVE) || mess.contains("you have received")) {
            pt = "New M-PESA balance is Ksh\\d+,\\d+.\\d+.";
            Matcher matcher = matched(pt, mess);
            message = (matcher.find()) ? mess.substring(0, matcher.end()) : mess;
        } else {
            pt = "Transaction cost, Ksh(\\d+.\\d+)|Transaction cost Ksh.(\\d+.\\d+)";
            Matcher matcher = matched(pt, mess);
            message = (matcher.find()) ? mess.substring(0, matcher.end()) : mess;
        }
        return message;
    }


    public static boolean isIn(@NonNull String mess) {
        if (mess.contains(WITHDRAW) || mess.contains(SENT_TO) || mess.contains(PAID_TO) || (mess.contains(BOUGHT) && mess.contains("airtime"))
                || mess.contains("transferred to M-Shwari") || mess.contains(FULIZA_OUT))
            return false;
        else
            return mess.contains(GIVE) || mess.contains(RECEIVED_FROM) || mess.contains("transferred from M-Shwari") || mess.contains(FULIZA_IN);
    }


    @NonNull
    public static String getTransactionNature(@NonNull String mess) {
        String nature = UNCLASSIFIED;
        if ((mess.contains(SENT_TO) && mess.contains(FOR_ACCOUNT)) || mess.contains(PAID_TO))
            nature = TILLS_PAYBILLS;
        else if (mess.contains(SENT_TO)) {
            nature = SENT;
        } else if (mess.contains(BOUGHT)) {
            nature = AIRTIME;
        } else if (mess.contains(WITHDRAW))
            nature = CASH_WITHDRAWAL;
        else if (mess.contains(TRANSFERRED) && mess.contains(MSHWARI)) {
            nature = MSHWARI;
        } else if (mess.contains(RECEIVED_FROM))
            nature = RECEIVED;
        else if (mess.contains(GIVE))
            nature = CASH_DEPOSIT;
        else if (mess.contains(FULIZA_OUT) || mess.contains(FULIZA_IN))
            nature = FULIZA;
        return nature;
    }


    @NonNull
    public static String getTransactionSubNature(@NonNull String mess) {
        String ctg = UNCLASSIFIED;
        switch (getTransactionNature(mess)) {
            case TILLS_PAYBILLS:
                ctg = (mess.contains(SENT_TO) && mess.contains(FOR_ACCOUNT)) ? PAYBILLS : TILL;
                break;
            case SENT:
                String pt = "sent to \\D+ (\\d+) on (\\d+)";
                Matcher matcher = matched(pt, mess);
                ctg = (matcher.find()) ? SENT : POCHI;
                break;
            case AIRTIME:
                ctg = (mess.contains("of airtime for")) ? OTHER_AIRTIME : AIRTIME;
                break;
            case CASH_WITHDRAWAL:
                ctg = CASH_WITHDRAWAL;
                break;
            case MSHWARI:
                ctg = (mess.contains("to M-Shwari")) ? MSHWARI_DEPOSIT : MSHWARI_WITHDRAWAL;
                break;
            case CASH_DEPOSIT:
                ctg = CASH_DEPOSIT;
                break;
            case FULIZA:
                ctg = (mess.contains(FULIZA_IN)) ? FULIZA_LOAN : FULIZA_PAYMENT;
                break;
            case RECEIVED:
                ctg = RECEIVED;
                break;
            default:
                ctg = UNCLASSIFIED;
        }
        return ctg;
    }

    //extract recipient
    public static String getTransactionRecipient(@NonNull String mess) {
        String subject = null;
        String pt;
        Matcher matcher;

        switch (getTransactionSubNature(mess)) {
            case PAYBILLS:
                subject = mess.substring(mess.indexOf(SENT_TO) + 8, mess.indexOf(FOR_ACCOUNT) - 1);
                break;
            case TILL:
                pt = "paid to\\b ([a-zA-Z0-9 ]+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subject = matcher.group(1);
                break;
            case SENT:
                pt = "sent to (\\D+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subject = matcher.group(1);
                break;
            case POCHI:
                pt = "sent to (\\D+) on ";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subject = matcher.group(1);
                break;
            case RECEIVED:
                pt = "from ([a-zA-Z ]*) (\\d+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subject = matcher.group(1);
                break;
            case OTHER_AIRTIME:
                subject = OTHER_AIRTIME;
                break;
            case AIRTIME:
                subject = "Own Airtime";
                break;
            case CASH_WITHDRAWAL:
                pt = "from ([a-zA-Z0-9 [^a-zA-Z0-9]]*) New M-PESA balance";
                matcher = matched(pt, mess);
                if (matcher.find()) {
                    subject = matcher.group(1).substring(8, matcher.group(1).length());
                    if (subject.startsWith("- "))
                        subject = matcher.group(1).substring(10, matcher.group(1).length());
                }
                break;
            case CASH_DEPOSIT:
                subject = "Cash Deposit";
                break;
            case MSHWARI_DEPOSIT:
            case MSHWARI_WITHDRAWAL:
                subject = "M-Shwari Account";
                break;
            case FULIZA_LOAN:
                subject = FULIZA_LOAN;
                break;
            case FULIZA_PAYMENT:
                subject = FULIZA_PAYMENT;
                break;
            default:
                subject = UNCLASSIFIED;
        }
        return (subject == null) ? null : subject.trim();
    }

    //extract recipient number/sender number/paybill account
    public static String getRecipientNumber(@NonNull String mess) {
        String subjNum = null;
        String pt;
        Matcher matcher;
        switch (getTransactionSubNature(mess)) {
            case PAYBILLS:
                pt = "for account\\b ([a-zA-Z0-9]* |\\s)\\bon";
                matcher = matched(pt, mess);
                if (matcher.find()) {
                    subjNum = matcher.group(1);
                }
                break;
            case TILL:
                pt = "paid to\\b ([a-zA-Z0-9 ]+)";
                matcher = matched(pt, mess);
                if (matcher.find()) {
                    subjNum = matcher.group(1);
                }
                break;
            case SENT:
                pt = "sent to \\D+ (\\d+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subjNum = matcher.group(1);
                break;
            case POCHI:
                subjNum = POCHI;
                break;
            case RECEIVED:
                pt = "from [a-zA-Z ]* (\\d+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subjNum = matcher.group(1);
                break;
            case OTHER_AIRTIME:
                pt = "airtime for (\\d+)";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subjNum = matcher.group(1);
                break;
            case AIRTIME:
                subjNum = "Self";
                break;
            case CASH_WITHDRAWAL:
                pt = "from ([a-zA-Z0-9 [^a-zA-Z0-9]]*) New M-PESA balance";
                matcher = matched(pt, mess);
                if (matcher.find()) {
                    subjNum = Objects.requireNonNull(matcher.group(1)).substring(0, 6);
                }
                break;
            case CASH_DEPOSIT:
                pt = "cash to ([a-zA-Z0-9 [^a-zA-Z0-9]]*) New M-PESA balance";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subjNum = matcher.group(1);
                break;
            case MSHWARI_DEPOSIT:
                subjNum = "Deposit";
                break;
            case MSHWARI_WITHDRAWAL:
                subjNum = "Withdrawal";
                break;
            case FULIZA_LOAN:
                subjNum = "Loan";
                break;
            case FULIZA_PAYMENT:
                subjNum = "Repayment";
                break;
            default:
                subjNum = UNCLASSIFIED;
        }
        return subjNum;
    }


    //extract amount transacted
    public static double tranAmount(@NonNull String mess) {
        double amt = 0;
        String pt;
        Matcher matcher;
        if (getTransactionSubNature(mess).equals(FULIZA_LOAN)) {
            amt = Double.parseDouble(mess.substring(49, mess.indexOf("Fee charged") - 2).replaceAll("[a-zA-Z,]", ""));
        } else if (getTransactionSubNature(mess).equals(FULIZA_PAYMENT)) {
            amt = Double.parseDouble(mess.substring(25, mess.indexOf(FULIZA_OUT)).replaceAll("[a-zA-Z,]", ""));
        } else {
            pt = "\\bKsh\\d+\\.\\d+|\\bKsh\\d+,\\d+\\.\\d+";
            matcher = matched(pt, mess);
            if (matcher.find()) {
                amt = Double.parseDouble(Objects.requireNonNull(matcher.group()).replaceAll("[a-zA-Z,]", ""));
            }
        }
        return amt;
    }

    //extract transaction cost
    public static double tranCost(@NonNull String mess) {
        double cost = 0;
        String pt = "Transaction cost, Ksh(\\d+.\\d+)|Transaction cost Ksh.(\\d+.\\d+)|Fee charged Ksh (\\d+.\\d+)";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            if (matcher.group(1) == null) {
                if (matcher.group(2) == null) {
                    cost = Double.parseDouble(Objects.requireNonNull(matcher.group(3)));
                } else
                    cost = Double.parseDouble(Objects.requireNonNull(matcher.group(2)));
            } else
                cost = Double.parseDouble(Objects.requireNonNull(matcher.group(1)));

        }
        return cost;
    }

    //extract balance
    public static double tranBalance(@NonNull String mess) {
        double bal = 0;
        String pt = "New M-PESA balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)|M-PESA balance is Ksh(\\d+\\.\\d+|\\d+,\\d+\\.\\d+)";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            bal = (matcher.group(1) == null) ?
                    Double.parseDouble(matcher.group(2).replaceAll(",", "")) :
                    Double.parseDouble(matcher.group(1).replaceAll(",", ""));

        }
        return bal;
    }

    //extract transaction date from message body
    @NonNull
    public static String txtDate(@NonNull String mess) {
        String dat = "";
        String pt = "\\d+/\\d+/\\d+";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            dat = matcher.group();
        }
        return dat;
    }

    //extract transaction time from message body
    @NonNull
    public static String txtTime(@NonNull String mess) {
        String tim = "";
        String pt = "\\d+:\\d+ AM|\\d+:\\d+ PM";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            tim = matcher.group();
        }
        return tim;
    }

    public static long transactionTime(@NonNull String mess) {
        String time = txtTime(mess) + txtDate(mess);
        Calendar c1 = Calendar.getInstance();
        try {
            c1.setTime(dateTimeFormatter.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return c1.getTimeInMillis();
    }

    public static void assignTransactionCategory(@NonNull Message message, @NonNull String mess) {
        switch (getTransactionSubNature(mess)) {
            case AIRTIME:
                message.category = AIRTIME;
                break;
            case CASH_WITHDRAWAL:
                message.category = CASH_WITHDRAWAL;
                break;
            case CASH_DEPOSIT:
                message.category = CASH_DEPOSIT;
                break;
            case FULIZA_LOAN:
                message.category = FULIZA_LOAN;
                break;
            case FULIZA_PAYMENT:
                message.category = FULIZA_PAYMENT;
                break;
            case MSHWARI_DEPOSIT:
                message.category = MSHWARI_DEPOSIT;
                break;
            case MSHWARI_WITHDRAWAL:
                message.category = MSHWARI_WITHDRAWAL;
                break;
        }

        if (getTransactionRecipient(mess) != null) {
            if (getTransactionRecipient(mess).equals("KPLC PREPAID")) {
                message.category = "Electricity";
            }
            if (getTransactionRecipient(mess).equals("Safaricom Offers") || getTransactionRecipient(mess).equals("Safaricom Limited")) {
                message.category = AIRTIME;
            }
        }
    }


    public static Matcher matched(@NonNull String ptn, @NonNull String mess) {
        Pattern pattern = Pattern.compile(ptn, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(mess);
    }

    public static CursorLoader defCursorLoader(@NonNull Context context, long lastID) {
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

    public static List<Message> loadMpesaTransactions(@NonNull Cursor c, @NonNull PrefManager prefManager) {

        ArrayList<Message> messageList = new ArrayList<>();
        int totalSMS = c.getCount();
        long id = 0;
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                String mess = c.getString(c.getColumnIndexOrThrow("body"));
                if (isValid(mess)) {
                    Message message = new Message();
                    id = c.getLong(c.getColumnIndexOrThrow("_id"));
                    message.id = id;
                    message.msg = (getMessageContent(mess));
                    message.transactionType = (getTransactionNature(mess));
                    message.subject = (getTransactionRecipient(mess));
                    message.amount = (tranAmount(mess));
                    message.cost = (tranCost(mess));
                    if (getTransactionNature(mess).equals(FULIZA)) {
                        message.forDay = c.getLong(c.getColumnIndexOrThrow("date"));
                    } else message.forDay = transactionTime(mess);
                    message.numSubject = (getRecipientNumber(mess));
                    message.total = (message.amount + message.cost);
                    message.isIn = isIn(mess);
                    assignTransactionCategory(message, mess);
                    messageList.add(message);
                }
                c.moveToNext();
            }
            prefManager.setMessageId(id);
        }
        c.close();
        return messageList;
    }

}
