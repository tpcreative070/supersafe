package co.tpcreative.supersafe.ui.restore
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_restore.*

class RestoreAct : BaseActivity(), TextView.OnEditorActionListener, BaseView<EmptyModel> {
    var dialog: AlertDialog? = null
    var isNext = false
    var presenter: RestorePresenter? = null
    var count = 0
    var subscriptions: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)
        initUI()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                val pin: String? = SuperSafeApplication.getInstance().readKey()
                if (pin == edtPreviousPIN?.text.toString()) {
                    Utils.hideKeyboard(currentFocus)
                    onStartProgressing()
                    Utils.onObserveData(2000, object :Listener {
                        override fun onStart() {
                            onRestore()
                        }
                    })
                } else {
                    edtPreviousPIN?.setText("")
                    tvWrongPin?.visibility = View.VISIBLE
                    shake()
                    Utils.hideKeyboard(currentFocus)
                    count += 1
                    if (count >= 4) {
                        btnForgotPin?.visibility = View.VISIBLE
                    }
                }
                return true
            }
            return false
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (subscriptions != null) {
            subscriptions?.dispose()
        }
    }

    override fun onStopListenerAWhile() {}

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            if (Utils.isValid(value)) {
                btnRestoreNow?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnRestoreNow?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                isNext = true
                tvWrongPin?.visibility = View.INVISIBLE
            } else {
                btnRestoreNow?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnRestoreNow?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                isNext = false
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }
}