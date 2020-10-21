package co.tpcreative.supersafe.common.controller
import android.os.CountDownTimer
import co.tpcreative.supersafe.common.util.Utils
class SingletonScreenLock {
    private var mCountDownTimer: CountDownTimer? = null
    private var ls: SingletonScreenLockListener? = null
    fun setListener(ls: SingletonScreenLockListener?) {
        this.ls = ls
    }

    fun onStartTimer(value: Long) {
        Utils.Log(TAG, "onStartTimer")
        if (mCountDownTimer != null) {
            Utils.Log(TAG, "Running............")
            return
        }
        Utils.Log(TAG, "Start")
        mCountDownTimer = object : CountDownTimer(value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                getInstance()?.onAttemptTimer("" + secondsRemaining)
            }

            override fun onFinish() {
                Utils.Log(TAG, "Finish :")
                mCountDownTimer = null
                getInstance()?.onAttemptTimerFinish()
            }
        }.start()
    }

    fun onStop() {
        if (mCountDownTimer != null) {
            mCountDownTimer?.cancel()
            mCountDownTimer?.onFinish()
            mCountDownTimer = null
        }
    }

    fun onAttemptTimer(seconds: String?) {
        if (ls != null) {
            ls?.onAttemptTimer(seconds)
        }
    }

    fun onAttemptTimerFinish() {
        if (ls != null) {
            ls?.onAttemptTimerFinish()
        }
    }

    interface SingletonScreenLockListener {
        open fun onAttemptTimer(seconds: String?)
        open fun onAttemptTimerFinish()
    }

    companion object {
        private var instance: SingletonScreenLock? = null
        private val TAG = SingletonScreenLock::class.java.simpleName
        fun getInstance(): SingletonScreenLock? {
            synchronized(SingletonScreenLock::class.java) {
                if (instance == null) {
                    instance = SingletonScreenLock()
                }
                return instance
            }
        }
    }
}