package com.example.lab2

import android.content.Context
import android.util.Log
import com.example.lab2.model.Profile
import com.example.lab2.viewmodel.UserViewModel
import com.google.firebase.firestore.auth.User

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lab2.model.Item
import com.example.lab2.viewmodel.ItemListViewModel
import com.example.lab2.viewmodel.ItemViewModel
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlinx.android.synthetic.main.user_card.*
import kotlinx.android.synthetic.main.user_card.view.*
import java.text.DecimalFormat


data class BuyerCardData(val name: String, val location: String, val rating: Float)

class InterestedBuyersAdapter(
    private val data: List<Profile>,
    private val view: View,
    private val userVm: UserViewModel,
    private val itemVm : ItemViewModel,
    private val context : Context?
) : RecyclerView.Adapter<InterestedBuyersAdapter.InterestedBuyerViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    val serverKey = "AAAAKYDtvwI:APA91bGqu8AJP6twAbj0VpeOeJCKPmiwjwEuphagZV3Np7PpZlSppjcoLQoSSBumcsv6cDggxCDSbxq1UQmDHyZyiV7qKkR4EW4S7WkpRT66QppcKOsfKjDYd7ZZwkJReSH7Sw0m_mec"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestedBuyerViewHolder{
        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_card, parent, false)
        return InterestedBuyerViewHolder(v)
    }


    override fun onBindViewHolder(holder: InterestedBuyerViewHolder, position: Int) {
        val cardItemData = data[position]
        val cardDetails = BuyerCardData(cardItemData.nickname,cardItemData.location, cardItemData.rating)

        holder.bind(cardDetails)

        holder.itemView.buyerCard.setOnClickListener {
            userVm.setUserId(cardItemData.documentId)
            userVm.setImageStoragePath(cardItemData.image)
            it.findNavController().navigate(R.id.action_nav_show_buyer_profile)
        }

        holder.itemView.buyerCard.setOnCreateContextMenuListener{contextMenu,_,_ ->
            contextMenu.add("Sell to him !").setOnMenuItemClickListener {
                Log.d("SOLD", "Sold to ${holder.itemView.buyerName.text}" )
                val topic = "BUYING_${itemVm.detailItem.value?.documentId}"
                val buyer = holder.itemView.buyerName.text.toString()
                val item = itemVm.detailItem.value?.title.toString()
                // retrieve buyer id and item id and then add item to buyed list of the user
                val userId = cardItemData.documentId
                val itemId = itemVm.detailItem.value?.documentId
                userVm.addItemToBoughtList(userId!!,itemId!!)
                // send notification to buyer and also other users
                sendSoldNotificationToAll(buyer,item,topic)
                true
            }
        }

    }

    class InterestedBuyerViewHolder (
        v: View
    ) : RecyclerView.ViewHolder(v){

        fun bind(cardDetails: BuyerCardData) {
            itemView.buyerName.text = cardDetails.name
            itemView.buyerLocationText.text = cardDetails.location
            val df = DecimalFormat("#.##")
            itemView.buyerRating.text = df.format(cardDetails.rating)
        }


    }

    private fun sendSoldNotificationToAll(buyer : String, Item : String, topic: String){
        val FCM_API = "https://fcm.googleapis.com/fcm/send"
        val serverKey =
            "key=$serverKey"
        val contentType = "application/json"
        val notification = JSONObject()
        val notificationBody = JSONObject()
        try {
            notificationBody.put("title", "Item in your wishlist has been sold")
            notificationBody.put("message", "$Item was sold to $buyer !\nThanks to those interested in the item!")   //Enter your notification message
            notification.put("to", "/topics/$topic")
            notification.put("data", notificationBody)
            Log.e("LAB_4_SEND", "try")
        } catch (e: JSONException) {
            Log.e("LAB_4_SEND", "onCreate: " + e.message)
        }

        Log.e("LAB_4_SEND", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener { response ->
                Log.i("LAB_4_SEND", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.i("LAB_4_SEND", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }


}