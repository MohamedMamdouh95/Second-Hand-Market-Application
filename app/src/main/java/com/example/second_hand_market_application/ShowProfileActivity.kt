package com.example.second_hand_market_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var imageButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        imageButton = findViewById(R.id.imageButton2)
        registerForContextMenu(imageButton)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }
    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        Log.d("test","you clicked the Image button")
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.change_picture, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("test","you clicked the button")
        return true
    }
}
