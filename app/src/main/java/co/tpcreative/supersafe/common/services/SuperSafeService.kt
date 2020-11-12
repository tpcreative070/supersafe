package co.tpcreative.supersafe.common.services
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import co.tpcreative.supersafe.common.presenter.BaseServiceView
import co.tpcreative.supersafe.common.presenter.PresenterService
import co.tpcreative.supersafe.common.services.download.DownloadService
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.*
class SuperSafeService : PresenterService<BaseServiceView<*>?>(), SuperSafeReceiver.ConnectivityReceiverListener {
    private val mBinder: IBinder? = LocalBinder() // Binder given to clients
    private var androidReceiver: SuperSafeReceiver? = null
    private var downloadService: DownloadService? = null
    private var isCallRefreshToken = false
    override fun onCreate() {
        super.onCreate()
        Utils.Log(TAG, "onCreate")
        downloadService = DownloadService()
        onInitReceiver()
        SuperSafeApplication.getInstance().setConnectivityListener(this)
    }

    private fun onInitReceiver() {
        Utils.Log(TAG, "onInitReceiver")
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        androidReceiver = SuperSafeReceiver()
        registerReceiver(androidReceiver, intentFilter)
        SuperSafeApplication.getInstance().setConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver)
        }
        stopSelf()
        stopForeground(true)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Connected :$isConnected")
        val view: BaseServiceView<*>? = view()
        if (view != null) {
            if (isConnected) {
                view.onSuccessful("Connected network", EnumStatus.CONNECTED)
            } else {
                view.onSuccessful("Disconnected network", EnumStatus.DISCONNECTED)
            }
        }
    }

    override fun onActionScreenOff() {
        view()?.onSuccessful("Screen Off", EnumStatus.SCREEN_OFF)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we get killed, after returning from here, restart
        Utils.Log(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        val extras: Bundle? = intent?.getExtras()
        Utils.Log(TAG, "onBind")
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra")
        }
        return mBinder
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): SuperSafeService? {
            return this@SuperSafeService
        }
    }

    private fun <T> isCheckNull(view: T?, status: EnumFunc): Boolean {
        if (subscriptions == null) {
            Utils.Log(TAG, "Subscriptions is null " + status.name)
            return true
        } else if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG, "No connection " + status.name)
            return true
        } else if (view == null) {
            Utils.Log(TAG, "View is null " + status.name)
            return true
        }
        when (status) {
            EnumFunc.GET_USER_INFO -> {
                Utils.Log(TAG, status.name)
                val mUser: User = Utils.getUserInfo() ?: return true
                mUser.author ?: return true
            }
            EnumFunc.UPDATE_USER_TOKEN -> {
                Utils.getUserInfo() ?: return true
            }
            else -> Utils.Log(TAG, "Nothing")
        }
        return false
    }

    companion object {
        private val TAG = SuperSafeService::class.java.simpleName
    }
}