package com.example.lab2

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels

import androidx.navigation.findNavController

import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer

import androidx.navigation.ui.*
import com.example.lab2.viewmodel.UserViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import com.example.lab2.model.repository.ImageRepository

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val user_id = FirebaseAuth.getInstance().currentUser!!.uid
    private val v_model: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment)

        //populate navigation header observing user livedata
        v_model.getUser(user_id)?.observe(this, Observer { usr ->
            navHeaderName.text = usr.fullname + " ( " + usr.nickname + " )"
            navHeaderEmail.text = usr.email
            v_model.setImageStoragePath(usr.image)
            v_model.bitmap.observe(this, Observer { bitmap ->
                navHeaderimageView.setImageBitmap(bitmap)
            })
        })




        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_on_sale_items, R.id.nav_own_items, R.id.nav_profile, R.id.nav_wishlist
            ), drawer_layout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        nav_view.setNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.nav_profile) {
                v_model.setUserId(FirebaseAuth.getInstance().currentUser!!.uid)
            }

            navController.navigate(item.itemId)
            true
        }

        app_bar_layout?.let {
            it.setupWithNavController(
                toolbar,
                navController,
                appBarConfiguration
            )
        } ?: toolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val inputMethodService =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodService?.hideSoftInputFromWindow(nav_view.windowToken, 0);
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                editImageButton.visibility = View.INVISIBLE
                app_bar_layout?.title = destination.label
                wishlistFab.visibility = View.INVISIBLE

                val params = appbar.layoutParams as CoordinatorLayout.LayoutParams
                if (params.behavior == null)
                    params.behavior = AppBarLayout.Behavior()
                val behaviour = params.behavior as AppBarLayout.Behavior


                //wishlistFab is set visible only in item details fragment if item is not user's
                wishlistFab.visibility = View.INVISIBLE

                if (destination.id == R.id.nav_own_items ||
                    destination.id == R.id.nav_edit_profile ||
                    destination.id == R.id.nav_profile ||
                    destination.id == R.id.nav_wishlist ||
                    destination.id == R.id.nav_on_sale_items
                ) {
                    appbar.setExpanded(false, true)
                    drawer_layout.closeDrawer(GravityCompat.START,true)
                    behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                        override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                            return false
                        }
                    })
                } else {
                    toolbar.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.toolbar_gradient,
                        resources.newTheme()
                    )
                    appbar.setExpanded(true, true)
                    val heightDp = resources.displayMetrics.heightPixels / 2;

                    params.height = heightDp;

                    if(destination.id == R.id.nav_item_edit ||
                        destination.id == R.id.nav_edit_profile) {
                        editImageButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else if (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack()
        else
            super.onBackPressed()
    }

}
