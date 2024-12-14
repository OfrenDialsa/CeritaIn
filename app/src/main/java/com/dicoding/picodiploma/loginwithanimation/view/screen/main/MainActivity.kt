package com.dicoding.picodiploma.loginwithanimation.view.screen.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.view.LoadingAdapter
import com.dicoding.picodiploma.loginwithanimation.view.StoryPagingAdapter
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.addstory.AddStoryActivity
import com.dicoding.picodiploma.loginwithanimation.view.screen.storymaplocation.StoryMapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.screen.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private val storiesAdapter = StoryPagingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }
        setupView()
        setupAction()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Keluar")
                    setMessage("Anda yakin ingin Log out?")
                    setPositiveButton("Log out") { _, _ ->
                        viewModel.logout()
                    }
                    create()
                    show()
                }
                true
            }
            R.id.menu_search -> {
                checkLocationPermission()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView() {
        binding.rvStory.apply {
            adapter = storiesAdapter.withLoadStateFooter(
                footer = LoadingAdapter {
                    storiesAdapter.retry()
                }
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        viewModel.getAllPagerStories().observe(this) { pagingData ->
            storiesAdapter.submitData(lifecycle, pagingData)
        }

        // Observe adapter load state for progress bar handling
        storiesAdapter.addLoadStateListener { loadState ->
            binding.linearProgressBar.visibility = if (loadState.refresh is androidx.paging.LoadState.Loading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupAction() {
        binding.buttonAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    // Check for location permission and request if not granted
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, proceed to StoryMapsActivity
            startActivity(Intent(this, StoryMapsActivity::class.java))
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to StoryMapsActivity
                    startActivity(Intent(this, StoryMapsActivity::class.java))
                } else {
                    // Permission denied, show a message
                    Toast.makeText(this, "Location permission is required to view the map.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}