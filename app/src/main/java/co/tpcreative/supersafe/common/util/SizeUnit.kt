package co.tpcreative.supersafe.common.util

import java.text.DecimalFormat

enum class SizeUnit(private val inBytes: Long) {
    B(1), KB(SizeUnit.BYTES.toLong()), MB((SizeUnit.BYTES * SizeUnit.BYTES).toLong()), GB((SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES).toLong()), TB((SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES * SizeUnit.BYTES).toLong());

    fun inBytes(): Long {
        return inBytes
    }

    companion object {
        private const val BYTES = 1024
        fun readableSizeUnit(bytes: Long): String {
            val df = DecimalFormat("0.00")
            return if (bytes < KB.inBytes()) {
                df.format((bytes / B.inBytes().toFloat()).toDouble()) + " B"
            } else if (bytes < MB.inBytes()) {
                df.format((bytes / KB.inBytes().toFloat()).toDouble()) + " KB"
            } else if (bytes < GB.inBytes()) {
                df.format((bytes / MB.inBytes().toFloat()).toDouble()) + " MB"
            } else {
                df.format((bytes / GB.inBytes().toFloat()).toDouble()) + " GB"
            }
        }
    }
}
