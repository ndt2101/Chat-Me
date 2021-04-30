package com.tuan2101.chatme.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityOutgoingInvitationBinding
import com.tuan2101.chatme.network.ApiClient
import com.tuan2101.chatme.network.ApiService
import com.tuan2101.chatme.viewModel.Constants
import com.tuan2101.chatme.viewModel.User
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.reflect.typeOf

class OutgoingInvitationActivity : AppCompatActivity() {

    lateinit var binding: ActivityOutgoingInvitationBinding
    lateinit var user: User
    lateinit var inviterToken: String
    lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView( this, R.layout.activity_outgoing_invitation)





//        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener ( object: OnCompleteListener<InstanceIdResult> {
//            override fun onComplete(task: Task<InstanceIdResult>) {
//                if (task.isSuccessful && task != null) {
//                    inviterToken = task.result!!.token
//                }
//            }
//
//        })




        user = intent.getSerializableExtra("user") as User

        val type = intent.getStringExtra("type")


        FirebaseDatabase.getInstance().reference.child("User")
            .child(FirebaseAuth.getInstance().currentUser.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User::class.java)!!

                        inviterToken = currentUser.getToken().toString()
                        if (type != null && user != null) {
                            initiateMeeting(type, user.getToken()!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        if (type.equals("video")) {
            binding.callingType.setImageResource(R.drawable.ic_video_call)
        }
        else {
            binding.callingType.setImageResource(R.drawable.ic_voice_call)
        }

        if (user != null){
            Picasso.get().load(user.getAvatar()).into(binding.userAvt)
            binding.userName.text = user.getName()
        }

        binding.decline.setOnClickListener {
            if (user != null) {
                cancelInvitation(user.getToken()!!)
            }
        }


    }

    fun initiateMeeting(meetingType: String, receiverToken: String) {
        try {

            val tokens =  JSONArray()
            tokens.put(receiverToken)


            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)

            data.put("userAvt", currentUser.getAvatar())
            data.put("userName", currentUser.getName())
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION)
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
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation sent successfully",Toast.LENGTH_SHORT).show()
                    }
                    else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation rejected",Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                else {
                    Toast.makeText(this@OutgoingInvitationActivity, response.message(),Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message,Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }

    fun cancelInvitation(receiverToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)
        } catch (e: Exception) {
            Toast.makeText(this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    val invitationResponseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent!!.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    Toast.makeText(applicationContext, "Invitation accepted", Toast.LENGTH_SHORT).show()
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    Toast.makeText(applicationContext, "Invitation rejected", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver, IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }
}