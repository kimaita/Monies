package com.kimaita.monies.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kimaita.monies.DAOs.MessageDao;
import com.kimaita.monies.Utils.PrefManager;
import com.kimaita.monies.database.MoneyRepository;
import com.kimaita.monies.models.Category;
import com.kimaita.monies.models.GraphEntry;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.models.Share;

import java.util.List;

public class MoneyViewModel extends AndroidViewModel {

    private final MoneyRepository mRepository;

    private final LiveData<List<Share>> mpesaNatureShares;
    private final LiveData<List<Share>> mpesaInShares;
    private final LiveData<List<Share>> mpesaOutShares;
    private final LiveData<Double> volumeTransacted;
    private final LiveData<Double> inVolume;
    private final LiveData<Double> outVolume;
    private final LiveData<List<GraphEntry>> graphPoints;
    private final LiveData<List<Message>> recentTransactions;
    private final LiveData<List<Category>> mCategories;
    private final LiveData<List<Share>> uncategorizedMsgList;

    public MoneyViewModel(Application application) {
        super(application);
        mRepository = new MoneyRepository(application);

        mpesaNatureShares = mRepository.getMpesaNatureShareList();
        volumeTransacted = mRepository.getVolumeTransacted();
        inVolume = mRepository.getInVolume();
        outVolume = mRepository.getOutVolume();
        graphPoints = mRepository.getGraphPoints();
        recentTransactions = mRepository.getRecentTransactions();
        mpesaInShares = mRepository.getMpesaNatureInShareList();
        mpesaOutShares = mRepository.getMpesaNatureOutShareList();
        mCategories = mRepository.getCategoryList();
        uncategorizedMsgList = mRepository.getUncategorizedMsgList();
    }

    public LiveData<List<Share>> getMpesaNatureShares() {
        return mpesaNatureShares;
    }

    public LiveData<List<Message>> getMpesaNatureList(String nature) {
        return mRepository.getMpesaPerNatureList(nature);
    }

    public LiveData<List<Share>> getMpesaInShares() {
        return mpesaInShares;
    }

    public LiveData<List<Share>> getMpesaOutShares() {
        return mpesaOutShares;
    }

    public LiveData<Double> getVolumeTransacted() {
        return volumeTransacted;
    }

    public LiveData<Double> getCategoryVolumeTransacted(long start, long end, String nature) {
        LiveData<Double> volumeTransacted = mRepository.getPeriodNatureExpenditure(start, end, nature);
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
        LiveData<List<GraphEntry>> entries = mRepository.getGraphPointsInPeriod(start, end, nature);
        return entries;
    }

    public LiveData<List<GraphEntry>> getNatureSubjectTotals(long start, long end, String nature) {
        LiveData<List<GraphEntry>> subjectTotals = mRepository.getNatureSubjectTotals(start, end, nature);
        return subjectTotals;
    }

    public LiveData<List<Message>> getRecentNatureTransactions(String nature) {
        return mRepository.getRecentNatureTransactions(nature);
    }

    public LiveData<List<Message>> getAllNatureTransactions(String nature) {
        return mRepository.getAllNatureTransactions(nature);
    }

    public LiveData<List<Message>> getRecentTransactions() {
        return recentTransactions;
    }

    public LiveData<Integer> getTransactionCount(@Nullable String nature, int period) {
        return mRepository.getTransactionCount(nature, period);
    }

    public LiveData<Double> getTransactionVolume(@Nullable String nature, int period) {
        return mRepository.getMpesaVolumeTransacted(nature, period);
    }

    public LiveData<String> getTopSubject(@Nullable String nature, int period) {
        return mRepository.getTopSubject(nature, period);
    }

    public LiveData<Double> getGrowth(@Nullable String nature, int period) {
        return mRepository.getGrowth(nature, period);
    }

    public LiveData<Double> getGrowthPercent(@Nullable String nature, int period) {
        return mRepository.getGrowthPercent(nature, period);
    }

    public LiveData<MessageDao.SubjectStats> getSubjectTransactionStats(String subject, boolean isIn) {
        LiveData<MessageDao.SubjectStats> volume = mRepository.getSubjectTransactionStats(subject, isIn);
        return volume;
    }


    public LiveData<List<Message>> getSubjectTransactions(String subject) {
        LiveData<List<Message>> entries = mRepository.getSubjectTransactions(subject);
        return entries;
    }

    public LiveData<List<Message>> getFilteredSubjectTransactions(String subject, boolean isIn) {
        LiveData<List<Message>> entries = mRepository.getFilteredSubjectTransactions(subject, isIn);
        return entries;
    }

    public void insertMessage(Message... message) {
        mRepository.insertMessage(message);
    }

    public void updateMessage(Message message) {
        mRepository.updateMessageCategory(message);
    }

    public void updateMessage(Message... message) {
        mRepository.updateMessageCategory(message);
    }

    public LiveData<List<Category>> getCategories() {
        return mCategories;
    }

    public LiveData<List<Share>> getUncategorizedMsgList() {
        return uncategorizedMsgList;
    }

    public LiveData<List<Message>> getSubjectUncategorizedMsg(String subject) {
        LiveData<List<Message>> subjectUncategorisedMsg = mRepository.getSubjectUncategorisedMsg(subject);
        return subjectUncategorisedMsg;
    }

    public List<Message> getMpesaTransactions(Cursor c, PrefManager prefManager) {
        return mRepository.getMpesaTransactions(c, prefManager);
    }

    public void insertCategory(Category category) {
        mRepository.insertCategory(category);
    }

    public void insertCategory(Category... category) {
        mRepository.insertCategory(category);
    }

}
