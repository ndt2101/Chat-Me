package com.tuan2101.chatme.viewModel

class ChatMessenger(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: Long, val fromName: String, val type: String, val img: String) {
    constructor() : this("", "", "", "", -1, "", "", "")
}
