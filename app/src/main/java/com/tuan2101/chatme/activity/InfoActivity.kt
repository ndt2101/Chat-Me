package com.tuan2101.chatme.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityInfoBinding
import com.tuan2101.chatme.viewModel.User

class InfoActivity : AppCompatActivity() {
    lateinit var binding:ActivityInfoBinding
    lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView (this, R.layout.activity_info)

        user = intent.getSerializableExtra("user") as User

        if (applicationContext != null) {
            binding.userName.text = user.getName().trim()
            Picasso.get().load(user.getAvatar()).into(binding.avt)
            Picasso.get().load(user.getCoverImage()).into(binding.coverImage)
            binding.introduce.text = user.getIntroduceYourself()
            binding.work.text = "Work at ${user.getWork()}"
            binding.homeTown.text = "From ${user.getHomeTown()}"
        }

    }
}