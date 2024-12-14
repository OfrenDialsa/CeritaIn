package com.dicoding.picodiploma.loginwithanimation.di

import android.content.Context
import com.dicoding.picodiploma.loginwithanimation.data.StoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.local.pref.UserPreferences
import com.dicoding.picodiploma.loginwithanimation.data.local.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.remote.retrofit.ApiConfig

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return UserRepository.getInstance(pref, apiService)
    }

    fun provideStoriesRepository(context: Context): StoryRepository {
        val pref = UserPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        val database = StoryDatabase.getInstance(context)
        return StoryRepository.getInstance(apiService, database)
    }
}