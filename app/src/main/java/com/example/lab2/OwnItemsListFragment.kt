package com.example.lab2

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab2.viewmodel.ItemListViewModel
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.own_items_list_fragment.*


class OwnItemsListFragment : Fragment() {

    private val vm: ItemListViewModel by viewModels()
    private val itemVm: ItemViewModel by activityViewModels()
    private val userVm : UserViewModel by activityViewModels()
    private lateinit var itemListViewAdapter: ItemListViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.own_items_list_fragment, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.ownItems.observe(viewLifecycleOwner, Observer { items ->
            itemListViewAdapter = ItemListViewAdapter(
                items.toMutableList(),
                view,
                itemVm,
                userVm,
                vm,
                viewLifecycleOwner,
                isOwnItems = true,
                isBought = false
            )
            recyclerViewId.adapter = itemListViewAdapter
        })
        recyclerViewId.layoutManager = LinearLayoutManager(this.context)

        recyclerViewId.isNestedScrollingEnabled = false

        val navController = view.findNavController()
        fab.setOnClickListener { _ ->
            itemVm.setItemId(null)
            itemVm.setImageStoragePath(null)
            itemVm.itemUnderEdit = null
            navController.navigate(R.id.nav_item_edit)
        }

    }
}

