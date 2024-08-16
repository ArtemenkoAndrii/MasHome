package com.mas.mobile.repository.db.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.dao.BudgetDAO
import com.mas.mobile.repository.db.dao.CategoryDAO
import com.mas.mobile.repository.db.dao.DeferredActionDAO
import com.mas.mobile.repository.db.dao.ExpenditureDAO
import com.mas.mobile.repository.db.dao.IdGeneratorDAO
import com.mas.mobile.repository.db.dao.MessageTemplateDAO
import com.mas.mobile.repository.db.dao.QualifierDAO
import com.mas.mobile.repository.db.dao.SettingsDAO
import com.mas.mobile.repository.db.dao.SpendingDAO
import com.mas.mobile.repository.db.dao.SpendingMessageDAO
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Category
import com.mas.mobile.repository.db.entity.DeferrableAction
import com.mas.mobile.repository.db.entity.ExpenditureData
import com.mas.mobile.repository.db.entity.IdGenerator
import com.mas.mobile.repository.db.entity.MessageTemplate
import com.mas.mobile.repository.db.entity.Qualifier
import com.mas.mobile.repository.db.entity.Settings
import com.mas.mobile.repository.db.entity.SpendingData
import com.mas.mobile.repository.db.entity.SpendingMessage
import com.mas.mobile.util.CurrencyTools
import java.time.LocalDate

@Database(
    version = 9,
    exportSchema = true,
    entities = [
        Budget::class,
        SpendingData::class,
        ExpenditureData::class,
        SpendingMessage::class,
        Settings::class,
        IdGenerator::class,
        Qualifier::class,
        DeferrableAction::class,
        MessageTemplate::class,
        Category::class
    ]
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
                            db.execSQL(DML.SCHEDULED_BUDGET)
                            db.execSQL(DML.TEMPLATE_GENERATOR)
                            db.execSQLs(DML.DEFAULT_QUALIFIERS)
                            db.execSQLs(DML.GREETING_MESSAGE_TEMPLATES)
                            db.execSQLs(DML.GREETING_CATEGORIES)
                        }
                    })
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
            }
            return INSTANCE!!
        }
    }

    abstract fun budgetDao(): BudgetDAO
    abstract fun expenditureDao(): ExpenditureDAO
    abstract fun spendingDao(): SpendingDAO
    abstract fun spendingMessageDao(): SpendingMessageDAO
    abstract fun settingsDao(): SettingsDAO
    abstract fun idGeneratorDAO(): IdGeneratorDAO
    abstract fun qualifierDAO(): QualifierDAO
    abstract fun deferredActionDAO(): DeferredActionDAO
    abstract fun messageTemplateDAO(): MessageTemplateDAO
    abstract fun categoryDAO(): CategoryDAO
}

internal fun SupportSQLiteDatabase.execSQLs(sqlBlock: String): Unit =
    sqlBlock
        .split(";")
        .filter { it.isNotBlank() }
        .forEach { this.execSQL(it.trim()) }

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE generator (id INTEGER NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("INSERT INTO generator(id) VALUES(1000)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE spending_messages ADD COLUMN status TEXT NOT NULL DEFAULT 'MATCHED'")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    private val maxDate = SQLiteTypeConverter().fromLocalDate(LocalDate.MAX)

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE budgets SET startsOn=$maxDate WHERE id=1")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE qualifiers (name TEXT NOT NULL PRIMARY KEY, type INTEGER NOT NULL)")
            execSQLs(DML.DEFAULT_QUALIFIERS)
        }
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS index_expenditures_on_budget_id ON expenditures(budget_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_spendings_on_expenditure_id ON spendings(expenditure_id)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val currency = CurrencyTools.getSystemCurrency()
        database.execSQL("ALTER TABLE budgets ADD COLUMN currency TEXT NOT NULL DEFAULT '$currency'")
        database.execSQL("ALTER TABLE message_rules ADD COLUMN currency TEXT NOT NULL DEFAULT '$currency'")
        database.execSQL("ALTER TABLE spendings ADD COLUMN currency TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE spendings ADD COLUMN rate REAL DEFAULT NULL")
        database.execSQL("ALTER TABLE spendings ADD COLUMN foreignAmount REAL DEFAULT NULL")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE deferrable_actions (key TEXT NOT NULL PRIMARY KEY, type INTEGER NOT NULL, increment INTEGER NOT NULL, active_after INTEGER NOT NULL)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE message_template (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                sender TEXT NOT NULL,
                pattern TEXT NOT NULL,
                example TEXT NOT NULL,
                currency TEXT NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 1
            );
        """.trimIndent())
        database.execSQLs(DML.GREETING_MESSAGE_TEMPLATES)

        database.execSQLs("""
            BEGIN TRANSACTION;
            CREATE TABLE qualifiers_new (
                id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                type INTEGER NOT NULL
            );
            INSERT INTO qualifiers_new (id, name, type)
            SELECT ROW_NUMBER() OVER (ORDER BY name), name, type
            FROM qualifiers;
            DROP TABLE qualifiers;
            ALTER TABLE qualifiers_new RENAME TO qualifiers;
            CREATE INDEX index_qualifiers_on_type_name ON qualifiers(type, name);
            COMMIT;
        """.trimIndent())

        database.execSQL("""
            CREATE TABLE categories (
                id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                plan REAL NOT NULL,
                active INTEGER NOT NULL,
                description TEXT NOT NULL,
                merchants TEXT NOT NULL,
                icon INTEGER
            );
        """.trimIndent())
        database.execSQL("CREATE INDEX index_categories_on_name ON categories(name);")
        database.execSQLs(DML.GREETING_CATEGORIES)

        database.execSQL("ALTER TABLE expenditures ADD COLUMN icon INTEGER;")
        database.execSQL("DELETE FROM expenditures WHERE budget_id = 1;")
        database.execSQL("DELETE FROM budgets WHERE id = 1;")
        database.execSQL("ALTER TABLE spending_messages ADD COLUMN suggested_merchant TEXT DEFAULT NULL;")
        database.execSQL("DROP TABLE IF EXISTS message_rules;")
        database.execSQL("ALTER TABLE spendings ADD COLUMN recurrence TEXT NOT NULL DEFAULT 'Never'")
        database.execSQL(DML.SCHEDULED_BUDGET)
    }
}