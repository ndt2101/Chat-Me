package com.tuan2101.chatme.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityIncomingInvitationBinding
import com.tuan2101.chatme.viewModel.Constants

class IncomingInvitationActivity : AppCompatActivity() {
    lateinit var binding: ActivityIncomingInvitationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_incoming_invitation)

        val type = intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE)

        val userAvt = intent.getStringExtra("userAvt")

        val userName = intent.getStringExtra("userName")

        binding.userName.text = userName

        Picasso.get().load(userAvt).into(binding.userAvt)

        if (type!!.equals("video")) {
            binding.callingType.setImageResource(R.drawable.ic_video_call)
        }else {
            binding.callingType.setImageResource(R.drawable.ic_voice_call)
        }
    }
}