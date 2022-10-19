package com.kimaita.monies.utils;

import static com.kimaita.monies.utils.Constants.AIRTIME;
import static com.kimaita.monies.utils.Constants.BOUGHT;
import static com.kimaita.monies.utils.Constants.CASH_DEPOSIT;
import static com.kimaita.monies.utils.Constants.CASH_WITHDRAWAL;
import static com.kimaita.monies.utils.Constants.FOR_ACCOUNT;
import static com.kimaita.monies.utils.Constants.FULIZA;
import static com.kimaita.monies.utils.Constants.FULIZA_IN;
import static com.kimaita.monies.utils.Constants.FULIZA_LOAN;
import static com.kimaita.monies.utils.Constants.FULIZA_OUT;
import static com.kimaita.monies.utils.Constants.FULIZA_PAYMENT;
import static com.kimaita.monies.utils.Constants.GIVE;
import static com.kimaita.monies.utils.Constants.MSHWARI;
import static com.kimaita.monies.utils.Constants.MSHWARI_DEPOSIT;
import static com.kimaita.monies.utils.Constants.MSHWARI_WITHDRAWAL;
import static com.kimaita.monies.utils.Constants.OTHER_AIRTIME;
import static com.kimaita.monies.utils.Constants.PAID_TO;
import static com.kimaita.monies.utils.Constants.PAYBILLS;
import static com.kimaita.monies.utils.Constants.POCHI;
import static com.kimaita.monies.utils.Constants.RECEIVED;
import static com.kimaita.monies.utils.Constants.RECEIVED_FROM;
import static com.kimaita.monies.utils.Constants.SENT;
import static com.kimaita.monies.utils.Constants.SENT_TO;
import static com.kimaita.monies.utils.Constants.TILL;
import static com.kimaita.monies.utils.Constants.TILLS_PAYBILLS;
import static com.kimaita.monies.utils.Constants.TRANSFERRED;
import static com.kimaita.monies.utils.Constants.UNCLASSIFIED;
import static com.kimaita.monies.utils.Constants.WITHDRAW;

import androidx.annotation.NonNull;

