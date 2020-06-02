package com.example.lab2

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

import com.example.lab2.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.itemdetailsfragment.*

import kotlinx.android.synthetic.main.show_profile_fragment.*

class ShowProfileFragment: Fragment() {

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_nav_profile_to_nav_on_sale_items2)
        }

        v_model.detailUser.observe(viewLifecycleOwner, Observer { usr ->
            if(usr.documentId != FirebaseAuth.getInstance().currentUser!!.uid ) {
                showProfileFullName.visibility = View.INVISIBLE
                setHasOptionsMenu(false)
            }
            showProfileFullName.text = usr.fullname
            showProfileNickname.text = usr.nickname
            showProfileEmail.text = usr.email
            showProfileLocation.text = usr.location
            v_model.setImageStoragePath(usr.image)
        })

        v_model.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
           profileImage.setImageBitmap(bitmap)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_menu_edit,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                    || super.onOptionsItemSelected(item)
    }


}