package com.tuan2101.chatme.activity

import android.content.Intent
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

        binding.sendMessage.setOnClickListener {
            val intent = Intent(this@InfoActivity, SingleChatLogActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
        }

        binding.coverImage.setOnClickListener {
            navigateToImageActivity(user.getCoverImage())
        }

        binding.avt.setOnClickListener {
            navigateToImageActivity(user.getAvatar())
        }

    }

    fun navigateToImageActivity(url: String) {
        val intent = Intent(this@InfoActivity, ImageActivity::class.java)
        intent.putExtra("image", url)
        startActivity(intent)
    }
}