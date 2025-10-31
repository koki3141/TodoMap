package com.example.todomap.infrastructure

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todomap.infrastructure.db.TodoDao
import com.example.todomap.infrastructure.db.TodoEntity

@Database(
    entities = [TodoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        fun build(context: Context, inMemory: Boolean = false): AppDatabase =
            if (inMemory) {
                Room.inMemoryDatabaseBuilder(context.applicationContext, AppDatabase::class.java)
                    .fallbackToDestructiveMigration()
                    .build()
            } else {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todos.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
    }
}
