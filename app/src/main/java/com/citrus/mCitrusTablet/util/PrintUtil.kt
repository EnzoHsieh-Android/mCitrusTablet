package com.citrus.mCitrusTablet.util


import com.citrus.mCitrusTablet.di.prefs
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*

enum class FontSize {
    Normal,
    Big,
    Height,
    Width
}

enum class PrintAction {
    Send,
    Payment,
    ReprintReceipt,
    ReprintNumTicket,
    ReprintSummary,
    ReprintLabel,
    ReprintInvoice,
    ReprintOrgInvoice,
    Void,
    Invoice,
    Report
}

//Bytes merge
fun b(bytesA: ByteArray, bytesB: ByteArray): ByteArray {
    val bytes = ByteArray(bytesA.size + bytesB.size)
    System.arraycopy(bytesA, 0, bytes, 0, bytesA.size)
    System.arraycopy(bytesB, 0, bytes, bytesA.size, bytesB.size)
    return bytes
}


fun getSplitBytes(bytes: ByteArray): List<ByteArray> {
    val unit = 600
    val byteList = mutableListOf<ByteArray>()
    if (bytes.size > unit) {
        val cnt = bytes.size / unit
        val remainder = bytes.size % unit
        for (i in 0 until cnt) {
            byteList.add(bytes.copyOfRange(i * unit, (i + 1) * unit))
        }
        byteList.add(bytes.copyOfRange(cnt * unit, cnt * unit + remainder))
    } else {
        byteList.add(bytes)
    }
    return byteList
}

fun initCmd(): ByteArray {
    return byteArrayOf(0x1B.toByte(), 0x40.toByte())
}

fun openCashDrawer(): ByteArray {
    return byteArrayOf(0x1B.toByte(), 0x70.toByte(), 0x0.toByte(), 0x3C.toByte(), 0xFF.toByte())
}

fun pageMode(): ByteArray {
    val pageMode = byteArrayOf(0x1B.toByte(), 0x4C.toByte())
    val motion = byteArrayOf(0x1d.toByte(), 0x50.toByte(), 0x00.toByte(), 0xCB.toByte())
    //Set print area in page mode
    val area = byteArrayOf(
        0x1B.toByte(),
        0x57.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0xA0.toByte(),
        0x01.toByte(),
        0x58.toByte(),
        0x02.toByte()
    )
    return b(b(pageMode, motion), area)
}

fun pageModePrint(): ByteArray {
    return byteArrayOf(0x0C.toByte())
}

//fun setLineSpace(): ByteArray {
//    return if (prefs.isLargeLineSpacing) byteArrayOf(0x1B.toByte(), 0x33.toByte(), 0x45.toByte())
//    else byteArrayOf(0x1B.toByte(), 0x32.toByte())
//}

fun setLineSpace(size:Int): ByteArray {
    return byteArrayOf(0x1B.toByte(), 0x33.toByte(), size.toByte())
}

fun cutPaperCmd(): ByteArray {
    return byteArrayOf(0x1D.toByte(), 0x56.toByte(), 0x42.toByte(), 0x00.toByte())
//    return byteArrayOf(0x1D.toByte(), 0x6D.toByte())
}

fun alignCmd(alignMode: Int): ByteArray {
    return byteArrayOf(0x1b.toByte(), 0x61.toByte(), alignMode.toByte())
}

fun fontSizeCmd(fontSize: FontSize): ByteArray {
    val data = byteArrayOf(0x1d.toByte(), 0x21.toByte(), 0x0.toByte())
    when (fontSize) {
        FontSize.Normal -> {
            data[2] = 0x00.toByte()
        }
        FontSize.Big -> {
            data[2] = 0x11.toByte()
        }
        FontSize.Height -> {
            data[2] = 0x01.toByte()
        }
        FontSize.Width -> {
            data[2] = 0x10.toByte()
        }
    }
    return data
}

fun boldCmd(isBold: Boolean): ByteArray {
    return byteArrayOf(0x1b.toByte(), 0x45.toByte(), (if (isBold) 0x01 else 0x00).toByte())
}


