package com.dicoding.picodiploma.loginwithanimation.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.google.gson.annotations.SerializedName

data class StoryResponse(

    @field:SerializedName("listStory")
    val listStory: List<Story> = emptyList(),

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

@Parcelize
data class ListStoryItem(

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("lon")
    val lon: Float? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("lat")
    val lat: Float? = null
): Parcelable

data class StoryUploadResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)