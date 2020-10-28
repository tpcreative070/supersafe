package co.tpcreative.supersafe.common.hiddencamera
import java.io.File

/**
 * Created by Keval on 14-Oct-16.
 *
 * @author [&#39;https://github.com/kevalpatel2106&#39;]['https://github.com/kevalpatel2106']
 */
interface CameraCallbacks {
    fun onImageCapture(imageFile: File, pin: String)
    fun onCameraError(@CameraError.CameraErrorCodes errorCode: Int)
}