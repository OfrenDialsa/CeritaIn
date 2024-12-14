package com.dicoding.picodiploma.loginwithanimation.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.RemoteKeys
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story

@Database(
    entities = [Story::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)

abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        private const val DATABASE_NAME = "story.db"

        @Volatile
        private var INSTANCE: StoryDatabase? = null
        fun getInstance(context: Context): StoryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                    .also { INSTANCE = it }
            }
    }
}