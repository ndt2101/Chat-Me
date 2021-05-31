package com.tuan2101.chatme.fragment

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.activity.SingleChatLogActivity


import com.tuan2101.chatme.R

import com.tuan2101.chatme.databinding.FragmentChatBinding
import com.tuan2101.chatme.viewModel.ChatMessenger
import com.tuan2101.chatme.viewModel.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_view_holder.view.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

val map = HashMap<String, Any>()

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    val adapter = GroupAdapter<ViewHolder>()
    var latestMessengerMap = LinkedHashMap<String, ChatMessenger>()
    var resColor: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        listenForLatestMessenger()

        resColor = requireContext().resources.getColor(R.color.black)


        /**
         * update token
         */
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { p0 ->
            if (p0.isSuccessful && p0.result != null) {
                map["token"] = p0.result!!.token
                FirebaseAuth.getInstance().uid?.let {
                    FirebaseDatabase.getInstance().reference.child("User").child(
                        it
                    ).updateChildren(map)
                }

            }
        }

        adapter.setOnItemClickListener { item, view ->

            var user: User

            view.last_messenger.setTextColor(resources.getColor(R.color.seen_color))

            (item as LatestMessenger).chatMessenger.status = "seen"

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


    /**
     * merge sort
     */
    fun mergeSort(list: List<ChatMessenger>): List<ChatMessenger> {
        if (list.size <= 1) {
            return list
        }

        val middle = list.size / 2
        var left = list.subList(0,middle);
        var right = list.subList(middle,list.size);

        return merge(mergeSort(left), mergeSort(right))
    }


    fun merge(left: List<ChatMessenger>, right: List<ChatMessenger>): List<ChatMessenger>  {
        var indexLeft = 0
        var indexRight = 0
        var newList : MutableList<ChatMessenger> = mutableListOf()

        while (indexLeft < left.count() && indexRight < right.count()) {
            if (left[indexLeft].timeStamp >= right[indexRight].timeStamp) {
                newList.add(left[indexLeft])
                indexLeft++
            } else {
                newList.add(right[indexRight])
                indexRight++
            }
        }

        while (indexLeft < left.size) {
            newList.add(left[indexLeft])
            indexLeft++
        }

        while (indexRight < right.size) {
            newList.add(right[indexRight])
            indexRight++
        }

        return newList;
    }




    fun refreshLatestChatMessenger() {
        adapter.clear()
        var list = mergeSort(latestMessengerMap.values.toList())
        for (element in list) {
            adapter.add(LatestMessenger(element, resColor))
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

class LatestMessenger(val chatMessenger: ChatMessenger, val color: Int) : Item<ViewHolder>() {

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

        FirebaseDatabase.getInstance().getReference("User").child(chatToUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                        viewHolder.itemView.user_name.text = user.getName()
                        Picasso.get().load(user.getAvatar()).into(viewHolder.itemView.avt)

                        if (user.getStatus().equals("online")) {
                            viewHolder.itemView.online.visibility = View.VISIBLE
                            viewHolder.itemView.offline.visibility = View.GONE
                        }else {
                            viewHolder.itemView.offline.visibility = View.VISIBLE
                            viewHolder.itemView.online.visibility = View.GONE
                        }

                        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                            fromName = "You"
                        }
                        else {
                            chatToUserId = chatMessenger.fromId
                            fromName = chatMessenger.fromName
                        }

                        if (chatMessenger.status.equals("")) {
                            viewHolder.itemView.last_messenger.setTextColor(color)
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