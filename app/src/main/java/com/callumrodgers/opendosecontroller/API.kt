package com.callumrodgers.opendosecontroller

import android.os.Build

object API {
    fun isAtLeast(level: Int): Boolean {
        return Build.VERSION.SDK_INT >= level
    }
}