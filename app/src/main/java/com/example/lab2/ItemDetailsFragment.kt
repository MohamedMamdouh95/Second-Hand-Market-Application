package com.example.lab2

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.itemdetailsfragment.*

class ItemDetailsFragment : Fragment() {

    private val itemVm: ItemViewModel by activityViewModels()
    private val userVm: UserViewModel by activityViewModels()
    private var menu: Menu? = null
    private var itemOnWishList = false;

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
        itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            if (item.vendorId != Firebase.auth.currentUser!!.uid) {
                menu.findItem(R.id.nav_item_edit)?.setVisible(false)
                menu.findItem(R.id.wishlist_option)?.setVisible(true)
            } else {
                menu.findItem(R.id.nav_item_edit)?.setVisible(true)
                menu.findItem(R.id.wishlist_option)?.setVisible(false)
            }
        })
        userVm.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->
            val wishlistIds = wishlist.map { item -> item.documentId }
            itemVm.detailItem.observe( viewLifecycleOwner, Observer { item ->
                item.documentId?.let{
                    var icon = R.drawable.favorite_border_white_24dp
                    if(wishlistIds.contains(it)) {
                        icon = R.drawable.favorite_white_24dp
                    }
                    menu?.findItem(R.id.wishlist_option)?.icon = ResourcesCompat.getDrawable(resources, icon, resources.newTheme())
                }
            })
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_item_edit -> findNavController().navigate(R.id.action_itemDetailsFragment_to_itemEditFragment2)
            R.id.wishlist_option -> itemVm.detailItem.value?.documentId?.let {
                if(itemOnWishList) {
                    item.icon = ResourcesCompat.getDrawable(resources,R.drawable.favorite_border_white_24dp, resources.newTheme())
                    itemOnWishList = false
                    userVm.removeItemFromWishlist(it)
                } else {
                    item.icon = ResourcesCompat.getDrawable(resources,R.drawable.favorite_white_24dp, resources.newTheme())
                    itemOnWishList = true
                    userVm.addItemToWishlist(it)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_nav_item_details_to_nav_item_list)
        }

        val wishFAB: FloatingActionButton? =
            view.rootView?.findViewById(R.id.wishlistFab)

        userVm.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->
            val wishlistIds = wishlist.map { item -> item.documentId }
            itemVm.detailItem.observe( viewLifecycleOwner, Observer { item ->
                item.documentId?.let{
                    var icon = R.drawable.favorite_border_white_24dp
                    itemOnWishList = false
                    if(wishlistIds.contains(it)) {
                        icon = R.drawable.favorite_white_24dp
                        itemOnWishList = true
                    }
                    wishFAB?.setImageResource(icon)
                }
            })
        })

        wishFAB?.setOnClickListener {
            itemVm.detailItem.value?.documentId?.let {
            if(itemOnWishList) {
                wishFAB?.setImageResource(R.drawable.favorite_border_white_24dp)
                itemOnWishList = false

                userVm.removeItemFromWishlist(it)
            } else {
                wishFAB?.setImageResource(R.drawable.favorite_white_24dp)
                itemOnWishList = true
                userVm.addItemToWishlist(it)
            }
            }
        }
        itemVm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            if (item.vendorId != Firebase.auth.currentUser!!.uid) {
                wishFAB?.visibility = View.VISIBLE
            } else {
                wishFAB?.visibility = View.INVISIBLE
            }
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
            val imageViewCollapsing: ImageView? =
                view.rootView?.findViewById(R.id.imageViewCollapsing)
            imageViewCollapsing?.setImageBitmap(bitmap)
        })





    }

}
