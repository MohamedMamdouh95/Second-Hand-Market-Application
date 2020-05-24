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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.itemeditfragment.*
import java.util.*
import com.example.lab2.model.Item
import com.example.lab2.viewmodel.ItemViewModel
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth


class ItemEditFragment : Fragment() {
    private val TAG = "ITEM_EDIT_FRAGMENT"
    private val vm: ItemViewModel by activityViewModels()

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
        private const val PERMISSION_REQUEST_STORAGE = 3
        private const val CAMERA_OPTION = "Take a Photo"
        private const val GALLERY_OPTION = "Choose from Gallery"
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
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_nav_item_edit_to_nav_item_details)
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

        vm.detailItem.observe(viewLifecycleOwner, Observer { item ->
            itemEditTitle.editText?.setText(item.title)
            itemEditPrice.editText?.setText(item.price)
            itemEditDescription.editText?.setText(item.description)
            itemEditCategory.editText?.setText(item.category)
            itemEditSubCategory.editText?.setText(item.subcategory)
            itemEditExpiryDate.editText?.setText(item.expiryDate)
            itemEditLocation.editText?.setText(item.location)
            vm.setImageStoragePath(item.image)
        })

        vm.bitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            itemEditImageLandscape?.setImageBitmap(bitmap)
            val imageViewCollapsing: ImageView? =
                view.rootView?.findViewById(R.id.imageViewCollapsing)
            imageViewCollapsing?.setImageBitmap(bitmap)
        })

        val categories = resources.getStringArray(R.array.Categories)
        val categoriesAdapter =
            ArrayAdapter(requireContext(), R.layout.category_list_item, categories)

        val keyboardHiderListener = View.OnFocusChangeListener { innerView: View?, hasFocus ->
            if (hasFocus) {
                val inputMethodService =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodService.hideSoftInputFromWindow(innerView?.windowToken, 0)
            }
        }

        (itemEditCategory.editText as? AutoCompleteTextView)?.onFocusChangeListener =
            keyboardHiderListener
        (itemEditSubCategory.editText as? AutoCompleteTextView)?.onFocusChangeListener =
            keyboardHiderListener

        (itemEditCategory.editText as? AutoCompleteTextView)?.setAdapter(categoriesAdapter)
        itemEditCategory.editText?.addTextChangedListener {
            (itemEditSubCategory.editText as? AutoCompleteTextView)?.setText("")
            val subCategories = when (it.toString()) {
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
        if (item.itemId == R.id.nav_item_details) {
            val firebaseWriteResultLiveData = vm.detailItem.value?.documentId?.let {
                vm.updateItem(createItemData(), vm.newImageBitmap.value)
            } ?: vm.createItem(createItemData(), vm.newImageBitmap.value)

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
            vendorId = Firebase.auth.currentUser?.uid
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