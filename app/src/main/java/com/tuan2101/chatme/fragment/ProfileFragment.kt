package com.tuan2101.chatme.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.activity.LoginActivity
import com.tuan2101.chatme.databinding.FragmentProfileBinding
import com.tuan2101.chatme.viewModel.User
import com.tuan2101.chatme.viewModel.hideKeyboard


class ProfileFragment : Fragment() {

    lateinit var userReference: DatabaseReference
    lateinit var firebaseUser: FirebaseUser
    lateinit var binding: FragmentProfileBinding
    private val _requestCode = 2101
    var imageUri: Uri? = null // de put anh len firebase
    lateinit var storageRef: StorageReference
    lateinit var clickCheck: String
    var userNameText: String = ""
    var nickName: String = ""
    var workText: String = ""
    var homeTownText: String = ""
    var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUser.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)

                    if (context != null) {
                        binding.userName.setText(user!!.getName().trim())
                        Picasso.get().load(user.getAvatar()).into(binding.avt)
                        Picasso.get().load(user.getCoverImage()).into(binding.coverImage)
                        binding.introduce.setText(user.getIntroduceYourself())
                        binding.work.setText( "Work at ${user!!.getWork()}")
                        binding.homeTown.setText("From ${user!!.getHomeTown()}")


                        userNameText = user.getName().trim()
                        nickName = user.getIntroduceYourself()
                        workText = "Work at ${user!!.getWork()}"
                        homeTownText = "From ${user!!.getHomeTown()}"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })


        binding.avt.setOnClickListener {
            clickCheck = "avatar"
            setImage()
        }

        binding.coverImage.setOnClickListener {
            clickCheck = "cover"
            setImage()
        }

        binding.userName.setOnClickListener {
            clickToEdit()
        }

        binding.introduce.setOnClickListener {
            clickToEdit()
        }

        binding.work.setOnClickListener {
            clickToEdit()
        }

        binding.homeTown.setOnClickListener {
            clickToEdit()
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sendToLoginActivity()
        }

        binding.update.setOnClickListener {
            if (binding.userNameEdit.text.toString().isNotEmpty()) {
                var updateMap = HashMap<String, Any>()
                updateMap["name"] = binding.userNameEdit.text.toString()
                userReference.updateChildren(updateMap)
            }

            if (binding.introduceEdit.text.toString().isNotEmpty()) {
                var updateMap = HashMap<String, Any>()
                updateMap["introduceYourself"] = binding.introduceEdit.text.toString()
                userReference.updateChildren(updateMap)
            }

            if (binding.workEdit.text.toString().isNotEmpty()) {
                var updateMap = HashMap<String, Any>()
                updateMap["work"] = binding.workEdit.text.toString()
                userReference.updateChildren(updateMap)
            }

            if (binding.homeTownEdit.text.toString().isNotEmpty()) {
                var updateMap = HashMap<String, Any>()
                updateMap["homeTown"] = binding.homeTownEdit.text.toString()
                userReference.updateChildren(updateMap)
            }

            context?.hideKeyboard(binding.update)
            doneUpdate()
        }

        return binding.root
    }


    private fun setImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 2101)
    }

    private fun sendToLoginActivity() {
        var map = HashMap<String, Any>()
        map["token"] = ""
        currentUser?.let { FirebaseDatabase.getInstance().reference.child("User").child(it.uid).updateChildren(map) }
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun clickToEdit() {

        binding.userName.visibility = View.GONE
        binding.userNameEdit.visibility = View.VISIBLE
        binding.introduce.visibility = View.GONE
        binding.introduceEdit.visibility = View.VISIBLE
        binding.work.visibility = View.GONE
        binding.workEdit.visibility = View.VISIBLE
        binding.homeTown.visibility = View.GONE
        binding.homeTownEdit.visibility = View.VISIBLE

        binding.userNameEdit.setText(binding.userName.text)
        binding.introduceEdit.setText(binding.introduce.text)
        binding.workEdit.setText(binding.work.text.substring(8))
        binding.homeTownEdit.setText(binding.homeTown.text.substring(5))

        binding.update.visibility = View.VISIBLE
        binding.logoutButton.visibility = View.GONE
        binding.guide.visibility = View.GONE

    }

    fun doneUpdate() {

        binding.userName.visibility = View.VISIBLE
        binding.userNameEdit.visibility = View.GONE
        binding.introduce.visibility = View.VISIBLE
        binding.introduceEdit.visibility = View.GONE
        binding.work.visibility = View.VISIBLE
        binding.workEdit.visibility = View.GONE
        binding.homeTown.visibility = View.VISIBLE
        binding.homeTownEdit.visibility = View.GONE

        binding.update.visibility = View.GONE
        binding.logoutButton.visibility = View.VISIBLE
        binding.guide.visibility = View.VISIBLE

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == _requestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Toast.makeText(context, "uploading...", Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, wait a second...")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)


            //kho hieu
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }

                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var downloadUrl = task.result // url de picasso load anh
                    val url = downloadUrl.toString()

                    if (clickCheck == "cover") {
                        val coverImage = HashMap<String, Any>()
                        coverImage["coverImage"] = url
                        userReference.updateChildren(coverImage)
                        clickCheck = ""
                    }
                    else {
                        val avatarImage = HashMap<String, Any>()
                        avatarImage["avatar"] = url
                        userReference.updateChildren(avatarImage)
                        clickCheck = ""
                    }
                    progressBar.dismiss()
                }
            }

        }
    }
}