package com.kimaita.monies.DAOs;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kimaita.monies.models.Message;
import com.kimaita.monies.models.Share;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert()
    void insertMessage(@NonNull Message message);

    @Insert()
    void insertMessage(Message... message);

    @Update
    void updateMessage(Message message);

    @Update
    void updateMessage(Message... message);

    //retrieve transaction details
    @Query("SELECT * FROM mpesa WHERE id = :id LIMIT 1")
    Message getTransaction(int id);

    //retrieve all M-PESA messages
    @Query("SELECT * FROM mpesa ORDER BY id DESC")
    LiveData<List<Message>> getAll();

    @Query("SELECT * FROM mpesa ORDER BY forDay DESC LIMIT 5")
    LiveData<List<Message>> getRecentTransactions();

    @Query("SELECT * FROM mpesa WHERE nature =:nature ORDER BY forDay DESC LIMIT 5")
    LiveData<List<Message>> getRecentNatureTransactions(String nature);

    @Query("SELECT * FROM mpesa WHERE nature =:nature ORDER BY forDay DESC")
    LiveData<List<Message>> getAllNatureTransactions(String nature);

    @Query("WITH mMpesa AS (SELECT sum(mpesa.total) as mTotal FROM mpesa )" +
            "SELECT mpesa.nature as name, SUM(mpesa.total) as amount, COUNT(mpesa.id) as count, (SUM(mpesa.total)/mMpesa.mTotal) as share, isIn " +
            "FROM mpesa, mMpesa GROUP BY name ORDER BY name DESC")
    LiveData<List<Share>> getMpesaNatureShares();

    @Query("WITH mMpesa AS (SELECT sum(mpesa.total) as mTotal FROM mpesa )" +
            "SELECT mpesa.nature as name, SUM(mpesa.total) as amount, COUNT(mpesa.id) as count, (SUM(mpesa.total)/mMpesa.mTotal) as share, isIn " +
            "FROM mpesa, mMpesa WHERE mpesa.isIn = 1 GROUP BY name ORDER BY share DESC")
    LiveData<List<Share>> getMpesaInNatureShares();

    @Query("WITH mMpesa AS (SELECT sum(mpesa.total) as mTotal FROM mpesa )" +
            "SELECT mpesa.nature as name, SUM(mpesa.total) as amount, COUNT(mpesa.id) as count, (SUM(mpesa.total)/mMpesa.mTotal) as share, isIn " +
            "FROM mpesa, mMpesa WHERE mpesa.isIn = 0 GROUP BY name ORDER BY share DESC")
    LiveData<List<Share>> getMpesaOutNatureShares();

    @Query("SELECT * FROM mpesa WHERE mpesa.nature LIKE :mName ORDER BY forDay DESC")
    LiveData<List<Message>> getMessagesPerNature(String mName);

    @Query("SELECT subject as name, numSubj as amount, COUNT(id) as count, SUM(total) as share, isIn FROM mpesa WHERE category ISNULL GROUP BY subject, isIn ORDER BY COUNT(id) desc, share DESC, name DESC")
    LiveData<List<Share>> getUncategorisedSubjects();

    @Query("SELECT * FROM mpesa WHERE subject LIKE :subject AND category ISNULL ORDER BY forDay DESC, total DESC")
    LiveData<List<Message>> getSubjectUncategorisedMsg(@NonNull String subject);

    //gets mpesa transactions volume
    @Query("SELECT SUM(total) from mpesa")
    LiveData<Double> getMpesaTransactionVolume();

    //gets income mpesa transactions volume
    @Query("SELECT SUM(total) from mpesa WHERE mpesa.isIn =1")
    LiveData<Double> getMpesaIncomeTransactionVolume();

    //gets expense mpesa transactions volume
    @Query("SELECT SUM(total) from mpesa WHERE mpesa.isIn = 0")
    LiveData<Double> getMpesaExpenseTransactionVolume();

    //gets mpesa transactions volume for nature
    @Query("SELECT SUM(total) from mpesa WHERE mpesa.nature LIKE :mName")
    LiveData<Double> getMpesaNatureTransactionVolume(@NonNull String mName);

    //gets mpesa transactions volume in a period
    @Query("SELECT SUM(total) from mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Double> getPeriodMpesaTransactionVolume(@NonNull int period);

    //gets mpesa transactions volume for nature in a period
    @Query("SELECT SUM(total) from mpesa WHERE mpesa.nature LIKE :mName AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Double> getPeriodMpesaNatureTransactionVolume(String mName, int period);

    //gets mpesa transactions count
    @Query("SELECT COUNT(id) from mpesa")
    LiveData<Integer> getMpesaTransactionCount();

    //gets mpesa transactions count for nature
    @Query("SELECT COUNT(id) from mpesa WHERE mpesa.nature LIKE :mName")
    LiveData<Integer> getMpesaNatureTransactionCount(@NonNull String mName);

    //gets mpesa transactions count in a period
    @Query("SELECT count(id) from mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Integer> getPeriodMpesaTransactionCount(int period);

    //gets mpesa transactions count for nature in a period
    @Query("SELECT count(id) from mpesa WHERE mpesa.nature LIKE :mName AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)")
    LiveData<Integer> getPeriodMpesaNatureTransactionCount(String mName, int period);

    //gets overall top mpesa subject on transaction volume
    @Query("SELECT subject FROM mpesa GROUP BY subject ORDER BY total DESC LIMIT 1")
    LiveData<String> getTopAllVolumeSubject();

    //gets top mpesa subject on transaction volume in Nature
    @Query("SELECT subject FROM mpesa  WHERE nature LIKE :mName GROUP BY subject ORDER BY total DESC LIMIT 1")
    LiveData<String> getTopNatureVolumeSubject(String mName);

    //gets top mpesa subject on transaction volume in Nature in period
    @Query("SELECT subject FROM mpesa  WHERE nature LIKE :mName AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) " +
            "GROUP BY subject ORDER BY total DESC LIMIT 1")
    LiveData<String> getTopPeriodNatureVolumeSubject(String mName, int period);

    //gets top mpesa subject on transaction volume in period
    @Query("SELECT subject FROM mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) GROUP BY subject ORDER BY total DESC LIMIT 1")
    LiveData<String> getTopPeriodVolumeSubject(int period);

    //gets overall top mpesa subject on transaction count
    @Query("SELECT subject FROM mpesa GROUP BY subject ORDER BY count(total) DESC LIMIT 1")
    LiveData<String> getTopAllCountSubject();

    //gets top mpesa subject on transaction count in Nature
    @Query("SELECT subject FROM mpesa  WHERE nature LIKE :mName GROUP BY subject ORDER BY count(total) DESC LIMIT 1")
    LiveData<String> getTopNatureCountSubject(String mName);

    //gets top mpesa subject on transaction volume in Nature in period
    @Query("SELECT subject FROM mpesa  WHERE nature LIKE :mName AND forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) " +
            "GROUP BY subject ORDER BY count(total) DESC LIMIT 1")
    LiveData<String> getTopPeriodNatureCountSubject(String mName, int period);

    //gets top mpesa subject on transaction volume in period
    @Query("SELECT subject FROM mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) GROUP BY subject ORDER BY count(total) DESC LIMIT 1")
    LiveData<String> getTopPeriodCountSubject(int period);

    //get volume growth over last in period
    @Query("SELECT (SELECT SUM(total) FROM mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)) - " +
            "(SELECT SUM(total) FROM mpesa WHERE forDay BETWEEN (CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*3* 86400000)) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*2* 86400000))")
    LiveData<Double> getGrowthOverLastPeriod(int period);

    //get volume growth percentage over last in period
    @Query("WITH mPeriod AS (SELECT SUM(total) AS periodVolume FROM mpesa WHERE forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)), " +
            "mLastPeriod AS (SELECT SUM(total) AS lastPeriodVolume FROM mpesa WHERE forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*3*86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*2*86400000)) " +
            "SELECT (mPeriod.periodVolume-mLastPeriod.lastPeriodVolume)/mLastPeriod.lastPeriodVolume  FROM mPeriod, mLastPeriod")
    LiveData<Double> getGrowthPercentOverLastPeriod(int period);

    //get volume growth over last in period in nature
    @Query("WITH mPeriod AS (SELECT SUM(total) AS periodVolume FROM mpesa WHERE mpesa.nature LIKE :nature AND forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)), " +
            "mLastPeriod AS (SELECT SUM(total) AS lastPeriodVolume FROM mpesa WHERE mpesa.nature LIKE :nature AND forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*3*86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*2*86400000)) " +
            "SELECT mPeriod.periodVolume-mLastPeriod.lastPeriodVolume FROM mPeriod, mLastPeriod")
    LiveData<Double> getNatureGrowthOverLastPeriod(String nature, int period);

    //get volume growth percentage over last in period in nature
    @Query("WITH mPeriod AS (SELECT SUM(total) AS periodVolume FROM mpesa WHERE mpesa.nature LIKE :nature AND forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period * 86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER)), " +
            "mLastPeriod AS (SELECT SUM(total) AS lastPeriodVolume FROM mpesa WHERE mpesa.nature LIKE :nature AND forDay BETWEEN CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*3*86400000) AND CAST((julianday('now')-2440587.5)*86400000 as INTEGER) - (:period*2*86400000)) " +
            "SELECT (mPeriod.periodVolume-mLastPeriod.lastPeriodVolume)/mLastPeriod.lastPeriodVolume  FROM mPeriod, mLastPeriod")
    LiveData<Double> getNatureGrowthPercentOverLastPeriod(String nature, int period);

    //get volume of subject's transactions
    @Query("SELECT SUM(total) as amt,  COUNT(id) as count FROM mpesa WHERE subject LIKE :subject AND isIn = :isIn")
    LiveData<SubjectStats> getSubjectTransactionsStats(@NonNull String subject, boolean isIn);

    static class SubjectStats {
        public double amt;
        public int count;
    }

    //get subject's messages
    @Query("SELECT * from mpesa WHERE subject LIKE :subject order by forDay DESC")
    LiveData<List<Message>> getSubjectTransactions(@NonNull String subject);

   /* //get subject's messages
    @Query("SELECT * from mpesa group by forDay ")
    LiveData<List<Message>> getSubjectTransactionPs(@NonNull String subject);
*/
    //get filtered subject's messages
    @Query("SELECT * from mpesa WHERE subject LIKE :subject AND isIn = :isIn order by forDay DESC")
    LiveData<List<Message>> getSubjectTransactions(@NonNull String subject, boolean isIn);

}
