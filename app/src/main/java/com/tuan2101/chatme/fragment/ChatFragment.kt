package com.tuan2101.chatme.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil


import com.tuan2101.chatme.R

import com.tuan2101.chatme.databinding.FragmentChatBinding


class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)


        return binding.root

    }

}