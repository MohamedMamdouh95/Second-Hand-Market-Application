package com.example.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.MapViewModel
import com.example.lab2.viewmodel.NavigationSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.item_info_fragment.*

class ItemInfoFragment : Fragment() {

    private val itemVm: ItemViewModel by activityViewModels()
    private val mapVm: MapViewModel by activityViewModels()
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
        val latitude = itemVm.detailItem.value?.latitude
        val longitudes = itemVm.detailItem.value?.longitude
        if (latitude != null && longitudes != null) {
            val position = LatLng(latitude, longitudes)
            val zoomLevel = 14f
            googleMap.addMarker(MarkerOptions().position(position).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
        }

        /* enableMyLocation()
         setMapLongClick(map)
         setPoiClick(map)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Observing item vm for item details and picture
        itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            itemDetailsTitle.text = item.title
            itemDetailsPrice.text = item.price
            itemDetailsDescription.text = item.description
            itemDetailsCategory.text = item.category + " - " + item.subcategory
            itemDetailsExpiryDate.text = item.expiryDate
            itemDetailLocation.text = item.location
            itemVm.setImageStoragePath(item.image)
        })

        itemVm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            itemDetailsImageLandscape?.setImageBitmap(bitmap)
        })
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        mapButton.setOnClickListener {
            mapVm.navigationSource = NavigationSource.COMING_FROM_ITEM_INFO
            findNavController().navigate(R.id.action_nav_item_details_to_mapsFragment)
        }
    }


}