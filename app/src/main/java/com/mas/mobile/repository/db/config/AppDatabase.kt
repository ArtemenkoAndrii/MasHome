package com.mas.mobile.repository.db.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.dao.*
import com.mas.mobile.repository.db.entity.*

@Database(
    version = 1,
    entities = [
        Budget::class,
        SpendingData::class,
        ExpenditureData::class,
        SpendingMessageData::class,
        MessageRuleData::class,
        Settings::class
        ],
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2, spec = AutoMigration::class)
//    ]
)
@TypeConverters(SQLiteTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null
        const val AUTOGENERATED = 0

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "mas_home.db")
                    .allowMainThreadQueries()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL(DDL.AFTER_SPENDING_INSERT)
                            db.execSQL(DDL.AFTER_SPENDING_UPDATE)
                            db.execSQL(DDL.AFTER_SPENDING_DELETE)
                            db.execSQL(DDL.AFTER_EXPENDITURE_INSERT)
                            db.execSQL(DDL.AFTER_EXPENDITURE_UPDATE)
                            db.execSQL(DDL.AFTER_EXPENDITURE_DELETE)

                            db.execSQL(DML.TEMPLATE_BUDGET)
                            db.execSQL(DML.TEMPLATE_EXPENDITURES1)
                            db.execSQL(DML.TEMPLATE_EXPENDITURES2)
                            db.execSQL(DML.GREETING_MESSAGE_RULES)
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
    abstract fun spendingMessageDao(): SpendingMessageDAO
    abstract fun messageRuleDao(): MessageRuleDAO
    abstract fun settingsDao(): SettingsDAO
}