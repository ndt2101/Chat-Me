package com.tuan2101.chatme.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tuan2101.chatme.activity.CreateGroupChatActivity
import com.tuan2101.chatme.databinding.FragmentGroupChatBinding

class GroupChatFragment : Fragment() {

    lateinit var binding: FragmentGroupChatBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        binding.createGroupButton.setOnClickListener {
            val intent = Intent(activity, CreateGroupChatActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

}