package com.tuan2101.chatme

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.databinding.ActivityChatLogBinding
import com.tuan2101.chatme.viewModel.ChatMessenger
import com.tuan2101.chatme.viewModel.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.avt
import kotlinx.android.synthetic.main.chat_from_row.view.messenger
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatLogActivity : AppCompatActivity() {

    var adapter = GroupAdapter<ViewHolder>()

    lateinit var currentUser: User

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
            loadMessenger()
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

                if (chatMessenger != null) {
                    if (chatMessenger.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(chatMessenger, currentUser))

                    }
                    else {
                        adapter.add(ChatFromItem(chatMessenger, user))
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
    private fun loadMessenger() {
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
                currentUser.getName()
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

class ChatFromItem(val chatMessenger: ChatMessenger, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messenger.text = chatMessenger.text.trim()
        Picasso.get().load(user.getAvatar()).into(viewHolder.itemView.avt)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val chatMessenger: ChatMessenger, val currentUser: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messenger.text = chatMessenger.text.trim()
        Picasso.get().load(currentUser.getAvatar()).into(viewHolder.itemView.avt)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}