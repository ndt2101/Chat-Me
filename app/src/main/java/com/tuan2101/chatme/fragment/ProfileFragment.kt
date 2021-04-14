package com.tuan2101.chatme.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.FragmentProfileBinding
import com.tuan2101.chatme.viewModel.User


class ProfileFragment : Fragment() {

    lateinit var userReference: DatabaseReference
    lateinit var firebaseUser: FirebaseUser
    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUser.uid)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)

                    if (context != null) {
                        binding.userName.text = user!!.getName().trim()
                        Picasso.get().load(user.getAvatar()).into(binding.avt)
                        Picasso.get().load(user.getCoverImage()).into(binding.coverImage)
                        binding.introduce.text = user!!.getIntroduceYourself()
                        binding.work.text = "Work at ${user!!.getWork()}"
                        binding.homeTown.text = "From ${user!!.getHomeTown()}"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        return binding.root
    }
}