package com.tuan2101.chatme

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.databinding.ActivityChatLogBinding
import com.tuan2101.chatme.viewModel.ChatMessenger
import com.tuan2101.chatme.viewModel.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.avt
import kotlinx.android.synthetic.main.chat_from_row.view.messenger
import com.google.android.gms.tasks.Continuation
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.image_chat_from_row.view.*
import kotlinx.android.synthetic.main.image_chat_from_row.view.image_messenger
import kotlinx.android.synthetic.main.image_chat_to_row.view.*


class ChatLogActivity : AppCompatActivity() {

    var adapter = GroupAdapter<ViewHolder>()


    lateinit var storageRef: StorageReference


    lateinit var currentUser: User
    var imageUri: Uri? = null // de put anh len firebase

    private val _requestCode = 1111

    lateinit var binding: ActivityChatLogBinding
    lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_log)

        user = intent.getSerializableExtra("user") as User

        if (applicationContext != null ){
            binding.userName.text = user.getName().trim()
            Picasso.get().load(user.getAvatar()).into(binding.avt)
        }

         FirebaseDatabase.getInstance().reference.child("User")
            .child(FirebaseAuth.getInstance().currentUser.uid).addValueEventListener(object :
                    ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User::class.java)!!
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                    })



        binding.listMessenger.layoutManager = LinearLayoutManager(applicationContext)


        binding.listMessenger.adapter = adapter

        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        binding.imageSend.setOnClickListener {
            setImage()
        }

        binding.chat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.chat.text.toString().isNotEmpty()) {
                    binding.imageSend.visibility = View.GONE
                    binding.sendButton.visibility = View.VISIBLE
                } else {
                    binding.imageSend.visibility = View.VISIBLE
                    binding.sendButton.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        listenForMessenger()

        binding.sendButton.setOnClickListener{
            loadTextMessenger()
            binding.chat.setText("")

//            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(binding.chat.getWindowToken(), 0)
//            val inputMethodManager = applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)

        }

        binding.chat.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
            }

        })
    }

    /**
     * ham lay toan bo tin nhan hien co tren firebase de load vao recyclerView theo uid de quyet dinh load item nao
     */
    private fun listenForMessenger() {

        val toId = user.getUid()
        val fromId = FirebaseAuth.getInstance().uid

        val reference = FirebaseDatabase.getInstance().getReference("/user_messengers/$fromId/$toId")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessenger = snapshot.getValue(ChatMessenger::class.java)

                if (chatMessenger != null && chatMessenger.type.equals("text")) {
                    if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatTextToItem(chatMessenger, currentUser))

                    }
                    else {
                        adapter.add(ChatTextFromItem(chatMessenger, user))
                    }
                    binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                }
                else if(chatMessenger != null && chatMessenger.type.equals("image")) {
                    if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatImageToItem(chatMessenger, currentUser))
                    }
                    else {
                        adapter.add(ChatImageFromRow(chatMessenger, user))
                    }
                    binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    /**
     * ham gui tin nhan len firebase
     */
    private fun loadTextMessenger() {
        val toId = user.getUid()
        val fromId = FirebaseAuth.getInstance().uid


        val reference = FirebaseDatabase.getInstance().getReference("/user_messengers/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user_messengers/$toId/$fromId").push()



        val messenger = binding.chat.text.toString()

        println("==============================================")
        println(user.getName())
        println(user.getUid())
        println("==============================================")


        if (fromId != null) {
            val chatMessenger = ChatMessenger(
                reference.key!!,
                messenger,
                fromId,
                toId,
                System.currentTimeMillis() / 1000,
                currentUser.getName(),
                "text",
                ""
            )
            reference.setValue(chatMessenger)
                .addOnSuccessListener {
                    binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                }

            toReference.setValue(chatMessenger)
                .addOnSuccessListener {
                    binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                }
            val latestMessengerReference = FirebaseDatabase.getInstance().getReference("latest-messenger/$fromId/$toId")
            latestMessengerReference.setValue(chatMessenger)

            val latestMessengerToReference = FirebaseDatabase.getInstance().getReference("latest-messenger/$toId/$fromId")
            latestMessengerToReference.setValue(chatMessenger)
        }



    }


    /**
     * ham gui tin nhan len firebase
     *
     * URl???
     */
    private fun loadImageMessenger() {
        val toId = user.getUid()
        val fromId = FirebaseAuth.getInstance().uid


        val reference = FirebaseDatabase.getInstance().getReference("/user_messengers/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user_messengers/$toId/$fromId").push()


        if (fromId != null) {

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


                        val chatMessenger = ChatMessenger(
                            reference.key!!,
                            "[image]",
                            fromId,
                            toId,
                            System.currentTimeMillis() / 1000,
                            currentUser.getName(),
                            "image",
                            url
                        )

                        reference.setValue(chatMessenger)
                            .addOnSuccessListener {
                                binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                            }

                        toReference.setValue(chatMessenger)
                            .addOnSuccessListener {
                                binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                            }
                        val latestMessengerReference = FirebaseDatabase.getInstance().getReference("latest-messenger/$fromId/$toId")
                        latestMessengerReference.setValue(chatMessenger)

                        val latestMessengerToReference = FirebaseDatabase.getInstance().getReference("latest-messenger/$toId/$fromId")
                        latestMessengerToReference.setValue(chatMessenger)
                    }
                    }
                }

            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == _requestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Toast.makeText(applicationContext, "uploading...", Toast.LENGTH_SHORT).show()
            loadImageMessenger()
        }
    }


    private fun setImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1111)
    }

}

class ChatTextFromItem(val chatMessenger: ChatMessenger, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messenger.text = chatMessenger.text.trim()
        Picasso.get().load(user.getAvatar()).into(viewHolder.itemView.avt)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatTextToItem(val chatMessenger: ChatMessenger, val currentUser: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messenger.text = chatMessenger.text.trim()
        Picasso.get().load(currentUser.getAvatar()).into(viewHolder.itemView.avt)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}

class ChatImageFromRow(val chatMessenger: ChatMessenger, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(user.getAvatar()).into(viewHolder.itemView.avt)
        Picasso.get().load((chatMessenger.img)).into(viewHolder.itemView.image_messenger)

    }

    override fun getLayout(): Int {
        return R.layout.image_chat_from_row
    }
}

class ChatImageToItem(val chatMessenger: ChatMessenger, val currentUser: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(currentUser.getAvatar()).into(viewHolder.itemView.avt)
        Picasso.get().load((chatMessenger.img)).into(viewHolder.itemView.image_messenger)
    }

    override fun getLayout(): Int {
        return R.layout.image_chat_to_row
    }

}

