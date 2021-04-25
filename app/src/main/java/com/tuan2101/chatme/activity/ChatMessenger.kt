package com.tuan2101.chatme.activity

import java.io.Serializable

class ChatMessenger(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: Long, val fromName: String, val type: String, val img: String): Serializable {
    constructor() : this("", "", "", "", -1, "", "", "")
}
