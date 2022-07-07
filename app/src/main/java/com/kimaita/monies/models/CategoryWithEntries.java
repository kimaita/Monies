package com.kimaita.monies.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CategoryWithEntries {
    @Embedded
    public Category category;
    @Relation(
            parentColumn = "categoryName",
            entityColumn = "category"
    )
    public List<Message> entries;
}
