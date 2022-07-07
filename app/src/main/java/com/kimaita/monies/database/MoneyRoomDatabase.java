package com.kimaita.monies.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kimaita.monies.DAOs.CategoryDAO;
import com.kimaita.monies.DAOs.MessageDao;
import com.kimaita.monies.Utils.DateConverter;
import com.kimaita.monies.models.Category;
import com.kimaita.monies.models.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Message.class, Category.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class MoneyRoomDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();

    public abstract CategoryDAO categoryDAO();

    private static volatile MoneyRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MoneyRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MoneyRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MoneyRoomDatabase.class, "money_database")
                            //.createFromAsset("database/monies.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
