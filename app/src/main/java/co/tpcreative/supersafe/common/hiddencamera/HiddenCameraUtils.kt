package co.tpcreative.supersafe.common.hiddencamera
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Build
import android.provider.Settings
import androidx.annotation.WorkerThread
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat
import co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
/**
 * Created by Keval on 11-Nov-16.
 * This class holds common camera utils.
 *
 * @author [&#39;https://github.com/kevalpatel2106&#39;]['https://github.com/kevalpatel2106']
 */
object HiddenCameraUtils {
    /**
     * Check if the application has "Draw over other app" permission? This permission is available to all
     * the application below Android M (<API 23). But for the API 23 and above user has to enable it mannually if the permission is not available by opening Settings -> Apps -> Gear icon on top-right corner ->
     * Draw Over other apps.
     *
     * @return true if the permission is available.
     * @see 'http://www.androidpolice.com/2015/09/07/android-m-begins-locking-down-floating-apps-requires-users-to-grant-special-permission-to-draw-on-other-apps/'
    </API> */
    @SuppressLint("NewApi")
    fun canOverDrawOtherApps(context: Context?): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    /**
     * This will open settings screen to allow the "Draw over other apps" permission to the application.
     *
     * @param context instance of caller.
     */
    fun openDrawOverPermissionSetting(context: Context?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    /**
     * Get the cache directory.
     *
     * @param context instance of the caller
     * @return cache directory file.
     */
    fun getCacheDir(context: Context?): File {
        return if (context?.getExternalCacheDir() == null) context!!.getCacheDir()!! else context.getExternalCacheDir()!!
    }

    /**
     * Check if the device has front camera or not?
     *
     * @param context context
     * @return true if the device has front camera.
     */
    fun isFrontCameraAvailable(context: Context): Boolean {
        val numCameras = Camera.getNumberOfCameras()
        return numCameras > 0 && context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    /**
     * Rotate the bitmap by 90 degree.
     *
     * @param bitmap original bitmap
     * @return rotated bitmap
     */
    @WorkerThread
    fun rotateBitmap(bitmap: Bitmap, @CameraRotation.SupportedRotation rotation: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
    }

    /**
     * Save image to the file.
     *
     * @param bitmap     bitmap to store.
     * @param fileToSave file where bitmap should stored
     */
    fun saveImageFromFile(bitmap: Bitmap,
                          fileToSave: File,
                          @CameraImageFormat.SupportedImageFormat imageFormat: Int): Boolean {
        var out: FileOutputStream? = null
        var isSuccess: Boolean

        //Decide the image format
        val compressFormat: Bitmap.CompressFormat
        compressFormat = when (imageFormat) {
            CameraImageFormat.FORMAT_JPEG -> Bitmap.CompressFormat.JPEG
            CameraImageFormat.FORMAT_WEBP -> Bitmap.CompressFormat.WEBP
            CameraImageFormat.FORMAT_PNG -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.PNG
        }
        try {
            if (!fileToSave.exists()) fileToSave.createNewFile()
            out = FileOutputStream(fileToSave)
            bitmap.compress(compressFormat, 100, out) // bmp is your Bitmap instance
            isSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccess = false
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return isSuccess
    }
}