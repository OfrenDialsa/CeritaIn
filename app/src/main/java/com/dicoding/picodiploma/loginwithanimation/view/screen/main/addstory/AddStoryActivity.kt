package com.dicoding.picodiploma.loginwithanimation.view.screen.main.addstory

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.Manifest
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoryUploadResponse
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.loginwithanimation.util.getImageUri
import com.dicoding.picodiploma.loginwithanimation.util.reduceFileImage
import com.dicoding.picodiploma.loginwithanimation.util.uriToFile
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.MainActivity
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.addStoryImage.setImageURI(uri)
            viewModel.setCurrentImageUri(uri)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            binding.addStoryImage.setImageURI(viewModel.currentImageUri.value)
        } else {
            viewModel.setCurrentImageUri(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.apply {
            show()
            title = getString(R.string.actionbar_upload_story)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCamera() }
            buttonAdd.setOnClickListener { uploadStory() }
            locCheck.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (checkLocationPermissions()) {
                        requestLocationUpdates()
                    } else {
                        requestLocationPermission()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentImageUri.observe(this) {
            binding.addStoryImage.setImageURI(it)
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString()

        if (description.isEmpty()) {
            Toast.makeText(this, getString(R.string.description_empty), Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.currentImageUri.value?.let { uri ->
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.confirm_upload_title))
                setMessage(getString(R.string.confirm_upload_message))
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    handleStoryUpload(uri, description)
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        } ?: Toast.makeText(this, getString(R.string.upload_error), Toast.LENGTH_SHORT).show()
    }

    private fun handleStoryUpload(uri: Uri, description: String) {
        binding.apply {
            buttonAdd.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            val imageFile = withContext(Dispatchers.IO) {
                uriToFile(uri, this@AddStoryActivity).reduceFileImage()
            }

            val descriptionRequestBody = description.toRequestBody(MultipartBody.FORM)
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                imageFile.asRequestBody(MultipartBody.FORM)
            )

            val latitudeRequestBody = currentLocation?.latitude?.toString()?.toRequestBody(MultipartBody.FORM)
            val longitudeRequestBody = currentLocation?.longitude?.toString()?.toRequestBody(MultipartBody.FORM)

            viewModel.uploadStory(
                multipartBody = imageMultipart,
                descRequestBody = descriptionRequestBody,
                latitudeRequestBody = latitudeRequestBody,
                longitudeRequestBody = longitudeRequestBody
            ).observe(this@AddStoryActivity) { result ->
                handleUploadResult(result)
            }
        }
    }

    private fun handleUploadResult(result: Result<StoryUploadResponse>) {
        when (result) {
            is Result.Error -> showError(result.error)
            is Result.Loading -> binding.progressBar.visibility = View.VISIBLE
            is Result.Success -> handleSuccess(result.data.message, result.data.error)
        }
    }

    private fun showError(error: String) {
        binding.apply {
            progressBar.visibility = View.GONE
            buttonAdd.isEnabled = true
        }
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccess(message: String, isError: Boolean) {
        binding.apply {
            progressBar.visibility = View.GONE
            buttonAdd.isEnabled = true
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        if (!isError) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }

    private fun startCamera() {
        viewModel.setCurrentImageUri(getImageUri(this))
        launcherIntentCamera.launch(viewModel.currentImageUri.value ?: Uri.EMPTY)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create().apply {
                priority = Priority.PRIORITY_HIGH_ACCURACY
                interval = 10000L
                fastestInterval = 5000L
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.isNotEmpty()) {
                        currentLocation = locationResult.lastLocation
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            requestLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }
}