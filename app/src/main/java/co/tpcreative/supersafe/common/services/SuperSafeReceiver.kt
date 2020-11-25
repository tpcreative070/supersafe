package co.tpcreative.supersafe.common.services
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class SuperSafeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val action: String? = intent.action
        /*In the case status of network changed and then updating status for app(Connected/Disconnect)*/
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION, ignoreCase = true)) {
            val cm: ConnectivityManager = context
                    ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
            val isConnected = (activeNetwork != null
                    && activeNetwork.isConnected())
            connectivityReceiverListener?.onNetworkConnectionChanged(isConnected)
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
            val cm: ConnectivityManager = SuperSafeApplication.Companion.getInstance().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
            return (activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting())
        }
    }
}