package com.musclegain.tracker.core.domain.calc

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.roundTo(scale: Int): Double =
    BigDecimal.valueOf(this).setScale(scale, RoundingMode.HALF_UP).toDouble()

fun sumKnown(values: Iterable<Double?>): Double? {
    var total = BigDecimal.ZERO
    var any = false
    for (value in values) {
        if (value != null) {
            total += BigDecimal.valueOf(value)
            any = true
        }
    }
    return if (any) total.setScale(2, RoundingMode.HALF_UP).toDouble() else null
}
