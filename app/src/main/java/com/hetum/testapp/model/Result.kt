package com.hetum.testapp.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class Result(
    @PrimaryKey
    @ColumnInfo
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    var isChecked: Boolean = false
)