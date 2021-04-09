package com.tuan2101.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseUser
import com.tuan2101.chatme.adapter.TabSwitcher

class MainActivity : AppCompatActivity() {

    var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.title = "Chat Me"

        findViewById<ViewPager>(R.id.main_view_pager).adapter = TabSwitcher(supportFragmentManager)
        findViewById<TabLayout>(R.id.tab_switcher).setupWithViewPager(findViewById(R.id.main_view_pager))
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            sendToLoginActivity()
        }
    }

    private fun sendToLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }


}