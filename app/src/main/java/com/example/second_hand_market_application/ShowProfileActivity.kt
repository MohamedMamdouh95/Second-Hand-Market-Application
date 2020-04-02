package com.example.second_hand_market_application

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import kotlinx.android.synthetic.main.activity_show_profile.*

class ShowProfileActivity : AppCompatActivity() {
    companion object {
        const val EDIT_PROFILE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
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
        val imgURI: String = if (profileImage.tag == "default") {
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(
                    R.drawable.profile_pic
                ) + '/' + resources.getResourceTypeName(R.drawable.profile_pic) + '/' + resources.getResourceEntryName(
                    R.drawable.profile_pic
                )
            ).toString()
        } else {
            profileImage.tag.toString()
        }
        detailIntent.putExtra("imageURI", imgURI)

        startActivityForResult(detailIntent, EDIT_PROFILE)
    }

}

