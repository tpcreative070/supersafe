package co.tpcreative.supersafe.common.activity
import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import butterknife.ButterKnife
import butterknife.Unbinder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.hiddencamera.*
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage
import spencerstudios.com.bungeelib.Bungee
import java.io.File

abstract class BaseVerifyPinActivity : AppCompatActivity(), CameraCallbacks, SensorFaceUpDownChangeNotifier.Listener {
    var unbinder: Unbinder? = null
    protected var actionBar: ActionBar? = null
    protected var storage: Storage? = null
    /*Hidden camera*/
    private var mCameraPreview: CameraPreview? = null
    private var mCachedCameraConfig: CameraConfig? = null
    var onStartCount = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = getSupportActionBar()
        storage = Storage(this)
        //Add the camera preview surface to the root of the activity view.
        mCameraPreview = addPreView()
        if (savedInstanceState == null) {
            Bungee.fade(this)
        } else {
            onStartCount = 2
        }
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true)
        }
        return theme
    }

    protected fun onFaceDown(isFaceDown: Boolean) {
        if (isFaceDown) {
            val result: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            if (result) {
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance())
            }
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        unbinder = ButterKnife.bind(this)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        stopCamera()
        unbinder?.unbind()
    }

    protected override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.Companion.getInstance()?.remove(this)
        stopCamera()
    }

    protected override fun onStop() {
        super.onStop()
    }

    protected override fun onResume() {
        SensorFaceUpDownChangeNotifier.Companion.getInstance()?.addListener(this)
        Utils.Log(TAG, "Action here........onResume")
        if (mCachedCameraConfig != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return
            }
            startCamera(mCachedCameraConfig)
        }
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onStart() {
        super.onStart()
        if (SingletonManager.Companion.getInstance().isAnimation()) {
            if (onStartCount > 1) {
                Bungee.fade(this)
            } else if (onStartCount == 1) {
                onStartCount++
            }
        } else {
            Bungee.zoom(this)
            SingletonManager.Companion.getInstance().setAnimation(true)
        }
    }
    /*Hidden camera*/
    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return [CameraPreview] that was added to the view.
     */
    private fun addPreView(): CameraPreview? {
        //create fake camera view
        val cameraSourceCameraPreview = CameraPreview(this, this)
        cameraSourceCameraPreview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val view: View = (getWindow().getDecorView().getRootView() as ViewGroup).getChildAt(0)
        if (view is LinearLayout) {
            val linearLayout: LinearLayout = view as LinearLayout
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(1, 1)
            linearLayout.addView(cameraSourceCameraPreview, params)
        } else if (view is RelativeLayout) {
            val relativeLayout: RelativeLayout = view as RelativeLayout
            val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(1, 1)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            relativeLayout.addView(cameraSourceCameraPreview, params)
        } else if (view is FrameLayout) {
            val frameLayout: FrameLayout = view as FrameLayout
            val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(1, 1)
            frameLayout.addView(cameraSourceCameraPreview, params)
        } else {
            throw RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout")
        }
        return cameraSourceCameraPreview
    }

    /**
     * Start the hidden camera. Make sure that you check for the runtime permissions before you start
     * the camera.
     *
     * @param cameraConfig camera configuration [CameraConfig]
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    protected fun startCamera(cameraConfig: CameraConfig?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { //check if the camera permission is available
            onCameraError(CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
        } else if (cameraConfig?.getFacing() == CameraFacing.Companion.FRONT_FACING_CAMERA
                && !HiddenCameraUtils.isFrontCameraAvailable(this)) {   //Check if for the front camera
            onCameraError(CameraError.Companion.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
        } else {
            mCachedCameraConfig = cameraConfig
            if (cameraConfig != null) {
                mCameraPreview?.startCameraInternal(cameraConfig)
            }
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using [.startCamera] before using this function.
     */
    protected fun takePicture() {
        if (mCameraPreview != null) {
            if (mCameraPreview?.isSafeToTakePictureInternal()!!) {
                mCameraPreview?.takePictureInternal()
            }
        } else {
            throw RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.")
        }
    }

    /**
     * Stop and release the camera forcefully.
     */
    protected fun stopCamera() {
        mCachedCameraConfig = null //Remove config.
        if (mCameraPreview != null) {
            mCameraPreview?.stopPreviewAndFreeCamera()
        }
    }

    override fun onImageCapture(imageFile: File, pin: String) {}
    override fun onCameraError(errorCode: Int) {}

    companion object {
        val TAG = BaseVerifyPinActivity::class.java.simpleName
    }
}