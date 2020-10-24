package co.tpcreative.supersafe.ui.dashboard
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import de.mrapp.android.dialog.MaterialDialog

class DashBoardActivity : BaseActivityNoneSlide() {
    private var isCancel = true
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        Utils.Log(TAG, "PIN " + SuperSafeApplication.getInstance().readKey())
    }

    @OnClick(R.id.btnLogin)
    fun onClickedLogin(view: View?) {
        Navigator.onMoveToLogin(this)
    }

    @OnClick(R.id.btnSignUp)
    fun onClickedSignUp(view: View?) {
        Navigator.onMoveSetPin(this, EnumPinAction.SIGN_UP)
    }

    protected override fun onResume() {
        super.onResume()
        val mUser: User? = SuperSafeApplication.getInstance().readUseSecret()
        Utils.Log(TAG, Gson().toJson(mUser))
        if (mUser != null) {
            onShowRestore()
        }
    }

    override fun onStopListenerAWhile() {}
    override fun onOrientationChange(isFaceDown: Boolean) {}

    fun onShowRestore() {
        try {
            val builder = MaterialDialog.Builder(this)
            val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
            builder.setHeaderBackground(themeApp?.getAccentColor()!!)
            builder.setTitle(getString(R.string.key_restore))
            builder.setMessage(getString(R.string.restore_detail))
            builder.setCustomHeader(R.layout.custom_header_restore)
            builder.setPadding(40, 40, 40, 0)
            builder.setMargin(60, 0, 60, 0)
            builder.showHeader(true)
            builder.setPositiveButton(getString(R.string.key_restore), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    Navigator.onMoveRestore(this@DashBoardActivity)
                    isCancel = false
                }
            })
            builder.setNegativeButton(getText(R.string.key_delete), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    SuperSafeApplication.Companion.getInstance().deleteFolder()
                    SuperSafeApplication.Companion.getInstance().initFolder()
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply()
                    isCancel = false
                }
            })
                    .setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialogInterface: DialogInterface?) {
                            Utils.Log(TAG, "Dismiss")
                            if (isCancel) {
                                finish()
                            }
                        }
                    })
            val dialog = builder.show()
            builder.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialogInterface: DialogInterface?) {
                    val positive = dialog.findViewById<AppCompatButton?>(android.R.id.button1)
                    val negative = dialog.findViewById<AppCompatButton?>(android.R.id.button2)
                    val textView: AppCompatTextView? = dialog.findViewById<AppCompatTextView?>(R.id.message)
                    if (positive != null && negative != null && textView != null) {
                        positive.setTextColor(ContextCompat.getColor(getApplicationContext(), themeApp?.getAccentColor()!!))
                        negative.setTextColor(ContextCompat.getColor(getApplicationContext(), themeApp.getAccentColor()))
                        textView.setTextSize(16f)
                        positive.textSize = 14f
                        negative.textSize = 14f
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = DashBoardActivity::class.java.simpleName
    }
}