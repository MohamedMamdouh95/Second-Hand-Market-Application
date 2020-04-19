package com.example.second_hand_market_application

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_show_profile.*

data class ApplicationData(
    val fullName: String,
    val nickname: String,
    val email: String,
    val location: String,
    val imagePath: String
)

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var imageUri: String

    companion object {
        const val EDIT_PROFILE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        val data = loadData()
        fullName.text = data.fullName
        nickname.text = data.nickname
        email.text = data.email
        location.text = data.location
        imageUri = data.imagePath
        profileImage.setImageURI(Uri.parse(imageUri))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        fullName.text = savedInstanceState.get("fullName").toString()
        nickname.text = savedInstanceState.get("nickName").toString()
        location.text = savedInstanceState.get("location").toString()
        email.text = savedInstanceState.get("email").toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("fullName", fullName.text.toString())
        outState.putString("nickName", nickname.text.toString())
        outState.putString("email", email.text.toString())
        outState.putString("location", location.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pencil -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val detailIntent = Intent(this, EditProfileActivity::class.java)
        detailIntent.putExtra("fullName", fullName.text.toString())
        detailIntent.putExtra("nickName", nickname.text.toString())
        detailIntent.putExtra("location", location.text.toString())
        detailIntent.putExtra("email", email.text.toString())
        /*val imgURI: String = if (profileImage.tag == "default") {
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + resources.getResourcePackageName(R.drawable.profile_pic) +
                        '/' + resources.getResourceTypeName(R.drawable.profile_pic) +
                        '/' + resources.getResourceEntryName(R.drawable.profile_pic)
            ).toString()
        } else {
            profileImage.tag.toString()
        }*/
        detailIntent.putExtra("imageURI", imageUri)

        startActivityForResult(detailIntent, EDIT_PROFILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                fullName.text = data.extras?.get("fullName").toString()
                nickname.text = data.extras?.get("nickName").toString()
                location.text = data.extras?.get("location").toString()
                email.text = data.extras?.get("email").toString()
                imageUri = data.extras?.get("imageURI").toString()
                profileImage.setImageURI(Uri.parse(imageUri))
                //Changing the tag is a work around to figure out if the user changed the picture before he didn't and it's still default one
               // profileImage.tag = Uri.parse(data.extras?.get("imageURI").toString())
                saveData()
            }
        }
    }

    private fun saveData() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val data = ApplicationData(
            fullName.text.toString(), nickname.text.toString(),
            email.text.toString(), location.text.toString(), imageUri
        )
        with(sharedPref.edit()) {
            putString("profile", Gson().toJson(data))
            this.apply()
        }
    }

    private fun loadData(): ApplicationData {
        val sharedPref: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val data =
            Gson().fromJson(sharedPref.getString("profile", null), ApplicationData::class.java)
        return (data ?: initialize())
    }

    private fun initialize(): ApplicationData {
        // This is the path to the avatar drawable picture
        val defPath: String = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + resources.getResourcePackageName(R.drawable.profile_pic) +
                    '/' + resources.getResourceTypeName(R.drawable.profile_pic) +
                    '/' + resources.getResourceEntryName(R.drawable.profile_pic)
        ).toString()

        return ApplicationData(
            "Full Name", "Nickname",
            "E-Mail", "Location", defPath
        )
    }
}

