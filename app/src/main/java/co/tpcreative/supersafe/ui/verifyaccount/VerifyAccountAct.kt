package co.tpcreative.supersafe.ui.verifyaccount
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.VerifyAccountViewModel
import kotlinx.android.synthetic.main.activity_verify_account.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import co.tpcreative.supersafe.model.EnumStepProgressing.*

class VerifyAccountAct : BaseActivity(), TextView.OnEditorActionListener {
    var isNext = false
    var isBack = true
    var isSync = true
    var progressing = NONE
    lateinit var viewModel : VerifyAccountViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_account)
        initUI()
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
        isSync = true
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

    override fun onEditorAction(textView: TextView?, i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                Utils.Log(TAG, "Next")
                if (currentFocus === edtCode) {
                    verifyCode()
                }
                if (currentFocus === edtEmail) {
                    Utils.hideSoftKeyboard(this)
                    changeEmail()
                }
                return true
            }
            return false
        }
        return false
    }

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            if (currentFocus === edtEmail) {
                viewModel.email = value
            } else if (currentFocus === edtCode) {
                viewModel.code = value
                Utils.Log(TAG, "code")
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                Utils.Log(TAG, "home")
                if (isBack) {
                    finish()
                    return true
                }
                Utils.hideSoftKeyboard(this)
                isBack = true
                onShowView(llAction!!)
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Utils.Log(TAG, "home")
        if (isBack) {
            super.onBackPressed()
        }
        Utils.hideSoftKeyboard(this)
        isBack = true
        onShowView(llAction!!)
    }

    fun onShowView(view: View) {
        when (view.id) {
            R.id.imgEdit -> {
                llAction?.visibility = View.GONE
                llVerifyCode?.visibility = View.GONE
                llChangeEmail?.visibility = View.VISIBLE
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.btnSendVerifyCode -> {
                llAction?.visibility = View.GONE
                llChangeEmail?.visibility = View.GONE
                llVerifyCode?.visibility = View.VISIBLE
                edtEmail?.setText("")
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.llAction -> {
                llAction?.visibility = View.VISIBLE
                llChangeEmail?.visibility = View.GONE
                llVerifyCode?.visibility = View.GONE
                edtEmail?.setText("")
                edtCode?.setText("")
                isBack = true
                isNext = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ENABLE_CLOUD -> if (resultCode == Activity.RESULT_OK) {
                finish()
            }
            Navigator.REQUEST_CODE_EMAIL -> if (resultCode == Activity.RESULT_OK) {
                val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "accountName : $accountName")
                val googleOauth = GoogleOauth()
                googleOauth.email = accountName
                googleOauth.isEnableSync = isSync
                Navigator.onCheckSystem(this@VerifyAccountAct, googleOauth)
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }
}