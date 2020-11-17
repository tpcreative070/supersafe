package co.tpcreative.supersafe.ui.unlockalbum
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.util.UtilsListener
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.UnlockAllAlbumViewModel
import kotlinx.android.synthetic.main.activity_unlock_all_album.*
import kotlinx.android.synthetic.main.activity_unlock_all_album.edtCode
import kotlinx.android.synthetic.main.activity_unlock_all_album.toolbar
import kotlinx.android.synthetic.main.layout_premium_header.*

fun UnlockAllAlbumAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val mUser: User? = Utils.getUserInfo()
    if (mUser != null) {
        val email = mUser.email
        if (email != null) {
            val result: String? = Utils.getFontString(R.string.request_an_access_code, email)
            tvStep1?.text = result?.toSpanned()
        }
    }
    edtCode?.addTextChangedListener(mTextWatcher)
    edtCode?.setOnEditorActionListener(this)
    tvPremiumDescription?.text = getString(R.string.unlock_all_album_title)

    btnUnlock.setOnClickListener {
        Utils.Log(TAG, "Action")
        if (isNext) {
            btnUnlock?.setText("")
            verifyCode()
        }
    }
    btnSendRequest.setOnClickListener {
        btnSendRequest?.isEnabled = false
        btnSendRequest?.setText("")
        if (Utils.getUserId() != null) {
            resendCode()
        } else {
            Utils.onBasicAlertNotify(this,"Alert","Email is null")
        }
    }

    viewModel.isLoading.observe(this,{
        if (it){
            when (progressing) {
                EnumStepProgressing.RESEND_CODE -> {
                    progressbar_circular?.visibility = View.VISIBLE
                    btnSendRequest.visibility = View.INVISIBLE
                }
                EnumStepProgressing.UNLOCK_ALBUMS -> {
                    progressbar_circular_unlock_albums?.visibility = View.VISIBLE
                    btnUnlock.visibility = View.INVISIBLE
                }
                else -> Utils.Log(TAG,"Nothing to stop")
            }
        }else{
            when (progressing) {
                EnumStepProgressing.SEND_CODE-> {
                    progressbar_circular?.visibility = View.INVISIBLE
                    btnSendRequest.visibility = View.VISIBLE
                }
                EnumStepProgressing.RESEND_CODE -> {
                    progressbar_circular?.visibility = View.INVISIBLE
                    btnSendRequest.visibility = View.VISIBLE
                }
                EnumStepProgressing.UNLOCK_ALBUMS -> {
                    progressbar_circular_unlock_albums?.visibility = View.INVISIBLE
                    btnUnlock.visibility = View.VISIBLE
                }
                else -> Utils.Log(TAG,"Nothing to stop")
            }
        }
    })

    viewModel.errorMessages.observe(this,{mResult ->
        mResult?.let {
            if (it.values.isEmpty()){
                btnUnlock?.setBackgroundResource(R.drawable.bg_button_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(this,R.color.white))
                btnUnlock?.isEnabled = true
                isNext = true
            }else{
                btnUnlock?.setBackgroundResource(R.drawable.bg_button_disable_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                btnUnlock?.isEnabled = false
                isNext = false
            }
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtCode.error = it.get(EnumValidationKey.EDIT_TEXT_CODE.name)
            if (it.values.isEmpty()){
                btnUnlock?.setBackgroundResource(R.drawable.bg_button_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(this,R.color.white))
                btnUnlock?.isEnabled = true
                isNext = true
            }else{
                btnUnlock?.setBackgroundResource(R.drawable.bg_button_disable_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                btnUnlock?.isEnabled = false
                isNext = false
            }
        }
    })
}

fun UnlockAllAlbumAct.verifyCode() {
    progressing = EnumStepProgressing.VERIFY_CODE
    btnUnlock.isEnabled = false
    viewModel.verifyCode().observe(this, Observer{ it ->
        when(it.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Success ${it.toJson()}")
                btnUnlock?.text = getString(R.string.unlock_all_albums)
                val mList = SQLHelper.getListCategories(false);
                var i = 0
                while (i < mList?.size!!) {
                    mList[i].pin = ""
                    mList[i].let { SQLHelper.updateCategory(it) }
                    i++
                }
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                Utils.onAlertNotify(this,"Unlocked Album",getString(R.string.unlocked_successful),object  : UtilsListener {
                    override fun onNegative() {
                        TODO("Not yet implemented")
                    }
                    override fun onPositive() {
                        finish()
                    }
                })
            }
            Status.ERROR -> {
                btnUnlock.isEnabled = true
                btnUnlock?.text = getString(R.string.unlock_all_albums)
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
    Utils.hideSoftKeyboard(this)
}

private fun UnlockAllAlbumAct.resendCode(){
    btnSendRequest.isEnabled = false
    progressing = EnumStepProgressing.RESEND_CODE
    viewModel.resendCode(EnumStatus.UNLOCK_ALBUMS).observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                btnSendRequest?.text = getString(R.string.send_verification_code)
                Utils.onBasicAlertNotify(this,message = "Code has been sent to your email. Please check it",title = "Alert")
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                btnSendRequest?.text = getString(R.string.send_verification_code)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
        btnSendRequest.isEnabled = true
    })
}

private fun UnlockAllAlbumAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(UnlockAllAlbumViewModel::class.java)
}
