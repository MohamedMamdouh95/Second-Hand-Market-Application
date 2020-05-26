package com.example.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.interested_buyers_fragment.*

class InterestedBuyersFragment : Fragment() {

    private val userVm: UserViewModel by activityViewModels()
    private val itemVm: ItemViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.interested_buyers_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemVm.interestedBuyers.observe(viewLifecycleOwner, Observer { buyers ->
            buyersRecycler.adapter = InterestedBuyersAdapter(
                buyers,
                view,
                userVm
            )
        })
        buyersRecycler.layoutManager = LinearLayoutManager(this.context)

        buyersRecycler.isNestedScrollingEnabled = false

    }
}