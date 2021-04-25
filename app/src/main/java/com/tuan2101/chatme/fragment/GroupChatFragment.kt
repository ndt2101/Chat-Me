package com.tuan2101.chatme.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.activity.ChatMessenger
import com.tuan2101.chatme.activity.CreateGroupChatActivity
import com.tuan2101.chatme.activity.GroupChatLogActivity
import com.tuan2101.chatme.activity.SingleChatLogActivity
import com.tuan2101.chatme.databinding.FragmentGroupChatBinding
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_view_holder.view.*

class GroupChatFragment : Fragment() {

    lateinit var binding: FragmentGroupChatBinding
    val adapter = GroupAdapter<ViewHolder>()
    val latestMessengerMap = HashMap<String, ChatMessenger>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        binding.createGroupButton.setOnClickListener {
            val intent = Intent(activity, CreateGroupChatActivity::class.java)
            startActivity(intent)
        }

        listenForLatestMessenger()

        println("==================================================================")
        println("chay ra ngoai")
        println("==================================================================")

        adapter.setOnItemClickListener { item, view ->

            var group: Group

            var latestMessenger: GroupLatestMessenger = item as GroupLatestMessenger

            var chatMessenger = latestMessenger.chatMessenger

            var groupId = chatMessenger.toId

            FirebaseDatabase.getInstance().getReference("Groups").child(groupId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            group = snapshot.getValue(Group::class.java)!!
                            val intent = Intent(activity, GroupChatLogActivity::class.java)
                            intent.putExtra("group", group)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        binding.listGroupChat.layoutManager = LinearLayoutManager(context)

        binding.listGroupChat.adapter = adapter

        return binding.root
    }

    fun refreshLatestChatMessenger() {
        adapter.clear()
        val list = latestMessengerMap.values.toList()
        for (element in list) {
            adapter.add(GroupLatestMessenger(element))
            println("""""""""""""""""""")
            println(element.text)
            println(list.size)
        }
    }

    private fun listenForLatestMessenger() {
        val fromId = FirebaseAuth.getInstance().uid

        val reference = FirebaseDatabase.getInstance().getReference("/User/$fromId/groups")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val group = snapshot.getValue(Group::class.java) ?: return
                val latestMessenger = group.getLatestMessenger()
                latestMessengerMap[snapshot.key!!] = latestMessenger

//                println("""""""""""""""""""")
//                println(latestMessenger.text)

                refreshLatestChatMessenger()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val group = snapshot.getValue(Group::class.java) ?: return
                val latestMessenger = group.getLatestMessenger()
                latestMessengerMap[snapshot.key!!] = latestMessenger
//                println("""""""""""""""""""")
//                println(latestMessenger.text)
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


class GroupLatestMessenger(val chatMessenger: ChatMessenger,) : Item<ViewHolder>() {

    lateinit var group: Group

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var fromName: String

        FirebaseDatabase.getInstance().getReference("Groups").child(chatMessenger.toId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        group = snapshot.getValue(Group::class.java)!!
                        viewHolder.itemView.user_name.text = group.getName()
                        Picasso.get().load(group.getAvt()).into(viewHolder.itemView.avt)

                        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                            fromName = "You"
                        }
                        else {
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