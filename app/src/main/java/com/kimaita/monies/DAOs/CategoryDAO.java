package com.kimaita.monies.DAOs;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kimaita.monies.models.Category;
import com.kimaita.monies.models.GraphEntry;

import java.util.List;

@Dao
public interface CategoryDAO {

    @Insert()
    void insertCategory(@NonNull Category category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategory(@NonNull Category... categories);

    @Update()
    void updateCategory(Category category);

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    //get volume transacted
    @Query("SELECT SUM(total) FROM mpesa as total")
    LiveData<Double> getVolume();

    //get volume transacted in period
    @Query("SELECT SUM(total) FROM mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Double> getVolume(int period);

    //get in volume
    @Query("SELECT SUM(total) FROM mpesa WHERE isIn = 1")
    LiveData<Double> getInVolume();

    //get in volume for period
    @Query("SELECT SUM(total) FROM mpesa WHERE isIn = 1 AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Double> getInVolume(int period);

    //get out volume
    @Query("SELECT SUM(total) FROM mpesa WHERE isIn = 0")
    LiveData<Double> getOutVolume();

    //get out volume for period
    @Query("SELECT SUM(total) FROM mpesa WHERE isIn = 0 AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Double> getOutVolume(int period);

    //get entries with date for Bar/LineGraph
    @Query("SELECT amt as amount, transCost as cost, forDay as date FROM mpesa ORDER BY date ASC")
    LiveData<List<GraphEntry>> getTransactions();

    //get nature entries between dates
    @Query("SELECT amt as amount, transCost as cost, forDay as date FROM mpesa " +
            "WHERE date BETWEEN :start AND :end AND nature LIKE :nature ORDER BY date ASC")
    LiveData<List<GraphEntry>> getTransactions(long start, long end, String nature);

    //get subject nature entries between dates
    @Query("SELECT SUM(total) as amount, subject as cost, forDay as date FROM mpesa " +
            "WHERE date BETWEEN :start AND :end AND nature LIKE :nature  GROUP BY subject ORDER BY amount DESC")
    LiveData<List<GraphEntry>> getSubjectNatureTransactions(long start, long end, String nature);

    //get volume of nature entries in a period
    @Query("SELECT SUM(total) FROM mpesa WHERE forDay BETWEEN :start AND :end AND nature LIKE :nature")
    LiveData<Double> getCategoryTransactionsVolume(long start, long end, @NonNull String nature);



}
