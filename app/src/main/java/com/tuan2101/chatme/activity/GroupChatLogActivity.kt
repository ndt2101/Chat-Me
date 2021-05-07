package com.tuan2101.chatme.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityChatLogBinding
import com.tuan2101.chatme.network.ApiClient
import com.tuan2101.chatme.network.ApiService
import com.tuan2101.chatme.viewModel.Constants
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.image_chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.image_chat_to_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupChatLogActivity : AppCompatActivity() {

    var adapter = GroupAdapter<ViewHolder>()


    lateinit var storageRef: StorageReference



    lateinit var currentUser: User
    var imageUri: Uri? = null // de put anh len firebase

    private val _requestCode = 5555

    lateinit var binding: ActivityChatLogBinding
    lateinit var group: Group


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_log)

        EmojiManager.install(IosEmojiProvider())

        group = intent.getSerializableExtra("group") as Group

        var popup = EmojiPopup.Builder.fromRootView(binding.chatLayout).build(binding.chat as EditText)

        if (applicationContext != null ){
            binding.userName.text = group.getName().trim()
            Picasso.get().load(group.getAvt()).into(binding.avt)
        }

//        FirebaseDatabase.getInstance().reference.child("User")
//            .child(FirebaseAuth.getInstance().currentUser.uid).addValueEventListener(object :
//                ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    currentUser = snapshot.getValue(User::class.java) ?: return
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })

        FirebaseDatabase.getInstance().reference.child("User")
            .child(FirebaseAuth.getInstance().currentUser.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java) ?: return
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })



        binding.listMessenger.layoutManager = LinearLayoutManager(applicationContext)


        binding.listMessenger.adapter = adapter

        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        binding.iconSend.setOnClickListener {
            popup.toggle()
        }

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

        binding.videoCall.setOnClickListener {
            val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
            val removeCurrentUser = ArrayList<User>()
            group.getMembers().forEach {
                if (!it.getUid().equals(currentUser.getUid())) {
                    removeCurrentUser.add(it)
                }
            }
            intent.putExtra("members", Gson().toJson(removeCurrentUser))
            intent.putExtra("groupName", group.getName())
            intent.putExtra("groupAvt", group.getAvt())
            intent.putExtra("type", "video")
            intent.putExtra("isMultiple", true)
            startActivity(intent)
        }

        binding.voiceCall.setOnClickListener {
            val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
            val removeCurrentUser = ArrayList<User>()
            group.getMembers().forEach {
                if (!it.getUid().equals(currentUser.getUid())) {
                    removeCurrentUser.add(it)
                }
            }
            intent.putExtra("members", Gson().toJson(removeCurrentUser))
            intent.putExtra("groupName", group.getName())
            intent.putExtra("groupAvt", group.getAvt())
            intent.putExtra("type", "voice")
            intent.putExtra("isMultiple", true)
            startActivity(intent)
        }

        binding.avt.setOnClickListener {
            val intent = Intent(this, EditGroupActivity::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }
    }

    /**
     * ham lay toan bo tin nhan hien co tren firebase de load vao recyclerView theo uid de quyet dinh load item nao
     */
    private fun listenForMessenger() {

        val groupId = group.getId()

        val reference = FirebaseDatabase.getInstance().getReference("/group_messengers/$groupId")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessenger = snapshot.getValue(GroupChatMessenger::class.java)



                if (chatMessenger != null) {

                    if (chatMessenger.type.equals("text")) {
                        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                            adapter.add(ChatTextToItemInGroup(chatMessenger))

                        } else {
                            adapter.add(ChatTextFromItemInGroup(chatMessenger))
                        }
                        binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                    } else if (chatMessenger.type.equals("image")) {
                        if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                            adapter.add(ChatImageToItemInGroup(chatMessenger, this@GroupChatLogActivity))
                        } else {
                            adapter.add(ChatImageFromRowInGroup(chatMessenger, this@GroupChatLogActivity))
                        }
                        binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                    }
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
//        val toId = user.getUid()
        val fromId = FirebaseAuth.getInstance().uid

        val groupId = group.getId()

        val reference = FirebaseDatabase.getInstance().getReference("/group_messengers/$groupId").push()


        val messenger = binding.chat.text.toString()


        if (fromId != null) {
            val chatMessenger = GroupChatMessenger(
                reference.key!!,
                messenger,
                fromId,
                groupId,
                System.currentTimeMillis() / 1000,
                currentUser.getName(),
                "text",
                "",
                currentUser.getAvatar()
            )
            reference.setValue(chatMessenger)
                .addOnSuccessListener {
                    binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                }
            val latestMessengerReference = FirebaseDatabase.getInstance()
                .getReference("/Groups/${groupId}")
            val map: HashMap<String, Any> = HashMap()

            map["latestMessenger"] = chatMessenger
            latestMessengerReference.updateChildren(map)

            group.getMembers().forEach {
                val latestMessengerForMembersReference = FirebaseDatabase.getInstance()
                    .getReference("/User_Groups/${it.getUid()}/${groupId}")

                latestMessengerForMembersReference.updateChildren(map)
                    .addOnSuccessListener {
                        initiateMessage("${currentUser.getName()}: ${chatMessenger.text}", (group.getMembers() as ArrayList<User>))
                    }
            }
        }
    }


    /**
     * ham gui tin nhan len firebase
     *
     * URl???
     */
    private fun loadImageMessenger() {
//        val toId = user.getUid()
        val fromId = FirebaseAuth.getInstance().uid

        val groupId = group.getId()


        val reference = FirebaseDatabase.getInstance().getReference("/group_messengers/$groupId").push()


        if (fromId != null) {

            if (imageUri != null) {
                val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg")
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
                        val url = downloadUrl.toString()


                        val chatMessenger = GroupChatMessenger(
                            reference.key!!,
                            "[image]",
                            fromId,
                            groupId,
                            System.currentTimeMillis() / 1000,
                            currentUser.getName(),
                            "image",
                            url,
                            currentUser.getAvatar()
                        )

                        reference.setValue(chatMessenger)
                            .addOnSuccessListener {
                                binding.listMessenger.scrollToPosition(adapter.itemCount - 1)
                            }


                        val latestMessengerReference = FirebaseDatabase.getInstance()
                            .getReference("/Groups/${groupId}")
                        val map: HashMap<String, Any> = HashMap()

                        map["latestMessenger"] = chatMessenger
                        latestMessengerReference.updateChildren(map)
                            .addOnSuccessListener {
                                initiateMessage("${currentUser.getName()}: ${chatMessenger.text}", (group.getMembers() as ArrayList<User>))
                            }

                        group.getMembers().forEach {
                            val latestMessengerForMembersReference = FirebaseDatabase.getInstance()
                                .getReference("/User_Groups/${it.getUid()}/${groupId}")
                            latestMessengerForMembersReference.updateChildren(map)
                        }

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
        startActivityForResult(intent, 5555)
    }


    /**
     * gui thong bao
     */

    fun initiateMessage(messageContent: String, receivers: ArrayList<User>) {
        try {

            val tokens =  JSONArray()

            val newArray = ArrayList<User>()

            receivers.forEach {
                if (!it.getUid().equals(currentUser.getUid())) {
                    newArray.add(it)
                }
            }

            if (receivers != null && receivers.size > 0) {
                newArray.forEach{
                    tokens.put(it.getToken())
                }
            }

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_MESSAGE)

            data.put("chatLogType", "group")
            data.put("messageContent", messageContent)
            data.put("userName", group.getName())
            data.put("userId", group.getId())

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_MESSAGE)



        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun sendRemoteMessage(remoteMessage: String, type: String) {
        ApiClient.getClient().create(ApiService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeaders(), remoteMessage
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@GroupChatLogActivity, "send successfully", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@GroupChatLogActivity, response.message(),Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@GroupChatLogActivity, t.message,Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }
    /**
     * ket thuc
     */

    fun navigateToImageActivity(url: String) {
        val intent = Intent(this@GroupChatLogActivity, ImageActivity::class.java)
        intent.putExtra("image", url)
        startActivity(intent)
        finish()
    }
}


class ChatTextFromItemInGroup(val chatMessenger: GroupChatMessenger): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        var clicked = false
        viewHolder.itemView.messenger_from_row.text = chatMessenger.text.trim()
        viewHolder.itemView.user_name_chat_from_row.text = chatMessenger.fromName
        viewHolder.itemView.user_name_chat_from_row.visibility = View.VISIBLE
        Picasso.get().load(chatMessenger.fromUserAvt).into(viewHolder.itemView.avt_from_row)
        val simpleDateFormat = SimpleDateFormat("h:mm a")
        val date = Date(chatMessenger.timeStamp * 1000)
        val time = simpleDateFormat.format(date)

        viewHolder.itemView.messenger_from_row.setOnClickListener {
            if (!clicked){
                viewHolder.itemView.time_of_chat_from_row.visibility = View.VISIBLE
                clicked = true}
            else {
                clicked = false
                viewHolder.itemView.time_of_chat_from_row.visibility = View.GONE
            }
            viewHolder.itemView.time_of_chat_from_row.text = time
        }


    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatTextToItemInGroup(val chatMessenger: GroupChatMessenger): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        var clicked = false
        viewHolder.itemView.messenger_to_row.text = chatMessenger.text.trim()
        Picasso.get().load(chatMessenger.fromUserAvt).into(viewHolder.itemView.avt_chat_to_row)

        val simpleDateFormat = SimpleDateFormat("h:mm a")
        val date = Date(chatMessenger.timeStamp * 1000)
        val time = simpleDateFormat.format(date)

        viewHolder.itemView.messenger_to_row.setOnClickListener {
            if (!clicked){
                viewHolder.itemView.time_of_chat_to_row.visibility = View.VISIBLE
                clicked = true}
            else {
                clicked = false
                viewHolder.itemView.time_of_chat_to_row.visibility = View.GONE
            }
            viewHolder.itemView.time_of_chat_to_row.text = time
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}

class ChatImageFromRowInGroup(val chatMessenger: GroupChatMessenger, val activity: GroupChatLogActivity): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(chatMessenger.fromUserAvt).into(viewHolder.itemView.avt_image_from_row)
        Picasso.get().load((chatMessenger.img)).into(viewHolder.itemView.image_messenger_from_row)
        viewHolder.itemView.user_name_image.text = chatMessenger.fromName
        viewHolder.itemView.user_name_image.visibility = View.VISIBLE

        val simpleDateFormat = SimpleDateFormat("h:mm a")
        val date = Date(chatMessenger.timeStamp * 1000)
        val time = simpleDateFormat.format(date)

        viewHolder.itemView.time_of_chat_image_from_row.text = time

        viewHolder.itemView.image_messenger_from_row.setOnClickListener {
            activity.navigateToImageActivity(chatMessenger.img)
        }

    }

    override fun getLayout(): Int {
        return R.layout.image_chat_from_row
    }
}

class ChatImageToItemInGroup(val chatMessenger: GroupChatMessenger, val activity: GroupChatLogActivity): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(chatMessenger.fromUserAvt).into(viewHolder.itemView.avt_image_to_row)
        Picasso.get().load((chatMessenger.img)).into(viewHolder.itemView.image_messenger_to_row)

        val simpleDateFormat = SimpleDateFormat("h:mm a")
        val date = Date(chatMessenger.timeStamp * 1000)
        val time = simpleDateFormat.format(date)

        viewHolder.itemView.time_of_chat_image_to_row.text = time

        viewHolder.itemView.image_messenger_to_row.setOnClickListener {
            activity.navigateToImageActivity(chatMessenger.img)
        }
    }

    override fun getLayout(): Int {
        return R.layout.image_chat_to_row
    }

}