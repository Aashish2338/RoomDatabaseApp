package com.example.myapplication.roomclasses

import androidx.room.TypeConverter
import java.util.Date

class VehicleDataConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}