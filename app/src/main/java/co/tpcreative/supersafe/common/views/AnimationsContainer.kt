package co.tpcreative.supersafe.common.views
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumStatus
import java.lang.ref.SoftReference

class AnimationsContainer private constructor() {
    var FPS = 4 // animation FPS
    private var mHandler: Handler? = null
    private var runnable: Runnable? = null
    interface OnAnimationStoppedListener {
        open fun AnimationStopped()
    }

    // animation progress dialog frames
    //   private int[] mProgressAnimFrames = { R.drawable.logo_30001, R.drawable.logo_30002, R.drawable.logo_30003 };
    var mUpAnimFrames: IntArray? = intArrayOf(R.drawable.ic_up_0,
            R.drawable.ic_up_1, R.drawable.ic_up_2,
            R.drawable.ic_up_3, R.drawable.ic_up_4
    )
    var mDownAnimFrames: IntArray? = intArrayOf(R.drawable.ic_down_0,
            R.drawable.ic_down_1, R.drawable.ic_down_2, R.drawable.ic_down_3,
            R.drawable.ic_down_4
    )

    /**
     * @param imageView
     * @return splash screen animation
     */
    fun createSplashAnim(imageView: MenuItem?, status: EnumStatus?): FramesSequenceAnimation? {
        when (status) {
            EnumStatus.DOWNLOAD -> {
                return FramesSequenceAnimation(imageView, mDownAnimFrames, FPS)
            }
            EnumStatus.UPLOAD -> {
                return FramesSequenceAnimation(imageView, mUpAnimFrames, FPS)
            }
            EnumStatus.DONE -> {
                return FramesSequenceAnimation(imageView, R.drawable.ic_sync_done)
            }
            EnumStatus.SYNC_NONE -> {
                return FramesSequenceAnimation(imageView, R.drawable.ic_sync_none)
            }
            EnumStatus.SYNC_ERROR -> {
                return FramesSequenceAnimation(imageView, R.drawable.ic_sync_error)
            }
        }
        return FramesSequenceAnimation()
    }

    /**
     * AnimationPlayer. Plays animation frames sequence in loop
     */
    inner class FramesSequenceAnimation {
        private var mFrames // animation frames
                : IntArray? = null
        private var mIndex = 0 // current frame = 0
        private var mShouldRun : Boolean = false // true if the animation should continue running. Used to stop the animation = false
        private var mIsRunning : Boolean = false // true if the animation currently running. prevents starting the animation twice = false
        private var mSoftReferenceImageView // Used to prevent holding ImageView when it should be dead.
                : SoftReference<MenuItem?>? = null
        private var mDelayMillis = 0
        private var isAllow = false
        private val mOnAnimationStoppedListener: OnAnimationStoppedListener? = null
        private var mBitmap: Bitmap? = null
        private var mBitmapOptions: BitmapFactory.Options? = null

        constructor(menuItem: MenuItem?, res: Int) {
            menuItem?.setIcon(res)
            isAllow = false
        }

        constructor() {}
        constructor(imageView: MenuItem?, frames: IntArray?, fps: Int) {
            try {
                isAllow = true
                mHandler = Handler(Looper.getMainLooper())
                mFrames = frames
                mIndex = -1
                mSoftReferenceImageView = SoftReference(imageView)
                mShouldRun = false
                mIsRunning = false
                mDelayMillis = 2000 / fps
                imageView?.setIcon(mFrames!!.get(0))

                // use in place bitmap to save GC work (when animation images are the same size & type)
                if (Build.VERSION.SDK_INT >= 11) {
                    val bmp: Bitmap = (imageView?.getIcon() as BitmapDrawable).getBitmap()
                    val width = 40
                    val height = 40
                    val config: Bitmap.Config = bmp.getConfig()
                    mBitmap = Bitmap.createBitmap(width, height, config)
                    mBitmapOptions = BitmapFactory.Options()
                    val mBitmapOptions: BitmapFactory.Options = BitmapFactory.Options()
                    mBitmapOptions.inBitmap = mBitmap
                    mBitmapOptions.inJustDecodeBounds = false
                    mBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
                    mBitmapOptions.inDither = true
                    mBitmapOptions.inSampleSize = 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getNext(): Int? {
            mIndex++
            if (mIndex >= mFrames?.size!!) mIndex = 0
            return mFrames?.get(mIndex)
        }

        /**
         * Starts the animation
         */
        @Synchronized
        fun start() {
            if (!isAllow) {
                return
            }
            mShouldRun = true
            if (mIsRunning) return
            runnable = object : Runnable {
                override fun run() {
                    val imageView = mSoftReferenceImageView?.get()
                    if (!mShouldRun || imageView == null) {
                        mIsRunning = false
                        mOnAnimationStoppedListener?.AnimationStopped()
                        return
                    }
                    mIsRunning = true
                    mHandler?.postDelayed(this, mDelayMillis.toLong())
                    if (imageView.isVisible) {
                        val imageRes = getNext()
                        if (mBitmap != null) { // so Build.VERSION.SDK_INT >= 11
                            var bitmap: Bitmap? = null
                            try {
                                bitmap = imageRes?.let { BitmapFactory.decodeResource(SuperSafeApplication.getInstance().getResources(), it, mBitmapOptions) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (bitmap != null) {
                                val d: Drawable = BitmapDrawable(SuperSafeApplication.getInstance().getResources(), bitmap)
                                imageView.icon = d
                            } else {
                                if (imageRes != null) {
                                    imageView.setIcon(imageRes)
                                }
                                mBitmap!!.recycle()
                                mBitmap = null
                            }
                        } else {
                            if (imageRes != null) {
                                imageView.setIcon(imageRes)
                            }
                        }
                    }
                }
            }
            mHandler?.post(runnable!!)
        }

        /**
         * Stops the animation
         */
        @Synchronized
        fun stop() {
            if (mHandler != null) {
                runnable?.let {
                    mHandler?.removeCallbacks(it)
                }
            }
            mShouldRun = false
        }
    }

    companion object {
        // single instance procedures
        private var mInstance: AnimationsContainer? = null
        fun getInstance(): AnimationsContainer? {
            if (mInstance == null) mInstance = AnimationsContainer()
            return mInstance
        }
    }
}