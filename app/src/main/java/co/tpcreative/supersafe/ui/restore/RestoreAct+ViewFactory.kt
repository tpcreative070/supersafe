package co.tpcreative.supersafe.ui.restore
import android.animation.ObjectAnimator
import android.view.View
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import dmax.dialog.SpotsDialog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_restore.*

fun RestoreAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    edtPreviousPIN?.addTextChangedListener(mTextWatcher)
    edtPreviousPIN?.setOnEditorActionListener(this)
    presenter = RestorePresenter()
    presenter?.bindView(this)
    presenter?.onGetData()

    btnRestoreNow.setOnClickListener {
        val pin: String? = SuperSafeApplication.getInstance().readKey()
        if (pin == edtPreviousPIN?.getText().toString()) {
            Utils.hideKeyboard(currentFocus)
            onStartProgressing()
            Utils.onObserveData(2000, object : Listener {
                override fun onStart() {
                    onRestore()
                }
            })
        } else {
            edtPreviousPIN?.setText("")
            tvWrongPin?.visibility = View.VISIBLE
            shake()
            Utils.hideKeyboard(currentFocus)
            count += 1
            if (count >= 4) {
                btnForgotPin?.visibility = View.VISIBLE
            }
        }
    }

    btnForgotPin.setOnClickListener {
        Navigator.onMoveToForgotPin(this, true)
    }
}

fun RestoreAct.onRestore() {
    subscriptions = Observable.create<Any?> { subscriber: ObservableEmitter<Any?>? ->
        Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeBackup(), SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), object : ServiceManager.ServiceManagerSyncDataListener {
            override fun onCompleted() {
                subscriber?.onNext(true)
                subscriber?.onComplete()
                Utils.Log(TAG, "Exporting successful")
                Utils.setUserPreShare(presenter?.mUser)
                Navigator.onMoveToMainTab(this@onRestore,false)
                finish()
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
    }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe {
                ServiceManager.getInstance()?.onStartService()
                onStopProgressing()
            }
}

fun RestoreAct.shake() {
    val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(edtPreviousPIN, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
    objectAnimator.start()
}

fun RestoreAct.onStopProgressing() {
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

fun RestoreAct.onStartProgressing() {
    try {
        runOnUiThread(Runnable {
            if (dialog == null) {
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                dialog = SpotsDialog.Builder()
                        .setContext(this)
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