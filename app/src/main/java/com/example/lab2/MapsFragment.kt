package com.example.lab2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.MapViewModel
import com.example.lab2.viewmodel.NavigationSource
import com.example.lab2.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private var markerLatitude: Double? = null
    private var markerLongitude: Double? = null
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private val itemViewModel: ItemViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()
    private val TAG = MapsFragment::class.java.simpleName
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        enableMyLocation()
        if (mapViewModel.navigationSource != NavigationSource.COMING_FROM_ITEM_INFO) {
            setMapLongClick(map)
            setPoiClick(map)
        }
        markerLatitude = itemViewModel.detailItem.value?.latitude
        markerLongitude = itemViewModel.detailItem.value?.longitude
        if (markerLatitude != null && markerLongitude != null) {
            val position = LatLng(markerLatitude!!, markerLongitude!!)
            val zoomLevel = 10f
            googleMap.addMarker(MarkerOptions().position(position).title("Marker on Item Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        if (mapViewModel.navigationSource == NavigationSource.COMING_FROM_ITEM_INFO) {
            mapOkButton.visibility = View.GONE
            mapCancelButton.visibility = View.GONE
        } else {
            setOnSelectClickListener()
            setCancelOnClickListener()
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                val userPosition = LatLng(it.latitude, it.longitude)
                userLatitude = it.latitude
                userLongitude = it.longitude
                val zoomLevel = 15f
                //Adding marker at the user position
                map.addMarker(
                    MarkerOptions().position(userPosition)
                        .title("My Location")
                )
                if (mapViewModel.navigationSource == NavigationSource.COMING_FROM_EDIT_USER) {
                    //Move camera to user position
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, zoomLevel))
                }

                if (mapViewModel.navigationSource == NavigationSource.COMING_FROM_ITEM_INFO) {
                    //Moving camera to item location
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatitude?.let { it1 ->
                        markerLongitude?.let { it2 ->
                            LatLng(
                                it1, it2
                            )
                        }
                    }, zoomLevel))
                    //Draw line between item and user current location
                    val line: Polyline = map.addPolyline(
                        PolylineOptions()
                            .add(
                                markerLongitude?.let { it1 ->
                                    markerLatitude?.let { it2 ->
                                        LatLng(
                                            it2,
                                            it1
                                        )
                                    }
                                },
                                LatLng(it.latitude, it.longitude)
                            )
                            .width(7f)
                            .color(Color.BLACK)
                    )
                }
            }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            markerLatitude = latLng.latitude
            markerLongitude = latLng.longitude
            if (mapViewModel.navigationSource == NavigationSource.COMING_FROM_EDIT_USER) {
                userLatitude = latLng.latitude
                userLongitude = latLng.longitude
            }
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
        }
        Log.d(TAG, "The selected longitude $markerLongitude and latitude $markerLatitude")
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setOnSelectClickListener() {
        mapOkButton.setOnClickListener {
            when (mapViewModel.navigationSource) {
                NavigationSource.COMING_FROM_ITEM_EDIT -> {
                    if (markerLongitude != null && markerLatitude != null) {
                        itemViewModel.itemUnderEdit?.latitude = markerLatitude
                        itemViewModel.itemUnderEdit?.longitude = markerLongitude
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(markerLatitude!!, markerLongitude!!, 1)
                        val locationString: String = addresses[0].getAddressLine(0)
                        itemViewModel.itemUnderEdit?.location = locationString
                        Log.d(TAG, locationString)
                        Log.d(
                            TAG,
                            "The Saved longitude $markerLongitude and latitude $markerLatitude"
                        )
                    }
                    findNavController().navigate(R.id.action_mapsFragment_to_nav_item_edit)
                }
                NavigationSource.COMING_FROM_EDIT_USER -> {
                    //Handle what should be returned to the user here.
                    if (userLatitude != null && userLatitude != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                        val locationString: String = addresses[0].getAddressLine(0)
                        userViewModel.profileUnderEdit?.location = locationString
                        userViewModel.profileUnderEdit?.latitude = userLatitude
                        userViewModel.profileUnderEdit?.longitude = userLongitude
                        Log.d(TAG, locationString)
                        Log.d(
                            TAG,
                            "The Saved longitude $userLatitude and latitude $userLongitude"
                        )
                        findNavController().navigate(R.id.action_mapsFragment_to_nav_edit_profile)
                    }
                }
                else -> {
                    //No case yet here
                }
            }

        }

    }

    private fun setCancelOnClickListener() {
        mapCancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_nav_item_edit)
        }
    }

}