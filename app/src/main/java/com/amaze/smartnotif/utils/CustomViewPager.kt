package com.amaze.smartnotif.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var enabledTouch: Boolean = false

    init {
        this.enabledTouch = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabledTouch) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabledTouch) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    fun setPagingEnabled(enabled: Boolean) {
        this.enabledTouch = enabled
    }

}