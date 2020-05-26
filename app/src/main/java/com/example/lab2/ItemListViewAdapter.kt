package com.example.lab2

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


data class CardData(val title: String, var price: String, val image: String?)

class ItemListViewAdapter(
    private val data: MutableList<Item>,
    private val view: View,
    private val itemVm: ItemViewModel,
    private val listVm: ItemListViewModel,
    private val lifeCycleOwner: LifecycleOwner,
    private val isOwnItems: Boolean
) : RecyclerView.Adapter<ItemListViewAdapter.ItemListViewHolder>(), Filterable {
    private val dataFullList = ArrayList<Item>(data)
    override fun getItemCount(): Int {
        if (data.isNotEmpty()) {
            view.findViewById<TextView>(R.id.empty_view).visibility = View.GONE
        }
        return data.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)

        if (!isOwnItems) {
            val editButton = v.findViewById<ImageButton>(R.id.editCard)
            editButton.visibility = View.GONE
        }

        return ItemListViewHolder(v, listVm, lifeCycleOwner)
    }


    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        val cardItemData = data[position]
        val cardDetails = CardData(cardItemData.title, cardItemData.price, cardItemData.image)

        holder.bind(cardDetails)

        holder.image.setOnClickListener {
            itemVm.setItemId(cardItemData.documentId)
            itemVm.setImageStoragePath(cardItemData.image)
            it.findNavController().navigate(R.id.nav_item_details)
        }
        holder.imageButton.setOnClickListener {
            itemVm.setItemId(cardItemData.documentId)
            itemVm.setImageStoragePath(cardItemData.image)
            it.findNavController().navigate(R.id.nav_item_edit)
        }
    }

    class ItemListViewHolder(
        v: View,
        private val listVm: ItemListViewModel,
        private val lifeCycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.card_title)
        val price: TextView = v.findViewById(R.id.card_price)
        val image: ImageView = v.findViewById(R.id.cardImageView)
        val imageButton: ImageButton = v.findViewById(R.id.editCard)

        fun bind(cardDetails: CardData) {
            title.text = cardDetails.title
            //TODO: price should never be "" and € should be added as a label in the layout
            if (cardDetails.price == "")
                cardDetails.price = "0"
            price.text = cardDetails.price + " €"
            cardDetails.image?.let {
                listVm.getItemImageAsBitmap(it).observe(lifeCycleOwner, Observer { bitmap ->
                    image.setImageBitmap(bitmap)
                })
            }

        }
    }

    //Create object that extends the Filter abstract class
    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Item>()
            //if there are no filter that means that all things have to be shown
            if (constraint.isNullOrEmpty() || constraint.isEmpty()) {
                filteredList.addAll(dataFullList)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()
                for (item in dataFullList) {
                    if (item.title.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            return FilterResults().apply {
                this.values = filteredList
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            data.clear()
            data.addAll(results?.values as MutableList<Item>)
            notifyDataSetChanged()
        }

    }

    override fun getFilter(): Filter {
        return filter
    }

}