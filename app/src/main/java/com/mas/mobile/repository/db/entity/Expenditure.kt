package com.mas.mobile.repository.db.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

data class Expenditure(
    @Embedded
    var data: ExpenditureData,

    @Relation(
        parentColumn = "budget_id",
        entityColumn = "id"
    )
    var budget: Budget
) {
    var id: Int
        get() = data.id
        set(value) { data.id = value }

    var name: String
        get() = data.name
        set(value) { data.name = value }

    var plan: Double
        get() = data.plan
        set(value) { data.plan = value }

    var fact: Double
        get() = data.fact
        set(value) { data.fact = value }

    var comment: String?
        get() = data.comment
        set(value) { data.comment = value }
}

@Entity(tableName = "expenditures",
        foreignKeys = [
            ForeignKey(entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budget_id"],
            onDelete = CASCADE)])
data class ExpenditureData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    override var id: Int = 0,

    @ColumnInfo(name = "name")
    override var name: String = "",

    @ColumnInfo(name = "plan")
    var plan: Double = 0.0,

    @ColumnInfo(name = "fact")
    var fact: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String? = null,

    @ColumnInfo(name = "budget_id")
    var budget_id: Int = 0
): Searchable(id, name)