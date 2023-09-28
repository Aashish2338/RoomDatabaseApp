package com.example.myapplication.roomclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_data")
data class VehicleData(
    @PrimaryKey
    @ColumnInfo(name = "rc_Number")
    var rcNumber: String,
    @ColumnInfo(name = "vehicle_Number")
    var vehicleNumber: String,
    @ColumnInfo(name = "vehicle_Type")
    var vehicleType: String,
    @ColumnInfo(name = "vehicle_Image")
    var vehicleDocsImage: String
)