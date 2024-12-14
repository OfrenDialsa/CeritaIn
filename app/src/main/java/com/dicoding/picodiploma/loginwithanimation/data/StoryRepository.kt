package com.dicoding.picodiploma.loginwithanimation.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.data.local.pref.UserPreferences
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoryUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
) {
    fun getAllStories(
        page: Int? = null,
        size: Int? = null,
        location: Int? = null,
    ): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getAllStories(page, size, location)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            Log.e("getAllStories", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, StoryResponse::class.java)
                emit(Result.Success(parsedError))
            } catch (e: Exception) {
                Log.e("getAllStories", "Error parsing error response: ${e.message}")
                emit(Result.Error("Error: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e("getAllStories", "General Exception: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    suspend fun getListStory(): List<Story>? {
        return apiService.getAllStories().listStory
    }

    fun uploadStory(
        multipartBody: MultipartBody.Part,
        descRequestBody: RequestBody,
        latitudeRequestBody: RequestBody? = null,
        longitudeRequestBody: RequestBody? = null,
    ): LiveData<Result<StoryUploadResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.uploadStory(
                multipartBody,
                descRequestBody,
                latitudeRequestBody,
                longitudeRequestBody
            )

            emit(Result.Success(response))
        } catch (e: HttpException) {
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, StoryUploadResponse::class.java)
                emit(Result.Success(parsedError))
            } catch (e: Exception) {
                emit(Result.Error("Error: ${e.message}"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getAllStoriesWithPager(): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = true
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }


    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase,
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(
                    apiService,
                    storyDatabase
                )
            }.also { instance = it }
    }
}