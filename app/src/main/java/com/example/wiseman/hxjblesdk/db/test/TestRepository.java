package com.example.wiseman.hxjblesdk.db.test;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.wiseman.hxjblesdk.db.AppRoomDatabase;
import com.example.wiseman.hxjblesdk.db.lock.Lock;
import com.example.wiseman.hxjblesdk.db.lock.LockDao;

import java.util.List;

public class TestRepository {
    private ConTimeDao conTimeDao;

    public TestRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        conTimeDao = db.testDao();
    }

    public void insert(ConTimeTest lock) {
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            conTimeDao.insert(lock);
        });
    }

    public void deleteAll() {
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            conTimeDao.deleteAll();
        });
    }
}
