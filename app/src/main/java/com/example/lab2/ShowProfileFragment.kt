package com.example.lab2

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.example.lab2.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.show_profile_fragment.*

class ShowProfileFragment: Fragment() {

    private val TAG = "SHOW_PROFILE_FRAGMENT"
    private val v_model: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.show_profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("ratingId")
        v_model.detailUser.observe(viewLifecycleOwner, Observer { usr ->
            showProfileFullName.text = usr.fullname
            showProfileNickname.text = usr.nickname
            showProfileEmail.text = usr.email
            showProfileLocation.text = usr.location
            if(findNavController().currentDestination?.id != R.id.nav_rate_user)
            profileRatingBar.rating = usr.rating
            v_model.setImageStoragePath(usr.image)
        })

        v_model.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
           profileImageLandscape?.setImageBitmap(bitmap)
            val imageViewCollapsing: ImageView? =
                view.rootView?.findViewById(R.id.imageViewCollapsing)
            imageViewCollapsing?.setImageBitmap(bitmap)
        })

        when(findNavController().currentDestination?.id) {
            R.id.nav_own_profile -> {
                showProfileFullName.visibility = View.VISIBLE
                setHasOptionsMenu(true)
                profileRatingBar.setIsIndicator(true)
            }
            R.id.nav_buyer_profile -> {
                showProfileFullName.visibility = View.GONE
                setHasOptionsMenu(false)
                profileRatingBar.setIsIndicator(true)
            }
            R.id.nav_rate_user -> {
                showProfileFullName.visibility = View.GONE
                setHasOptionsMenu(false)
                profileRatingBar.setIsIndicator(false)
                profileRatingBar.setOnRatingChangeListener { _, rating ->
                    v_model.updateNumberOfRewiews(userId!!)
                    v_model.updateRating(userId!!,rating)
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_menu_edit,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        v_model.newImageBitmap.value = null
        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
        return true
    }


}