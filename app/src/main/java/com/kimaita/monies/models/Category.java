package com.kimaita.monies.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @NonNull
    @PrimaryKey()
    public String categoryName;

    @ColumnInfo(name = "inOut")
    public Boolean isIn;

    @NonNull
    @Override
    public String toString() {
        return categoryName;
    }
}
