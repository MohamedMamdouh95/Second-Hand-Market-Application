package com.example.lab2

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.lab2.model.Item
import com.example.lab2.viewmodel.ItemViewModel
import com.example.lab2.viewmodel.MapViewModel
import com.example.lab2.viewmodel.NavigationSource
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.itemeditfragment.*
import java.util.*


class ItemEditFragment : Fragment() {
    private val TAG = ItemEditFragment::class.java.simpleName
    private val vm: ItemViewModel by activityViewModels()
    private val mapVm: MapViewModel by activityViewModels()

    private var latitude: Double? = null
    private var longitude: Double? = null

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
        private const val PERMISSION_REQUEST_STORAGE = 3
        private const val CAMERA_OPTION = "Take a Photo"
        private const val GALLERY_OPTION = "Choose from Gallery"
        private const val AUTOCOMPLETE_REQUEST_CODE = 5
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.itemeditfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_nav_item_edit_to_nav_on_sale_items)
        }

        val imgButton: ImageButton? = view.rootView.findViewById(R.id.editImageButton)
        //Using toolbar for image and editing when in portrait
        imgButton?.let {
            registerForContextMenu(it)
        }
        //Using different button for image and editing when in landscape
        itemEditImageButtonLandscape?.let {
            registerForContextMenu(it)
        }
        if (vm.getItemId().value != null && vm.itemUnderEdit == null) {
            //Item already exists in the database and we are editing it
            vm.detailItem.observe(viewLifecycleOwner, Observer { item ->
                vm.itemUnderEdit = item.copy()
                itemEditTitle.editText?.setText(item.title)
                itemEditPrice.editText?.setText(item.price)
                itemEditDescription.editText?.setText(item.description)
                (itemEditCategory.editText as? AutoCompleteTextView)?.setText(item.category, false)
                updateSubcategoryFieldDropdown(item.category)
                itemEditExpiryDate.editText?.setText(item.expiryDate)
                itemEditLocation.editText?.setText(item.location)
                longitude = item.longitude
                latitude = item.latitude
                vm.setImageStoragePath(item.image)
            })
        } else if (vm.itemUnderEdit == null) {
            //The user will enter here the first time he opens the itemEditFragment (When creating new item)
            //Item is newly created so we need to construct an item in the View Model
            val item = Item()
            item.vendorId = Firebase.auth.currentUser?.uid
            vm.itemUnderEdit = item
        } else {
            /*the user is updating an item or when the user return from the MapsFragment so we need to repopulate the field that
            * the user wrote before navigating to the MapsFragment*/
            vm.itemUnderEdit?.let { item ->
                itemEditTitle.editText?.setText(item.title)
                itemEditPrice.editText?.setText(item.price)
                itemEditDescription.editText?.setText(item.description)
                (itemEditCategory.editText as? AutoCompleteTextView)?.setText(item.category, false)
                updateSubcategoryFieldDropdown(item.category)
                itemEditExpiryDate.editText?.setText(item.expiryDate)
                itemEditLocation.editText?.setText(item.location)
                latitude = item.latitude
                longitude = item.longitude
                vm.setImageStoragePath(item.image)
            }
        }
        vm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            if (vm.newImageBitmap.value == null) {
                itemEditImageLandscape?.setImageBitmap(bitmap)
                val imageViewCollapsing: ImageView? =
                    view.rootView?.findViewById(R.id.imageViewCollapsing)
                imageViewCollapsing?.setImageBitmap(bitmap)
            }
        })

        vm.newImageBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            bitmap?.let {
                itemEditImageLandscape?.setImageBitmap(bitmap)
                val imageViewCollapsing: ImageView? =
                    view.rootView?.findViewById(R.id.imageViewCollapsing)
                imageViewCollapsing?.setImageBitmap(bitmap)
            }

        })
        itemEditTitle.editText?.addTextChangedListener {
            vm.itemUnderEdit?.title = it.toString()
        }
        itemEditSubCategory.editText?.addTextChangedListener {
            vm.itemUnderEdit?.subcategory = it.toString()
        }
        itemEditDescription.editText?.addTextChangedListener {
            vm.itemUnderEdit?.description = it.toString()
        }
        itemEditPrice.editText?.addTextChangedListener {
            vm.itemUnderEdit?.price = it.toString()
        }
        itemEditExpiryDate.editText?.addTextChangedListener {
            vm.itemUnderEdit?.expiryDate = it.toString()
        }


        val keyboardHiderListener = View.OnFocusChangeListener { innerView: View?, hasFocus ->
            if (hasFocus) {
                val inputMethodService =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodService.hideSoftInputFromWindow(innerView?.windowToken, 0)
            }
        }

        (itemEditSubCategory.editText as? AutoCompleteTextView)?.onFocusChangeListener =
            keyboardHiderListener
        (itemEditLocation.editText as? AutoCompleteTextView)?.onFocusChangeListener =
            keyboardHiderListener

        //Add listener for the map button
        mapButton.setOnClickListener {
            mapVm.navigationSource = NavigationSource.COMING_FROM_ITEM_EDIT
            findNavController().navigate(R.id.action_nav_item_edit_to_mapsFragment)
        }
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        )
            .build(requireContext())

        itemEditLocationTextInputEditText.setOnClickListener {
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }


        val categories = resources.getStringArray(R.array.Categories)
        val categoriesAdapter =
            ArrayAdapter(requireContext(), R.layout.category_list_item, categories)

        (itemEditCategory.editText as? AutoCompleteTextView)?.setAdapter(categoriesAdapter)
        itemEditCategory.editText?.addTextChangedListener {
            vm.itemUnderEdit?.category = it.toString()
            updateSubcategoryFieldDropdown(it.toString())
        }

        itemEditExpiryDate.editText?.inputType = InputType.TYPE_NULL
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        itemEditExpiryDate.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { v: View?, hasFocus ->
                if (hasFocus) {
                    val inputMethodService =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodService.hideSoftInputFromWindow(v?.windowToken, 0)
                    val dpd = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            itemEditExpiryDate.editText?.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)
                        },
                        year,
                        month,
                        day
                    )
                    dpd.show()
                }

            }
    }

    fun updateSubcategoryFieldDropdown(category: String) {
        val currentSubcategory = vm.itemUnderEdit?.subcategory
        val subCategories = when (category) {
            "Arts & Crafts" -> resources.getStringArray(R.array.C1)
            "Sports & Hobby" -> resources.getStringArray(R.array.C2)
            "Baby" -> resources.getStringArray(R.array.C3)
            "Women's fashion" -> resources.getStringArray(R.array.C4)
            "Men's fashion" -> resources.getStringArray(R.array.C5)
            "Electronics" -> resources.getStringArray(R.array.C6)
            "Games & Videogames" -> resources.getStringArray(R.array.C7)
            "Automotive" -> resources.getStringArray(R.array.C8)
            else -> resources.getStringArray(R.array.C1)
        }

        val subCategoriesAdapter =
            ArrayAdapter(requireContext(), R.layout.category_list_item, subCategories)

        (itemEditSubCategory.editText as? AutoCompleteTextView)?.setAdapter(subCategoriesAdapter)
        if(!subCategories.contains(currentSubcategory)) {
            (itemEditSubCategory.editText as? AutoCompleteTextView)?.setText("",false)
        } else {
            (itemEditSubCategory.editText as? AutoCompleteTextView)?.setText(currentSubcategory,false)
        }
    }
    override fun onPause() {
        super.onPause()
        val imgButton: ImageButton? = view?.rootView?.findViewById(R.id.editImageButton)
        imgButton?.let {
            unregisterForContextMenu(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_item_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (itemEditCategory.editText?.text.isNullOrEmpty()) {
            itemEditCategory.error = "Category is required"
        } else if (itemEditTitle.editText?.text.isNullOrEmpty()) {
            itemEditTitle.error = "Title is required"
        } else if (itemEditPrice.editText?.text.isNullOrEmpty()) {
            itemEditPrice.error = "Price is required"
        } else {
            if (item.itemId == R.id.nav_item_details) {
                val firebaseWriteResultLiveData = vm.detailItem.value?.documentId?.let {
                    vm.updateItem(createItemData(), vm.newImageBitmap.value)
                } ?: vm.itemUnderEdit?.let { vm.createItem(it, vm.newImageBitmap.value) }
                ?: vm.createItem(createItemData(), vm.newImageBitmap.value)

                firebaseWriteResultLiveData.observe(viewLifecycleOwner, Observer {
                    Toast.makeText(requireContext(), it.statusString, Toast.LENGTH_SHORT).show()
                    if (it.hasFailed) {
                        findNavController().navigate(R.id.action_itemEditFragment_to_itemListFragment)
                    } else if (it.documentId != null) {
                        vm.setItemId(it.documentId)
                        findNavController().navigate(R.id.action_itemEditFragment_to_itemDetailsFragment)
                    }
                })
            } else {
                super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    private fun createItemData(): Item {
        return Item(
            category = itemEditCategory.editText?.text.toString(),
            description = itemEditDescription.editText?.text.toString(),
            expiryDate = itemEditExpiryDate.editText?.text.toString(),
            location = itemEditLocation.editText?.text.toString(),
            price = itemEditPrice.editText?.text.toString(),
            subcategory = itemEditSubCategory.editText?.text.toString(),
            title = itemEditTitle.editText?.text.toString(),
            image = vm.detailItem.value?.image,
            documentId = vm.detailItem.value?.documentId,
            vendorId = Firebase.auth.currentUser?.uid,
            buyers = vm.detailItem.value!!.buyers,
            latitude = latitude,
            longitude = longitude
        )
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Change item picture")
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
            ) { _, _ ->
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
                    vm.itemUnderEdit?.latitude = it.latitude
                    vm.itemUnderEdit?.longitude = it.longitude
                    longitude = it.longitude
                }
                vm.itemUnderEdit?.location = place.name.toString()
                itemEditLocation.editText?.setText(place.name.toString())
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status =
                    Autocomplete.getStatusFromIntent(data!!)
                Log.i(TAG, status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        bitmap?.let {
            val collapsingImageView: ImageView? =
                view?.rootView?.findViewById(R.id.imageViewCollapsing)
            collapsingImageView?.setImageBitmap(it)
            itemEditImageLandscape?.setImageBitmap(it)
            vm.newImageBitmap.value = bitmap
        }
    }

    override fun onResume() {
        super.onResume()
        val imgButton: ImageButton? = view?.rootView?.findViewById(R.id.editImageButton)
        imgButton?.let {
            registerForContextMenu(it)
        }
        //Using different button for image and editing when in landscape
        itemEditImageButtonLandscape?.let {
            registerForContextMenu(it)
        }
    }


}