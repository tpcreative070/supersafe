package co.tpcreative.supersafe.ui.settings
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.extension.instantiate
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumSettingsAct : BaseActivity(), BaseView<EmptyModel> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_settings)
        initUI()
        onSetUpPreference()
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
        presenter?.getData()
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

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return applicationContext
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var mName: MyPreferenceAlbumSettings? = null
        private var mLockAlbum: MyPreferenceAlbumSettings? = null
        private var mAlbumCover: MyPreferenceAlbumSettings? = null

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type [ ]
         */
        private fun createChangeListener(): Preference.OnPreferenceChangeListener? {
            return Preference.OnPreferenceChangeListener { preference, newValue -> true }
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener? {
            return Preference.OnPreferenceClickListener { preference ->
                if (preference is Preference) {
                    if (preference.key == getString(R.string.key_name)) {
                        val main: String = Utils.getHexCode(getString(R.string.key_main_album))
                        val trash: String = Utils.getHexCode(getString(R.string.key_trash))
                        val name = preference.summary.toString()
                        if (main != presenter?.mMainCategories?.categories_hex_name && trash != presenter?.mMainCategories?.categories_hex_name) {
                            onShowChangeCategoriesNameDialog(EnumStatus.CHANGE, name)
                        }
                    } else if (preference.key == getString(R.string.key_album_lock)) {
                        val name = preference.summary.toString()
                        onShowChangeCategoriesNameDialog(EnumStatus.SET, null)
                    } else if (preference.key == getString(R.string.key_album_cover)) {
                        val main: MainCategoryModel? = presenter?.mMainCategories
                        if (main?.pin == "") {
                            presenter?.mMainCategories?.let { Navigator.onMoveAlbumCover(getActivity()!!, it) }
                        }
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*change categories name*/mName = findPreference(getString(R.string.key_name)) as MyPreferenceAlbumSettings?
            mName?.onPreferenceChangeListener = createChangeListener()
            mName?.onPreferenceClickListener = createActionPreferenceClickListener()
            mName?.summary = presenter?.mMainCategories?.categories_name
            mLockAlbum = findPreference(getString(R.string.key_album_lock)) as MyPreferenceAlbumSettings?
            mLockAlbum?.onPreferenceChangeListener = createChangeListener()
            mLockAlbum?.onPreferenceClickListener = createActionPreferenceClickListener()
            val isPin: String? = presenter?.mMainCategories?.pin
            if (isPin == "") {
                mLockAlbum?.summary = getString(R.string.unlocked)
            } else {
                mLockAlbum?.summary = getString(R.string.locked)
            }

            /*Album cover*/mAlbumCover = findPreference(getString(R.string.key_album_cover)) as MyPreferenceAlbumSettings?
            mAlbumCover?.onPreferenceClickListener = createActionPreferenceClickListener()
            mAlbumCover?.onPreferenceChangeListener = createChangeListener()
            mAlbumCover?.onUpdatedView = {
                if (mAlbumCover?.imageViewCover != null) {
                    val main: MainCategoryModel? = presenter?.mMainCategories
                    if (main?.pin == "") {
                        val items: ItemModel? = SQLHelper.getItemId(main?.items_id)
                        if (items != null) {
                            when (EnumFormatType.values()[items.formatType]) {
                                EnumFormatType.AUDIO -> {
                                    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                                    val note1: Drawable? = ContextCompat.getDrawable(context!!,themeApp?.getAccentColor()!!)
                                    Glide.with(context!!)
                                            .load(note1)
                                            .apply(options)
                                            .into(mAlbumCover?.imageViewCover!!)
                                    mAlbumCover?.imgViewSuperSafe?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                                }
                                EnumFormatType.FILES -> {
                                    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                                    val note1: Drawable? = ContextCompat.getDrawable(context!!,themeApp?.getAccentColor()!!)
                                    Glide.with(context!!)
                                            .load(note1)
                                            .apply(options)
                                            .into(mAlbumCover?.imageViewCover!!)
                                    mAlbumCover?.imgViewSuperSafe?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                                }
                                else -> {
                                    try {
                                        if (storage?.isFileExist("" + items.thumbnailPath)!!) {
                                            mAlbumCover?.imageViewCover?.setRotation(items.degrees.toFloat())
                                            Glide.with(context!!)
                                                    .load(storage!!.readFile(items.thumbnailPath))
                                                    .apply(options)
                                                    .into(mAlbumCover?.imageViewCover!!)
                                            mAlbumCover?.imgViewSuperSafe?.setVisibility(View.INVISIBLE)
                                        } else {
                                            mAlbumCover?.imageViewCover?.setImageResource(0)
                                            val myColor = Color.parseColor(main.image)
                                            mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                                            mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(getContext(), main.icon))
                                            mAlbumCover?.imgViewSuperSafe?.visibility = (View.VISIBLE)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } else {
                            mAlbumCover?.imageViewCover?.setImageResource(0)
                            val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(main.mainCategories_Local_Id)
                            if (mainCategories != null) {
                                mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(context, mainCategories.icon))
                                mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                                try {
                                    val myColor = Color.parseColor(mainCategories.image)
                                    mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(context, main.icon))
                                mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                                try {
                                    val myColor = Color.parseColor(main.image)
                                    mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        mAlbumCover?.imageViewCover?.setImageResource(0)
                        mAlbumCover?.imgViewSuperSafe?.setImageResource(R.drawable.baseline_https_white_48)
                        mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                        try {
                            val myColor = Color.parseColor(main?.image)
                            mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    Utils.Log(TAG, "Log album cover.........")
                } else {
                    Utils.Log(TAG, "Log album cover is null.........")
                }
            }
            if (SingletonManager.getInstance().isVisitFakePin()) {
                mLockAlbum?.isVisible = false
                mAlbumCover?.isVisible = false
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_album_settings)
        }

        fun onShowChangeCategoriesNameDialog(enumStatus: EnumStatus?, name: String?) {
            var title: String? = ""
            var content: String? = ""
            var positiveAction: String? = ""
            val isPin: String? = presenter?.mMainCategories?.pin
            var inputType = 0
            when (enumStatus) {
                EnumStatus.CHANGE -> {
                    title = getString(R.string.change_album)
                    content = ""
                    positiveAction = getString(R.string.ok)
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                EnumStatus.SET -> {
                    if (isPin != "") {
                        title = getString(R.string.remove_password)
                        content = getString(R.string.enter_a_password_for_this_album)
                        positiveAction = getString(R.string.unlock)
                        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    } else {
                        title = getString(R.string.lock_album)
                        content = getString(R.string.enter_a_password_for_this_album)
                        positiveAction = getString(R.string.lock)
                        inputType = InputType.TYPE_CLASS_TEXT
                    }
                }
            }
            val builder: MaterialDialog = MaterialDialog(getActivity()!!)
                    .title(text = title!!)
                    .message(text = content!!)
                    .cancelable(false)
                    .negativeButton(text =  getString(R.string.cancel))
                    .positiveButton(text = positiveAction!!)
                    .negativeButton { it.dismiss() }
                    .input(inputType = inputType,hint = null, prefill = name,allowEmpty = false){ dialog,input ->
                        when (enumStatus) {
                            EnumStatus.CHANGE -> {
                                Utils.Log(TAG, "Value")
                                val value = input.toString()
                                val base64Code: String = Utils.getHexCode(value)
                                val item: MainCategoryModel? = SQLHelper.getTrashItem()
                                val result: String? = item?.categories_hex_name
                                val main: String? = Utils.getHexCode(getString(R.string.key_main_album))
                                if (presenter?.mMainCategories == null) {
                                    Toast.makeText(context, "Can not change category name", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else if (base64Code == result) {
                                    Toast.makeText(context, "This name already existing", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else if (base64Code == main) {
                                    Toast.makeText(context, "This name already existing", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    presenter?.mMainCategories?.categories_name = value
                                    val response: Boolean = SQLHelper.onChangeCategories(presenter?.mMainCategories)
                                    if (response) {
                                        Toast.makeText(context, "Changed album successful", Toast.LENGTH_SHORT).show()
                                        mName?.summary = presenter?.mMainCategories?.categories_name
                                        if (!presenter?.mMainCategories?.isFakePin!!) {
                                            ServiceManager.getInstance()?.onPreparingSyncCategoryData()
                                        }
                                    } else {
                                        Toast.makeText(context, "Album name already existing.", Toast.LENGTH_SHORT).show()
                                    }
                                    if (!(presenter?.mMainCategories?.isFakePin)!!) {
                                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                                    }
                                    dialog.dismiss()
                                }
                            }
                            EnumStatus.SET -> {
                                if (isPin != "") {
                                    if (isPin == input.toString()) {
                                        presenter?.mMainCategories?.pin = ""
                                        SQLHelper.updateCategory(presenter?.mMainCategories!!)
                                        mLockAlbum?.summary = getString(R.string.unlocked)
                                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                                        dialog.dismiss()
                                    } else {
                                        Utils.showInfoSnackbar(view!!, R.string.wrong_password, true)
                                        dialog.getInputField().setText("")
                                    }
                                } else {
                                    presenter?.mMainCategories?.pin = input.toString()
                                    presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
                                    mLockAlbum?.summary = getString(R.string.locked)
                                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
            builder.show()
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                if (presenter?.mMainCategories != null) {
                    title = presenter?.mMainCategories?.categories_name
                }
            }
        }
    }

    private fun onSetUpPreference() {
        val fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ALBUM_COVER -> {
                if (resultCode == Activity.RESULT_OK) {
                    onSetUpPreference()
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                    Utils.Log(TAG, "onActivityResult...")
                }
            }
        }
    }

    companion object {
        private val TAG = AlbumSettingsAct::class.java.simpleName
        private val FRAGMENT_TAG: String? = SettingsAct::class.java.getSimpleName() + "::fragmentTag"
        var presenter: AlbumSettingsPresenter? = null
        var storage: Storage? = null
        var options: RequestOptions = RequestOptions()
                .centerCrop()
                .override(400, 400)
                .placeholder(R.color.colorPrimary)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.color.colorPrimary)
                .priority(Priority.HIGH)
    }
}