package co.tpcreative.supersafe.ui.dashboard

import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils


class DashBoardAct : BaseActivityNoneSlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        initUI()
        //Utils.clearAppDataAndReCreateData()
        clearAppData()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData() // note: it has a return value!
            } else {
                val packageName = applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStopListenerAWhile() {}
    override fun onOrientationChange(isFaceDown: Boolean) {}

    companion object {
        private val TAG = DashBoardAct::class.java.simpleName
    }
}