package com.tuan2101.chatme.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.tuan2101.chatme.activity.view.DrawingView


class DrawingActivity: AppCompatActivity() {
    var mDrawingView: DrawingView? = null
    lateinit var mFirebaseReference: DatabaseReference
    lateinit var textView: TextView
    lateinit var clear: TextView
    var height: Int = 0
    var width: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId")

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