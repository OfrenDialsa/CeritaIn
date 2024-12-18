package com.dicoding.picodiploma.loginwithanimation.view.component

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.databinding.ItemStoryBinding
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.detail.DetailStoryActivity
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.detail.DetailStoryActivity.Companion.STORY

class StoryPagingAdapter :
    PagingDataAdapter<Story, StoryPagingAdapter.MyViewHolder>(DIFF_CALLBACK) {

    class MyViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            Glide
                .with(binding.root.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.ivItemPhoto)

            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java).apply {
                    putExtra(STORY, story)
                }

                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    Pair(binding.ivItemPhoto, "image"),
                    Pair(binding.tvItemName, "title"),
                    Pair(binding.tvItemDescription, "desc"),
                )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id // Compare story IDs
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem // Compare entire story contents
            }
        }
    }
}