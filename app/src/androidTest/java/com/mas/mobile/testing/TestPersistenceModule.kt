package com.mas.mobile.testing

import android.content.Context
import androidx.room.Room
import androidx.test.espresso.idling.CountingIdlingResource
import com.mas.mobile.PersistenceModule
import com.mas.mobile.repository.db.config.AppDatabase
import dagger.Provides
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Instrumented-test replacement for [PersistenceModule]. Builds a fresh in-memory
 * [AppDatabase] per Dagger graph instead of the real, file-backed singleton, so every
 * test starts from an empty schema. Tests are responsible for inserting their own
 * fixtures via the DAOs - this keeps them independent of the seed data in `DML.kt`.
 *
 * [idlingResource] tracks the database's query/transaction executor (see
 * [IdlingResourceExecutor]) - [FakeAppComponentRule] registers it with Espresso so a test's
 * `perform()`/`check()` waits for any in-flight Room `LiveData` query instead of racing it.
 */
class TestPersistenceModule : PersistenceModule() {
    val idlingResource = CountingIdlingResource("RoomQueries")

    @Provides
    @Singleton
    override fun providesDb(context: Context): AppDatabase {
        val executor = IdlingResourceExecutor(Executors.newSingleThreadExecutor(), idlingResource)
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(executor)
            .setTransactionExecutor(executor)
            .build()
    }
}
