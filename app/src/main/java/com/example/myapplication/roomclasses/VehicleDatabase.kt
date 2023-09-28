package com.example.myapplication.roomclasses

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VehicleData::class], version = 1, exportSchema = true)
abstract class VehicleDatabase : RoomDatabase() {

    abstract fun repoDao(): Dao

    companion object {
        @Volatile
        private var INSTANCE: VehicleDatabase? = null
        fun getDatabase(context: Context): VehicleDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): VehicleDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VehicleDatabase::class.java,
                "vehicle_Data"
            ).build()
        }
    }
}