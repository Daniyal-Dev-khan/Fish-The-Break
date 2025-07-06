package com.cp.fishthebreak.utils.recycler

import android.view.MotionEvent

interface OnActivityTouchListener {
    fun getTouchCoordinates(ev: MotionEvent?)
}