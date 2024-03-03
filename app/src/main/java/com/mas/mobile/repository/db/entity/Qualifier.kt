package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mas.mobile.repository.db.config.AppDatabase

@Entity(tableName = "qualifiers")
data class Qualifier(
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "type")
    var type: Short = 0,
) {
    companion object {
        const val CATCH: Short = 1
        const val SKIP: Short = 2
    }
}
