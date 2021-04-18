package com.tuan2101.chatme.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


import com.tuan2101.chatme.R

import com.tuan2101.chatme.databinding.FragmentChatBinding
import com.tuan2101.chatme.viewModel.ChatMessenger
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_view_holder.view.*


class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    val adapter = GroupAdapter<ViewHolder>()
    var latestMessengerMap = HashMap<String, ChatMessenger>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)


        listenForLatestMessenger()
        binding.chatList.layoutManager = LinearLayoutManager(context)
        binding.chatList.adapter = adapter
        return binding.root

    }

    fun refreshLatestChatMessenger() {
        adapter.clear()
        latestMessengerMap.values.forEach {
            adapter.add(LatestMessenger(it))
        }
    }

    private fun listenForLatestMessenger() {
        val fromId = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/latest-messenger/$fromId")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessenger = snapshot.getValue((ChatMessenger::class.java)) ?: return
                latestMessengerMap[snapshot.key!!] = chatMessenger
                refreshLatestChatMessenger()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessenger = snapshot.getValue((ChatMessenger::class.java)) ?: return
                latestMessengerMap[snapshot.key!!] = chatMessenger
                refreshLatestChatMessenger()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}

class LatestMessenger(val chatMessenger: ChatMessenger) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.last_messenger.text = chatMessenger.text
    }

    override fun getLayout(): Int {
        return R.layout.chat_view_holder
    }

}