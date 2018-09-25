package co.tpcreative.supersafe.common.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumStatus;

public class AnimationsContainer {
    public int FPS = 10;  // animation FPS

    // single instance procedures
    private static AnimationsContainer mInstance;


    public interface OnAnimationStoppedListener{
        void AnimationStopped();
    }

    private AnimationsContainer() {
    };

    public static AnimationsContainer getInstance() {
        if (mInstance == null)
            mInstance = new AnimationsContainer();
        return mInstance;
    }

    // animation progress dialog frames
 //   private int[] mProgressAnimFrames = { R.drawable.logo_30001, R.drawable.logo_30002, R.drawable.logo_30003 };

    int[] mUpAnimFrames = {R.drawable.ic_15,
            R.drawable.ic_0, R.drawable.ic_1, R.drawable.ic_2,
            R.drawable.ic_3, R.drawable.ic_4, R.drawable.ic_5,
            R.drawable.ic_6, R.drawable.ic_7, R.drawable.ic_8,
            R.drawable.ic_9, R.drawable.ic_10, R.drawable.ic_11,
            R.drawable.ic_12, R.drawable.ic_13, R.drawable.ic_14,
            R.drawable.ic_14_0, R.drawable.ic_14_1
    };

    int[] mDownAnimFrames = {R.drawable.ic_down_15,R.drawable.ic_down_0,
            R.drawable.ic_down_1, R.drawable.ic_down_2, R.drawable.ic_down_3,
            R.drawable.ic_down_4, R.drawable.ic_down_5, R.drawable.ic_down_6,
            R.drawable.ic_down_7, R.drawable.ic_down_8, R.drawable.ic_down_9,
            R.drawable.ic_down_10, R.drawable.ic_down_11, R.drawable.ic_down_12,
            R.drawable.ic_down_13, R.drawable.ic_down_14
    };


    /**
     * @param imageView
     * @return splash screen animation
     */

    public FramesSequenceAnimation createSplashAnim(MenuItem imageView, EnumStatus status) {
        switch (status){
            case DOWNLOAD:{
                return new FramesSequenceAnimation(imageView, mDownAnimFrames,FPS);
            }
            case UPLOAD:{
                return new FramesSequenceAnimation(imageView, mUpAnimFrames,FPS);
            }
            case DONE:{
                return new FramesSequenceAnimation(imageView,R.drawable.ic_sync_done);
            }
            case SYNC_NONE:{
                return new FramesSequenceAnimation(imageView,R.drawable.ic_sync_none);
            }
            case SYNC_ERROR:{
                return new FramesSequenceAnimation(imageView,R.drawable.ic_sync_error);
            }
        }
        return new FramesSequenceAnimation();
    }



    /**
     * AnimationPlayer. Plays animation frames sequence in loop
     */
    public class FramesSequenceAnimation {
        private int[] mFrames; // animation frames
        private int mIndex; // current frame
        private boolean mShouldRun; // true if the animation should continue running. Used to stop the animation
        private boolean mIsRunning; // true if the animation currently running. prevents starting the animation twice
        private SoftReference<MenuItem> mSoftReferenceImageView; // Used to prevent holding ImageView when it should be dead.
        private Handler mHandler;
        private int mDelayMillis;
        private boolean isAllow;
        private OnAnimationStoppedListener mOnAnimationStoppedListener;

        private Bitmap mBitmap = null;
        private BitmapFactory.Options mBitmapOptions;

        public FramesSequenceAnimation(MenuItem menuItem,int res){
            menuItem.setIcon(res);
            isAllow = false;
        }

        public FramesSequenceAnimation(){

        }

        public FramesSequenceAnimation(MenuItem imageView, int[] frames, int fps) {
            isAllow = true;
            mHandler = new Handler();
            mFrames = frames;
            mIndex = -1;
            mSoftReferenceImageView = new SoftReference<MenuItem>(imageView);
            mShouldRun = false;
            mIsRunning = false;
            mDelayMillis = 1000 / fps;

            imageView.setIcon(mFrames[0]);

            // use in place bitmap to save GC work (when animation images are the same size & type)
            if (Build.VERSION.SDK_INT >= 11) {
                Bitmap bmp = ((BitmapDrawable) imageView.getIcon()).getBitmap();
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                Bitmap.Config config = bmp.getConfig();
                mBitmap = Bitmap.createBitmap(width, height, config);
                mBitmapOptions = new BitmapFactory.Options();
                // setup bitmap reuse options.
                mBitmapOptions.inBitmap = mBitmap;
                mBitmapOptions.inMutable = true;
                mBitmapOptions.inSampleSize = 1;
            }
        }

        private int getNext() {
            mIndex++;
            if (mIndex >= mFrames.length)
                mIndex = 0;
            return mFrames[mIndex];
        }

        /**
         * Starts the animation
         */
        public synchronized void start() {
            if (!isAllow){
                return;
            }
            mShouldRun = true;
            if (mIsRunning)
                return;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    MenuItem imageView = mSoftReferenceImageView.get();
                    if (!mShouldRun || imageView == null) {
                        mIsRunning = false;
                        if (mOnAnimationStoppedListener != null) {
                            mOnAnimationStoppedListener.AnimationStopped();
                        }
                        return;
                    }

                    mIsRunning = true;
                    mHandler.postDelayed(this, mDelayMillis);

                    if (imageView.isVisible()) {
                        int imageRes = getNext();
                        if (mBitmap != null) { // so Build.VERSION.SDK_INT >= 11
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().getResources(), imageRes, mBitmapOptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {
                                Drawable d = new BitmapDrawable(SuperSafeApplication.getInstance().getResources(), bitmap);
                                imageView.setIcon(d);
                            } else {
                                imageView.setIcon(imageRes);
                                mBitmap.recycle();
                                mBitmap = null;
                            }
                        } else {
                            imageView.setIcon(imageRes);
                        }
                    }

                }
            };
            mHandler.post(runnable);
        }

        /**
         * Stops the animation
         */
        public synchronized void stop() {
            mShouldRun = false;
        }


    }
}