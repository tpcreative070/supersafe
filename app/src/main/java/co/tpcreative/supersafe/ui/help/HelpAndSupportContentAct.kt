package co.tpcreative.supersafe.ui.help
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.setIconTint
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.HelpAndSupportViewModel
import kotlinx.android.synthetic.main.activity_help_and_support_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HelpAndSupportContentAct : BaseActivity(), TextView.OnEditorActionListener {
    var isNext = false
    var dialog: AlertDialog? = null
    var menuItem: MenuItem? = null
    lateinit var viewModel : HelpAndSupportViewModel
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
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        Utils.hideSoftKeyboard(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onEditorAction(textView: TextView?, i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                onStartProgressing()
                sendEmail()
                Utils.hideKeyboard(edtSupport)
                return true
            }
            return false
        }
        return false
    }

    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.content = s.toString()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_help_support, menu)
        menuItem = menu?.findItem(R.id.menu_item_send)
        menuItem?.isVisible = contentHelpAndSupport?.content == getString(R.string.contact_support_content)
        menuItem?.isEnabled = false
        menuItem?.setIconTint(R.color.material_gray_700)
        Utils.Log(TAG, "Menu.............")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_send -> {
                if (isNext) {
                    sendEmail()
                    Utils.hideKeyboard(edtSupport)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onStartProgressing() {
        try {
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this@HelpAndSupportContentAct,R.string.Sending)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onStopProgressing() {
        try {
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    var contentHelpAndSupport : HelpAndSupportModel? = null

    companion object {
        private val TAG = HelpAndSupportContentAct::class.java.simpleName
    }
}