fun text(text: String): ByteArray {
    return (text + "\n").toByteArray(charset(prefs.charSet))
}

fun textNoLineup(text: String): ByteArray {
    return (text).toByteArray(charset(prefs.charSet))
}

//fun line(int: Int): ByteArray {
//    return byteArrayOf(0x1b.toByte(), 0x4A.toByte(), int.toByte())
//}

fun dashLine(is80mm: Boolean): ByteArray {
    return (if (is80mm) "------------------------------------------------" else "--------------------------------" + "\n").toByteArray(charset(prefs.charSet))
}

fun getBarcodeCmd(barcode: String): ByteArray {
    var data: ByteArray

    // 設置HRI的位置，02表示下方
    val hriPosition = byteArrayOf(0x1d.toByte(), 0x48.toByte(), 0x00.toByte())
    // 最後一個參數表示寬度 取值範圍 1-6 如果條碼超長則無法打印
    val width = byteArrayOf(0x1d.toByte(), 0x77.toByte(), 0x01.toByte())
    val height = byteArrayOf(0x1d.toByte(), 0x68.toByte(), 0x30.toByte())
    // 39 是0x04
//        byte[] barcodeType = {(byte) 0x1d, (byte) 0x6b, (byte) 0x04};
    val barcodePrint = byteArrayOf(0x1d.toByte(), 0x6b.toByte(), 0x45.toByte(), 0x13.toByte())
    val print = byteArrayOf(10.toByte(), 0.toByte())

    //參考:使用 EPSON TM 列印電子發票常用指令集
    data = b(hriPosition, width)
    data = b(data, height)
    data = b(data, barcodePrint)
    data = b(data, barcode.toByteArray())
    return data
}

fun twoQrCode(qrCode: String, qrCode2: String): ByteArray {
    val storeLen = qrCode.toByteArray(StandardCharsets.UTF_8).size + 3
    val storePL = (storeLen % 256).toByte()
    val storePH = (storeLen / 256).toByte()
    val storeLen2 = qrCode2.toByteArray(StandardCharsets.UTF_8).size + 3
    val storePL2 = (storeLen2 % 256).toByte()
    val storePH2 = (storeLen2 / 256).toByte()
    val modelQR = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), 0x04.toByte(), 0x00.toByte(), 0x31.toByte(), 0x41.toByte(), 0x32.toByte(), 0x00.toByte()
    )
    val sizeQR = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), 0x03.toByte(), 0x00.toByte(), 0x31.toByte(), 0x43.toByte(), 0x03.toByte()
    )
    val errorQR = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), 0x03.toByte(), 0x00.toByte(), 0x31.toByte(), 0x45.toByte(), 0x31.toByte()
    )
    val storeQR = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), storePL, storePH, 0x31.toByte(), 0x50.toByte(), 0x30.toByte()
    )
    val storeQR2 = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), storePL2, storePH2, 0x31.toByte(), 0x50.toByte(), 0x30.toByte()
    )
    val printQR = byteArrayOf(
        0x1d.toByte(),
        0x28.toByte(), 0x6b.toByte(), 0x03.toByte(), 0x00.toByte(), 0x31.toByte(), 0x51.toByte(), 0x30.toByte()
    )
    val verticalPosition = byteArrayOf(0x1d.toByte(), 0x24.toByte(), 150.toByte(), 1.toByte())
    val position = byteArrayOf(0x1B.toByte(), 0x24.toByte(), 20.toByte(), 0.toByte())
    val position2 = byteArrayOf(0x1B.toByte(), 0x24.toByte(), 360.toByte(), 0.toByte())
    var data = verticalPosition
    data = b(data, position)
    data = b(data, modelQR)
    data = b(data, sizeQR)
    data = b(data, errorQR)
    data = b(data, storeQR)
    data = b(data, qrCode.toByteArray(StandardCharsets.UTF_8))
    data = b(data, printQR)
    data = b(data, "    ".toByteArray())
    data = b(data, verticalPosition)
    //        data = b(data, position2);
    data = b(data, modelQR)
    data = b(data, sizeQR)
    data = b(data, errorQR)
    data = b(data, storeQR2)
    data = b(data, qrCode2.toByteArray(StandardCharsets.UTF_8))
    data = b(data, printQR)
    return data
}

