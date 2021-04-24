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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.adapter.SearchUserToAddAdapter
import com.tuan2101.chatme.databinding.ActivityCreateGroupChatBinding
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
        }
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

    fun createGroup() {
        val reference = FirebaseDatabase.getInstance().getReference("/Groups").push()

        val groupName: String = binding.setGroupName.text.toString()

        if (groupName.isNotEmpty() && groupMembers.value!!.isNotEmpty() && url.isNotEmpty()) {

            val group = Group(
                groupName,
                reference.key!!,
                url,
                System.currentTimeMillis() / 1000,
                groupMembers.value!!
            )

            reference.setValue(group)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Create group successfully", Toast.LENGTH_LONG).show()
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