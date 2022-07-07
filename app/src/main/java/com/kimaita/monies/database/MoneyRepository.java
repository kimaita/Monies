package com.kimaita.monies.database;

import static com.kimaita.monies.Utils.MessageUtils.loadMpesaTransactions;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.kimaita.monies.DAOs.CategoryDAO;
import com.kimaita.monies.DAOs.MessageDao;
import com.kimaita.monies.Utils.PrefManager;
import com.kimaita.monies.models.Category;
import com.kimaita.monies.models.GraphEntry;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.models.Share;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class MoneyRepository {

    private final CategoryDAO mCategoryDAO;
    private final MessageDao mMessageDAO;

    private final LiveData<List<Category>> categoryList;
    private final LiveData<List<Share>> mpesaNatureShareList;
    private final LiveData<List<Share>> mpesaNatureInShareList;
    private final LiveData<List<Share>> mpesaNatureOutShareList;
    private final LiveData<List<Share>> uncategorizedMsgList;
    private final LiveData<Double> volumeTransacted;
    private final LiveData<Double> inVolume;
    private final LiveData<Double> outVolume;
    private final LiveData<List<Message>> recentTransactions;
    private final LiveData<List<GraphEntry>> graphPoints;

    public MoneyRepository(@NonNull Application application) {
        MoneyRoomDatabase db = MoneyRoomDatabase.getDatabase(application);
        mMessageDAO = db.messageDao();
        mCategoryDAO = db.categoryDAO();

        categoryList = mCategoryDAO.getAllCategories();
        mpesaNatureInShareList = mMessageDAO.getMpesaInNatureShares();
        mpesaNatureOutShareList = mMessageDAO.getMpesaOutNatureShares();
        mpesaNatureShareList = mMessageDAO.getMpesaNatureShares();
        uncategorizedMsgList = mMessageDAO.getUncategorisedSubjects();
        recentTransactions = mMessageDAO.getRecentTransactions();
        volumeTransacted = mCategoryDAO.getVolume();
        inVolume = mCategoryDAO.getInVolume();
        outVolume = mCategoryDAO.getOutVolume();
        graphPoints = mCategoryDAO.getTransactions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    public LiveData<List<Message>> getMpesaPerNatureList(String nature) {
        return mMessageDAO.getMessagesPerNature(nature);
    }

    public LiveData<List<Category>> getCategoryList() {
        return categoryList;
    }

    public LiveData<List<Share>> getMpesaNatureShareList() {
        return mpesaNatureShareList;
    }

    public LiveData<List<Share>> getUncategorizedMsgList() {
        return uncategorizedMsgList;
    }

    public LiveData<List<Message>> getSubjectUncategorisedMsg(String subject) {
        return mMessageDAO.getSubjectUncategorisedMsg(subject);
    }

    public LiveData<List<Share>> getMpesaNatureInShareList() {
        return mpesaNatureInShareList;
    }

    public LiveData<List<Share>> getMpesaNatureOutShareList() {
        return mpesaNatureOutShareList;
    }

    public LiveData<Double> getVolumeTransacted() {
        return volumeTransacted;
    }

    public LiveData<Double> getInVolume() {
        return inVolume;
    }

    public LiveData<Double> getOutVolume() {
        return outVolume;
    }

    public LiveData<List<GraphEntry>> getGraphPoints() {
        return graphPoints;
    }

    public LiveData<List<GraphEntry>> getGraphPointsInPeriod(long start, long end, String nature) {
        return mCategoryDAO.getTransactions(start, end, nature);
    }

    public LiveData<Double> getPeriodNatureExpenditure(long start, long end, String nature) {
        return mCategoryDAO.getCategoryTransactionsVolume(start, end, nature);
    }

    public LiveData<List<GraphEntry>> getNatureSubjectTotals(long start, long end, String nature) {
        return mCategoryDAO.getSubjectNatureTransactions(start, end, nature);
    }

    public LiveData<List<Message>> getRecentTransactions() {
        return recentTransactions;
    }

    public LiveData<List<Message>> getRecentNatureTransactions(String nature) {
        return mMessageDAO.getRecentNatureTransactions(nature);
    }

    public LiveData<List<Message>> getAllNatureTransactions(String nature) {
        return mMessageDAO.getAllNatureTransactions(nature);
    }

    public LiveData<Double> getMpesaVolumeTransacted(@Nullable String nature, int period) {
        LiveData<Double> volumeTransacted = null;
        if (nature == null && period == 0) {
            volumeTransacted = mMessageDAO.getMpesaTransactionVolume();
        } else if (nature == null) {
            volumeTransacted = mMessageDAO.getPeriodMpesaTransactionVolume(period);
        } else if (period == 0) {
            volumeTransacted = mMessageDAO.getMpesaNatureTransactionVolume(nature);
        } else {
            volumeTransacted = mMessageDAO.getPeriodMpesaNatureTransactionVolume(nature, period);
        }
        return volumeTransacted;
    }

    public LiveData<Integer> getTransactionCount(@Nullable String nature, int period) {
        LiveData<Integer> transactionCount = null;
        if (nature == null && period == 0) {
            transactionCount = mMessageDAO.getMpesaTransactionCount();
        } else if (nature == null) {
            transactionCount = mMessageDAO.getPeriodMpesaTransactionCount(period);
        } else if (period == 0) {
            transactionCount = mMessageDAO.getMpesaNatureTransactionCount(nature);
        } else {
            transactionCount = mMessageDAO.getPeriodMpesaNatureTransactionCount(nature, period);
        }
        return transactionCount;
    }

    public LiveData<String> getTopSubject(@Nullable String nature, int period) {
        LiveData<String> topSubject = null;
        if (nature == null && period == 0) {
            topSubject = mMessageDAO.getTopAllVolumeSubject();
        } else if (nature == null) {
            topSubject = mMessageDAO.getTopPeriodVolumeSubject(period);
        } else if (period == 0) {
            topSubject = mMessageDAO.getTopNatureVolumeSubject(nature);
        } else {
            topSubject = mMessageDAO.getTopPeriodNatureVolumeSubject(nature, period);
        }
        return topSubject;
    }

    public LiveData<Double> getGrowth(@Nullable String nature, int period) {
        LiveData<Double> growth = null;
        if (nature == null && period != 0) {
            growth = mMessageDAO.getGrowthOverLastPeriod(period);
        } else if (nature != null && period != 0) {
            growth = mMessageDAO.getNatureGrowthOverLastPeriod(nature, period);
        }
        return growth;
    }

    public LiveData<Double> getGrowthPercent(@Nullable String nature, int period) {
        LiveData<Double> growth = null;
        if (nature == null && period != 0) {
            growth = mMessageDAO.getGrowthPercentOverLastPeriod(period);
        } else if (nature != null && period != 0) {
            growth = mMessageDAO.getNatureGrowthPercentOverLastPeriod(nature, period);
        }
        return growth;
    }

    public LiveData<MessageDao.SubjectStats> getSubjectTransactionStats(@NonNull String subject, boolean isIn) {
        return mMessageDAO.getSubjectTransactionsStats(subject, isIn);
    }

    public LiveData<List<Message>> getSubjectTransactions(@NonNull String subject) {
        return mMessageDAO.getSubjectTransactions(subject);
    }
    public LiveData<List<Message>> getFilteredSubjectTransactions(@NonNull String subject, boolean isIn) {
        return mMessageDAO.getSubjectTransactions(subject, isIn);
    }


    public void insertMessage(Message message) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mMessageDAO.insertMessage(message));
    }

    public void insertMessage(@NonNull Message... message) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mMessageDAO.insertMessage(message));
    }

    public void insertCategory(@NonNull Category category) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mCategoryDAO.insertCategory(category));
    }

    public void insertCategory(Category... category) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mCategoryDAO.insertCategory(category));
    }

    public void updateMessageCategory(Message message) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mMessageDAO.updateMessage(message));
    }

    public void updateMessageCategory(Message... message) {
        MoneyRoomDatabase.databaseWriteExecutor.execute(() -> mMessageDAO.updateMessage(message));
    }

    public List<Message> getMpesaTransactions(@NonNull Cursor c, @NonNull PrefManager prefManager) {
        List<Message> messages = new ArrayList<>();

        Future<List<Message>> result = MoneyRoomDatabase.databaseWriteExecutor.submit(() -> loadMpesaTransactions(c, prefManager));
        try {
            messages = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

}