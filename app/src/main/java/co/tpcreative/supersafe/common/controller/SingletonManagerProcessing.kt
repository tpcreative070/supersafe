package co.tpcreative.supersafe.common.controller
import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.progressing.SpotsDialog
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SingletonManagerProcessing {
    private var dialog: AlertDialog? = null
    fun onStartProgressing(activity: Activity?, res: Int, listener : SingletonManagerProgressingListener? = null) = CoroutineScope(Dispatchers.Main).launch  {
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
                            .setCancelListener { }
                            .build()
                }
            }
            dialog?.let {
                if (!it.isShowing){
                    it.show()
                    listener?.onShow()
                    Utils.Log(TAG,"Showing progress dialog")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onStopProgressing(activity: Activity?)  = CoroutineScope(Dispatchers.Main).launch{
        Utils.Log(TAG, "onStopProgressing")
        try {
            dialog?.dismiss()
            dialog = null
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    companion object {
        private val TAG = SingletonManagerProcessing::class.java.simpleName
        private var instance: SingletonManagerProcessing? = null
        val core = CoroutineScope(Dispatchers.Main)
        fun getInstance(): SingletonManagerProcessing? {
            if (instance == null) {
                instance = SingletonManagerProcessing()
            }
            return instance
        }
    }
}

interface SingletonManagerProgressingListener {
    fun onShow()
}