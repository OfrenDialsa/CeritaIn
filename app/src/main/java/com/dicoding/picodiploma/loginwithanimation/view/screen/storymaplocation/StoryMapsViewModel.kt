package com.dicoding.picodiploma.loginwithanimation.view.screen.storymaplocation

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.StoryRepository

class StoryMapsViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStoriesWithLocation() = repository.getAllStories(location = 1)

}