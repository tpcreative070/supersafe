package co.tpcreative.supersafe.common.services
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import co.tpcreative.supersafe.common.util.NetworkUtil

class SuperSafeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val action: String? = intent.action
        /*In the case status of network changed and then updating status for app(Connected/Disconnect)*/
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION, ignoreCase = true)) {
            connectivityReceiverListener?.onNetworkConnectionChanged(!NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance()))
        }
        if (action.equals(Intent.ACTION_SCREEN_OFF, ignoreCase = true)) {
            connectivityReceiverListener?.onActionScreenOff()
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
        fun onActionScreenOff()
    }

    companion object {
        val TAG = SuperSafeReceiver::class.java.simpleName
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
        fun isConnected(): Boolean {
            return !NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())
        }
    }
}