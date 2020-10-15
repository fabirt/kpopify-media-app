package com.fabirt.kpopify.core.util

import android.content.res.Resources

/** Convert this [Int] to dp representation. */
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

/** Convert this [Float] to dp representation. */
val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
