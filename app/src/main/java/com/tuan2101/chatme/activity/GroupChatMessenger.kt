package com.tuan2101.chatme.activity

import com.tuan2101.chatme.viewModel.Group
import com.tuan2101.chatme.viewModel.User
import java.io.Serializable

class GroupChatMessenger(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: Long, val fromName: String, val type: String, val img: String, val fromUserAvt: String):
    Serializable {
    constructor() : this("", "", "", "", -1, "", "", "", "")
}