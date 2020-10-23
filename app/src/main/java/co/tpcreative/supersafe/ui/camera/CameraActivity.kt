package co.tpcreative.supersafe.ui.camera
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Grid
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CameraActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var mCurrentFlash = 0
    private var isReload = false

    @BindView(R.id.camera)
    var mCameraView: CameraView? = null

    @BindView(R.id.btnSwitch)
    var btnSwitch: AppCompatImageButton? = null

    @BindView(R.id.btnDone)
    var btnDone: AppCompatButton? = null

    @BindView(R.id.btnFlash)
    var btnFlash: AppCompatImageButton? = null

    @BindView(R.id.btnAutoFocus)
    var btnAutoFocus: AppCompatImageButton? = null

    @BindView(R.id.take_picture)
    var take_picture: FloatingActionButton? = null
    private val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
    private val mOnClickListener: View.OnClickListener? = View.OnClickListener { v ->
        when (v.id) {
            R.id.take_picture -> if (mCameraView != null) {
                if (GalleryCameraMediaManager.Companion.getInstance()?.isProgressing()!!) {
                    Utils.Log(TAG, "Progressing")
                }
                mCameraView?.takePicture()
                GalleryCameraMediaManager.Companion.getInstance()?.setProgressing(true)
            }
            R.id.btnFlash -> if (mCameraView != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS?.size!!
                FLASH_ICONS?.get(mCurrentFlash)?.let { btnFlash?.setImageResource(it) }
                mCameraView?.setFlash(FLASH_OPTIONS?.get(mCurrentFlash))
            }
            R.id.btnSwitch -> if (mCameraView != null) {
                val facing: Facing = mCameraView?.getFacing()!!
                if (facing == Facing.FRONT) {
                    mCameraView?.setFacing(Facing.BACK)
                } else {
                    mCameraView?.setFacing(Facing.FRONT)
                }
            }
            R.id.btnDone -> {
                ServiceManager.Companion.getInstance()?.onPreparingSyncData()
                onBackPressed()
            }
        }
    }
    private var mainCategories: MainCategoryModel? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (mCameraView != null) {
            mCameraView?.addCameraListener(mCallback!!)
            mCameraView?.setLifecycleOwner(this)
        }
        take_picture?.setOnClickListener(mOnClickListener)
        btnDone?.setOnClickListener(mOnClickListener)
        btnFlash?.setOnClickListener(mOnClickListener)
        btnSwitch?.setOnClickListener(mOnClickListener)
        val toolbar = findViewById<View?>(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = getSupportActionBar()
        actionBar?.setDisplayShowTitleEnabled(false)
        try {
            val bundle: Bundle? = getIntent().getExtras()
            mainCategories = bundle?.get(getString(R.string.key_main_categories)) as MainCategoryModel
        } catch (e: Exception) {
            Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
        }
        btnAutoFocus?.setVisibility(View.INVISIBLE)
    }

    @OnClick(R.id.btnAutoFocus)
    fun onClickedFocus(view: View?) {
        if (mCameraView != null) {
            if (mCameraView?.getGrid() == Grid.OFF) {
                btnAutoFocus?.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN)
                mCameraView?.setGrid(Grid.DRAW_3X3)
            } else {
                btnAutoFocus?.setColorFilter(ContextCompat.getColor(this,themeApp?.getAccentColor()!!), PorterDuff.Mode.SRC_IN)
                mCameraView?.setGrid(Grid.OFF)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        if (mCameraView != null) {
            mCameraView?.open()
        }
        //        if (mCameraView.getAutoFocus()){
//            btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(themeApp.getAccentColor()), android.graphics.PorterDuff.Mode.SRC_IN);
//        }
//        else{
//            btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
//        }
        GalleryCameraMediaManager.getInstance()?.setProgressing(false)
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        if (mCameraView != null) {
            mCameraView?.destroy()
        }
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onPause() {
        Utils.Log(TAG, "onPause")
        super.onPause()
        if (mCameraView != null) {
            mCameraView?.close()
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

    private val mCallback: CameraListener? = object : CameraListener() {
        override fun onCameraOpened(options: com.otaliastudios.cameraview.CameraOptions) {
            super.onCameraOpened(options)
        }

        override fun onPictureTaken(result: com.otaliastudios.cameraview.PictureResult) {
            super.onPictureTaken(result)
            val data: ByteArray = result.getData()
            android.widget.Toast.makeText(this@CameraActivity, R.string.picture_taken, Toast.LENGTH_SHORT).show()
            if (mainCategories == null) {
                Utils.Log(TAG, "Local id is null")
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                return
            }
            isReload = true
            ServiceManager.getInstance()?.onSaveDataOnCamera(data, mainCategories)
            Utils?.Log(TAG, "take picture")
        }
    } //    private CameraView.Callback mCallback

    //            = new CameraView.Callback() {
    //        @Override
    //        public void onCameraOpened(CameraView cameraView) {
    //            Utils.Log(TAG, "onCameraOpened");
    //        }
    //        @Override
    //        public void onCameraClosed(CameraView cameraView) {
    //            Utils.Log(TAG, "onCameraClosed");
    //        }
    //
    //        @Override
    //        public void onPictureTaken(CameraView cameraView, final byte[] data,int orientation) {
    //            Utils.Log(TAG, "onPictureTaken " + data.length);
    //            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
    //            if (mainCategories==null){
    //                Utils.Log(TAG, "Local id is null");
    //                Utils.onWriteLog("Main categories is null",EnumStatus.WRITE_FILE);
    //                return;
    //            }
    //            isReload = true;
    //            ServiceManager.getInstance().onSaveDataOnCamera(data,mainCategories);
    //        }
    //    };
    companion object {
        private val TAG = CameraActivity::class.java.simpleName
        private val FLASH_OPTIONS: Array<Flash>? = arrayOf<Flash>(
                Flash.AUTO,
                Flash.OFF,
                Flash.ON)
        private val FLASH_ICONS: IntArray? = intArrayOf(
                R.drawable.ic_flash_auto,
                R.drawable.ic_flash_off,
                R.drawable.ic_flash_on)
    }
}