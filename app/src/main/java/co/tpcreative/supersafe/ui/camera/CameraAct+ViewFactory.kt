package co.tpcreative.supersafe.ui.camera

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import com.otaliastudios.cameraview.controls.Grid
import kotlinx.android.synthetic.main.activity_camera.*

fun CameraAct.iniUI(){
    if (camera != null) {
        camera?.addCameraListener(mCallback!!)
        camera?.setLifecycleOwner(this)
    }
    take_picture?.setOnClickListener(mOnClickListener)
    btnDone?.setOnClickListener(mOnClickListener)
    btnFlash?.setOnClickListener(mOnClickListener)
    btnSwitch?.setOnClickListener(mOnClickListener)
    setSupportActionBar(toolbar)
    val actionBar: ActionBar? = supportActionBar
    actionBar?.setDisplayShowTitleEnabled(false)
    try {
        val bundle: Bundle? = getIntent().getExtras()
        mainCategories = bundle?.get(getString(R.string.key_main_categories)) as MainCategoryModel
    } catch (e: Exception) {
        Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
    }
    btnAutoFocus?.visibility = View.INVISIBLE

    btnAutoFocus.setOnClickListener {
        if (camera != null) {
            if (camera?.getGrid() == Grid.OFF) {
                btnAutoFocus?.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN)
                camera?.grid = Grid.DRAW_3X3
            } else {
                btnAutoFocus?.setColorFilter(ContextCompat.getColor(this,themeApp?.getAccentColor()!!), PorterDuff.Mode.SRC_IN)
                camera?.grid = Grid.OFF
            }
        }
    }
}