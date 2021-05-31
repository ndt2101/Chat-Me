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
import com.tuan2101.chatme.activity.*
import com.tuan2101.chatme.databinding.FragmentGroupChatBinding
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.GroupChatMessenger
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_view_holder.view.*

class GroupChatFragment : Fragment() {

    lateinit var binding: FragmentGroupChatBinding
    val adapter = GroupAdapter<ViewHolder>()
    val latestMessengerMap = LinkedHashMap<String, GroupChatMessenger>()

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


    /**
     * merge sort
     */



    fun mergeSort(list: List<GroupChatMessenger>): List<GroupChatMessenger> {
        if (list.size <= 1) {
            return list
        }

        val middle = list.size / 2
        var left = list.subList(0,middle);
        var right = list.subList(middle,list.size);

        return merge(mergeSort(left), mergeSort(right))
    }


    fun merge(left: List<GroupChatMessenger>, right: List<GroupChatMessenger>): List<GroupChatMessenger>  {
        var indexLeft = 0
        var indexRight = 0
        var newList : MutableList<GroupChatMessenger> = mutableListOf()

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
        var list = ArrayList<GroupChatMessenger>()

        list.addAll(mergeSort(latestMessengerMap.values.toList()))

        var i = 0
        var n = list.size

        while (i < n) {
            if (list[i].timeStamp.toInt() == 100) {
                list.remove(list[i])

                n--
            }
            else { i++ }
        }

        for (i in list.indices) {
            adapter.add(GroupLatestMessenger(list[i]))
        }
    }

    private fun listenForLatestMessenger() {
        val fromId = FirebaseAuth.getInstance().uid

        val reference = FirebaseDatabase.getInstance().getReference("/User_Groups/$fromId")

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
//                val group = snapshot.getValue(Group::class.java) ?: return
//                val latestMessenger = group.getLatestMessenger()
//                latestMessenger.timeStamp = 100
//                latestMessengerMap[snapshot.key!!] = latestMessenger
////                println("""""""""""""""""""")
////                println(latestMessenger.text)
//                refreshLatestChatMessenger()
//                latestMessengerMap.keys.remove(snapshot.key!!)
                activity?.recreate()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}


class GroupLatestMessenger(val chatMessenger: GroupChatMessenger) : Item<ViewHolder>() {

    lateinit var group: Group

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var fromName: String


        FirebaseDatabase.getInstance().getReference("Groups").child(chatMessenger.toId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        group = snapshot.getValue(Group::class.java)!!
                        viewHolder.itemView.user_name.text = group.getName()
                        Picasso.get().load(group.getAvt()).into(viewHolder.itemView.avt)

                        if (group.getLatestMessenger().fromId.equals(FirebaseAuth.getInstance().uid)){
                            viewHolder.itemView.last_messenger.text =
                                "You: ${group.getLatestMessenger().text}"
                        }
                        else {
                            viewHolder.itemView.last_messenger.text =
                            "${group.getLatestMessenger().fromName}: ${group.getLatestMessenger().text}"
                        }

//                        viewHolder.itemView.last_messenger.text =
//                            "${group.getLatestMessenger().fromName}: ${group.getLatestMessenger().text}"
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