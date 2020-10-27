package co.tpcreative.supersafe.common.util
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.io.*
import java.util.*


/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/13
 * desc  : utils about convert
</pre> *
 */
class ConvertUtils private constructor() {
    /**
     * Output stream to input stream.
     *
     * @param out The output stream.
     * @return input stream
     */
    fun output2InputStream(out: OutputStream?): ByteArrayInputStream? {
        return if (out == null) null else ByteArrayInputStream((out as ByteArrayOutputStream?)?.toByteArray())
    }

    companion object {
        private val hexDigits: CharArray? = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        /**
         * Bytes to bits.
         *
         * @param bytes The bytes.
         * @return bits
         */
        fun bytes2Bits(bytes: ByteArray?): String? {
            if (bytes == null || bytes.size == 0) return ""
            val sb = StringBuilder()
            for (aByte in bytes) {
                for (j in 7 downTo 0) {
                    sb.append(if (aByte.toInt() shl j and 0x01 == 0) '0' else '1')
                }
            }
            return sb.toString()
        }


        /**
         * Bits to bytes.
         *
         * @param bits The bits.
         * @return bytes
         */
        fun bits2Bytes(bits: String?): ByteArray? {
            var bits = bits
            val lenMod = bits?.length?.rem(8)
            var byteLen = bits?.length?.div(8)
            // add "0" until length to 8 times
            if (lenMod != 0) {
                if (lenMod != null) {
                    for (i in lenMod..7) {
                        bits = "0$bits"
                    }
                }
                byteLen?.let {
                    byteLen +=it
                }
            }
            val bytes = byteLen?.let { ByteArray(it) }
            for (i in 0 until byteLen!!) {
                for (j in 0..7) {
                    bytes?.set(i, (bytes[i].toInt() shl 1).toByte())
                    bytes?.set(i, ((bytes[i].toInt() or (bits?.get(i * 8 + j)?.minus('0'))!!.toByte().toInt()).toByte()))
                }
            }
            return bytes
        }

        /**
         * Bytes to chars.
         *
         * @param bytes The bytes.
         * @return chars
         */
        fun bytes2Chars(bytes: ByteArray?): CharArray? {
            if (bytes == null) return null
            val len = bytes.size
            if (len <= 0) return null
            val chars = CharArray(len)
            for (i in 0 until len) {
                chars[i] = (bytes[i].toInt() and 0xff) as Char
            }
            return chars
        }

        /**
         * Chars to bytes.
         *
         * @param chars The chars.
         * @return bytes
         */
        fun chars2Bytes(chars: CharArray?): ByteArray? {
            if (chars == null || chars.size <= 0) return null
            val len = chars.size
            val bytes = ByteArray(len)
            for (i in 0 until len) {
                bytes[i] = chars[i] as Byte
            }
            return bytes
        }

        /**
         * Bytes to hex string.
         *
         * e.g. bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns "00A8"
         *
         * @param bytes The bytes.
         * @return hex string
         */
        fun bytes2HexString(bytes: ByteArray?): String? {
            if (bytes == null) return ""
            val len = bytes.size
            if (len <= 0) return ""
            val ret = CharArray(len shl 1)
            var i = 0
            var j = 0
            while (i < len) {
                ret[j++] = hexDigits?.get(bytes[i].toInt() shr 4 and 0x0f)!!
                ret[j++] = hexDigits.get(bytes[i].toInt() and 0x0f)
                i++
            }
            return String(ret)
        }

        /**
         * Hex string to bytes.
         *
         * e.g. hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
         *
         * @param hexString The hex string.
         * @return the bytes
         */
        fun hexString2Bytes(hexString: String): ByteArray? {
            var hexString = hexString
            if (isSpace(hexString)) return null
            var len = hexString.length
            if (len % 2 != 0) {
                hexString = "0$hexString"
                len = len + 1
            }
            val hexBytes = hexString.toUpperCase(Locale.ROOT).toCharArray()
            val ret = ByteArray(len shr 1)
            var i = 0
            while (i < len) {
                ret[i shr 1] = (hex2Int(hexBytes[i]) shl 4 or hex2Int(hexBytes[i + 1])) as Byte
                i += 2
            }
            return ret
        }

        private fun hex2Int(hexChar: Char): Int {
            return if (hexChar >= '0' && hexChar <= '9') {
                hexChar - '0'
            } else if (hexChar >= 'A' && hexChar <= 'F') {
                hexChar - 'A' + 10
            } else {
                throw IllegalArgumentException()
            }
        }

        /**
         * Size of memory in unit to size of byte.
         *
         * @param memorySize Size of memory.
         * @param unit       The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of byte
         */
        fun memorySize2Byte(memorySize: Long,
                            @MemoryConstants.Unit unit: Int): Long {
            return if (memorySize < 0) -1 else memorySize * unit
        }

        /**
         * Size of byte to size of memory in unit.
         *
         * @param byteSize Size of byte.
         * @param unit     The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of memory in unit
         */
        fun byte2MemorySize(byteSize: Long,
                            @MemoryConstants.Unit unit: Int): Double {
            return if (byteSize < 0) (-1).toDouble() else byteSize.toDouble() / unit
        }

        /**
         * Size of byte to fit size of memory.
         *
         * to three decimal places
         *
         * @param byteSize Size of byte.
         * @return fit size of memory
         */
        @SuppressLint("DefaultLocale")
        fun byte2FitMemorySize(byteSize: Long): String? {
            return if (byteSize < 0) {
                "shouldn't be less than zero!"
            } else if (byteSize < MemoryConstants.KB) {
                String.format("%.1f B", byteSize.toDouble())
            } else if (byteSize < MemoryConstants.MB) {
                String.format("%.1f KB", byteSize.toDouble() / MemoryConstants.KB)
            } else if (byteSize < MemoryConstants.GB) {
                String.format("%.1f MB", byteSize.toDouble() / MemoryConstants.MB)
            } else {
                String.format("%.1f GB", byteSize.toDouble() / MemoryConstants.GB)
            }
        }

        /**
         * Time span in unit to milliseconds.
         *
         * @param timeSpan The time span.
         * @param unit     The unit of time span.
         *
         *  * [TimeConstants.MSEC]
         *  * [TimeConstants.SEC]
         *  * [TimeConstants.MIN]
         *  * [TimeConstants.HOUR]
         *  * [TimeConstants.DAY]
         *
         * @return milliseconds
         */
        fun timeSpan2Millis(timeSpan: Long, @TimeConstants.Unit unit: Int): Long {
            return timeSpan * unit
        }

        /**
         * Milliseconds to time span in unit.
         *
         * @param millis The milliseconds.
         * @param unit   The unit of time span.
         *
         *  * [TimeConstants.MSEC]
         *  * [TimeConstants.SEC]
         *  * [TimeConstants.MIN]
         *  * [TimeConstants.HOUR]
         *  * [TimeConstants.DAY]
         *
         * @return time span in unit
         */
        fun millis2TimeSpan(millis: Long, @TimeConstants.Unit unit: Int): Long {
            return millis / unit
        }

        /**
         * Milliseconds to fit time span.
         *
         * @param millis    The milliseconds.
         *
         * millis &lt;= 0, return null
         * @param precision The precision of time span.
         *
         *  * precision = 0, return null
         *  * precision = 1, return 天
         *  * precision = 2, return 天, 小时
         *  * precision = 3, return 天, 小时, 分钟
         *  * precision = 4, return 天, 小时, 分钟, 秒
         *  * precision &gt;= 5，return 天, 小时, 分钟, 秒, 毫秒
         *
         * @return fit time span
         */
        @SuppressLint("DefaultLocale")
        fun millis2FitTimeSpan(millis: Long, precision: Int): String? {
            var millis = millis
            var precision = precision
            if (millis <= 0 || precision <= 0) return null
            val sb = StringBuilder()
            val units = arrayOf<String?>("天", "小时", "分钟", "秒", "毫秒")
            val unitLen = intArrayOf(86400000, 3600000, 60000, 1000, 1)
            precision = Math.min(precision, 5)
            for (i in 0 until precision) {
                if (millis >= unitLen[i]) {
                    val mode = millis / unitLen[i]
                    millis -= mode * unitLen[i]
                    sb.append(mode).append(units[i])
                }
            }
            return sb.toString()
        }

        /**
         * Input stream to output stream.
         *
         * @param is The input stream.
         * @return output stream
         */
        fun input2OutputStream(`is`: InputStream?): ByteArrayOutputStream? {
            return if (`is` == null) null else try {
                val os = ByteArrayOutputStream()
                val b = ByteArray(MemoryConstants.KB)
                var len: Int
                while (`is`.read(b, 0, MemoryConstants.KB).also { len = it } != -1) {
                    os.write(b, 0, len)
                }
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Input stream to bytes.
         *
         * @param is The input stream.
         * @return bytes
         */
        fun inputStream2Bytes(`is`: InputStream?): ByteArray? {
            return if (`is` == null) null else input2OutputStream(`is`)?.toByteArray()
        }

        /**
         * Bytes to input stream.
         *
         * @param bytes The bytes.
         * @return input stream
         */
        fun bytes2InputStream(bytes: ByteArray?): InputStream? {
            return if (bytes == null || bytes.size <= 0) null else ByteArrayInputStream(bytes)
        }

        /**
         * Output stream to bytes.
         *
         * @param out The output stream.
         * @return bytes
         */
        fun outputStream2Bytes(out: OutputStream?): ByteArray? {
            return if (out == null) null else (out as ByteArrayOutputStream?)?.toByteArray()
        }

        /**
         * Bytes to output stream.
         *
         * @param bytes The bytes.
         * @return output stream
         */
        fun bytes2OutputStream(bytes: ByteArray?): OutputStream? {
            if (bytes == null || bytes.size <= 0) return null
            var os: ByteArrayOutputStream? = null
            return try {
                os = ByteArrayOutputStream()
                os.write(bytes)
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Input stream to string.
         *
         * @param is          The input stream.
         * @param charsetName The name of charset.
         * @return string
         */
        fun inputStream2String(`is`: InputStream?, charsetName: String): String? {
            return if (`is` == null || isSpace(charsetName)) "" else try {
                inputStream2Bytes(`is`)?.let {
                    String(it, charset(charsetName))
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to input stream.
         *
         * @param string      The string.
         * @param charsetName The name of charset.
         * @return input stream
         */
        fun string2InputStream(string: String?, charsetName: String): InputStream? {
            return if (string == null || isSpace(charsetName)) null else try {
                ByteArrayInputStream(string.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Output stream to string.
         *
         * @param out         The output stream.
         * @param charsetName The name of charset.
         * @return string
         */
        fun outputStream2String(out: OutputStream?, charsetName: String): String? {
            return if (out == null || isSpace(charsetName)) "" else try {
                outputStream2Bytes(out)?.let { String(it, charset(charsetName)) }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to output stream.
         *
         * @param string      The string.
         * @param charsetName The name of charset.
         * @return output stream
         */
        fun string2OutputStream(string: String?, charsetName: String): OutputStream? {
            return if (string == null || isSpace(charsetName)) null else try {
                bytes2OutputStream(string.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Bitmap to bytes.
         *
         * @param bitmap The bitmap.
         * @param format The format of bitmap.
         * @return bytes
         */
        fun bitmap2Bytes(bitmap: Bitmap?, format: Bitmap.CompressFormat?): ByteArray? {
            if (bitmap == null) return null
            val baos = ByteArrayOutputStream()
            bitmap.compress(format, 100, baos)
            return baos.toByteArray()
        }

        /**
         * Bytes to bitmap.
         *
         * @param bytes The bytes.
         * @return bitmap
         */
        fun bytes2Bitmap(bytes: ByteArray?): Bitmap? {
            return if (bytes == null || bytes.size == 0) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        /**
         * Drawable to bitmap.
         *
         * @param drawable The drawable.
         * @return bitmap
         */
        fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
            if (drawable is BitmapDrawable) {
                val bitmapDrawable: BitmapDrawable? = drawable as BitmapDrawable?
                if (bitmapDrawable?.getBitmap() != null) {
                    return bitmapDrawable?.getBitmap()
                }
            }
            val bitmap: Bitmap
            bitmap = if (drawable?.getIntrinsicWidth()!! <= 0 || drawable?.getIntrinsicHeight() <= 0) {
                Bitmap.createBitmap(1, 1,
                        if (drawable.getOpacity() != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            } else {
                Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        if (drawable.getOpacity() != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        /**
         * Bitmap to drawable.
         *
         * @param bitmap The bitmap.
         * @return drawable
         */
        fun bitmap2Drawable(bitmap: Bitmap?): Drawable? {
            return if (bitmap == null) null else BitmapDrawable(SuperSafeApplication.getInstance().getResources(), bitmap)
        }

        /**
         * Drawable to bytes.
         *
         * @param drawable The drawable.
         * @param format   The format of bitmap.
         * @return bytes
         */
        fun drawable2Bytes(drawable: Drawable?,
                           format: Bitmap.CompressFormat?): ByteArray? {
            return if (drawable == null) null else bitmap2Bytes(drawable2Bitmap(drawable), format)
        }

        /**
         * Bytes to drawable.
         *
         * @param bytes The bytes.
         * @return drawable
         */
        fun bytes2Drawable(bytes: ByteArray?): Drawable? {
            return if (bytes == null) null else bitmap2Drawable(bytes2Bitmap(bytes))
        }

        /**
         * View to bitmap.
         *
         * @param view The view.
         * @return bitmap
         */
        fun view2Bitmap(view: View?): Bitmap? {
            if (view == null) return null
            val ret: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(ret)
            val bgDrawable = view.background
            if (bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            view.draw(canvas)
            return ret
        }

        /**
         * Value of dp to value of px.
         *
         * @param dpValue The value of dp.
         * @return value of px
         */
        fun dp2px(dpValue: Float): Int {
            val scale: Float = SuperSafeApplication.Companion.getInstance().getResources().getDisplayMetrics().density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * Value of px to value of dp.
         *
         * @param pxValue The value of px.
         * @return value of dp
         */
        fun px2dp(pxValue: Float): Int {
            val scale: Float = SuperSafeApplication.getInstance().getResources().getDisplayMetrics().density
            return (pxValue / scale + 0.5f).toInt()
        }

        /**
         * Value of sp to value of px.
         *
         * @param spValue The value of sp.
         * @return value of px
         */
        fun sp2px(spValue: Float): Int {
            val fontScale: Float = SuperSafeApplication.getInstance().getResources().getDisplayMetrics().scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }

        /**
         * Value of px to value of sp.
         *
         * @param pxValue The value of px.
         * @return value of sp
         */
        fun px2sp(pxValue: Float): Int {
            val fontScale: Float = SuperSafeApplication.getInstance().getResources().getDisplayMetrics().scaledDensity
            return (pxValue / fontScale + 0.5f).toInt()
        }

        ///////////////////////////////////////////////////////////////////////////
        // other utils methods
        ///////////////////////////////////////////////////////////////////////////
        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            var i = 0
            val len = s.length
            while (i < len) {
                if (!Character.isWhitespace(s[i])) {
                    return false
                }
                ++i
            }
            return true
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}