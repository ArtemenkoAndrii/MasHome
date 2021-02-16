package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    var notificationId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String
)
