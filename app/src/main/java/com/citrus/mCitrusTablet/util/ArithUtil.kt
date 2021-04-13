package com.citrus.mCitrusTablet.util

import java.math.BigDecimal

private const val DEF_DIV_SCALE = 10

/**
 * 提供精確的加法運算。
 *
 * @param v1 被加數
 * @param v2 加數
 * @return 兩個引數的和
 */
fun add(v1: Double, v2: Double): Double {
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())
    return b1.add(b2).toDouble()
}

/**
 * 提供精確的減法運算。
 *
 * @param v1 被減數
 * @param v2 減數
 * @return 兩個引數的差
 */
fun sub(v1: Double, v2: Double): Double {
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())
    return b1.subtract(b2).toDouble()
}

/**
 * 提供精確的乘法運算。
 *
 * @param v1 被乘數
 * @param v2 乘數
 * @return 兩個引數的積
 */
fun mul(v1: Double, v2: Double): Double {
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())
    return b1.multiply(b2).toDouble()
}

/**
 * 提供（相對）精確的除法運算，當發生除不盡的情況時，精確到
 * 小數點以後10位，以後的數字四捨五入。
 *
 * @param v1 被除數
 * @param v2 除數
 * @return 兩個引數的商
 */
fun div(v1: Double, v2: Double): Double {
    return div(v1, v2, DEF_DIV_SCALE)
}

/**
 * 提供（相對）精確的除法運算。當發生除不盡的情況時，由scale引數指
 * 定精度，以後的數字四捨五入。
 *
 * @param v1    被除數
 * @param v2    除數
 * @param scale 表示表示需要精確到小數點以後幾位。
 * @return 兩個引數的商
 */
fun div(v1: Double, v2: Double, scale: Int): Double {
    require(scale >= 0) { "The scale must be a positive integer or zero" }
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())
    return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
}

/**
 * 提供精確的小數位四捨五入處理。
 *
 * @param v     需要四捨五入的數字
 * @param scale 小數點後保留幾位
 * @return 四捨五入後的結果
 */
fun round(v: Double, scale: Int): Double {
    require(scale >= 0) { "The scale must be a positive integer or zero" }
    val b = BigDecimal(v.toString())
    val one = BigDecimal("1")
    return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toDouble()
}