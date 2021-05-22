package com.tuan2101.chatme.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User


class EditGroupActivity : AppCompatActivity()  {

    lateinit var adapter: SearchUserToAddAdapter
    lateinit var binding: ActivityCreateGroupChatBinding
    lateinit var group: Group
    lateinit var users: List<User>
    var groupMembers: MutableLiveData<List<User>> = MutableLiveData()
    lateinit var groupMembersClone: List<User>
    lateinit var currentUser: User


    private val _requestCode = 8888
    var imageUri: Uri? = null
    var url: String = ""
    lateinit var userReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var listMemberName: String = ""
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group_chat)

        group = intent.getSerializableExtra("group") as Group

        Picasso.get().load(group.getAvt()).into(binding.setGroupAvt)
        binding.setGroupName.setText(group.getName())
        url = group.getAvt()

        val oldMembersList: List<User> = group.getMembers()


        binding.searchList.layoutManager = LinearLayoutManager(applicationContext) //context

        users = ArrayList()

        groupMembersClone = ArrayList<User>()
        (groupMembersClone as ArrayList<User>).addAll(group.getMembers())


        groupMembersClone.forEach {
            listMemberName += "${it.getName()}, "
        }

        binding.listMembers.text = listMemberName

        if (FirebaseAuth.getInstance().uid.equals(group.getAdminId())){
            binding.createButton.text = "Update"
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
                updateGroup(oldMembersList)
                navigateToGroupChatLogActivity()
            }

        }else {
            binding.editGroup.visibility = View.GONE
            binding.setGroupName.visibility = View.GONE
            binding.groupName.visibility = View.VISIBLE
            binding.groupName.text = group.getName()
            binding.search.visibility = View.GONE

            adapter = SearchUserToAddAdapter(
                applicationContext,
                groupMembersClone, groupMembers,
                groupMembersClone
            )

            binding.searchList.adapter = adapter

            binding.createButton.visibility = View.GONE
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

                    adapter.groupMembers.observe(this@EditGroupActivity , Observer{
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
            val intent = Intent(this@EditGroupActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    fun updateGroup(oldMembersList: List<User>) {
        val reference = FirebaseDatabase.getInstance().getReference("/Groups").child(group.getId())

        val groupName: String = binding.setGroupName.text.toString()

        if (groupName.isNotEmpty()) {
            var clickedOutMembers = ArrayList<User>()
            val map = HashMap<String, Any>()
            map["avt"] = url
            map["name"] = binding.setGroupName.text.toString()
            if (groupMembers.value != null && groupMembers.value!!.isNotEmpty()) {
                map["members"] = groupMembers.value!!

                for (old in oldMembersList) {

                    if (groupMembers.value!!.indexOf(old) == -1) {
                        val removeMembersReference = FirebaseDatabase.getInstance()
                            .getReference("/User_Groups/${old.getUid()}/${group.getId()}")

                        removeMembersReference.setValue(null)

                    }
                }

                for (new in groupMembers.value!!) {

                    if (oldMembersList.indexOf(new) == -1) {
                        val removeMembersReference = FirebaseDatabase.getInstance()
                            .getReference("/User_Groups/${new.getUid()}/${group.getId()}")

                        removeMembersReference.setValue(group)

                    }
                }

            }
            else {
                map["members"] = group.getMembers()
            }

            reference.updateChildren(map)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Update successfully", Toast.LENGTH_SHORT).show()
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

                    Toast.makeText( this@EditGroupActivity, "Set image successfully", Toast.LENGTH_SHORT).show()
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
        startActivityForResult(intent, 8888)
    }

}