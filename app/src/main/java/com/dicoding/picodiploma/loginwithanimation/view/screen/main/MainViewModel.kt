package com.dicoding.picodiploma.loginwithanimation.view.screen.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.StoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.data.local.pref.UserModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainViewModel(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    private var _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: MutableLiveData<Uri?> = _currentImageUri

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun getAllPagerStories(): LiveData<PagingData<Story>> =
        storyRepository.getAllStoriesWithPager().cachedIn(viewModelScope)

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun uploadStory(
        multipartBody: MultipartBody.Part,
        descRequestBody: RequestBody,
        latitudeRequestBody: RequestBody?,
        longitudeRequestBody: RequestBody?,
    ) = storyRepository.uploadStory(
        multipartBody,
        descRequestBody,
        latitudeRequestBody,
        longitudeRequestBody
    )

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

}