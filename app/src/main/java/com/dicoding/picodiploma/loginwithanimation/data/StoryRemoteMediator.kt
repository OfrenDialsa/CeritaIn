package com.dicoding.picodiploma.loginwithanimation.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.RemoteKeys
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.remote.retrofit.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) : RemoteMediator<Int, Story>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeysClosestCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyLastItem(state)
                val nextPage = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextPage
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeysFirstItem(state)
                val prevPage = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevPage
            }
        }

        try {
            val response = apiService.getAllStories(page, state.config.pageSize)
            val endOfPagination = response.listStory.isEmpty()
            val data = response.listStory

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao().deleteRemoteKeys()
                    storyDatabase.storyDao().deleteAllStory()
                }

                val prevPage = if (page == 1) null else page - 1
                val nextPage = if (endOfPagination == true) null else page + 1
                val keys = data.map {
                    RemoteKeys(id = it.id.toString(), prevKey = prevPage, nextKey = nextPage)
                }
                storyDatabase.remoteKeysDao().insertAll(keys)
                storyDatabase.storyDao().insertStory(data)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPagination == true)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }

    }

    private suspend fun getRemoteKeyLastItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let {
            storyDatabase.remoteKeysDao().getRemoteKeysId(it.id)
        }
    }

    private suspend fun getRemoteKeysFirstItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let {
            storyDatabase.remoteKeysDao().getRemoteKeysId(it.id)
        }
    }

    private suspend fun getRemoteKeysClosestCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let {
                storyDatabase.remoteKeysDao().getRemoteKeysId(it)
            }
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}