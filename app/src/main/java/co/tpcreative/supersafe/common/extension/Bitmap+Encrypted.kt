package co.tpcreative.supersafe.common.extension

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toByteArray() : ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}