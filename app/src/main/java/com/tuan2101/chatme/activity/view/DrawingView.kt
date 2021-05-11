package com.tuan2101.chatme.activity.view

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.tuan2101.chatme.R
import com.tuan2101.chatme.viewModel.Point
import com.tuan2101.chatme.viewModel.Segment
import kotlin.math.abs
import kotlin.math.roundToInt

class DrawingView(
    context: Context,
    var mFirebaseRef: DatabaseReference,
    var mScale: Float,
    private var mCanvasWith: Int,
    private var mCanvasHeight: Int
) : View(context) {
    private var mPaint: Paint
    private var mLastX: Int = 0
    private var mLastY: Int = 0
    private var mBuffer: Canvas? = null
    private lateinit var mBitmap: Bitmap
    private var mBitMapPaint: Paint
    private var mListener: ChildEventListener? = null
    private var mPath: Path = Path()
    private var mOutStandingSegments: Set<String> = HashSet()
    private lateinit var mCurrentSegment: Segment
    val currentColor: Long = 0xFFFF0000
    lateinit var drawSegment: Segment


    constructor(
        context: Context,
        mFirebaseRef: DatabaseReference
    ) : this(
        context,
        mFirebaseRef,
        1.0f,
        0,
        0
    )

    constructor(
        context: Context,
        mFirebaseRef: DatabaseReference,
        width: Int,
        height: Int
    ) : this(context, mFirebaseRef, 1.0f, width, height)

    constructor(
        context: Context,
        mFirebaseRef: DatabaseReference,
        scale: Float
    ) : this(context, mFirebaseRef, scale, 0, 0)

    init {

        mListener = mFirebaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var name: String? = snapshot.key

                if (!mOutStandingSegments.contains(name)) {
                    val segment = snapshot.getValue(Segment::class.java)

                    if (segment != null) {
                        drawSegment(segment, paintFromColor(R.color.black))
                    }

                    invalidate()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.setColor(currentColor)
        mPaint.style = Paint.Style.STROKE

        //??
        mBitMapPaint = Paint(Paint.DITHER_FLAG)

    }

    companion object {

        const val PIXEL_SIZE = 1

        /**
         * set mau
         */
        fun paintFromColor(color: Int): Paint {
            return paintFromColor(color, Paint.Style.STROKE)
        }

        fun paintFromColor(color: Int, style: Paint.Style): Paint {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.color = color
            paint.style = style
            return paint
        }

        /**
         * lay vector
         */
        fun getPathForPoint(points: List<Point>, _scale: Float): Path {
            val path = Path()
            var scale = _scale * PIXEL_SIZE
            var current = points[0]

            path.moveTo(
                Math.round(scale * current.getX()).toFloat(),
                Math.round(scale * current.getY()).toFloat()
            )
            var next: Point? = null


            for (i in 1 until points.size) {
                next = points.get(i)
                path.quadTo(
                    Math.round(scale * current.getX()).toFloat(),
                    Math.round(scale * current.getY()).toFloat(),
                    Math.round(scale * (next.getX() + current.getX()) / 2).toFloat(),
                    Math.round(scale * (next.getY() + current.getY()) / 2).toFloat()
                )
                current = next
            }
            if (next != null) {
                path.lineTo(
                    Math.round(scale * next.getX()).toFloat(),
                    Math.round(scale * next.getY()).toFloat()
                )
            }
            return path
        }

    }

    /**
     * ve duong
     */
    private fun drawSegment(segment: Segment, paint: Paint) {
        if (mBuffer != null) {
            mBuffer?.drawPath(getPathForPoint(segment.getPoints(), mScale), paint)
        }
    }

    fun cleanUp() {
        mListener?.let { mFirebaseRef.removeEventListener(it) }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mScale = Math.min(1.0f * w / mCanvasWith, 1.0f * h / mCanvasHeight)

        mBitmap = Bitmap.createBitmap(
            (mCanvasWith * mScale).roundToInt(),
            (mCanvasHeight * mScale).roundToInt(),
            Bitmap.Config.ARGB_8888
        )
        mBuffer = Canvas(mBitmap)

//        Toast.makeText(
//            context,
//            "\"onSizeChanged: created bitmap/buffer of ${mBitmap.width} x ${mBitmap.height}",
//            Toast.LENGTH_SHORT
//        ).show()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(Color.DKGRAY)
        canvas?.drawRect(
            0F, 0F, mBitmap.width.toFloat(), mBitmap.height.toFloat(), paintFromColor(
                Color.WHITE,
                Paint.Style.FILL_AND_STROKE
            )
        )
        canvas?.drawBitmap(mBitmap, 0f, 0f, mBitMapPaint)
        canvas?.drawPath(mPath, mPaint)
    }

    /**
     * bat dau ve va add point vao mCurrentSegment
     */
    fun onTouchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mCurrentSegment = Segment(currentColor)
        mLastX = (x / PIXEL_SIZE).toInt()
        mLastY = (y / PIXEL_SIZE).toInt()
        mCurrentSegment.addPoint(mLastX, mLastY)

        drawSegment = Segment(currentColor)

        drawSegment.addPoint((mLastX / mScale).roundToInt(), (mLastY / mScale).roundToInt())

        drawSegment(drawSegment, paintFromColor(R.color.violet))
    }


    /**
     * tinh toan toa do va vector de ve va add diem vao mCurrentSegment
     */
    fun onTouchMove(x: Float, y: Float) {
        val x1: Int = (x / PIXEL_SIZE).toInt()
        val y1: Int = (y / PIXEL_SIZE).toInt()

        val dx: Float = abs(x1 - mLastX).toFloat()
        val dy: Float = abs(y1 - mLastY).toFloat()

        if (dx >= 1 || dy >= 1) {
            mPath.quadTo(
                (mLastX * PIXEL_SIZE).toFloat(),
                (mLastY * PIXEL_SIZE).toFloat(),
                (((x1 + mLastX) * PIXEL_SIZE) / 2).toFloat(),
                (((y1 + mLastY) * PIXEL_SIZE) / 2).toFloat()
            )

            mLastX = x1
            mLastY = y1
            mCurrentSegment.addPoint(mLastX, mLastY)

            drawSegment.addPoint((mLastX / mScale).roundToInt(), (mLastY / mScale).roundToInt())

            drawSegment(drawSegment, paintFromColor(R.color.violet))
        }
    }

    fun onTouchEnd() {
        mPath.lineTo((mLastX * PIXEL_SIZE).toFloat(), (mLastY * PIXEL_SIZE).toFloat())
        mBuffer?.drawPath(mPath, mPaint)
        mPath.reset()

        val segmentReference = mFirebaseRef.push()

        val segmentName = segmentReference.key
        if (segmentName != null) {
            (mOutStandingSegments as HashSet<String>).add(segmentName)
        }

        //scale segment cho vua man hinh
        var segment = Segment(mCurrentSegment.getColor())
        for (point in mCurrentSegment.getPoints()) {
            segment.addPoint((point.getX() / mScale).roundToInt(),
                (point.getY() / mScale).roundToInt()
            )
        }

        //push segment len firebase dong thoi xoa ten segment ra khoi mOutStandingSegments de tranh bi lap lai (vi da push len firebase roi, luc do canvas se lay segment moi tren firebase de load vao canvas)
        segmentReference.setValue(segment)
            .addOnCompleteListener() {
                (mOutStandingSegments as HashSet<String>).remove(segmentName)
            }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float = event.x
        val y: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                onTouchEnd()
                invalidate()
            }
        }
        return true
    }


}
