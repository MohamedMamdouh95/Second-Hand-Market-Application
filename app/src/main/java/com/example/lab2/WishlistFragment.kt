package com.example.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab2.viewmodel.ItemListViewModel
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.wishlistfragment.*

class WishlistFragment : Fragment() {
    private val userVm: UserViewModel by activityViewModels()
    private val itemVm: ItemViewModel by activityViewModels()
    private val itemListVm: ItemListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.wishlistfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userVm.wishlist.observe(viewLifecycleOwner, Observer { items ->
            wishlistRecyclerViewId.adapter =
                ItemListViewAdapter(
                    items.toMutableList(),
                    view,
                    itemVm,
                    itemListVm,
                    viewLifecycleOwner,
                    isOwnItems = false
                )
        })

        wishlistRecyclerViewId.layoutManager = LinearLayoutManager(this.context)
        wishlistRecyclerViewId.isNestedScrollingEnabled = false

    }
}