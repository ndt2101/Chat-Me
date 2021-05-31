package com.tuan2101.chatme.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityGroupChatLogBinding
import com.tuan2101.chatme.network.ApiClient
import com.tuan2101.chatme.network.ApiService
import com.tuan2101.chatme.viewModel.Constants
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.GroupChatMessenger
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

    lateinit var binding: ActivityGroupChatLogBinding
    lateinit var _group: Group
    lateinit var group: Group
    var listener: Boolean = false
    lateinit var valueEventListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_chat_log)

        listener = true

        EmojiManager.install(IosEmojiProvider())

        _group = intent.getSerializableExtra("group") as Group



        FirebaseDatabase.getInstance().getReference("Groups").child(_group.getId())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    group = snapshot.getValue(Group::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })



        var popup = EmojiPopup.Builder.fromRootView(binding.chatLayout).build(binding.chat as EditText)

        if (applicationContext != null ){
            binding.userName.text = _group.getName().trim()
            Picasso.get().load(_group.getAvt()).into(binding.avt)
        }

        FirebaseDatabase.getInstance().reference.child("User")
            .child(FirebaseAuth.getInstance().currentUser.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java) ?: return
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        val reference = FirebaseDatabase.getInstance().getReference("Groups").child(_group.getId())
            if (listener){

                initValueListener(reference)
                reference
                    .addValueEventListener(valueEventListener)
            } else {
                reference.removeEventListener(valueEventListener)
            }


        binding.listMessenger.layoutManager = LinearLayoutManager(applicationContext)


        binding.listMessenger.adapter = adapter

        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        binding.iconSend.setOnClickListener {
            popup.toggle()
        }

        binding.imageSend.setOnClickListener {
            setImage()
        }

        val whiteBoardReference = FirebaseDatabase.getInstance().reference.child("Group_WhiteBoard").child(_group.getId())

        whiteBoardReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    binding.createBoard.setOnClickListener {
                        val map = HashMap<String, Any>()
                        map["createTime"] = System.currentTimeMillis()
                        whiteBoardReference.setValue(map)
                    }
                } else {
                    binding.createBoard.visibility = View.GONE
                    binding.board.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.board.setOnClickListener {
            val intent = Intent(this@GroupChatLogActivity, DrawingActivity::class.java)
            intent.putExtra("groupId", group.getId())
            startActivity(intent)
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
        }

        binding.chat.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            binding.listMessenger.scrollToPosition(
                adapter.itemCount - 1
            )
        }

        binding.videoCall.setOnClickListener {
            val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
            val removeCurrentUser = ArrayList<User>()

            println(currentUser.getUid())
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

            val popupMenu = PopupMenu(applicationContext, binding.avt)
            popupMenu.inflate(R.menu.group_menu)
            popupMenu.setOnMenuItemClickListener {
                if (it.itemId.equals(R.id.profile)) {
                    navigateToEditGroupActivity()
                } else if (it.itemId.equals(R.id.delete)) {
                    removeConversation()
                } else if (it.itemId.equals(R.id.leave_group)) {
                    removeConversation()
                    leaveGroup()
                }
                true
            }
            popupMenu.show()
        }

        binding.userName.setOnClickListener {
            val popupMenu = PopupMenu(applicationContext, binding.avt)
            popupMenu.inflate(R.menu.group_menu)
            popupMenu.setOnMenuItemClickListener {
                if (it.itemId.equals(R.id.profile)) {
                    navigateToEditGroupActivity()
                } else if (it.itemId.equals(R.id.delete)) {
                    removeConversation()
                } else if (it.itemId.equals(R.id.leave_group)) {
                    removeConversation()
                    leaveGroup()
                }
                true
            }
            popupMenu.show()
        }
    }

    fun leaveGroup() {
        var targetUser: User
        var leavedGroupMembers: ArrayList<User> = (group.getMembers() as ArrayList<User>)
        loop@for (item in (group.getMembers() as ArrayList<User>)) {
            if (item.getUid().equals(currentUser.getUid())) {
                targetUser = item
                leavedGroupMembers.remove(targetUser)
                break@loop
            }
        }

        if (FirebaseAuth.getInstance().uid?.equals(_group.getAdminId()) == true) {
            FirebaseDatabase.getInstance().getReference("Groups").child(_group.getId()).child("adminId")
                .setValue(group.getMembers()[1].getUid())
        }

        FirebaseDatabase.getInstance().getReference("Groups").child(_group.getId()).child("members")
            .setValue(leavedGroupMembers)
    }

    fun initValueListener(reference: DatabaseReference) {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val realTimeGroup = snapshot.getValue(Group::class.java)!!
                    var check = false

                    realTimeGroup.getMembers().forEach {
                        if (it.getUid().equals(FirebaseAuth.getInstance().uid)) {
                            check = true
                        }
                    }

                    if (!check) {
                        Toast.makeText(
                            applicationContext,
                            "This group is not available to you",
                            Toast.LENGTH_LONG
                        ).show()
                        reference.removeEventListener(this)
                        listener = false
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun removeConversation() {
        val removeMembersReference = FirebaseDatabase.getInstance()
            .getReference("/User_Groups/${currentUser.getUid()}/${group.getId()}")

        removeMembersReference.removeValue()
            .addOnCompleteListener {
                Toast.makeText(applicationContext, "Delete conversation successfully", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    fun navigateToEditGroupActivity() {
        val intent = Intent(this, EditGroupActivity::class.java)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    /**
     * ham lay toan bo tin nhan hien co tren firebase de load vao recyclerView theo uid de quyet dinh load item nao
     */
    private fun listenForMessenger() {

        val groupId = _group.getId()

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
                if (!response.isSuccessful) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = false
    }
}


class ChatTextFromItemInGroup(val chatMessenger: GroupChatMessenger): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        var clicked = false
        viewHolder.itemView.messenger_from_row.text = chatMessenger.text.trim()
        viewHolder.itemView.user_name_chat_from_row.text = chatMessenger.fromName
        viewHolder.itemView.user_name_chat_from_row.visibility = View.VISIBLE
        Picasso.get().load(chatMessenger.fromUserAvt).into(viewHolder.itemView.avt_from_row)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm a")
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

        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm a")
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