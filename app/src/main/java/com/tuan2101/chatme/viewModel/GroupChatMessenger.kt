package com.tuan2101.chatme.viewModel

import java.io.Serializable

class GroupChatMessenger(val id: String, val text: String, val fromId: String, val toId: String, var timeStamp: Long, val fromName: String, val type: String, val img: String, val fromUserAvt: String, var status: String):
    Serializable {
    constructor() : this("", "", "", "", -1, "", "", "", "","")
}