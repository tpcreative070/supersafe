package co.tpcreative.supersafe.common.extension

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.MenuItem
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.ui.unlockalbum.initUI


fun MenuItem.setIconTint(color: Int) {
    val drawable: Drawable = this.icon
    drawable.mutate()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        drawable.colorFilter = BlendModeColorFilter(ContextCompat.getColor(SuperSafeApplication.getInstance(), color), BlendMode.SRC_ATOP)
    } else {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}