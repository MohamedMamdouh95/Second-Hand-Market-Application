package com.example.lab2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodel.MapViewModel

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private var latitude: Double? = null
    private var longitude: Double? = null
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
        val sydney = LatLng(-34.0, 151.0)
        val zoomLevel = 17f
        //googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))
        enableMyLocation()
        setMapLongClick(map)
        setPoiClick(map)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        setOnSelectClickListener()
        setCancelOnClickListener()
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
            latitude = latLng.latitude
            longitude = latLng.longitude

            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
        }
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
            if (longitude != null && latitude != null) {
                mapViewModel.latitude.value = latitude.toString()
                mapViewModel.longitude.value = longitude.toString()
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                val locationString:String = addresses[0].getAddressLine(0)
                mapViewModel.locationString.value = locationString
                Log.d(TAG,locationString)
            }
            findNavController().navigate(R.id.action_mapsFragment_to_nav_item_edit)
        }

    }

    private fun setCancelOnClickListener() {
        mapCancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_nav_item_edit)
        }
    }

}