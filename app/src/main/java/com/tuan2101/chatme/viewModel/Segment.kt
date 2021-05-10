package com.tuan2101.chatme.viewModel

class Segment(private var color: Long) {
    private val points: List<Point> = ArrayList()

    constructor() : this(0xFFFF0000)

    fun addPoint(x: Int, y: Int) {
        val point = Point(x,y)
        (points as ArrayList<Point>).add(point)
    }

    fun getPoints(): List<Point> {
        return points
    }

    fun getColor(): Long {
        return color
    }

}