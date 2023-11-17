package com.chaitanya.todoweathermvvmdi.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.model.WeatherResponse
import com.chaitanya.todoweathermvvmdi.utils.DataHandler
import com.chaitanya.todoweathermvvmdi.utils.DetailState
import com.chaitanya.todoweathermvvmdi.viewModel.NoteViewModel
import com.chaitanya.todomvvmdi.views.adapter.NoteAdapter
import com.chaitanya.todoweathermvvmdi.R
import com.chaitanya.todoweathermvvmdi.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val viewModel: NoteViewModel by activityViewModels()

    private lateinit var adapter: NoteAdapter

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    // Permission request launcher
    private var requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permission = it.key
                val isGranted = it.value
                if (isGranted) {
                    when (permission) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> {
                            setLocation()
                        }
                    }
                } else {
                    if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                        error()
                        Toast.makeText(
                            requireContext(),
                            "Provide Fine location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FAB to add new Note
        binding.fabAdd.setOnClickListener {
            viewModel.setDetailState(DetailState.Insert)
            findNavController().navigate(R.id.action_homeFragment_to_detailsFragment)
        }
        initNotes()

        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Observe the fetchLocation LiveData to detrmine whether to fetch the location data
        viewModel.fetchLocation.observe(viewLifecycleOwner) {
            if (it) {
                initLocation()
            } else {
                weatherLayout()
                viewModel.weatherData.observe(viewLifecycleOwner) { data ->
                    setWeatherUI(data)
                }
            }
        }

        // refresh and try again button
        binding.ivRefresh.setOnClickListener { initLocation() }
        binding.tryAgain.setOnClickListener { initLocation() }
    }

    private fun initNotes() {
        adapter = NoteAdapter(
            emptyList(),
            { note ->
                viewModel.setDetailState(DetailState.Update)
                viewModel.setDetail(note)
                findNavController().navigate(R.id.action_homeFragment_to_detailsFragment)
            },
            { deletenote ->
                viewModel.deleteNote(deletenote)
            },
            { showNote ->
                viewModel.setDetailState(DetailState.Show)
                viewModel.setDetail(showNote)
                findNavController().navigate(R.id.action_homeFragment_to_detailsFragment)
            },
            { coNote, isChecked ->
                viewModel.updateNote(
                    NoteEntity(
                        id = coNote.id,
                        title = coNote.title,
                        description = coNote.description,
                        isPriority = coNote.isPriority,
                        isCompleted = isChecked
                    )
                )
            },
            { card, note ->
                performLongClick(card, note)
            }
        )
        binding.rvNotes.adapter = adapter

        // Observe the LiveData for the list of notes and update the adapter
        viewModel.allNotes.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.updateItems(it)
            }
        }
    }

    private fun performLongClick(card: MaterialCardView, note: NoteEntity) {

        //Highlight Card on long Click
        card.strokeWidth = dpToPx(5)
        card.setStrokeColor(ColorStateList.valueOf(Color.GREEN))

        // Create a popup menu for context menu
        val popupMenu = PopupMenu(requireContext(), binding.ivMenu)
        popupMenu.inflate(R.menu.context_menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.menu_item_high_priority -> {

                        viewModel.updateNoteToPriority(note.id.toLong())

                        // Reset card styling
                        card.strokeWidth = dpToPx(2)
                        card.setStrokeColor(ColorStateList.valueOf(Color.BLACK))
                        return true
                    }
                }
                return false
            }
        })
        binding.ivMenu.visibility = View.VISIBLE

        popupMenu.show()

        // Reset card styling on dismiss
        popupMenu.setOnDismissListener {
            card.strokeWidth = dpToPx(2)
            card.setStrokeColor(ColorStateList.valueOf(Color.BLACK))
            binding.ivMenu.visibility = View.INVISIBLE
        }
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics = requireContext().resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun initLocation() {
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            Toast.makeText(
                requireContext(),
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun setLocation() {
        showProgressDialog("Getting Location")

        // Create a location request
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(100)
            .setMaxUpdateDelayMillis(500)
            .setMaxUpdates(1)
            .build()

        try {
            // Request location updates using the FusedLocationProviderClient
            mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } catch (_: Exception) {
            // Show Error UI
            error()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation

            if (lastLocation != null) {
                this@HomeFragment.mLatitude = lastLocation.latitude
            }
            if (lastLocation != null) {
                this@HomeFragment.mLongitude = lastLocation.longitude
            }
            getLocationWeatherDetails()
        }
    }

    private fun getLocationWeatherDetails() {
        try {
            // Observe the weather data using LiveData and handle different states
            viewModel.weatherDetails.observe(viewLifecycleOwner) { dataHandler ->
                when (dataHandler) {
                    is DataHandler.SUCCESS -> {
                        val data = dataHandler.data
                        if (data != null) {
                            // Set weather data in the view model and indicate that weather has been fetched
                            viewModel.setWeatherData(data)
                            viewModel.shouldFetch(false)
                        }
                    }

                    is DataHandler.ERROR -> {
                        error()
                    }

                    is DataHandler.LOADING -> {
                        showProgressDialog("Getting Weather!")
                    }
                }
            }
        } catch (_: Exception) {
            error()
        }

        viewModel.getWeather(mLatitude.toString(), mLongitude.toString())
    }

    private fun setWeatherUI(data: WeatherResponse) {

        //Load and display weather icon using Glide

        Glide.with(this)
            .load("https:" + data.current.condition.icon)
            .error(R.drawable.baseline_broken_image_24)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivCondition)

        weatherLayout()

        binding.apply {
            tvCondition.text = data.current.condition.text
            tvName.text = data.location.name
            tvRegion.text = data.location.region
            tvCountry.text = data.location.country
            tvTemp.text = data.current.temp_c.toString()
            tvWind.text = data.current.wind_kph.toString()

            // Set the appropriate image based on whether it's day or night
            if (data.current.is_day == 0) {
                ivIsDay.setImageResource(R.drawable.moon_and_stars)
            } else {
                ivIsDay.setImageResource(R.drawable.sun_icon)
            }
        }
    }

    private fun error() {
        binding.lnlProgress.visibility = View.INVISIBLE
        binding.Error.visibility = View.VISIBLE
        binding.lnlDetails.visibility = View.INVISIBLE
    }

    private fun weatherLayout() {
        binding.lnlProgress.visibility = View.INVISIBLE
        binding.lnlDetails.visibility = View.VISIBLE
        binding.Error.visibility = View.INVISIBLE
    }

    private fun showProgressDialog(message: String) {
        binding.tvProgress.text = message
        binding.lnlProgress.visibility = View.VISIBLE
        binding.Error.visibility = View.INVISIBLE
        binding.lnlDetails.visibility = View.INVISIBLE
    }

}