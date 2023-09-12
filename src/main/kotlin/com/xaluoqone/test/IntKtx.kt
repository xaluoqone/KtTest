package com.xaluoqone.test

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun Int.processOf(minValue: Int, maxValue: Int): Float {
    return (this + minValue.absoluteValue) / (minValue.absoluteValue + maxValue.absoluteValue).toFloat()
}

fun Float.valueOf(minValue: Int, maxValue: Int): Int {
    return (this * (minValue.absoluteValue + maxValue.absoluteValue) - minValue.absoluteValue).roundToInt()
}