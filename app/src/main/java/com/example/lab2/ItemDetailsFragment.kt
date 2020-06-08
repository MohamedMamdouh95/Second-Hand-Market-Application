package com.example.lab2

import android.app.Notification
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import io.opencensus.trace.MessageEvent.builder
import kotlinx.android.synthetic.main.itemdetailsfragment.*
import org.json.JSONException
import org.json.JSONObject

class ItemDetailsFragment : Fragment() {

    private val itemVm: ItemViewModel by activityViewModels()
    private val userVm: UserViewModel by activityViewModels()
    private var menu: Menu? = null
    private var itemOnWishList = false
    private var bought = false
    private val serverKey = "AAAAKYDtvwI:APA91bGqu8AJP6twAbj0VpeOeJCKPmiwjwEuphagZV3Np7PpZlSppjcoLQoSSBumcsv6cDggxCDSbxq1UQmDHyZyiV7qKkR4EW4S7WkpRT66QppcKOsfKjDYd7ZZwkJReSH7Sw0m_mec"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.itemdetailsfragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.item_details_menu, menu)
        this.menu = menu

        //Setting up option menu base on whether the item is own or not
        itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            if (item.vendorId != Firebase.auth.currentUser!!.uid) {
                menu.findItem(R.id.nav_item_edit)?.setVisible(false)
                menu.findItem(R.id.wishlist_option)?.setVisible(true)
            } else {
                menu.findItem(R.id.nav_item_edit)?.setVisible(true)
                menu.findItem(R.id.wishlist_option)?.setVisible(false)
            }
        })

        //Handling wishlist button state for horizontal layout
        userVm.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->
            val wishlistIds = wishlist.map { item -> item.documentId }
            itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
                item.documentId?.let {
                    var icon = R.drawable.favorite_border_white_24dp
                    if (wishlistIds.contains(it)) {
                        icon = R.drawable.favorite_white_24dp
                    }
                    menu?.findItem(R.id.wishlist_option)?.icon =
                        ResourcesCompat.getDrawable(resources, icon, resources.newTheme())
                }
            })
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_item_edit -> {
                itemVm.newImageBitmap.value = null
                itemVm.itemUnderEdit = null
                findNavController().navigate(R.id.action_itemDetailsFragment_to_itemEditFragment2)
            }
            //Handling wishlist button clicks for horizontal layout
            R.id.wishlist_option -> itemVm.detailItem.value?.documentId?.let {
                if (itemOnWishList) {
                    item.icon = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.favorite_border_white_24dp,
                        resources.newTheme()
                    )
                    itemOnWishList = false
                    userVm.removeItemFromWishlist(it)
                } else {
                    item.icon = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.favorite_white_24dp,
                        resources.newTheme()
                    )
                    itemOnWishList = true
                    userVm.addItemToWishlist(it)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments?.getBoolean("bought") == true) {
            Log.d("FAB", "FAB INVISIBLE")
            bought = true

        }

        //Getting wishlist FAB from the App bar layout
        val wishFAB: FloatingActionButton? =
            view.rootView?.findViewById(R.id.wishlistFab)

        if(bought) wishFAB?.visibility = View.GONE

        //Checking if item is on wishlist and setting the button correctly
        userVm.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->
            val wishlistIds = wishlist.map { item -> item.documentId }
            itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
                item.documentId?.let {
                    var icon = R.drawable.favorite_border_white_24dp
                    itemOnWishList = false
                    if (wishlistIds.contains(it)) {
                        icon = R.drawable.favorite_white_24dp
                        itemOnWishList = true
                    }
                    wishFAB?.setImageResource(icon)
                }
            })
        })

        //Handling wishlist button clicks for portrait layout
        wishFAB?.setOnClickListener {
            itemVm.detailItem.value?.documentId?.let {
                val selling_topic : String = "SELLING_$it"
                val buying_topic : String = "BUYING_$it"
                if (itemOnWishList) {
                    wishFAB?.setImageResource(R.drawable.favorite_border_white_24dp)
                    itemOnWishList = false
                    userVm.removeItemFromWishlist(it)
                    unsubscribeBuyingItem(buying_topic)

                } else {
                    wishFAB?.setImageResource(R.drawable.favorite_white_24dp)
                    itemOnWishList = true
                    userVm.addItemToWishlist(it)
                    val interestedUser = userVm.getUser(Firebase.auth.uid!!)?.value?.nickname.toString()
                    val item = itemVm.detailItem.value?.title.toString()
                    subscribeBuyingItem(buying_topic)
                    sendNotificationToSeller(interestedUser,item,selling_topic)
                }

            }
        }

        //Observing item vm for item details and picture

        itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            if (item.vendorId != Firebase.auth.currentUser!!.uid) {
                tabLayout.visibility = View.GONE
                if(!bought)
                wishFAB?.visibility = View.VISIBLE
            } else {
                tabLayout.visibility = View.VISIBLE
                if(!bought)
                wishFAB?.visibility = View.INVISIBLE
            }
        })

        itemVm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            val imageViewCollapsing: ImageView? =
                view.rootView?.findViewById(R.id.imageViewCollapsing)
            imageViewCollapsing?.setImageBitmap(bitmap)
        })

        //TAB HANDLING
        viewPager.isUserInputEnabled = false
        viewPager.adapter = ItemDetailsPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Info"
                1 -> tab.text = "Interested buyers"
            }
        }.attach()

    }

    private inner class ItemDetailsPagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {

            val frag = when (position) {
                1 -> InterestedBuyersFragment()
                else -> ItemInfoFragment()
            }
            return frag
        }
    }

    private fun subscribeBuyingItem(topic: String){
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$topic")
            .addOnCompleteListener { task ->
                Log.d("LAB4_BUYING", topic)
            }
    }

    private fun unsubscribeBuyingItem(topic:String){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/$topic")
            .addOnCompleteListener { task ->
                Log.d("LAB4_UNBUYING", topic)
            }
    }

    private fun sendNotificationToSeller(user : String, item : String ,topic : String){
         val FCM_API = "https://fcm.googleapis.com/fcm/send"
         val serverKey =
             "key=$serverKey"
         val contentType = "application/json"
        val notification = JSONObject()
        val notificationBody = JSONObject()
        try {
            notificationBody.put("title", "New user added your item in wishlist")
            notificationBody.put("message", "$item has been added to an user's wishlist\nMaybe he's the right person to sell!")   //Enter your notification message
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
