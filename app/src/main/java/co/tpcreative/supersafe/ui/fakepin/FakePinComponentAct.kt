package co.tpcreative.supersafe.ui.fakepin
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePin
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.android.synthetic.main.activity_fake_pin_component.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FakePinComponentAct : BaseActivityNoneSlideFakePin(), BaseView<EmptyModel>, FakePinComponentAdapter.ItemSelectedListener, SingletonFakePinComponent.SingletonPrivateFragmentListener {
    var adapter: FakePinComponentAdapter? = null
    var presenter: FakePinComponentPresenter? = null
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
                    val images: ArrayList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    val mListImport: MutableList<ImportFilesModel> = ArrayList<ImportFilesModel>()
                    var i = 0
                    val l = images?.size
                    while (i < l!!) {
                        val path = images.get(i).path
                        val name = images.get(i).name
                        val id = "" + images.get(i).id
                        val mimeType: String? = Utils.getMimeType(path)
                        Utils.Log(TAG, "mimeType $mimeType")
                        Utils.Log(TAG, "name $name")
                        Utils.Log(TAG, "path $path")
                        val fileExtension: String? = Utils.getFileExtension(path)
                        Utils.Log(TAG, "file extension " + Utils.getFileExtension(path))
                        try {
                            val mimeTypeFile: MimeTypeFile = Utils.mediaTypeSupport().get(fileExtension)
                                    ?: return
                            mimeTypeFile.name = name
                            val list: MutableList<MainCategoryModel>? = SQLHelper.getListFakePin()
                            if (list == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val importFiles = ImportFilesModel(list[0], mimeTypeFile, path, 0, false)
                            mListImport.add(importFiles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }
                    ServiceManager.getInstance()?.setListImport(mListImport)
                    ServiceManager.getInstance()?.onPreparingImportData()
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
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        presenter?.getData()
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
        presenter?.unbindView()
        SingletonManager.getInstance().setVisitFakePin(false)
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }
    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onUpdateView() {
        runOnUiThread(Runnable { presenter?.getData() })
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                adapter?.setDataSource(presenter?.mList)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "Position :$position")
        try {
            val value: String = Utils.getHexCode(getString(R.string.key_trash))
            if (value == presenter?.mList?.get(position)?.categories_hex_name) {
                getActivity()?.let { Navigator.onMoveTrash(it) }
            } else {
                val mainCategories: MainCategoryModel? = presenter?.mList?.get(position)
                getActivity()?.let {
                    if (mainCategories != null) {
                        Navigator.onMoveAlbumDetail(it, mainCategories)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSetting(position: Int) {
        getActivity()?.let { presenter?.mList?.get(position)?.let { it1 -> Navigator.onAlbumSettings(it, it1) } }
    }

    override fun onDeleteAlbum(position: Int) {
        presenter?.onDeleteAlbum(position)
    }

    override fun onEmptyTrash(position: Int) {}
    override fun onBackPressed() {
        if (speedDial?.isOpen!!) {
            speedDial?.close()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private val TAG = FakePinComponentAct::class.java.simpleName
    }
}