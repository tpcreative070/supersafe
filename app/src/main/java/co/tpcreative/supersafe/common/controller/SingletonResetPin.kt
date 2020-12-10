package co.tpcreative.supersafe.common.controller
import android.os.CountDownTimer
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.extension.putUserPreShare
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User

class SingletonResetPin {
    var waitingLeft: Long = 0
    private var mCountDownTimer: CountDownTimer? = null
    fun onStartTimer(value: Long) {
        Utils.Log(TAG, "onStartTimer")
        if (mCountDownTimer != null) {
            Utils.Log(TAG, "Running............")
            return
        }
        ServiceManager.getInstance()?.onStartService()
        ServiceManager.getInstance()?.setIsWaitingSendMail(true)
        mCountDownTimer = object : CountDownTimer(value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                waitingLeft = secondsRemaining
                Utils.Log(TAG, "" + secondsRemaining)
                Utils.onPushEventBus(EnumStatus.WAITING_LEFT)
            }
            override fun onFinish() {
                Utils.Log(TAG, "Finish :")
                val mUser: User? = Utils.getUserInfo()
                mUser?.isWaitingSendMail = true
                Utils.putUserPreShare(mUser)
                mCountDownTimer = null
                ServiceManager.getInstance()?.onSendEmail()
                ServiceManager.getInstance()?.setIsWaitingSendMail(false)
                Utils.onPushEventBus(EnumStatus.WAITING_DONE)
            }
        }.start()
    }

    fun onStop() {
        if (mCountDownTimer != null) {
            mCountDownTimer?.cancel()
            mCountDownTimer = null
            ServiceManager.getInstance()?.setIsWaitingSendMail(false)
            val mUser: User? = Utils.getUserInfo()
            mUser?.isWaitingSendMail = false
            Utils.putUserPreShare(mUser)
        } else {
            try {
                ServiceManager.getInstance()?.setIsWaitingSendMail(false)
                val mUser: User? = Utils.getUserInfo()
                mUser?.isWaitingSendMail = false
                Utils.putUserPreShare(mUser)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private var instance: SingletonResetPin? = null
        private val TAG = SingletonResetPin::class.java.simpleName
        fun getInstance(): SingletonResetPin? {
            if (instance == null) {
                instance = SingletonResetPin()
            }
            return instance
        }
    }
}