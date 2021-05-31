package com.tuan2101.chatme.viewModel

import java.io.Serializable

class Group(private var name: String,
            private var id: String,
            private var avt: String,
            private var createTime: Long,
            private var members: List<User>,
            private var latestMessenger: GroupChatMessenger,
            private var adminId: String
) : Serializable {


    constructor() : this("","","",-1,ArrayList(), GroupChatMessenger(),"")

    fun getId(): String {
        return this.id
    }

    fun getName(): String {
        return this.name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getAvt(): String {
        return this.avt
    }

    fun setAvt(avt: String) {
        this.avt= avt
    }

    fun getCreateTime(): Long {
        return this.createTime
    }

    fun setCreateTime(createTime: Long) {
        this.createTime = createTime
    }

    fun getMembers(): List<User> {
        return this.members
    }

    fun setMembers(members: List<User>) {
        this.members = members
    }

    fun getLatestMessenger(): GroupChatMessenger {
        return this.latestMessenger
    }

    fun setLatestMessenger(latestMessenger: GroupChatMessenger) {
        this.latestMessenger = latestMessenger
    }

    fun getAdminId(): String {
        return this.adminId
    }

    fun setAdminId(adminId: String) {
        this.adminId = adminId
    }

}