package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mas.mobile.repository.db.config.AppDatabase.Companion.AUTOGENERATED

@Entity(tableName = "notifications")
data class Notification (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    var notificationId: Int = AUTOGENERATED,

    @ColumnInfo(name = "name")
    val name: String
)
