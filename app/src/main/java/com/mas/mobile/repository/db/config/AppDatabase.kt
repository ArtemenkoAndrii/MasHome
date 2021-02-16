package com.mas.mobile.repository.db.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.dao.BudgetDAO
import com.mas.mobile.repository.db.dao.ExpenditureDAO
import com.mas.mobile.repository.db.dao.SpendingDAO
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.ExpenditureData
import com.mas.mobile.repository.db.entity.SpendingData

@Database(entities = [Budget::class, SpendingData::class, ExpenditureData::class], version = 1)
@TypeConverters(SQLiteTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "mas_home.db")
                    .allowMainThreadQueries()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL(DDL.AFTER_SPENDING_INSERT)
                            db.execSQL(DDL.AFTER_SPENDING_UPDATE)
                            db.execSQL(DDL.AFTER_EXPENDITURE_INSERT)
                            db.execSQL(DDL.AFTER_EXPENDITURE_UPDATE)

                            db.execSQL(DML.GREETING_BUDGET)
                            db.execSQL(DML.GREETING_EXPENDITURE)
                            db.execSQL(DML.GREETING_SPENDING)
                        }
                    })
                    .build()
            }
            return INSTANCE!!
        }
    }

    abstract fun budgetDao(): BudgetDAO
    abstract fun expenditureDao(): ExpenditureDAO
    abstract fun spendingDao(): SpendingDAO
}