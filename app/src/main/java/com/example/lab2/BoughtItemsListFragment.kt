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
import kotlinx.android.synthetic.main.boughtitemslistfragment.*

class BoughtItemsListFragment : Fragment() {
    private val userVm: UserViewModel by activityViewModels()
    private val itemVm: ItemViewModel by activityViewModels()
    private val itemListVm: ItemListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.boughtitemslistfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userVm.boughtList.observe(viewLifecycleOwner, Observer { items ->
            boughtlistRecyclerViewId.adapter =
                ItemListViewAdapter(
                    items.toMutableList(),
                    view,
                    itemVm,
                    userVm,
                    itemListVm,
                    viewLifecycleOwner,
                    isOwnItems = false,
                    isBought = true
                )
        })

        boughtlistRecyclerViewId.layoutManager = LinearLayoutManager(this.context)
        boughtlistRecyclerViewId.isNestedScrollingEnabled = false

    }
}