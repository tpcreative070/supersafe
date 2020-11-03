package co.tpcreative.supersafe.ui.help
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmailToken
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_help_and_support_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HelpAndSupportContentAct : BaseActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    var presenter: HelpAndSupportPresenter? = null
    var isNext = false
    var mUser: User? = null
    var dialog: AlertDialog? = null
    var menuItem: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_and_support_content)
        iniUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        Utils.hideSoftKeyboard(this)
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SEND_EMAIL -> {
                onStopProgressing()
                Utils.showGotItSnackbar(currentFocus!!, R.string.send_email_failed)
                edtSupport?.setText("")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, Gson().toJson(presenter?.content))
        when (status) {
            EnumStatus.RELOAD -> {
                if (presenter?.content?.content == getString(R.string.contact_support_content)) {
                    llEmail?.visibility = View.VISIBLE
                    edtSupport?.visibility = View.VISIBLE
                    webview?.visibility = View.GONE
                } else {
                    tvTitle?.text = presenter?.content?.title
                    llEmail?.visibility = View.GONE
                    edtSupport?.visibility = View.GONE
                    webview?.visibility = View.VISIBLE
                    presenter?.content?.content?.let { webview?.loadUrl(it) }
                }
            }
            EnumStatus.SEND_EMAIL -> {
                onStopProgressing()
                Utils.showInfoSnackbar(getCurrentFocus()!!, R.string.thank_you, true)
                edtSupport?.setText("")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onEditorAction(textView: TextView?, i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                onStartProgressing()
                val content: String = edtSupport?.getText().toString()
                val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertTextObject(it, content) }
                if (emailToken != null) {
                    presenter?.onSendMail(emailToken, content)
                }
                Utils.hideKeyboard(edtSupport)
                return true
            }
            return false
        }
        return false
    }

    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = Utils.isValid(value)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_help_support, menu)
        menuItem = menu?.findItem(R.id.menu_item_send)
        if (presenter != null) {
            menuItem?.isVisible = presenter?.content?.content == getString(R.string.contact_support_content)
        }
        Utils.Log(TAG, "Menu.............")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_send -> {
                if (isNext) {
                    val content: String? = edtSupport?.text.toString()
                    val emailToken: EmailToken? = EmailToken.getInstance()?.convertTextObject(mUser!!, content!!)
                    if (emailToken != null) {
                        presenter?.onSendMail(emailToken, content)
                    }
                    onStartProgressing()
                    Utils.hideKeyboard(edtSupport)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    dialog = SpotsDialog.Builder()
                            .setContext(this@HelpAndSupportContentAct)
                            .setMessage(getString(R.string.Sending))
                            .setCancelable(true)
                            .build()
                }
                if (!(dialog?.isShowing)!!) {
                    dialog?.show()
                    Utils.Log(TAG, "Showing dialog...")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onStopProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog != null) {
                    dialog?.dismiss()
                }
            })
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    companion object {
        private val TAG = HelpAndSupportContentAct::class.java.simpleName
    }
}