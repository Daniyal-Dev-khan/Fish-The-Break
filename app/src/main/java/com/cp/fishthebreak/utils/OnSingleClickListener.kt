package com.cp.fishthebreak.utils

import android.os.Handler
import android.os.Looper
import android.view.View


class OnSingleClickListener(private val block: () -> Unit,private val wait: Long = 100) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        view.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed(Runnable { view.isEnabled = true }, wait)

        block()
    }
//    override fun onClick(view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < wait) {
//            return
//        }
//        lastClickTime = SystemClock.elapsedRealtime()
//
//        block()
//    }
}