package com.tuan2101.chatme.activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.adapter.TabSwitcher
import com.tuan2101.chatme.databinding.ActivityMainBinding
import com.tuan2101.chatme.viewModel.User

class MainActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    lateinit var binding: ActivityMainBinding
    lateinit var referenceUser: DatabaseReference
    var REQUEST_CODE_BATTERY_OPTIMIXATION = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this , R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        referenceUser = FirebaseDatabase.getInstance().reference

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.title = ""

        binding.mainViewPager.adapter = TabSwitcher(supportFragmentManager)
        binding.tabSwitcher.setupWithViewPager(binding.mainViewPager)

        binding.tabSwitcher.getTabAt(0)?.setIcon(R.drawable.ic_baseline_chat_16)
        binding.tabSwitcher.getTabAt(1)?.setIcon(R.drawable.group_chat)
        binding.tabSwitcher.getTabAt(2)?.setIcon(R.drawable.ic_baseline_home_24)

        binding.tabSwitcher.setSelectedTabIndicatorColor(resources.getColor(R.color.catalyst_redbox_background))

        if (currentUser?.uid != null) {

            referenceUser!!.child("User").child(currentUser!!.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user: User? = snapshot.getValue(User::class.java)

                        binding.userName.text = user!!.getName()
                        Picasso.get()
                            .load(user.getAvatar())
                            .fit()
                            .into(binding.imageProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            val updateRef = referenceUser!!.child("User").child(currentUser!!.uid)

            val map: HashMap<String, Any> = HashMap()
            map["status"] = "online"
            updateRef.updateChildren(map)

        }

        binding.search.setOnClickListener {
            sendToSearchActivity()
        }

        if (currentUser != null){ checkForBatteryOptimization() }
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            sendToLoginActivity()
        }
    }

    private fun sendToLoginActivity() {
        var map = HashMap<String, Any>()
        map["token"] = ""
        currentUser?.let { FirebaseDatabase.getInstance().reference.child("User").child(it.uid).updateChildren(map) }
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun sendToSearchActivity() {
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
//        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        if (item.itemId == R.id.option_setting) {
            navigateToAboutApp()
        }
        return true
    }

    fun checkForBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService((POWER_SERVICE)) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Warning")
                builder.setMessage("Battery optimization is enable. It can interrupt running background services.")
                builder.setPositiveButton("Disable") { dialog, which ->
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIXATION)
                }

                builder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIXATION) {
            checkForBatteryOptimization()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(currentUser!=null){
            val updateRef = referenceUser!!.child("User").child(currentUser!!.uid)

            val map: HashMap<String, Any> = HashMap()
            map["status"] = "offline"
            updateRef.updateChildren(map)
        }
    }
    fun navigateToAboutApp() {
        val intent = Intent(this@MainActivity, AboutAppActivity::class.java)
        startActivity(intent)
    }
}
