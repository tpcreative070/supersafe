package co.tpcreative.supersafe.common
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import co.tpcreative.supersafe.common.util.Utils

class HomeWatcher(private val mContext: Context?) {
    private val mFilter: IntentFilter?
    private var mListener: OnHomePressedListener? = null
    private var mReceiver: InnerReceiver? = null
    var isRegistered = false
    fun setOnHomePressedListener(listener: OnHomePressedListener?) {
        mListener = listener
        mReceiver = InnerReceiver()
    }

    fun startWatch() {
        if (mReceiver != null) {
            mContext?.registerReceiver(mReceiver, mFilter)
            isRegistered = true
        }
    }

    fun stopWatch() {
        if (mReceiver != null) {
            if (isRegistered) {
                mContext?.unregisterReceiver(mReceiver)
                isRegistered = false
            }
        }
    }

    internal inner class InnerReceiver : BroadcastReceiver() {
        val SYSTEM_DIALOG_REASON_KEY: String? = "reason"
        val SYSTEM_DIALOG_REASON_RECENT_APPS: String? = "recentapps"
        val SYSTEM_DIALOG_REASON_HOME_KEY: String? = "homekey"
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.getAction()
            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason: String? = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                if (reason != null) {
                    Utils.Log("HomeWatcher", "action:$action,reason:$reason")
                    if (mListener != null) {
                        if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                            mListener?.onHomePressed()
                        } else if (reason == SYSTEM_DIALOG_REASON_RECENT_APPS) {
                            mListener?.onHomeLongPressed()
                        }
                    }
                }
            }
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
        fun onHomeLongPressed()
    }

    companion object {
        val TAG: String? = "hg"
    }

    init {
        mFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    }
}