package com.tuan2101.chatme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tuan2101.chatme.R
import com.tuan2101.chatme.activity.*
import com.tuan2101.chatme.viewModel.Constants
import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User

class MessagingService: FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data[Constants.REMOTE_MSG_TYPE]

        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                val intent = Intent(applicationContext, IncomingInvitationActivity::class.java)
                intent.putExtra(Constants.REMOTE_MSG_MEETING_TYPE,
                remoteMessage.data[Constants.REMOTE_MSG_MEETING_TYPE])

                intent.putExtra("userName",
                remoteMessage.data["userName"])

                intent.putExtra("userAvt",
                remoteMessage.data["userAvt"])

                intent.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN,
                remoteMessage.data[Constants.REMOTE_MSG_INVITER_TOKEN])

                intent.putExtra(Constants.REMOTE_MSG_MEETING_ROOM,
                remoteMessage.data[Constants.REMOTE_MSG_MEETING_ROOM])

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                val intent = Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE)

                intent.putExtra(
                    Constants.REMOTE_MSG_INVITATION_RESPONSE,
                    remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE]
                )

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }else if (type.equals(Constants.REMOTE_MSG_MESSAGE)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("Chat Me", "Chat Me", NotificationManager.IMPORTANCE_DEFAULT)
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.createNotificationChannel(channel)
                }

                if (remoteMessage.data["chatLogType"].equals("single")){
                    val buider = NotificationCompat.Builder(applicationContext, "Chat Me")
                    buider.setContentTitle(remoteMessage.data["userName"])
                        .setContentText(remoteMessage.data["messageContent"])
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.chatmelogo)

                    var user: User

                    FirebaseDatabase.getInstance().getReference("User")
                        .child(remoteMessage.data["userId"]!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.exists()) {
                                    user = snapshot.getValue(User::class.java)!!

                                    val intent = Intent(
                                        applicationContext,
                                        SingleChatLogActivity::class.java
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.putExtra("user", user)

                                    if (applicationContext != MainActivity::class.java && applicationContext != SearchActivity::class.java && applicationContext != CreateGroupChatActivity::class.java
                                        && applicationContext != SingleChatLogActivity::class.java && applicationContext != GroupChatMessenger::class.java && applicationContext != IncomingInvitationActivity::class.java
                                        && applicationContext != OutgoingInvitationActivity::class.java && applicationContext != GroupChatLogActivity::class.java && applicationContext != InfoActivity::class.java
                                    ) {

                                        val intent0 =
                                            Intent(applicationContext, MainActivity::class.java)

                                        val pendingIntent = PendingIntent.getActivities(
                                            applicationContext,
                                            0,
                                            arrayOf(intent0, intent),
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                        buider.setContentIntent(pendingIntent)
                                    } else {
                                        val pendingIntent = PendingIntent.getActivity(
                                            applicationContext,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                        buider.setContentIntent(pendingIntent)
                                    }

                                    val managerCompat =
                                        NotificationManagerCompat.from(applicationContext)
                                    managerCompat.notify(1, buider.build())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                } else if (remoteMessage.data["chatLogType"].equals("group")) {
                    val buider = NotificationCompat.Builder(applicationContext, "Chat Me")
                    buider.setContentTitle(remoteMessage.data["userName"])
                        .setContentText(remoteMessage.data["messageContent"])
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.chatmelogo)



                    var group: Group

                    FirebaseDatabase.getInstance().getReference("Groups")
                        .child(remoteMessage.data["userId"]!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.exists()) {
                                    group = snapshot.getValue(Group::class.java)!!

                                    val intent = Intent(
                                        applicationContext,
                                        GroupChatLogActivity::class.java
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.putExtra("group", group)

                                    if (applicationContext != MainActivity::class.java && applicationContext != SearchActivity::class.java && applicationContext != CreateGroupChatActivity::class.java
                                        && applicationContext != SingleChatLogActivity::class.java && applicationContext != GroupChatMessenger::class.java && applicationContext != IncomingInvitationActivity::class.java
                                        && applicationContext != OutgoingInvitationActivity::class.java && applicationContext != GroupChatLogActivity::class.java && applicationContext != InfoActivity::class.java
                                    ) {
                                        val intent0 = Intent(applicationContext, MainActivity::class.java)

                                        val pendingIntent = PendingIntent.getActivities(
                                            applicationContext,
                                            0,
                                            arrayOf(intent0, intent),
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                        buider.setContentIntent(pendingIntent)
                                    } else {
                                        val pendingIntent = PendingIntent.getActivity(
                                            applicationContext,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                        buider.setContentIntent(pendingIntent)
                                    }

                                    val managerCompat = NotificationManagerCompat.from(applicationContext)
                                    managerCompat.notify(1, buider.build())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

            }
        }

    }
}