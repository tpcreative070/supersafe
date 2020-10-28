package co.tpcreative.supersafe.ui.resetpin
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.SingletonResetPin
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.google.gson.Gson
import com.snatik.storage.security.SecurityUtil
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable
import kotlinx.android.synthetic.main.activity_reset_pin.*

fun ResetPinAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = ResetPinPresenter()
    presenter?.bindView(this)
    if (presenter?.mUser != null) {
        val email: String? = presenter?.mUser?.email
        if (email != null) {
            val result: String? = Utils.getFontString(R.string.request_an_access_code, email)
            tvStep1?.text = result?.toSpanned()
            val support: String? = Utils.getFontString(R.string.send_an_email_to, SecurityUtil.MAIL)
            tvSupport?.text = support?.toSpanned()
        }
    }
    edtCode?.addTextChangedListener(mTextWatcher)
    edtCode?.setOnEditorActionListener(this)
    try {
        val bundle: Bundle? = getIntent().getExtras()
        isRestoreFiles = bundle?.get(ResetPinAct::class.java.simpleName) as Boolean
    } catch (e: Exception) {
        e.printStackTrace()
    }

    btnReset.setOnClickListener {
        onVerifyCode()
    }

    btnSendRequest.setOnClickListener {
        btnSendRequest?.isEnabled = false
        btnSendRequest?.setText("")
        onStartLoading(EnumStatus.OTHER)
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.email != null) {
                val request = VerifyCodeRequest()
                request.user_id = presenter?.mUser?.email
                presenter?.onRequestCode(request)
            }
        }
    }

    llSupport.setOnClickListener {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + SecurityUtil.MAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, "SuperSafe App Support")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}

fun ResetPinAct.setProgressValue() {
    var circularProgressDrawable: CircularProgressDrawable? = null
    val b = CircularProgressDrawable.Builder(this)
            .colors(resources.getIntArray(R.array.gplus_colors))
            .sweepSpeed(2f)
            .rotationSpeed(2f)
            .strokeWidth(Utils.dpToPx(3).toFloat())
            .style(CircularProgressDrawable.STYLE_ROUNDED)
    progressbar_circular?.indeterminateDrawable = b.build().also { circularProgressDrawable = it }
    // /!\ Terrible hack, do not do this at home!
    progressbar_circular?.width?.let {
        circularProgressDrawable?.setBounds(0,
                0,
                it,
                progressbar_circular?.height!!)
    }
    progressbar_circular?.visibility = View.INVISIBLE
    progressbar_circular?.visibility = View.VISIBLE
    Utils.Log(TAG, "Action here set progress")
}


fun ResetPinAct.onVerifyCode() {
    if (isNext) {
        val code: String = edtCode?.text.toString().trim({ it <= ' ' })
        val request = VerifyCodeRequest()
        request.code = code
        request.user_id = presenter?.mUser?.email
        request._id = presenter?.mUser?._id
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        presenter?.onVerifyCode(request)
    }
}

fun ResetPinAct.onShowDialogWaitingCode() {
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    val mUser: User? = Utils.getUserInfo()
    Utils.Log(TAG, "Preparing " + Gson().toJson(mUser))
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.send_code_later)
            .setDescription(R.string.send_code_later_detail)
            .setHeaderDrawable(R.drawable.baseline_email_white_48)
            .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
            .setHeaderColor(themeApp?.getPrimaryColor()!!)
            .setCancelable(true)
            .setPositiveText(R.string.continue_value)
            .setNegativeText(R.string.cancel)
            .setCheckBox(false, R.string.enable_cloud)
            .onPositive { dialog, which ->
                Utils.Log(TAG, "positive")
                val mUser: User? = Utils.getUserInfo()
                Utils.Log(TAG, "Pressed " + Gson().toJson(mUser))
                SingletonResetPin.getInstance()?.onStartTimer(300000)
            }
            .onNegative { dialog, which -> finish() }
            .show()
}


