package co.tpcreative.supersafe.common.hiddencamera
import android.content.Context
import android.hardware.Camera
import co.tpcreative.supersafe.common.hiddencamera.config.*
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.io.File

class CameraConfig {
    private var mContext: Context? = null

    @CameraResolution.SupportedResolution
    private var mResolution: Int = CameraResolution.MEDIUM_RESOLUTION

    @CameraFacing.SupportedCameraFacing
    private var mFacing: Int = CameraFacing.REAR_FACING_CAMERA

    @CameraImageFormat.SupportedImageFormat
    private var mImageFormat: Int = CameraImageFormat.FORMAT_JPEG

    @CameraResolution.SupportedResolution
    private var mImageRotation: Int = CameraRotation.ROTATION_0

    @CameraFocus.SupportedCameraFocus
    private var mCameraFocus: Int = CameraFocus.AUTO
    private var mImageFile: File? = null
    private var mPin: String? = null
    fun getBuilder(context: Context?): CameraConfig.Builder? {
        mContext = context
        return Builder()
    }

    @CameraResolution.SupportedResolution
    fun getResolution(): Int {
        return mResolution
    }

    fun getFocusMode(): String? {
        return when (mCameraFocus) {
            CameraFocus.AUTO -> Camera.Parameters.FOCUS_MODE_AUTO
            CameraFocus.CONTINUOUS_PICTURE -> Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            CameraFocus.NO_FOCUS -> null
            else -> throw RuntimeException("Invalid camera focus mode.")
        }
    }

    @CameraFacing.SupportedCameraFacing
    fun getFacing(): Int {
        return mFacing
    }

    @CameraImageFormat.SupportedImageFormat
    fun getImageFormat(): Int {
        return mImageFormat
    }

    fun getImageFile(): File? {
        return mImageFile
    }

    fun getPin(): String? {
        return mPin
    }

    @CameraResolution.SupportedResolution
    fun getImageRotation(): Int {
        return mImageRotation
    }

    inner class Builder {
        /**
         * Set the resolution of the output camera image. If you don't specify any resolution,
         * default image resolution will set to [CameraResolution.MEDIUM_RESOLUTION].
         *
         * @param resolution Any resolution from:
         *  * [CameraResolution.HIGH_RESOLUTION]
         *  * [CameraResolution.MEDIUM_RESOLUTION]
         *  * [CameraResolution.LOW_RESOLUTION]
         * @return [Builder]
         * @see CameraResolution
         */
        fun setCameraResolution(@CameraResolution.SupportedResolution resolution: Int): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {

            //Validate input
            if (resolution != CameraResolution.HIGH_RESOLUTION && resolution != CameraResolution.MEDIUM_RESOLUTION && resolution != CameraResolution.LOW_RESOLUTION) {
                throw RuntimeException("Invalid camera resolution.")
            }
            mResolution = resolution
            return this
        }

        /**
         * Set the camera facing with which you want to capture image.
         * Either rear facing camera or front facing camera. If you don't provide any camera facing,
         * default camera facing will be [CameraFacing.FRONT_FACING_CAMERA].
         *
         * @param cameraFacing Any camera facing from:
         *  * [CameraFacing.REAR_FACING_CAMERA]
         *  * [CameraFacing.FRONT_FACING_CAMERA]
         * @return [Builder]
         * @see CameraFacing
         */
        fun setCameraFacing(@CameraFacing.SupportedCameraFacing cameraFacing: Int): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {
            //Validate input
            if (cameraFacing != CameraFacing.REAR_FACING_CAMERA &&
                    cameraFacing != CameraFacing.FRONT_FACING_CAMERA) {
                throw RuntimeException("Invalid camera facing value.")
            }
            mFacing = cameraFacing
            return this
        }

        /**
         * Set the camera focus mode. If you don't provide any camera focus mode,
         * default focus mode will be [CameraFocus.AUTO].
         *
         * @param focusMode Any camera focus mode from:
         *  * [CameraFocus.AUTO]
         *  * [CameraFocus.CONTINUOUS_PICTURE]
         *  * [CameraFocus.NO_FOCUS]
         * @return [Builder]
         * @see CameraFacing
         */
        fun setCameraFocus(@CameraFocus.SupportedCameraFocus focusMode: Int): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {
            //Validate input
            if (focusMode != CameraFocus.AUTO && focusMode != CameraFocus.CONTINUOUS_PICTURE && focusMode != CameraFocus.Companion.NO_FOCUS) {
                throw RuntimeException("Invalid camera focus mode.")
            }
            mCameraFocus = focusMode
            return this
        }

        /**
         * Specify the image format for the output image. If you don't specify any output format,
         * default output format will be [CameraImageFormat.FORMAT_JPEG].
         *
         * @param imageFormat Any supported image format from:
         *  * [CameraImageFormat.FORMAT_JPEG]
         *  * [CameraImageFormat.FORMAT_PNG]
         * @return [Builder]
         * @see CameraImageFormat
         */
        fun setImageFormat(@CameraImageFormat.SupportedImageFormat imageFormat: Int): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {
            //Validate input
            if (imageFormat != CameraImageFormat.FORMAT_JPEG &&
                    imageFormat != CameraImageFormat.FORMAT_PNG) {
                throw RuntimeException("Invalid output image format.")
            }
            mImageFormat = imageFormat
            return this
        }

        /**
         * Specify the output image rotation. The output image will be rotated by amount of degree specified
         * before stored to the output file. By default there is no rotation applied.
         *
         * @param rotation Any supported rotation from:
         *  * [CameraRotation.ROTATION_0]
         *  * [CameraRotation.ROTATION_90]
         *  * [CameraRotation.ROTATION_180]
         *  * [CameraRotation.ROTATION_270]
         * @return [Builder]
         * @see CameraRotation
         */
        fun setImageRotation(@CameraRotation.SupportedRotation rotation: Int): CameraConfig.Builder? {
            //Validate input
            if (rotation != CameraRotation.Companion.ROTATION_0 && rotation != CameraRotation.Companion.ROTATION_90 && rotation != CameraRotation.Companion.ROTATION_180 && rotation != CameraRotation.Companion.ROTATION_270) {
                throw RuntimeException("Invalid image rotation.")
            }
            mImageRotation = rotation
            return this
        }

        /**
         * Set the location of the out put image. If you do not set any file for the output image, by
         * default image will be stored in the application's cache directory.
         *
         * @param imageFile [File] where you want to store the image.
         * @return [Builder]
         */
        fun setImageFile(imageFile: File?): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {
            mImageFile = imageFile
            return this
        }

        fun setPin(pin: String?): co.tpcreative.supersafe.common.hiddencamera.CameraConfig.Builder? {
            mPin = pin
            return this
        }

        /**
         * Build the configuration.
         *
         * @return [CameraConfig]
         */
        fun build(): CameraConfig? {
            if (mImageFile == null) mImageFile = getDefaultStorageFile()
            return this@CameraConfig
        }

        /**
         * Get the new file to store the image if there isn't any custom file location available.
         * This will create new file into the cache directory of the application.
         */
        private fun getDefaultStorageFile(): File {
            return File(SuperSafeApplication.getInstance().getSuperSafeBreakInAlerts()
                    + File.separator
                    + "IMG_" + System.currentTimeMillis() //IMG_214515184113123.png
                    + if (mImageFormat == CameraImageFormat.FORMAT_JPEG) ".jpeg" else ".png")
        }
    }
}