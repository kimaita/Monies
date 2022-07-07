package com.kimaita.monies.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public class PrefManager {

    final SharedPreferences pref;
    final SharedPreferences defPref;
    final SharedPreferences.Editor editor;
    final Context context;
    // shared pref mode
    final int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String MESSAGE_ID = "lastMessageID";
    public static final String THEME = "theme";


    public PrefManager(@NonNull Context mContext) {
        this.context = mContext;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        defPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean isNightMode() {
        return defPref.getBoolean(THEME, false);
    }


    public void setMessageId(long id) {
        editor.putLong(MESSAGE_ID, id);
        editor.apply();
    }

    public long getMessageId() {
        return pref.getLong(MESSAGE_ID, 0);
    }

}
