package com.kimaita.monies.models;

public abstract class ListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_DETAIL = 1;

    abstract public int getType();
}