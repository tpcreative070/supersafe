package co.tpcreative.supersafe.ui.twofactoryauthentication
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.NetworkUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.viewmodel.EnumTwoFactoryAuthentication
import co.tpcreative.supersafe.viewmodel.TwoFactoryAuthenticationViewModel
import kotlinx.android.synthetic.main.activity_two_factory_authentication.*

class TwoFactoryAuthenticationAct : BaseActivity(), CompoundButton.OnCheckedChangeListener,TextView.OnEditorActionListener {
    lateinit var viewModel : TwoFactoryAuthenticationViewModel
    var isNext = false
    var  status : EnumTwoFactoryAuthentication = EnumTwoFactoryAuthentication.NONE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_factory_authentication)
        initUI()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        if (NetworkUtil.pingIpAddress(this)){
            Utils.onBasicAlertNotify(this,"Alert","No Internet.Please check network connection")
            btnSwitch.isChecked = !b
        }
        if(b){
            if (status == EnumTwoFactoryAuthentication.DISABLE || status == EnumTwoFactoryAuthentication.ENABLE){
                btnChange.text = getString(R.string.change_secret_pin)
            }
        }
        Utils.Log(TAG,"onCheckedChanged $b")
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                /*Do something here*/
                if (status==EnumTwoFactoryAuthentication.REQUEST_GENERATE){
                    viewModel.isEnabled = true
                    addTwoFactoryAuthentication()
                    Utils.hideSoftKeyboard(this)
                }
                else if (status ==EnumTwoFactoryAuthentication.REQUEST_CHANGE){
                    changeTwoFactoryAuthentication()
                    Utils.hideSoftKeyboard(this)
                }else {
                    edtSecretPin.visibility = View.VISIBLE
                    btnChange.text =  getString(R.string.change)
                }
                return true
            }
            return false
        }
        return false
    }

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.newSecretPin = s.toString()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG,"onResume!!!")
    }

}