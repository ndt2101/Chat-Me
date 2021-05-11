package com.tuan2101.chatme.activity

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tuan2101.chatme.R
import com.tuan2101.chatme.activity.view.DrawingView
import com.tuan2101.chatme.viewModel.Group


class DrawingActivity: AppCompatActivity() {
    var mDrawingView: DrawingView? = null
    lateinit var mFirebaseReference: DatabaseReference
    lateinit var textView: TextView
    lateinit var clear: TextView
    var height: Int = 2160
    var width: Int = 1080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId")

        if (groupId != null) {
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val realTimeGroup = snapshot.getValue(Group::class.java)!!
                            var check = false

                            realTimeGroup.getMembers().forEach {
                                if (it.getUid().equals(FirebaseAuth.getInstance().uid)) {
                                    check = true
                                }
                            }

                            if (check == false) {
                                Toast.makeText(applicationContext, "This group is not available to you", Toast.LENGTH_LONG).show()

                                val intent = Intent(this@DrawingActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
                )
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        mFirebaseReference = FirebaseDatabase.getInstance().reference.child("Group_WhiteBoard").child(
            groupId!!
        )



        mFirebaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mDrawingView != null) {
                    (mDrawingView!!.parent as ViewGroup).removeView(mDrawingView)
                    mDrawingView!!.cleanUp()
                    mDrawingView = null
                }

                mDrawingView = DrawingView(
                    this@DrawingActivity,
                    mFirebaseReference.child("segments"),
                    width,
                    height
                )

                setContentView(mDrawingView)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menu?.add(0, Menu.FIRST, 0, "Clear")?.setShortcut('5', 'x')
            ?.setIcon(R.drawable.ic_baseline_clear_24)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mDrawingView?.cleanUp()
        mFirebaseReference.removeValue()
            .addOnCompleteListener{
                mDrawingView =  DrawingView(
                    this@DrawingActivity,
                    mFirebaseReference.child("segments"),
                    width,
                    height
                )
                setContentView(mDrawingView)
            }
        return true
    }

}