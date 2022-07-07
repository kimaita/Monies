package com.kimaita.monies.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mpesa")
public class Message extends ListItem {

    public String category;

    @ColumnInfo(name = "amt", defaultValue = "0")
    public double amount;

    @Override
    public int getType() {
        return TYPE_DETAIL;
    }

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "msgBody")
    public String msg;

    @ColumnInfo(name = "transCost")
    public double cost;

    @ColumnInfo(name = "total")
    public double total;

    public long forDay;

    @ColumnInfo(name = "subject")
    public String subject;

    @ColumnInfo(name = "numSubj")
    public String numSubject;

    @ColumnInfo(name = "nature")
    public String transactionType;

    public boolean isIn;

    @NonNull
    @Override
    public String toString() {
        return msg;
    }
}