import com.kimaita.monies.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageExtractor {

    Message message= new Message();

    public MessageExtractor() {

    }

    public Message getMessage(String msg) {
        return parseMessage(msg);
    }

    private Message parseMessage(String mess) {
        messageID.andThen(messageBody)
                .andThen(isIn)
                .andThen(assignTransactionNature)
                .andThen(assignTransactionSubNature)
                .andThen(assignTransactionRecipient)
                .andThen(assignTransactionRecipientNumber)
                .andThen(assignTransactionAmount)
                .andThen(assignTransactionCost)
                .andThen(assignTransactionTime)
                .andThen(assignTransactionCategory)
                .accept(mess);

        message.total = (message.amount + message.cost);
        return message;
    }

    Consumer<String> messageID = mess -> message.id = Long.parseLong(mess.substring(4, mess.indexOf("body: ")));

    Consumer<String> messageBody = mess -> {
        String pt;
        String s;
        if (mess.contains(GIVE) || mess.contains("you have received")) {
            pt = "";
            Matcher matcher = matched(pt, mess);
            s = (matcher.find()) ? mess.substring(0, matcher.end()) : mess;
        } else {
            pt = "Transaction cost, Ksh(\\d+.\\d+)|Transaction cost Ksh.(\\d+.\\d+)|New M-PESA balance is Ksh(\\d+,\\d+.\\d+.)";
            Matcher matcher = matched(pt, mess);
            s = (matcher.find()) ? mess.substring(0, matcher.end()) : mess;
        }
        message.msg = s;
    };

    Consumer<String> isIn = mess -> message.isIn = (mess.contains(GIVE) || mess.contains(RECEIVED_FROM) || mess.contains("transferred from M-Shwari") || mess.contains(FULIZA_IN));

    Consumer<String> assignTransactionNature = mess -> {
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
        message.transactionType = nature;
    };

    Consumer<String> assignTransactionSubNature = mess -> {
        String ctg;
        switch (message.transactionType) {
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
        message.transactionSubType = ctg;
    };

    Consumer<String> assignTransactionRecipient = mess -> {
        String subject = null;
        String pt;
        Matcher matcher;
        switch (message.transactionSubType) {
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
                pt = "from ([a-zA-Z-.' ]*) (\\d+)";
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
                    subject = Objects.requireNonNull(matcher.group(1)).substring(8);
                    if (subject.startsWith("- "))
                        subject = Objects.requireNonNull(matcher.group(1)).substring(10);
                }
                break;
            case CASH_DEPOSIT:
                pt = "cash to ([a-zA-Z0-9 [^a-zA-Z0-9]]*) New M-PESA balance";
                matcher = matched(pt, mess);
                if (matcher.find())
                    subject = matcher.group(1);
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
        message.subject = (subject == null) ? null : subject.trim();
    };

    Consumer<String> assignTransactionRecipientNumber = mess -> {
        String subjNum = null;
        String pt;
        Matcher matcher;
        switch (message.transactionSubType) {
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
        message.numSubject = subjNum;
    };

    Consumer<String> assignTransactionAmount = mess -> {
        double amt = 0;
        String pt;
        Matcher matcher;
        if (message.transactionSubType.equals(FULIZA_LOAN)) {
            amt = Double.parseDouble(mess.substring(49, mess.indexOf("Fee charged") - 2).replaceAll("[a-zA-Z,]", ""));
        } else if (message.transactionSubType.equals(FULIZA_PAYMENT)) {
            amt = Double.parseDouble(mess.substring(mess.indexOf("Ksh" )+3, mess.indexOf(FULIZA_OUT)).replaceAll("[a-zA-Z,]", ""));
        } else {
            pt = "\\bKsh\\d+\\.\\d+|\\bKsh\\d+,\\d+\\.\\d+";
            matcher = matched(pt, mess);
            if (matcher.find()) {
                amt = Double.parseDouble(Objects.requireNonNull(matcher.group()).replaceAll("[a-zA-Z,]", ""));
            }
        }
        message.amount = amt;
    };

    Consumer<String> assignTransactionCost = mess -> {
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
        message.cost = cost;
    };

    Consumer<String> assignTransactionTime = mess -> {
        if (message.transactionType.equals(FULIZA)) {
            message.forDay = Long.parseLong(mess.substring(mess.indexOf("time: ") + 6));
        } else {
            String time = getTransactionTime(mess) + getTransactionDate(mess);
            Calendar c1 = Calendar.getInstance();
            try {
                SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("h:mm add/MM/yy");
                c1.setTime(dateTimeFormatter.parse(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            message.forDay = c1.getTimeInMillis();
        }
    };

    Consumer<String> assignTransactionCategory = mess -> {
        if (!(message.transactionType.equals(TILLS_PAYBILLS) || message.transactionType.equals(SENT) || message.transactionType.equals(RECEIVED)))
            message.category = message.transactionSubType;

        if (message.subject != null) {
            if (message.subject.equals("KPLC PREPAID")) {
                message.category = "Electricity";
            }
            if (message.subject.equals("Safaricom Offers") || message.subject.equals("Safaricom Limited")) {
                message.category = AIRTIME;
            }
            if (message.subject.equals("SAFARICOM DATA BUNDLES")) {
                message.category = "Mobile Data";
            }
        }
    };

    public static Matcher matched(@NonNull String ptn, @NonNull String mess) {
        Pattern pattern = Pattern.compile(ptn, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(mess);
    }

    @NonNull
    static String getTransactionDate(@NonNull String mess) {
        String dat = "";
        String pt = "\\d+/\\d+/\\d+";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            dat = matcher.group();
        }
        return dat;
    }

    @NonNull
    static String getTransactionTime(@NonNull String mess) {
        String time = "";
        String pt = "\\d+:\\d+ AM|\\d+:\\d+ PM";
        Matcher matcher = matched(pt, mess);
        if (matcher.find()) {
            time = matcher.group();
        }
        return time;
    }

}
