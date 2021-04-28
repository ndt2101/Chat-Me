package com.tuan2101.chatme.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityOutgoingInvitationBinding
import com.tuan2101.chatme.viewModel.User

class OutgoingInvitationActivity : AppCompatActivity() {

    lateinit var binding: ActivityOutgoingInvitationBinding
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView( this, R.layout.activity_outgoing_invitation)

        user = intent.getSerializableExtra("user") as User

        val type = intent.getStringExtra("type")

        if (type.equals("video")) {
            binding.callingType.setImageResource(R.drawable.ic_video_call)
        }
        else {
            binding.callingType.setImageResource(R.drawable.ic_voice_call)
        }

        if (user != null){
            Picasso.get().load(user.getAvatar()).into(binding.userAvt)
            binding.userName.text = user.getName()
        }

        binding.decline.setOnClickListener {
            onBackPressed()
        }
    }
}