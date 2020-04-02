package com.example.second_hand_market_application

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.*
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

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
        //Set hints
        fullName.hint = intent.extras?.get("fullName").toString()
        nickname.hint = intent.extras?.get("nickName").toString()
        location.hint = intent.extras?.get("location").toString()
        email.hint = intent.extras?.get("email").toString()
        //Set the image as the current image of the user
        profileImage.setImageURI(Uri.parse(intent.extras?.get("imageURI").toString()))
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.change_picture, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
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
        //TODO FIX THIS BUG
        //THIS URL IS ALWAYS NULL
        intent.putExtra("imageURI", "profilePictureUri.toString()")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