fun twoColumn(title: String, content: String, is80mm: Boolean): ByteArray {
    return (fillBlank(title, content, 12, 24, if (is80mm) 48 else 32) + "\n").toByteArray(charset(prefs.charSet))
}

fun twoColumnBig(title: String, content: String, is80mm: Boolean): ByteArray {
    return (fillBlank(title, content, 12, 24, if (is80mm) 24 else 16) + "\n").toByteArray(charset(prefs.charSet))
}

fun fillBlank(title: String, content: String, enUnit: Int, cnUnit: Int, totalLen: Int): String {
    val w = mul(enUnit.toDouble(), totalLen.toDouble()).toInt()
    val p1: Int = getStringPixLength(title, enUnit, cnUnit)
    val p2: Int = getStringPixLength(content, enUnit, cnUnit)
    var blankPixel = 0
    if (p1 + p2 <= w) {
        blankPixel = div((w - p1 - p2).toDouble(), enUnit.toDouble(), 0).toInt()
    } else if (p1 + p2 > w && p1 + p2 <= w * 2) {
        blankPixel = div((w * 2 - p1 - p2).toDouble(), enUnit.toDouble(), 0).toInt()
    } else if (p1 + p2 > w * 2 && p1 + p2 <= w * 3) {
        blankPixel = div((w * 3 - p1 - p2).toDouble(), enUnit.toDouble(), 0).toInt()
    } else if (p1 + p2 > w * 4 && p1 + p2 <= w * 4) {
        blankPixel = div((w * 4 - p1 - p2).toDouble(), enUnit.toDouble(), 0).toInt()
    }
    var blankStr = ""
    for (i in 0 until blankPixel) {
        blankStr += " "
    }
    return title + blankStr + content
}

fun getStringPixLength(str: String, enSize: Int, chSize: Int): Int {
    var pixLength = 0
    var c: Char
    val fullSizeSymbol: MutableList<String> = ArrayList()
    fullSizeSymbol.add("（")
    fullSizeSymbol.add("）")
    fullSizeSymbol.add("±")
    fullSizeSymbol.add("「")
    fullSizeSymbol.add("」")
    fullSizeSymbol.add("“")
    fullSizeSymbol.add("】")
    fullSizeSymbol.add("【")
    fullSizeSymbol.add("＆")
    fullSizeSymbol.add("％")
    fullSizeSymbol.add("＄")
    fullSizeSymbol.add("＠")
    fullSizeSymbol.add("！")
    fullSizeSymbol.add("～")
    fullSizeSymbol.add("＝")
    fullSizeSymbol.add("、")
    fullSizeSymbol.add("。")
    fullSizeSymbol.add("，")
    fullSizeSymbol.add("？")
    fullSizeSymbol.add("｜")
    fullSizeSymbol.add("・")
    fullSizeSymbol.add("＿")
    fullSizeSymbol.add("－")
    fullSizeSymbol.add("——")
    fullSizeSymbol.add("×")
    for (element in str) {
        c = element
        pixLength += if (isCJK(c.toString()) || fullSizeSymbol.contains(c.toString())) {
            chSize
        } else {
            enSize
        }
    }
    return pixLength
}

//TODO API24 can use
//fun containsHanScript(s: String): Boolean {
//    return s.codePoints().anyMatch { codepoint: Int -> Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN }
//}

fun isCJK(str: String): Boolean {
    val length = str.length
    for (i in 0 until length) {
        val ch = str[i]
        val block = Character.UnicodeBlock.of(ch)
        if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS == block || Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS == block || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A == block) {
            return true
        }
    }
    return false
}

fun isConnected(socket: Socket?): Boolean {
    return socket?.inetAddress?.isReachable(200) ?: false
}


