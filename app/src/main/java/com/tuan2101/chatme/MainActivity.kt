package com.tuan2101.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.adapter.TabSwitcher
import com.tuan2101.chatme.databinding.ActivityMainBinding
import com.tuan2101.chatme.viewModel.User

class MainActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    var currentUser: FirebaseUser? = null
    lateinit var binding: ActivityMainBinding
    lateinit var referenceUser: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this ,R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        referenceUser = FirebaseDatabase.getInstance().reference

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.title = ""

        binding.mainViewPager.adapter = TabSwitcher(supportFragmentManager)
        binding.tabSwitcher.setupWithViewPager(binding.mainViewPager)

//        referenceUser!!.child("User").child(currentUser!!.uid).addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val user: User? = snapshot.getValue(User::class.java)
//
//                    binding.userName.text = user?.getName()
//
////                    Picasso.get().load(user?.getAvatar()).into(binding.imageProfile)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            sendToLoginActivity()
        }
    }

    private fun sendToLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        if (item.itemId == R.id.option_setting) {

        }
        else if (item.itemId == R.id.option_logout) {
            firebaseAuth.signOut()
            sendToLoginActivity()
        }
        return true
    }


}