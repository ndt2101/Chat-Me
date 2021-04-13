package com.tuan2101.chatme.viewModel

class User {

    private var name: String = ""
    private var uid: String = ""
    private var status: String = ""
    private var avatar: String = ""
    private var search: String = ""

    constructor()

    constructor(name: String, uid: String, status: String, avatar: String, search: String) {
        this.name = name
        this.uid = uid
        this.status = status
        this.avatar = avatar
        this.search = search


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

        println("==================================================")
        println(" day la avatar  ${getAvatar()}")
        println("==================================================")
        return this.search
    }
}