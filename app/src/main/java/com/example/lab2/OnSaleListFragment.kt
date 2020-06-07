package com.example.lab2

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab2.model.Item
import com.example.lab2.viewmodel.ItemListViewModel
import com.example.lab2.viewmodel.ItemViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.onsalelistfragment.*

class OnSaleListFragment : Fragment(), MyContract {
    private val itemListVm: ItemListViewModel by viewModels()
    private val itemVm: ItemViewModel by activityViewModels()
    private val TAG = "ON_SALE_FRAGMENT"
    private lateinit var itemListViewAdapter: ItemListViewAdapter
    private lateinit var filterDialogFragment: FilterDialogFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        filterDialogFragment = FilterDialogFragment(this)
        return inflater.inflate(R.layout.onsalelistfragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_option_menu, menu)
        // Associate searchable configuration with the SearchView
        val searchView =
            SearchView((context as MainActivity).supportActionBar?.themedContext ?: context)
        menu.findItem(R.id.search).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = searchView
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                itemListViewAdapter.filter.filter(newText)
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        filterDialogFragment.setTargetFragment(requireParentFragment(), 0)
        return when (item.itemId) {
            R.id.search -> {
                Log.d(tag, "YOU CLICKED ON THE SEARCH ICON")
                filterDialogFragment.show(transaction, "Filter Dialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        }
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        userId?.let {
            itemListVm.itemsOnSale.observe(viewLifecycleOwner, Observer { items ->
                itemListViewAdapter = ItemListViewAdapter(
                    items.toMutableList(),
                    view,
                    itemVm,
                    itemListVm,
                    viewLifecycleOwner,
                    isOwnItems = false
                )
                onSaleRecyclerViewId.adapter = itemListViewAdapter

            })

            onSaleRecyclerViewId.layoutManager = LinearLayoutManager(this.context)
            onSaleRecyclerViewId.isNestedScrollingEnabled = false
            val navController = view.findNavController()
            onSaleFab.setOnClickListener {
                itemVm.setItemId(null)
                itemVm.setImageStoragePath(null)
                itemVm.itemUnderEdit = null
                navController.navigate(R.id.nav_item_edit)
            }
        }

    }


    override fun searchCriteria(minValue: Int, maxValue: Int, location: String, category: String) {
        val copyOfData = ArrayList<Item>()
        itemListVm.itemsOnSale.value?.let { copyOfData.addAll(it) }
        val filteredList = ArrayList<Item>()
        //I don't check the price because it has value by default
        val compareOption = getCompareOption(location, category)
        for (item in copyOfData) {
            when (compareOption) {
                CompareOptions.LOCATION_CATEGORY -> {
                    if (item.price == "")
                        item.price = 0.toString()
                    if (haveSameLocation(location, item.location)
                        && inTheSameCategory(category, item.category)
                        && withinRange(minValue, maxValue, item.price.toInt())
                    ) {
                        filteredList.add(item)
                    }
                }
                CompareOptions.NO_LOCATION_CATEGORY -> {
                    if (item.price == "")
                        item.price = 0.toString()
                    if (inTheSameCategory(category, item.category)
                        && withinRange(minValue, maxValue, item.price.toInt())
                    ) {
                        filteredList.add(item)
                    }
                }
                CompareOptions.LOCATION_NO_CATEGORY -> {
                    if (item.price == "")
                        item.price = 0.toString()
                    if (haveSameLocation(location, item.location)
                        && withinRange(minValue, maxValue, item.price.toInt())
                    ) {
                        filteredList.add(item)
                    }
                }
                else -> {
                    if (item.price == "")
                        item.price = 0.toString()
                    if (withinRange(minValue, maxValue, item.price.toInt())) {
                        filteredList.add(item)
                    }
                }
            }

        }
        itemListVm.itemsOnSale.value = filteredList
    }

    private fun getCompareOption(location: String, category: String): CompareOptions {
        return if (location == "" && category == "")
            CompareOptions.NO_LOCATION_NO_CATEGORY
        else if (location != "" && category == "")
            CompareOptions.LOCATION_NO_CATEGORY
        else if (location == "" && category != "")
            CompareOptions.NO_LOCATION_CATEGORY
        else
            CompareOptions.LOCATION_CATEGORY
    }

    enum class CompareOptions {
        NO_LOCATION_NO_CATEGORY, LOCATION_NO_CATEGORY, NO_LOCATION_CATEGORY, LOCATION_CATEGORY
    }


    private fun haveSameLocation(locationRequired: String, itemLocation: String) =
        locationRequired.toLowerCase() == itemLocation.toLowerCase()

    private fun inTheSameCategory(categoryRequired: String, itemCategory: String) =
        categoryRequired.toLowerCase() == itemCategory.toLowerCase()

    private fun withinRange(min: Int, max: Int, itemPrice: Int) = itemPrice in min..max

}
