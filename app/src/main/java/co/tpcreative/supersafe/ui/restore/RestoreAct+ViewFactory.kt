package co.tpcreative.supersafe.ui.restore
import android.animation.ObjectAnimator
import android.view.View
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_restore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun RestoreAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    edtPreviousPIN?.addTextChangedListener(mTextWatcher)
    edtPreviousPIN?.setOnEditorActionListener(this)

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

fun RestoreAct.onRestore()  = CoroutineScope(Dispatchers.Main).launch{
    val mUser = SuperSafeApplication.getInstance().readUseSecret()
    onStartProgressing()
    Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSuperSafeBackup(), SuperSafeApplication.getInstance().getSuperSafeDataBaseFolder(), object : ServiceManager.ServiceManagerSyncDataListener {
        override fun onCompleted() {
            Utils.Log(TAG, "Exporting successful")
            Utils.setUserPreShare(mUser)
            Navigator.onMoveToMainTab(this@onRestore,false)
            finish()
        }
        override fun onError() {
            Utils.Log(TAG, "Exporting error")
        }

        override fun onCancel() {
        }
    })
    ServiceManager.getInstance()?.onStartService()
    onStopProgressing()
}

fun RestoreAct.shake() {
    val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(edtPreviousPIN, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).setDuration(1000)
    objectAnimator.start()
}

fun RestoreAct.onStopProgressing() {
    Utils.Log(TAG, "onStopProgressing")
    try {
        SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
    } catch (e: Exception) {
        Utils.Log(TAG, e.message+"")
    }
}

fun RestoreAct.onStartProgressing() {
    try {
        SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.progressing)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}