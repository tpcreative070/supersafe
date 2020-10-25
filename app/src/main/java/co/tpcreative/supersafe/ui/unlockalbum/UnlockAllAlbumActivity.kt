package co.tpcreative.supersafe.ui.unlockalbum
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UnlockAllAlbumActivity : BaseActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    @BindView(R.id.tvStep1)
    var tvStep1: AppCompatTextView? = null

    @BindView(R.id.edtCode)
    var edtCode: AppCompatEditText? = null

    @BindView(R.id.btnSendRequest)
    var btnSendRequest: AppCompatButton? = null

    @BindView(R.id.btnUnlock)
    var btnUnlock: AppCompatButton? = null

    @BindView(R.id.progressbar_circular)
    var mCircularProgressBar: CircularProgressBar? = null

    @BindView(R.id.progressbar_circular_unlock_albums)
    var mCircularProgressBarUnlock: CircularProgressBar? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null
    private var presenter: UnlockAllAlbumPresenter? = null
    private var isNext = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock_all_album)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = UnlockAllAlbumPresenter()
        presenter?.bindView(this)
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            val email = mUser.email
            if (email != null) {
                val result: String? = Utils.getFontString(R.string.request_an_access_code, email)
                tvStep1?.setText(result?.toSpanned())
            }
        }
        edtCode?.addTextChangedListener(mTextWatcher)
        edtCode?.setOnEditorActionListener(this)
        tvPremiumDescription?.setText(getString(R.string.unlock_all_album_title))
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
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    fun setProgressValue(status: EnumStatus?) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                var circularProgressDrawable: CircularProgressDrawable? = null
                val b = CircularProgressDrawable.Builder(this)
                        .colors(getResources().getIntArray(R.array.gplus_colors))
                        .sweepSpeed(2f)
                        .rotationSpeed(2f)
                        .strokeWidth(Utils.dpToPx(3).toFloat())
                        .style(CircularProgressDrawable.STYLE_ROUNDED)
                mCircularProgressBar?.setIndeterminateDrawable(b.build().also { circularProgressDrawable = it })
                // /!\ Terrible hack, do not do this at home!
                circularProgressDrawable?.setBounds(0,
                        0,
                        mCircularProgressBar?.getWidth()!!,
                        mCircularProgressBar?.getHeight()!!)
                mCircularProgressBar?.setVisibility(View.VISIBLE)
            }
            EnumStatus.UNLOCK_ALBUMS -> {
                var circularProgressDrawable: CircularProgressDrawable? = null
                val b = CircularProgressDrawable.Builder(this)
                        .colors(getResources().getIntArray(R.array.gplus_colors))
                        .sweepSpeed(2f)
                        .rotationSpeed(2f)
                        .strokeWidth(Utils.dpToPx(3).toFloat())
                        .style(CircularProgressDrawable.STYLE_ROUNDED)
                mCircularProgressBarUnlock?.setIndeterminateDrawable(b.build().also { circularProgressDrawable = it })
                // /!\ Terrible hack, do not do this at home!
                circularProgressDrawable?.setBounds(0,
                        0,
                        mCircularProgressBarUnlock?.getWidth()!!,
                        mCircularProgressBarUnlock?.getHeight()!!)
                mCircularProgressBarUnlock?.setVisibility(View.VISIBLE)
            }
        }
    }

    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnUnlock?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnUnlock?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnUnlock?.setEnabled(true)
                true
            } else {
                btnUnlock?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                btnUnlock?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnUnlock?.setEnabled(false)
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    fun onVerifyCode() {
        if (isNext) {
            val code: String = edtCode?.getText().toString().trim({ it <= ' ' })
            val request = VerifyCodeRequest()
            val mUser: User? = Utils.getUserInfo()
            if (mUser != null) {
                request.code = code
                request.user_id = mUser.email
                request._id = mUser._id
                request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
                presenter?.onVerifyCode(request)
            }
        }
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            return false
        }
        return false
    }

    @OnClick(R.id.btnUnlock)
    fun onUnlockAlbums(view: View?) {
        Utils.Log(TAG, "Action")
        if (isNext) {
            btnUnlock?.setText("")
            onStartLoading(EnumStatus.UNLOCK_ALBUMS)
            Utils.Log(TAG, "onUnlockAlbums")
            btnUnlock?.setEnabled(false)
            onVerifyCode()
        }
    }

    @OnClick(R.id.btnSendRequest)
    fun onSentRequest() {
        btnSendRequest?.setEnabled(false)
        btnSendRequest?.setText("")
        onStartLoading(EnumStatus.REQUEST_CODE)
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            if (mUser.email != null) {
                val request = VerifyCodeRequest()
                request.user_id = mUser.email
                presenter?.onRequestCode(request)
            } else {
                Toast.makeText(this, "Email is null", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Email is null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartLoading(status: EnumStatus) {
        setProgressValue(status)
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                if (mCircularProgressBar != null) {
                    mCircularProgressBar?.progressiveStop()
                }
            }
            EnumStatus.UNLOCK_ALBUMS -> {
                if (mCircularProgressBarUnlock != null) {
                    mCircularProgressBarUnlock?.progressiveStop()
                }
            }
        }
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        onStopLoading(EnumStatus.REQUEST_CODE)
        onStopLoading(EnumStatus.UNLOCK_ALBUMS)
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.setText(getString(R.string.send_verification_code))
                btnSendRequest?.setEnabled(true)
                btnUnlock?.setText(getString(R.string.unlock_all_albums))
                btnUnlock?.setEnabled(true)
                Utils.showGotItSnackbar(tvStep1!!, message!!)
            }
            EnumStatus.SEND_EMAIL -> {
                Utils.showGotItSnackbar(tvStep1!!, message!!)
            }
            EnumStatus.VERIFY -> {
                btnUnlock?.setText(getString(R.string.unlock_all_albums))
                Utils.showGotItSnackbar(tvStep1!!, message!!)
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.setEnabled(false)
            }
            EnumStatus.SEND_EMAIL -> {
                onStopLoading(EnumStatus.REQUEST_CODE)
                btnSendRequest?.setText(getString(R.string.send_verification_code))
                Toast.makeText(this, "Sent the code to your email. Please check it", Toast.LENGTH_SHORT).show()
            }
            EnumStatus.VERIFY -> {
                btnUnlock?.setText(getString(R.string.unlock_all_albums))
                onStopLoading(EnumStatus.UNLOCK_ALBUMS)
                if (presenter?.mListCategories != null) {
                    var i = 0
                    while (i < presenter?.mListCategories?.size!!) {
                        presenter?.mListCategories?.get(i)?.pin = ""
                        presenter?.mListCategories?.get(i)?.let { SQLHelper.updateCategory(it) }
                        i++
                    }
                }
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                Utils.showGotItSnackbar(getCurrentFocus()!!, R.string.unlocked_successful, object : ServiceManager.ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        finish()
                    }

                    override fun onError() {}
                    override fun onCancel() {}
                })
            }
            else -> {
                btnSendRequest?.setEnabled(true)
                btnUnlock?.setEnabled(true)
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        private val TAG = UnlockAllAlbumActivity::class.java.simpleName
    }
}