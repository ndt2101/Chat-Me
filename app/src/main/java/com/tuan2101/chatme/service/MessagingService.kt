package com.tuan2101.chatme.service

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tuan2101.chatme.activity.IncomingInvitationActivity
import com.tuan2101.chatme.viewModel.Constants

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

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                val intent = Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE)

                intent.putExtra(
                    Constants.REMOTE_MSG_INVITATION_RESPONSE,
                    remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE]
                )

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

    }
}