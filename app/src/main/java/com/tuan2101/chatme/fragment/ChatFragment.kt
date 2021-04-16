package com.tuan2101.chatme.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tuan2101.chatme.R
import com.tuan2101.chatme.adapter.UserAdapter
import com.tuan2101.chatme.databinding.FragmentChatBinding
import com.tuan2101.chatme.viewModel.User

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    lateinit var users: List<User>
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        binding.chatList.setHasFixedSize(true)
        binding.chatList.layoutManager = LinearLayoutManager(context)

        users = ArrayList()

        retrieveAllUser()

        return binding.root

    }

    private fun retrieveAllUser() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser.uid

        val referenceUser = FirebaseDatabase.getInstance().reference.child("User")

        referenceUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (users as java.util.ArrayList<User>).clear()
                    for (snapshot in p0.children) {
                        val user: User? = snapshot.getValue(User::class.java)
                        if (!(user!!.getUid()).equals(firebaseUserID)) {
                            (users as java.util.ArrayList<User>).add(user)
                        }
                    }

                    userAdapter = UserAdapter(context!!, users, false) // khong chac context

                    binding.chatList.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}