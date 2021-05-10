package com.tuan2101.chatme.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.tuan2101.chatme.activity.view.DrawingView


class DrawingActivity: AppCompatActivity() {
    var mDrawingView: DrawingView? = null
    lateinit var mFirebaseReference: DatabaseReference
    var mBoardWidth: Int = 3000
    var mBoardHeight: Int = 3000
    lateinit var textView: TextView
    lateinit var clear: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId")

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

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



}