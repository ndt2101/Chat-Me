package com.tuan2101.chatme.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.tuan2101.chatme.databinding.FragmentProfileBinding
import com.tuan2101.chatme.viewModel.User


class ProfileFragment : Fragment() {

    lateinit var userReference: DatabaseReference
    lateinit var firebaseUser: FirebaseUser
    lateinit var binding: FragmentProfileBinding
    private val _requestCode = 2101
    var imageUri: Uri? = null // de put anh len firebase
    lateinit var storageRef: StorageReference
    lateinit var clickCheck: String

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


        binding.avt.setOnClickListener {
            clickCheck = "avatar"
            setImage()
        }

        binding.coverImage.setOnClickListener {
            clickCheck = "cover"
            setImage()
        }

        return binding.root
    }

    private fun setImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 2101)
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