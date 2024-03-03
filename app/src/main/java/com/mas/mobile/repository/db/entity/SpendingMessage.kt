package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mas.mobile.repository.db.config.AppDatabase.Companion.AUTOGENERATED
import java.time.LocalDateTime

@Entity(tableName = "spending_messages")
data class SpendingMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    override var id: Int = AUTOGENERATED,

    @ColumnInfo(name = "name")
    override var name: String = "",

    @ColumnInfo(name = "message")
    var message: String = "",

    @ColumnInfo(name = "receivedAt")
    var receivedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "rule_id")
    var ruleId: Int = -1,

    @ColumnInfo(name = "spending_id")
    var spendingId: Int? = null,

    @ColumnInfo(name = "suggested_expenditure_name")
    var suggestedExpenditureName: String? = null,

    @ColumnInfo(name = "suggested_amount")
    var suggestedAmount: Double = 0.0,

    @ColumnInfo(name = "is_new")
    var isNew: Boolean = true,

    @ColumnInfo(name = "status")
    var status: String = "",
): Searchable(id, name) {
    companion object {
        const val MATCHED = "MATCHED"
        const val RECOMMENDED = "RECOMMENDED"
    }
}