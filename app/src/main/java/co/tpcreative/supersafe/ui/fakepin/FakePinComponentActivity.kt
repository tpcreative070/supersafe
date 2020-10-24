package co.tpcreative.supersafe.ui.fakepin
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePin
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.ui.fakepin.FakePinComponentAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FakePinComponentActivity : BaseActivityNoneSlideFakePin(), BaseView<EmptyModel>, FakePinComponentAdapter.ItemSelectedListener, SingletonFakePinComponent.SingletonPrivateFragmentListener {
    @BindView(R.id.speedDial)
    var mSpeedDialView: SpeedDialView? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null
    private var adapter: FakePinComponentAdapter? = null
    private var presenter: FakePinComponentPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_pin_component)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(false)
        initSpeedDial()
        initRecycleView(getLayoutInflater())
        presenter = FakePinComponentPresenter()
        presenter?.bindView(this)
    }

    fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = FakePinComponentAdapter(layoutInflater, this, this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getContext(), 2)
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(GridSpacingItemDecoration(2, 10, true))
        recyclerView?.setItemAnimator(DefaultItemAnimator())
        recyclerView?.setAdapter(adapter)
    }

    private fun initSpeedDial() {
        val mThemeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
        var drawable: Drawable? = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24)
        mSpeedDialView?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp?.getPrimaryColor()!!,
                        getTheme()))
                .setLabel(getString(R.string.camera))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create())
        drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
        mSpeedDialView?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp?.getPrimaryColor()!!,
                        getTheme()))
                .setLabel(R.string.photo)
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create())
        mSpeedDialView?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp?.getPrimaryColor()!!,
                        getTheme()))
                .setLabel(getString(R.string.album))
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create())
        mSpeedDialView?.setMainFabAnimationRotateAngle(180f)

        //Set main action clicklistener.
        mSpeedDialView?.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                return false // True to keep the Speed Dial open
            }

            override fun onToggleChanged(isOpen: Boolean) {
                Utils.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
            }
        })

        //Set option fabs clicklisteners.
        mSpeedDialView?.setOnActionSelectedListener(object : SpeedDialView.OnActionSelectedListener {
            override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
                when (actionItem?.getId()) {
                    R.id.fab_album -> {
                        onShowDialog()
                        return false // false will close it without animation
                    }
                    R.id.fab_photo -> {
                        Navigator.onMoveToAlbum(this@FakePinComponentActivity)
                        return false // closes without animation (same as mSpeedDialView.close(false); return false;)
                    }
                    R.id.fab_camera -> {
                        onAddPermissionCamera()
                        return false
                    }
                }
                return true // To keep the Speed Dial open
            }
        })
    }

    fun onShowDialog() {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(ContextCompat.getColor(this,R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, object : MaterialDialog.InputCallback {
                    override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                        Utils.Log(TAG, "Value")
                        val value = input.toString()
                        val base64Code: String = Utils.getHexCode(value)
                        val item: MainCategoryModel? = SQLHelper.getTrashItem()
                        val result: String? = item?.categories_hex_name
                        if (base64Code == result) {
                            Toast.makeText(this@FakePinComponentActivity, "This name already existing", Toast.LENGTH_SHORT).show()
                        } else {
                            val response: Boolean = SQLHelper.onAddFakePinCategories(base64Code, value, true)
                            if (response) {
                                Toast.makeText(this@FakePinComponentActivity, "Created album successful", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@FakePinComponentActivity, "Album name already existing", Toast.LENGTH_SHORT).show()
                            }
                            presenter?.getData()
                        }
                    }
                })
        builder.show()
    }

    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted()!!) {
                            val list: MutableList<MainCategoryModel>? = SQLHelper.getListFakePin()
                            if (list != null) {
                                Navigator.onMoveCamera(this@FakePinComponentActivity, list[0])
                            }
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    SingletonFakePinComponent.Companion.getInstance().onUpdateView()
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val images: ArrayList<Image>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
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

    protected override fun onResume() {
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
        SingletonManager.Companion.getInstance().setVisitFakePin(false)
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
        return getApplicationContext()
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
        if (mSpeedDialView?.isOpen()!!) {
            mSpeedDialView?.close()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private val TAG = FakePinComponentActivity::class.java.simpleName
    }
}