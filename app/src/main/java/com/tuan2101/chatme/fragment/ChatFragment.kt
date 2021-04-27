package com.tuan2101.chatme.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.activity.SingleChatLogActivity


import com.tuan2101.chatme.R

import com.tuan2101.chatme.databinding.FragmentChatBinding
import com.tuan2101.chatme.activity.ChatMessenger
import com.tuan2101.chatme.viewModel.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_view_holder.view.*


class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    val adapter = GroupAdapter<ViewHolder>()
    var latestMessengerMap = LinkedHashMap<String, ChatMessenger>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        listenForLatestMessenger()
//
//        println("==================================================================")
//        println("chay ra ngoai")
//        println("==================================================================")

        adapter.setOnItemClickListener { item, view ->

            var user: User

            var latestMessenger: LatestMessenger = item as LatestMessenger

            var chatMessenger = latestMessenger.chatMessenger

            var chatToUserId: String

            if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                chatToUserId = chatMessenger.toId
            }
            else {
                chatToUserId = chatMessenger.fromId
            }

            FirebaseDatabase.getInstance().getReference("User").child(chatToUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            user = snapshot.getValue(User::class.java)!!
                            val intent = Intent(activity, SingleChatLogActivity::class.java)
                            intent.putExtra("user", user)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        binding.chatList.layoutManager = LinearLayoutManager(context)
        binding.chatList.adapter = adapter
        return binding.root
    }

    fun refreshLatestChatMessenger() {
        adapter.clear()
        var list = latestMessengerMap.values.toList()
        for (i in list.size - 1 downTo 0) {
            adapter.add(LatestMessenger(list[i]))
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

//        println("==================================================================")
//        println("chay vao day")
//        println("==================================================================")
    }

}

class LatestMessenger(val chatMessenger: ChatMessenger,) : Item<ViewHolder>() {

    lateinit var user: User

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var chatToUserId: String
        var fromName: String

        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
            chatToUserId = chatMessenger.toId
        }
        else {
            chatToUserId = chatMessenger.fromId
        }

        val reference = FirebaseDatabase.getInstance().getReference("User").child(chatToUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                        viewHolder.itemView.user_name.text = user.getName()
                        Picasso.get().load(user.getAvatar()).into(viewHolder.itemView.avt)

                        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                            fromName = "You"
                        }
                        else {
                            chatToUserId = chatMessenger.fromId
                            fromName = chatMessenger.fromName
                        }
                        viewHolder.itemView.last_messenger.text = "$fromName: ${chatMessenger.text}"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }

    override fun getLayout(): Int {
        return R.layout.chat_view_holder
    }

}