package com.dicoding.picodiploma.loginwithanimation.view

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.remote.retrofit.ApiService

//class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, ListStoryItem>() {
//
//    private companion object {
//        const val INITIAL_PAGE_INDEX = 1
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
//    }
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
//        return try {
//            val position = params.key ?: INITIAL_PAGE_INDEX
//            val response = apiService.getAllStories(position, params.loadSize)
//
//            val storyItems = response.listStory?.filterNotNull() ?: emptyList()
//
//            LoadResult.Page(
//                data = storyItems,
//                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
//                nextKey = if (storyItems.isEmpty()) null else position + 1
//            )
//        } catch (exception: Exception) {
//            LoadResult.Error(exception)
//        }
//    }
//}