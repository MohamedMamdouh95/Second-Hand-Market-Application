package com.example.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodel.ItemViewModel
import kotlinx.android.synthetic.main.item_info_fragment.*
class ItemInfoFragment : Fragment() {

    private val itemVm: ItemViewModel by activityViewModels()

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
            itemDetailsCategory.text = item.category
            itemDetailsExpiryDate.text = item.expiryDate
            itemDetailLocation.text = item.location

            itemVm.setImageStoragePath(item.image)
        })

        itemVm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            itemDetailsImageLandscape?.setImageBitmap(bitmap)
        })

    }
}