package com.example.second_hand_market_application

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private var profilePictureUri: Uri? = null
    private var defaultUri: Uri? = null

    companion object {
        //image pick code
        private const val IMAGE_PICK_CODE = 1000;

        //Permission code
        private const val PERMISSION_CODE = 1001;
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2
        private const val REQUEST_CAMERA_PERMISSION = 3
        private const val REQUEST_GALLERY_PERMISSION = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        registerForContextMenu(profileImageButton)

        //Changed the text inside the text box with the previous values
        fullName.text =
            Editable.Factory.getInstance().newEditable(intent.extras?.get("fullName").toString())
        nickname.text =
            Editable.Factory.getInstance().newEditable(intent.extras?.get("nickName").toString())
        location.text =
            Editable.Factory.getInstance().newEditable(intent.extras?.get("location").toString())
        email.text =
            Editable.Factory.getInstance().newEditable(intent.extras?.get("email").toString())
        //Set the image as the current image of the user
        defaultUri = Uri.parse(intent.extras?.get("imageURI").toString())
        Log.d("TEST", defaultUri.toString())
        profileImage.setImageURI(defaultUri)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.change_picture, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Camera -> if (askForPermission(
                    Manifest.permission.CAMERA,
                    REQUEST_CAMERA_PERMISSION
                )
            ) takePicture()
            R.id.Gallery -> if (askForPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    REQUEST_GALLERY_PERMISSION
                )
            ) pickImageFromGallery()
            else -> return false
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_menu, menu)
        return true
    }


    private fun isPermissionAllowed(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this as Activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun askForPermission(permission: String, requestCode: Int): Boolean {
        if (!isPermissionAllowed(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity, permission)) {
                showAlertBox()
            } else {
                ActivityCompat.requestPermissions(
                    this as Activity,
                    arrayOf(permission),
                    requestCode
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                }
                return
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                }
                return
            }
        }
    }

    private fun showAlertBox() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings") { _, _ ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("Cancel", null)
            .show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveOption -> {
                saveData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveData() {
        intent = Intent(this, ShowProfileActivity::class.java)
        intent.putExtra("fullName", fullName.text.toString())
        intent.putExtra("nickName", nickname.text.toString())
        intent.putExtra("location", location.text.toString())
        intent.putExtra("email", email.text.toString())
        intent.putExtra("imageURI", profilePictureUri ?: defaultUri)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var imageBitmap: Bitmap? = null
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap

        } else if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK) {
            imageBitmap = data?.data?.let {
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, it))
                } else {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        imageBitmap?.let {
            profilePictureUri?.let { deleteInternalPictureWithUri(it) }
            profilePictureUri = saveImage(it)
            profileImage.tag = profilePictureUri
            profileImage.setImageBitmap(rotateBitmap(profilePictureUri!!, it))
        }
    }

    private fun saveImage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    //Rotation of the bitmap
    private fun rotateBitmap(uri: Uri, bitmap: Bitmap): Bitmap? {
        val ei = ExifInterface(uri.path)

        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true
        )
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
    }

    private fun deleteInternalPictureWithUri(uri: Uri): Boolean {
        val wrapper = ContextWrapper(this)
        val file = wrapper.getDir("images", Context.MODE_PRIVATE)
        return File(file, uri.toString().split("/").last()).delete()
    }


}
