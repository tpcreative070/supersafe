package co.tpcreative.supersafe.ui.unlockalbum
import android.view.View
import android.widget.Toast
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable
import kotlinx.android.synthetic.main.activity_unlock_all_album.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun UnlockAllAlbumAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = UnlockAllAlbumPresenter()
    presenter?.bindView(this)
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
            onStartLoading(EnumStatus.UNLOCK_ALBUMS)
            Utils.Log(TAG, "onUnlockAlbums")
            btnUnlock?.isEnabled = false
            onVerifyCode()
        }
    }

    btnSendRequest.setOnClickListener {
        btnSendRequest?.isEnabled = false
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
}

fun UnlockAllAlbumAct.setProgressValue(status: EnumStatus?) {
    when (status) {
        EnumStatus.REQUEST_CODE -> {
            var circularProgressDrawable: CircularProgressDrawable? = null
            val b = CircularProgressDrawable.Builder(this)
                    .colors(resources.getIntArray(R.array.gplus_colors))
                    .sweepSpeed(2f)
                    .rotationSpeed(2f)
                    .strokeWidth(Utils.dpToPx(3).toFloat())
                    .style(CircularProgressDrawable.STYLE_ROUNDED)
            progressbar_circular?.indeterminateDrawable = b.build().also { circularProgressDrawable = it }
            // /!\ Terrible hack, do not do this at home!
            circularProgressDrawable?.setBounds(0,
                    0,
                    progressbar_circular?.width!!,
                    progressbar_circular?.height!!)
            progressbar_circular?.visibility = View.VISIBLE
        }
        EnumStatus.UNLOCK_ALBUMS -> {
            var circularProgressDrawable: CircularProgressDrawable? = null
            val b = CircularProgressDrawable.Builder(this)
                    .colors(resources.getIntArray(R.array.gplus_colors))
                    .sweepSpeed(2f)
                    .rotationSpeed(2f)
                    .strokeWidth(Utils.dpToPx(3).toFloat())
                    .style(CircularProgressDrawable.STYLE_ROUNDED)
            progressbar_circular_unlock_albums?.indeterminateDrawable = b.build().also { circularProgressDrawable = it }
            // /!\ Terrible hack, do not do this at home!
            circularProgressDrawable?.setBounds(0,
                    0,
                    progressbar_circular_unlock_albums?.width!!,
                    progressbar_circular_unlock_albums?.height!!)
            progressbar_circular_unlock_albums?.visibility = View.VISIBLE
        }
    }
}
