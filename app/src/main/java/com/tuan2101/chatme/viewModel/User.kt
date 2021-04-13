package com.tuan2101.chatme.viewModel

class User(private var name: String, private var uid: String, private var status: String, private var avatar: String, private var search: String) {

    fun setUid(uid: String) {
        this.uid = uid
    }

    fun getUid(): String {
        return this.uid
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getName(): String {
        return this.name
    }

    fun setAvatar(avatar: String) {
        this.avatar = avatar
    }

    fun getAvatar(): String {
        return this.avatar
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun getStatus(): String {
        return this.status
    }

    fun setSearch(search: String) {
        this.search = search
    }

    fun getSearch(): String {
        return this.search
    }
}