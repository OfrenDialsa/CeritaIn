package com.dicoding.picodiploma.loginwithanimation.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.StoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import kotlinx.coroutines.*

internal class StackRemoteViewsFactory(
    private val mContext: Context,
    private val storiesRepository: StoryRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<Bitmap>()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
    }

    private var dataLoaded = false

    override fun onDataSetChanged() {
        if (dataLoaded) {
            // If the data has already been loaded, skip the re-fetch
            return
        }

        dataLoaded = true

        mWidgetItems.clear()

        runBlocking {
            val imageResUrls: List<Story> = try {
                storiesRepository.getListStory()?.take(2) ?: listOf()
            } catch (e: Exception) {
                Log.e("StackRemoteViewsFactory", "Error fetching stories: ${e.message}")
                listOf()
            }

            Log.d("StackRemoteViewsFactory", "Fetched ${imageResUrls.size} stories")

            val bitmaps = imageResUrls.mapNotNull { resUrl ->
                try {
                    Log.d("StackRemoteViewsFactory", "Loading image from: ${resUrl?.photoUrl}")
                    Glide.with(mContext)
                        .asBitmap()
                        .load(resUrl?.photoUrl ?: "https://sample-videos.com/img/Sample-jpg-image-50kb.jpg")
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get()
                } catch (e: Exception) {
                    Log.e("StackRemoteViewsFactory", "Error loading image: ${e.message}")
                    null
                }
            }

            Log.d("StackRemoteViewsFactory", "Loaded ${bitmaps.size} bitmaps")

            mWidgetItems.addAll(bitmaps)

            val remoteViews = RemoteViews(mContext.packageName, R.layout.story_widget)

            val textToDisplay = if (bitmaps.isNotEmpty()) {
                "Loaded ${bitmaps.size} stories!"
            } else {
                "No stories available"
            }
            remoteViews.setTextViewText(R.id.banner_text, textToDisplay)

            val appWidgetManager = AppWidgetManager.getInstance(mContext)
            appWidgetManager.updateAppWidget(
                appWidgetManager.getAppWidgetIds(ComponentName(mContext, StoryWidget::class.java)),
                remoteViews
            )

            appWidgetManager.notifyAppWidgetViewDataChanged(
                appWidgetManager.getAppWidgetIds(ComponentName(mContext, StoryWidget::class.java)),
                R.id.stack_view
            )
        }
    }

    override fun onDestroy() {
        mWidgetItems.clear()
        scope.cancel()
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.item_widget)

        if (position < mWidgetItems.size) {
            rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])
        } else {
            Log.e("StackRemoteViewsFactory", "Invalid position $position")
        }

        val extras = bundleOf(
            StoryWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}