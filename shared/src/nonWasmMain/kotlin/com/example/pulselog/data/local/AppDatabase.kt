package com.example.pulselog.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.pulselog.data.local.dao.DropdownDao
import com.example.pulselog.data.local.dao.MistakeDao
import com.example.pulselog.data.local.dao.UserDao
import com.example.pulselog.data.local.entity.*

@Database(
    entities = [
        User::class,
        Mistake::class,
        PageOption::class,
        ReasonOption::class,
        DesignationOption::class
    ],
    version = 2,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun dropdownDao(): DropdownDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
