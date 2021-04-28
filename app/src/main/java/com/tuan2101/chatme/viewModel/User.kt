package com.tuan2101.chatme.viewModel

import java.io.Serializable


class User() : Serializable {

    private var name: String = ""
    private var uid: String = ""
    private var status: String = ""
    private var avatar: String = ""
    private var coverImage: String = ""
    private var introduceYourself: String = ""
    private var work: String = ""
    private var homeTown: String = ""
    private var search: String = ""
    private var groups: HashMap<String, Any> = HashMap()
    private var token: String? = null


    constructor(name: String,
                uid: String,
                status: String,
                avatar: String,
                coverImage: String,
                introduceYourself: String,
                work: String,
                homeTown: String,
                search: String,
                groups: HashMap<String, Any>,
                token: String
    ) : this() {
        this.name = name
        this.uid = uid
        this.status = status
        this.avatar = avatar
        this.coverImage = coverImage
        this.introduceYourself = introduceYourself
        this.work = work
        this.homeTown = homeTown
        this.search = search
        this.groups = groups
        this.token = token
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

    fun setCoverImage(coverImage: String) {
        this.coverImage = coverImage
    }

    fun getCoverImage(): String {
        return this.coverImage
    }

    fun setIntroduceYourself(introduceYourself: String) {
        this.introduceYourself = introduceYourself
    }

    fun getIntroduceYourself(): String {
        return this.introduceYourself
    }

    fun setWork(work: String) {
        this.work = work
    }

    fun getWork(): String {
        return this.work
    }

    fun setHomeTown(homeTown: String) {
        this.homeTown = homeTown
    }

    fun getHomeTown(): String {
        return this.homeTown
    }

    fun getGroups(): HashMap<String, Any> {
        return this.groups
    }

    fun setGroup(groups: HashMap<String, Any>) {
        this.groups = groups
    }

    fun getToken(): String? {
        return this.token
    }

    fun setToken(token: String) {
        this.token = token
    }
}