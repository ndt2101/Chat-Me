package com.tuan2101.chatme.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.squareup.picasso.Picasso
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityIncomingInvitationBinding
import com.tuan2101.chatme.network.ApiClient
import com.tuan2101.chatme.network.ApiService
import com.tuan2101.chatme.viewModel.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class IncomingInvitationActivity : AppCompatActivity() {
    lateinit var binding: ActivityIncomingInvitationBinding
    lateinit var _type: String
    lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_incoming_invitation)

        _type = intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE)!!

        val userAvt = intent.getStringExtra("userAvt")

        val userName = intent.getStringExtra("userName")

        binding.userName.text = userName

        Picasso.get().load(userAvt).into(binding.userAvt)

        if (_type.equals("video")) {
            binding.callingType.setImageResource(R.drawable.ic_video_call)
        }else {
            binding.callingType.setImageResource(R.drawable.ic_voice_call)
        }

        binding.accept.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)!!
            )
        }

        binding.decline.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)!!
            )
        }

    }

    fun sendInvitationResponse(type: String, receiverToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), type)
        } catch (e: Exception) {
            Toast.makeText(this@IncomingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun sendRemoteMessage(remoteMessage: String, type: String) {
        ApiClient.getClient().create(ApiService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeaders(), remoteMessage
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        Toast.makeText(
                            this@IncomingInvitationActivity,
                            "Invitation accepted",
                            Toast.LENGTH_SHORT
                        ).show()

                        try {
                            val serviceURL = URL("https://meet.jit.si")


                            val builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serviceURL)
                            builder.setWelcomePageEnabled(false)
                            builder.setRoom(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))


                            if (_type.equals("voice")) {
                                builder.setVideoMuted(true)
//                                builder.setAudioOnly(true)
                            }


                            JitsiMeetActivity.launch(this@IncomingInvitationActivity, builder.build())
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@IncomingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
                            finish()
                        }

                    } else {
                        Toast.makeText(
                            this@IncomingInvitationActivity,
                            "Invitation rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@IncomingInvitationActivity,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@IncomingInvitationActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }

        })
    }


    val invitationResponseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent!!.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(applicationContext, "Invitation canceled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(this,R.raw.receive_music)
        mediaPlayer.isLooping=true
        mediaPlayer.start()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver, IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }
}