package co.tpcreative.supersafe.common.controller
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SingletonManagerProcessing {
    private var dialog: AlertDialog? = null
    fun onStartProgressing(activity: Activity?, res: Int) = CoroutineScope(Dispatchers.Main).launch  {
        try {
            if (dialog == null) {
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                themeApp.let {
                    dialog = SpotsDialog.Builder()
                            .setContext(activity)
                            .setDotColor(it!!.getAccentColor())
                            .setTheme(R.style.CustomDialog)
                            .setMessage(SuperSafeApplication.getInstance().getString(res))
                            .setCancelable(true)
                            .build()
                }
            }
            if (!dialog!!.isShowing) {
                dialog?.show()
                Utils.Log(TAG, "Showing dialog...")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onStopProgressing(activity: Activity?)  = CoroutineScope(Dispatchers.Main).launch{
        Utils.Log(TAG, "onStopProgressing")
        try {
            activity?.runOnUiThread {
                if (dialog != null) {
                    dialog?.dismiss()
                    dialog = null
                }
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    companion object {
        private val TAG = SingletonManagerProcessing::class.java.simpleName
        private var instance: SingletonManagerProcessing? = null
        fun getInstance(): SingletonManagerProcessing? {
            if (instance == null) {
                instance = SingletonManagerProcessing()
            }
            return instance
        }
    }
}