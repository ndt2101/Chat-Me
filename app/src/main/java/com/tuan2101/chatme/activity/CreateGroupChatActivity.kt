package com.tuan2101.chatme.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.adapter.SearchUserToAddAdapter
import com.tuan2101.chatme.databinding.ActivityCreateGroupChatBinding
import com.tuan2101.chatme.network.ApiClient
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User
import kotlinx.android.synthetic.main.image_chat_from_row.view.*
import kotlin.collections.ArrayList

class CreateGroupChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreateGroupChatBinding
    lateinit var adapter: SearchUserToAddAdapter
    lateinit var users: List<User>
    lateinit var groupMembers: MutableLiveData<List<User>>
    lateinit var groupMembersClone: List<User>
    private val _requestCode = 2222
    var imageUri: Uri? = null
    var url: String = ""
    lateinit var userReference: DatabaseReference
    lateinit var currentUser: User
    var group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group_chat)


        binding.searchList.setHasFixedSize(true)
        binding.searchList.layoutManager = LinearLayoutManager(applicationContext) //context

        users = ArrayList()
        groupMembers = MutableLiveData()
        groupMembersClone = ArrayList()


        retrieveAllUser()

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUser(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.setGroupAvt.setOnClickListener {
            setImage()
        }

        binding.createButton.setOnClickListener {
            createGroup()
            navigateToGroupChatLogActivity()
        }

        /**
         * get current user
         */
        FirebaseDatabase.getInstance().reference.child("User")
            .child(FirebaseAuth.getInstance().currentUser.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User::class.java)!!
                        (groupMembersClone as ArrayList<User>).add(currentUser)
                        binding.listMembers.text = currentUser.getName()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun retrieveAllUser() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser.uid

        val referenceUser = FirebaseDatabase.getInstance().reference.child("User")

        referenceUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<User>).clear()
                if (binding.search.text.toString().isEmpty()) {
                    for (snapshot in p0.children) {
                        val user: User? = snapshot.getValue(User::class.java)
                        if (!(user!!.getUid()).equals(firebaseUserID)) {
                            (users as ArrayList<User>).add(user)
                        }
                    }

                    adapter = SearchUserToAddAdapter(
                        applicationContext,
                        users, groupMembers,
                        groupMembersClone
                    )

                    binding.searchList.adapter = adapter

                    adapter.groupMembers.observe(this@CreateGroupChatActivity, Observer{
                        var membersName = ""
                        adapter.groupMembers.value!!.forEach {
                            membersName += "${it.getName()}, "
                        }

                        binding.listMembers.text = membersName
                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun searchForUser(str: String) {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("User").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<User>).clear()

                for (snapshot in p0.children) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if (!(user!!.getUid()).equals(firebaseUserID)) {
                        (users as ArrayList<User>).add(user)
                    }
                }
                adapter = SearchUserToAddAdapter(
                    applicationContext,
                    users, groupMembers, groupMembersClone)
                binding.searchList.adapter = adapter


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun navigateToGroupChatLogActivity() {
        if (group != null){
            val intent = Intent(this@CreateGroupChatActivity, GroupChatLogActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("group", group)
            startActivity(intent)
            finish()
        }
    }

    fun createGroup() {
        val reference = FirebaseDatabase.getInstance().getReference("/Groups").push()

        val groupName: String = binding.setGroupName.text.toString()

        if (groupName.isNotEmpty() && groupMembers.value!!.isNotEmpty() && url.isNotEmpty()) {

                group = Group(
                groupName,
                reference.key!!,
                url,
                System.currentTimeMillis() / 1000,
                groupMembers.value!!, GroupChatMessenger(
                        reference.key!!,
                        "Now you can chat with each others in group",
                        currentUser.getUid(),
                        reference.key!!,
                        System.currentTimeMillis() / 1000,
                        groupName,
                        "text",
                        "",
                        url
                )
            )

            reference.setValue(group)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Create group successfully", Toast.LENGTH_LONG).show()
                }


            /**
             * them truong groups cho tung user co trong group
             */


            /**
             * can xem lai phan nay
             */

            group!!.getMembers().forEach {
                    userReference = FirebaseDatabase.getInstance().reference.child("User_Groups")
                        .child(it.getUid()).child("${group!!.getId()}")
                    userReference.setValue(group)
                }
        }
        else {
            Toast.makeText(applicationContext, "Group name, members list, group avatar must be set", Toast.LENGTH_SHORT).show()
        }

    }

    fun setAvatar() {

        if (imageUri != null) {
            val fileRef = FirebaseStorage.getInstance().reference.child("User Images")
                .child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)


            //kho hieu
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }

                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var downloadUrl = task.result // url de picasso load anh
                    url = downloadUrl.toString()
                    Picasso.get().load(url).into(binding.setGroupAvt)
                } else {
                    Toast.makeText(applicationContext, "Avatar update error", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == _requestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Toast.makeText(applicationContext, "uploading...", Toast.LENGTH_SHORT).show()
            setAvatar()
        }
    }


    private fun setImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 2222)
    }

}