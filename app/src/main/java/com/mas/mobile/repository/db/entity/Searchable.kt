package com.mas.mobile.repository.db.entity

import androidx.room.Ignore

abstract class Searchable(
    @Ignore
    open val id: Int = 0,

    @Ignore
    open val name: String
)
