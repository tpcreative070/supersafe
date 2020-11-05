package co.tpcreative.supersafe.ui.dashboard
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import com.snatik.storage.Storage
import de.mrapp.android.dialog.MaterialDialog

class DashBoardAct : BaseActivityNoneSlide() {
    private var isCancel = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        initUI()
        Utils.Log(TAG, "PIN " + SuperSafeApplication.getInstance().readKey())
    }

    override fun onResume() {
        super.onResume()
        val mUser: User? = SuperSafeApplication.getInstance().readUseSecret()
        Utils.Log(TAG, Gson().toJson(mUser))
        if (mUser != null) {
            onShowRestore()
        }
    }

    override fun onStopListenerAWhile() {}
    override fun onOrientationChange(isFaceDown: Boolean) {}

    private fun onShowRestore() {
        try {
            val builder = MaterialDialog.Builder(this,Utils.getCurrentTheme())
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            builder.setHeaderBackground(themeApp?.getAccentColor()!!)
            builder.setTitle(getString(R.string.key_restore))
            builder.setMessage(getString(R.string.restore_detail))
            builder.setCustomHeader(R.layout.custom_header_restore)
            builder.setPadding(40, 40, 40, 0)
            builder.setMargin(60, 0, 60, 0)
            builder.showHeader(true)
            builder.setPositiveButton(getString(R.string.key_restore)) { dialogInterface, i ->
                Navigator.onMoveRestore(this@DashBoardAct)
                isCancel = false
            }
            builder.setNegativeButton(getString(R.string.key_delete)) { dialogInterface, i ->
                SuperSafeApplication.getInstance().deleteFolder()
                SuperSafeApplication.getInstance().initFolder()
                PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().clear().apply()
                isCancel = false
            }.setOnDismissListener {
                Utils.Log(TAG, "Dismiss")
                if (isCancel) {
                    finish()
                }
            }
           builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = DashBoardAct::class.java.simpleName
    }
}