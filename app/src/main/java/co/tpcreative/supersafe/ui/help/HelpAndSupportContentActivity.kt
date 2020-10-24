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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
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
import com.rengwuxian.materialedittext.MaterialEditText
import dmax.dialog.SpotsDialog
import im.delight.android.webview.AdvancedWebView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HelpAndSupportContentActivity : BaseActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    private var presenter: HelpAndSupportPresenter? = null

    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null

    @BindView(R.id.webview)
    var webview: AdvancedWebView? = null

    @BindView(R.id.llEmail)
    var llEmail: LinearLayout? = null

    @BindView(R.id.tvEmail)
    var tvEmail: AppCompatTextView? = null

    @BindView(R.id.edtSupport)
    var edtSupport: MaterialEditText? = null
    private var isNext = false
    private var mUser: User? = null
    private var dialog: AlertDialog? = null
    private var menuItem: MenuItem? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_and_support_content)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = HelpAndSupportPresenter()
        presenter?.bindView(this)
        presenter?.onGetDataIntent(this)
        mUser = Utils.getUserInfo()
        tvEmail?.setText(mUser?.email)
        edtSupport?.addTextChangedListener(mTextWatcher)
        edtSupport?.setOnEditorActionListener(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        Utils.hideSoftKeyboard(this)
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
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
                Utils.showGotItSnackbar(getCurrentFocus()!!, R.string.send_email_failed)
                edtSupport?.setText("")
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, Gson().toJson(presenter?.content))
        when (status) {
            EnumStatus.RELOAD -> {
                if (presenter?.content?.content == getString(R.string.contact_support_content)) {
                    llEmail?.setVisibility(View.VISIBLE)
                    edtSupport?.setVisibility(View.VISIBLE)
                    webview?.setVisibility(View.GONE)
                } else {
                    tvTitle?.setText(presenter?.content?.title)
                    llEmail?.setVisibility(View.GONE)
                    edtSupport?.setVisibility(View.GONE)
                    webview?.setVisibility(View.VISIBLE)
                    presenter?.content?.content?.let { webview?.loadUrl(it) }
                }
            }
            EnumStatus.SEND_EMAIL -> {
                onStopProgressing()
                Utils.showInfoSnackbar(getCurrentFocus()!!, R.string.thank_you, true)
                edtSupport?.setText("")
            }
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

    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                true
            } else {
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_support, menu)
        menuItem = menu?.findItem(R.id.menu_item_send)
        if (presenter != null) {
            if (presenter?.content?.content == getString(R.string.contact_support_content)) {
                menuItem?.setVisible(true)
            } else {
                menuItem?.setVisible(false)
            }
        }
        Utils.Log(TAG, "Menu.............")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_send -> {
                if (isNext) {
                    val content: String? = edtSupport?.getText().toString()
                    val emailToken: EmailToken? = EmailToken?.getInstance()?.convertTextObject(mUser!!, content!!)
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
                            .setContext(this@HelpAndSupportContentActivity)
                            .setMessage(getString(R.string.Sending))
                            .setCancelable(true)
                            .build()
                }
                if (!(dialog?.isShowing())!!) {
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
        private val TAG = HelpAndSupportContentActivity::class.java.simpleName
    }
}