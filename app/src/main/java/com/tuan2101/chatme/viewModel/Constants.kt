package com.tuan2101.chatme.viewModel

class Constants {
    companion object {
        const val REMOTE_MSG_AUTHORIZATION = "authorization"
        const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        const val REMOTE_MSG_TYPE = "type"
        const val REMOTE_MSG_INVITATION = "invitation"
        const val REMOTE_MSG_MEETING_TYPE = "meetingType"
        const val REMOTE_MSG_INVITER_TOKEN = "inviterToken"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
        const val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"
        const val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
        const val REMOTE_MSG_INVITATION_REJECTED = "rejected"
        const val REMOTE_MSG_INVITATION_CANCELLED = "cancelled"

        const val REMOTE_MSG_MEETING_ROOM = "meetingRoom"



        /**
         * lay header
         */
        fun getRemoteMessageHeaders(): HashMap<String, String> {
            var headers = HashMap<String, String>()
            headers.put(Constants.REMOTE_MSG_AUTHORIZATION, "key=AAAA_M0h6F0:APA91bGntUrewudWgPxTDSXwBFvCpUFEZVgqSzlSlC2a-fa2m4rNusL0lUGm6mpAlQc9KHikvg8jO2P4cQkumIJIIqkvDrsx6abl2yT-zTfYi_PeP2b68W_6TfwwMOaThPrA_r0NyiZd")
            headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json")
            return headers
        }
    }
}