package co.tpcreative.supersafe.ui.fakepin
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePin
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.FakePinComponentViewModel
import kotlinx.android.synthetic.main.activity_fake_pin_component.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FakePinComponentAct : BaseActivityNoneSlideFakePin(), FakePinComponentAdapter.ItemSelectedListener, SingletonFakePinComponent.SingletonPrivateFragmentListener {
    var adapter: FakePinComponentAdapter? = null
    lateinit var viewModel : FakePinComponentViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_pin_component)
        initUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    SingletonFakePinComponent.getInstance().onUpdateView()
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val mData: ArrayList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    mData?.let {
                        val list: MutableList<MainCategoryModel>? = SQLHelper.getListFakePin()
                        if (list == null) {
                            Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                            return
                        }
                        val mCategory: MainCategoryModel = list[0]
                        val mResult = mCategory.let { it1 -> Utils.getDataItemsFromImport(it1,it) }
                        importingData(mResult)
                    }

                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
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
        SingletonFakePinComponent.getInstance().setListener(this)
        onRegisterHomeWatcher()
        SingletonManager.getInstance().setVisitFakePin(true)
        ServiceManager.getInstance()?.setRequestShareIntent(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceManager.getInstance()?.onDismissServices()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        SingletonManager.getInstance().setVisitFakePin(false)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }
    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onUpdateView() {
        runOnUiThread(Runnable { getData() })
    }

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "Position :$position")
        try {
            val value: String = Utils.getHexCode(getString(R.string.key_trash))
            if (value == dataSource[position].categories_hex_name) {
                Navigator.onMoveTrash(this)
            } else {
                val mainCategories: MainCategoryModel = dataSource[position]
                Navigator.onMoveAlbumDetail(this, mainCategories)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSetting(position: Int) {
        Navigator.onAlbumSettings(this, dataSource[position])
    }

    override fun onDeleteAlbum(position: Int) {
        deleteAlbum(position)
    }

    override fun onEmptyTrash(position: Int) {}
    override fun onBackPressed() {
        if (speedDial?.isOpen!!) {
            speedDial?.close()
        } else {
            super.onBackPressed()
        }
    }

    val dataSource : MutableList<MainCategoryModel>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }
    companion object {
        private val TAG = FakePinComponentAct::class.java.simpleName
    }
}