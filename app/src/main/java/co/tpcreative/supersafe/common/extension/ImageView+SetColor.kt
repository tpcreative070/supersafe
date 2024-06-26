package tpcreative.co.qrscanner.common.extension
import android.graphics.BlendModeColorFilter
import android.os.Build
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import co.tpcreative.supersafe.model.EnumMode

fun AppCompatImageView.setColorFilter(color: Int, mode: EnumMode = EnumMode.SRC_ATOP) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(color, mode.getBlendMode())
    } else {
        @Suppress("DEPRECATION")
        setColorFilter(color, mode.getPorterDuffMode())
    }
}