package com.example.lab2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.lab2.model.Profile
import com.example.lab2.viewmodel.MapViewModel
import com.example.lab2.viewmodel.NavigationSource
import com.example.lab2.viewmodel.UserViewModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.edit_profile_fragment.*

class EditProfileFragment : Fragment() {

    private val userVm: UserViewModel by activityViewModels()
    private val mapVm: MapViewModel by activityViewModels()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val TAG = EditProfileFragment::class.java.simpleName
    private var latitude: Double? = null
    private var longitude: Double? = null

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
        private const val PERMISSION_REQUEST_STORAGE = 3
        private const val AUTOCOMPLETE_REQUEST_CODE = 5
        private const val CAMERA_OPTION = "Take a Photo"
        private const val GALLERY_OPTION = "Choose from Gallery"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val v: View = inflater.inflate(R.layout.edit_profile_fragment, container, false)
        registerForContextMenu(v.findViewById(R.id.editProfileImageButton))
        userVm.setUserId(userId)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.nav_edit_profile) {
                navController.navigate(R.id.action_nav_edit_profile_to_nav_profile)
            }
        }
        if (userVm.profileUnderEdit == null) {
            userVm.detailUser.observe(viewLifecycleOwner, Observer {
                userVm.profileUnderEdit = it.copy()
                editProfileFullName.text = Editable.Factory.getInstance().newEditable(it.fullname)
                editProfileNickname.text = Editable.Factory.getInstance().newEditable(it.nickname)
                editProfileEmail.text = Editable.Factory.getInstance().newEditable(it.email)
                editProfileLocation.text = Editable.Factory.getInstance().newEditable(it.location)
                longitude = it.longitude
                latitude = it.latitude
                userVm.setImageStoragePath(it.image)

            })
        } else {
            userVm.detailUser.observe(viewLifecycleOwner, Observer {
                editProfileFullName.text =
                    Editable.Factory.getInstance().newEditable(userVm.profileUnderEdit!!.fullname)
                editProfileNickname.text =
                    Editable.Factory.getInstance().newEditable(userVm.profileUnderEdit!!.nickname)
                editProfileEmail.text =
                    Editable.Factory.getInstance().newEditable(userVm.profileUnderEdit!!.email)
                editProfileLocation.text =
                    Editable.Factory.getInstance().newEditable(userVm.profileUnderEdit!!.location)
                longitude = userVm.profileUnderEdit!!.longitude
                latitude = userVm.profileUnderEdit!!.latitude
                userVm.setImageStoragePath(it.image)
            })
        }


        userVm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            editProfileImage.setImageBitmap(bitmap)

        })

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())

        editProfileLocation.setOnClickListener {
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
        editProfileEmail.addTextChangedListener {
            userVm.profileUnderEdit?.email = it.toString()
        }
        editProfileFullName.addTextChangedListener {
            userVm.profileUnderEdit?.fullname = it.toString()
        }
        editProfileNickname.addTextChangedListener {
            userVm.profileUnderEdit?.nickname = it.toString()
        }

        profileMapButton.setOnClickListener {
            mapVm.navigationSource = NavigationSource.COMING_FROM_EDIT_USER
            findNavController().navigate(R.id.action_nav_edit_profile_to_mapsFragment)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_menu_save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_profile) {
            val firebaseWriteResultLiveData = userVm.detailUser.value?.documentId?.let {
                userVm.updateUser(fillProfile(), userVm.newImageBitmap.value)
            } ?: userVm.createUser(fillProfile(), userVm.newImageBitmap.value)

            firebaseWriteResultLiveData.observe(viewLifecycleOwner, Observer {
                Toast.makeText(requireContext(), it.statusString, Toast.LENGTH_SHORT).show()
                if (it.hasFailed) {
                    //findNavController().navigate(R.id.action_itemEditFragment_to_itemListFragment)
                } else if (it.documentId != null) {
                    userVm.setUserId(it.documentId)
                    val navController = findNavController()
                    if (navController.currentDestination?.id == R.id.nav_edit_profile) {
                        navController.navigate(R.id.action_nav_edit_profile_to_nav_profile)
                    }
                }
            })
        } else {
            super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun fillProfile(): Profile {
        return Profile(
            if (editProfileFullName.text.toString() == "") "Full Name" else editProfileFullName.text.toString(),
            if (editProfileNickname.text.toString() == "") "Nickname" else editProfileNickname.text.toString(),
            if (editProfileEmail.text.toString() == "") "Email" else editProfileEmail.text.toString(),
            if (editProfileLocation.text.toString() == "") "Location" else editProfileLocation.text.toString(),
            latitude,
            longitude,
            userVm.detailUser.value!!.image,
            userVm.detailUser.value!!.documentId,
            userVm.detailUser.value!!.wishlist
        )
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Change Profile Picture")
        menu.add(0, v.id, 0, CAMERA_OPTION)
        menu.add(0, v.id, 0, GALLERY_OPTION)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            CAMERA_OPTION -> if (askForPermissions(
                    Manifest.permission.CAMERA,
                    PERMISSION_REQUEST_CAMERA
                )
            ) takePhoto()
            GALLERY_OPTION -> if (askForPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    PERMISSION_REQUEST_STORAGE
                )
            ) selectImageInAlbum()
            else -> return false
        }
        return true
    }

    private fun askForPermissions(permission: String, requestCode: Int): Boolean {
        if (!isPermissionsAllowed(permission)) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionDeniedDialog()
            } else {
                requestPermissions(
                    arrayOf(permission),
                    requestCode
                )
            }
            return false
        }
        return true
    }

    private fun isPermissionsAllowed(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                }
                return
            }
            PERMISSION_REQUEST_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageInAlbum()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this.requireActivity())
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton(
                "App Settings"
            ) { _, i ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var bitmap: Bitmap? = null
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM) {
            val galleryUri = data?.data
            bitmap = galleryUri?.let {
                if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                } else {
                    val source =
                        ImageDecoder.createSource(requireActivity().contentResolver, galleryUri)
                    ImageDecoder.decodeBitmap(source)
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            bitmap = data?.extras?.get("data") as Bitmap
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(TAG, "Place: " + place.name + ", " + place.id)
                place.latLng?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status =
                    Autocomplete.getStatusFromIntent(data!!)
                Log.i(TAG, status.statusMessage.toString())
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        bitmap?.let {
            editProfileImage.setImageBitmap(bitmap)
            userVm.newImageBitmap.value = bitmap
        }
    }

}



