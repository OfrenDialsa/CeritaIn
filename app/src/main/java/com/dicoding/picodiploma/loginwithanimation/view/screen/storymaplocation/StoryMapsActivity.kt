package com.dicoding.picodiploma.loginwithanimation.view.screen.storymaplocation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.detail.DetailStoryActivity
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.bumptech.glide.request.transition.Transition
import com.dicoding.picodiploma.loginwithanimation.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.OnMapReadyCallback
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityStoryMapsLocationBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.target.CustomTarget
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.google.android.gms.maps.model.MapStyleOptions
import kotlin.getValue

class StoryMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoryMapsLocationBinding
    private val viewModel by viewModels< StoryMapsViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryMapsLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val jakartaLatLng = LatLng(-6.2088, 106.8456)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakartaLatLng, 10f))

        setMapStyle()
        getMyLocation()
        addStoriesToMap()
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addStoriesToMap() {
        viewModel.getStoriesWithLocation().observe(this) { result ->
            if (isDestroyed || isFinishing) return@observe // Skip processing if activity is destroyed

            when (result) {
                is Result.Loading -> {
                    Toast.makeText(
                        this,
                        getString(R.string.getting_worldwide_stories),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    result.data.listStory.forEach { data ->
                        // Ensure lat and lon are not null before proceeding
                        val lat = data.lat
                        val lon = data.lon

                        if (lat != null && lon != null) {
                            val latLng = LatLng(lat.toDouble(), lon.toDouble())
                            boundsBuilder.include(latLng)

                            if (!isDestroyed && !isFinishing) {
                                Glide.with(this)
                                    .asBitmap()
                                    .load(data.photoUrl)
                                    .apply(RequestOptions().override(200, 200))
                                    .signature(ObjectKey(data.id))
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_place_holder)
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            val bitmapDescriptor =
                                                BitmapDescriptorFactory.fromBitmap(resource)
                                            val marker = mMap.addMarker(
                                                MarkerOptions()
                                                    .position(latLng)
                                                    .title(data.name)
                                                    .snippet(data.description)
                                                    .icon(bitmapDescriptor)
                                            )
                                            marker?.tag = data
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                        }
                                    })
                            }
                        } else {
                            Log.e("StoryMapsActivity", "Invalid latitude or longitude for story: ${data.id}")
                        }
                    }

                    mMap.setOnMarkerClickListener { marker: Marker? ->
                        marker?.apply {
                            showInfoWindow()
                            val story = marker.tag as? ListStoryItem
                            story?.let {
                                val intent = Intent(
                                    this@StoryMapsActivity,
                                    DetailStoryActivity::class.java
                                ).apply {
                                    putExtra(DetailStoryActivity.STORY, it)
                                }
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (!isDestroyed && !isFinishing) {
                                        startActivity(
                                            intent,
                                            ActivityOptionsCompat.makeSceneTransitionAnimation(this@StoryMapsActivity)
                                                .toBundle()
                                        )
                                    }
                                }, 500)
                            }
                        }
                        true
                    }

                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            resources.displayMetrics.widthPixels,
                            resources.displayMetrics.heightPixels,
                            0
                        )
                    )

                }
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Toast.makeText(this, getString(R.string.style_parsing_failed), Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.cannot_find_style_map), Toast.LENGTH_SHORT).show()
        }
    }

}