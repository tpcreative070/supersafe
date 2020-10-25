package co.tpcreative.supersafe.ui.restore
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import com.rengwuxian.materialedittext.MaterialEditText
import dmax.dialog.SpotsDialog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class RestoreActivity : BaseActivity(), TextView.OnEditorActionListener, BaseView<EmptyModel> {
    @BindView(R.id.edtPreviousPIN)
    var edtPreviousPIN: MaterialEditText? = null

    @BindView(R.id.btnForgotPin)
    var btnForgotPin: AppCompatButton? = null

    @BindView(R.id.btnRestoreNow)
    var btnRestoreNow: AppCompatButton? = null

    @BindView(R.id.tvWrongPin)
    var tvWrongPin: AppCompatTextView? = null
    private var dialog: AlertDialog? = null
    private var isNext = false
    private var presenter: RestorePresenter? = null
    private var count = 0
    private var subscriptions: Disposable? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        edtPreviousPIN?.addTextChangedListener(mTextWatcher)
        edtPreviousPIN?.setOnEditorActionListener(this)
        presenter = RestorePresenter()
        presenter?.bindView(this)
        presenter?.onGetData()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                    dialog = SpotsDialog.Builder()
                            .setContext(this@RestoreActivity)
                            .setDotColor(themeApp?.getAccentColor()!!)
                            .setMessage(getString(R.string.progressing))
                            .setCancelable(true)
                            .build()
                }
                if (!dialog?.isShowing()!!) {
                    dialog?.show()
                    Utils.Log(TAG, "Showing dialog...")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onStopProgressing() {
        Utils.Log(TAG, "onStopProgressing")
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

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.Companion.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                val pin: String? = SuperSafeApplication.Companion.getInstance().readKey()
                if (pin == edtPreviousPIN?.getText().toString()) {
                    Utils.hideKeyboard(getCurrentFocus())
                    onStartProgressing()
                    Utils.onObserveData(2000, object :Listener {
                        override fun onStart() {
                            onRestore()
                        }
                    })
                } else {
                    edtPreviousPIN?.setText("")
                    tvWrongPin?.setVisibility(View.VISIBLE)
                    shake()
                    Utils.hideKeyboard(getCurrentFocus())
                    count += 1
                    if (count >= 4) {
                        btnForgotPin?.setVisibility(View.VISIBLE)
                    }
                }
                return true
            }
            return false
        }
        return false
    }

    @OnClick(R.id.btnRestoreNow)
    fun onRestoreNow(view: View?) {
        val pin: String? = SuperSafeApplication.getInstance().readKey()
        if (pin == edtPreviousPIN?.getText().toString()) {
            Utils.hideKeyboard(getCurrentFocus())
            onStartProgressing()
            Utils.onObserveData(2000, object :Listener {
                override fun onStart() {
                    onRestore()
                }
            })
        } else {
            edtPreviousPIN?.setText("")
            tvWrongPin?.setVisibility(View.VISIBLE)
            shake()
            Utils.hideKeyboard(getCurrentFocus())
            count += 1
            if (count >= 4) {
                btnForgotPin?.setVisibility(View.VISIBLE)
            }
        }
    }

    @OnClick(R.id.btnForgotPin)
    fun onForgotPIN(view: View?) {
        Navigator.onMoveToForgotPin(this, true)
    }

    fun onRestore() {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
            Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeBackup(), SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), object : ServiceManager.ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    subscriber?.onNext(true)
                    subscriber?.onComplete()
                    Utils.Log(TAG, "Exporting successful")
                    Utils.setUserPreShare(presenter?.mUser)
                    Navigator.onMoveToMainTab(this@RestoreActivity)
                }

                override fun onError() {
                    Utils.Log(TAG, "Exporting error")
                    subscriber?.onNext(true)
                    subscriber?.onComplete()
                }

                override fun onCancel() {
                    subscriber?.onNext(true)
                    subscriber?.onComplete()
                }
            })
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    ServiceManager.Companion.getInstance()?.onStartService()
                    onStopProgressing()
                }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        if (subscriptions != null) {
            subscriptions?.dispose()
        }
    }

    protected override fun onStopListenerAWhile() {}

    /*Detecting textWatch*/
    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            if (Utils.isValid(value)) {
                btnRestoreNow?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnRestoreNow?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                isNext = true
                tvWrongPin?.setVisibility(View.INVISIBLE)
            } else {
                btnRestoreNow?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                btnRestoreNow?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                isNext = false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    private fun shake() {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(edtPreviousPIN, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
        objectAnimator.start()
    }

    companion object {
        private val TAG = RestoreActivity::class.java.simpleName
    }
}