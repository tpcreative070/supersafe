package co.tpcreative.supersafe.ui.camera
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import kotlinx.android.synthetic.main.activity_camera.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CameraAct : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    var mCurrentFlash = 1
    var isReload = false

    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.take_picture -> if (camera != null) {
                if (GalleryCameraMediaManager.getInstance()?.isProgressing()!!) {
                    Utils.Log(TAG, "Progressing")
                    return@OnClickListener
                }
                camera?.takePicture()
                GalleryCameraMediaManager.getInstance()?.setProgressing(true)
            }
            R.id.btnFlash -> if (camera != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.size
                FLASH_ICONS.get(mCurrentFlash).let { btnFlash?.setImageResource(it) }
                camera?.flash = FLASH_OPTIONS[mCurrentFlash]
            }
            R.id.btnSwitch -> if (camera != null) {
                val facing: Facing = camera?.facing!!
                if (facing == Facing.FRONT) {
                    camera?.facing = Facing.BACK
                } else {
                    camera?.facing = Facing.FRONT
                }
            }
            R.id.btnDone -> {
                ServiceManager.getInstance()?.onPreparingSyncData()
                onBackPressed()
            }
        }
    }
    var mainCategories: MainCategoryModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        iniUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        if (camera != null) {
            camera?.open()
        }
        GalleryCameraMediaManager.getInstance()?.setProgressing(false)
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        if (camera != null) {
            camera?.destroy()
        }
        EventBus.getDefault().unregister(this)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onPause() {
        Utils.Log(TAG, "onPause")
        super.onPause()
        if (camera != null) {
            camera?.close()
        }
    }

    override fun onBackPressed() {
        if (isReload) {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            Utils.Log(TAG, "onBackPressed")
        }
        super.onBackPressed()
    }

    val mCallback: CameraListener = object : CameraListener() {
        override fun onPictureTaken(result: com.otaliastudios.cameraview.PictureResult) {
            super.onPictureTaken(result)
            val mData: ByteArray = result.data
            if (mainCategories == null) {
                Utils.Log(TAG, "Local id is null")
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                return
            }
            isReload = true
            result.toBitmap {
                it?.let {mBitmap ->
                    onReviewPicture(mBitmap)
                }
            }
            ServiceManager.getInstance()?.onSaveDataOnCamera(mData, mainCategories)
            Utils.Log(TAG, "take picture")
        }
    }
    companion object {
        private val FLASH_OPTIONS: Array<Flash> = arrayOf(
                Flash.AUTO,
                Flash.OFF,
                Flash.ON)
        private val FLASH_ICONS: IntArray = intArrayOf(
                R.drawable.ic_flash_auto,
                R.drawable.ic_flash_off,
                R.drawable.ic_flash_on)
    }
}