package com.example.lab2

import com.example.lab2.model.Profile
import com.example.lab2.viewmodel.UserViewModel
import com.google.firebase.firestore.auth.User

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.model.Item
import com.example.lab2.viewmodel.ItemListViewModel
import com.example.lab2.viewmodel.ItemViewModel


data class BuyerCardData(val name: String)

class InterestedBuyersAdapter(
    private val data: List<Profile>,
    private val view: View,
    private val userVm: UserViewModel
) : RecyclerView.Adapter<InterestedBuyersAdapter.InterestedBuyerViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestedBuyerViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_card, parent, false)
        return InterestedBuyerViewHolder(v)
    }


    override fun onBindViewHolder(holder: InterestedBuyerViewHolder, position: Int) {
        val cardItemData = data[position]
        val cardDetails = BuyerCardData(cardItemData.nickname)

        holder.bind(cardDetails)

        holder.name.setOnClickListener {
            userVm.setUserId(cardItemData.documentId)
            userVm.setImageStoragePath(cardItemData.image)/*
            if (it.findNavController().currentDestination?.id == R.id.nav_edit_profile) {
                it.findNavController().navigate(R.id.action_nav_show_buyer_profile)
            }*/
            it.findNavController().navigate(R.id.action_nav_show_buyer_profile)
        }

    }

    class InterestedBuyerViewHolder(
        v: View
    ) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.buyerName)

        fun bind(cardDetails: BuyerCardData) {
            name.text = cardDetails.name
        }
    }


}