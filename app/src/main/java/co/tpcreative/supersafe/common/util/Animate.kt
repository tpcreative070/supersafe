package co.tpcreative.supersafe.common.util
import android.annotation.TargetApi
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView

/**
 * Created by Arcane on 7/23/2017.
 */
object Animate {
    @TargetApi(Build.VERSION_CODES.M)
    fun animate(view: AppCompatImageView?, scanFingerprint: AnimatedVectorDrawable?) {
        view?.setImageDrawable(scanFingerprint)
        scanFingerprint?.start()
    }
}