package com.dicoding.picodiploma.loginwithanimation.view.screen.main.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.Story
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val story: Story? = intent.getParcelableExtra(STORY)

        story?.let {
            Glide.with(this)
                .load(it.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.ivDetailPhoto)
            binding.tvDetailName.text = it.name
            binding.tvDetailDescription.text = it.description
        }
    }

    companion object {
        const val STORY = "story"
    }
}