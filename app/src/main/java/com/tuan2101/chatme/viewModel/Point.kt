package com.tuan2101.chatme.viewModel

class Point(private val x: Int,private val y: Int) {

    constructor() : this(0,0)

    fun getX(): Int {
        return x
    }

    fun getY(): Int {
        return y
    }
}