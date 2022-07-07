package com.kimaita.monies.models;

import java.sql.Date;

public class DateListHeader extends ListItem {

    private final Date date;

    public Date getDate() {
        return date;
    }

    public DateListHeader(Date date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

}