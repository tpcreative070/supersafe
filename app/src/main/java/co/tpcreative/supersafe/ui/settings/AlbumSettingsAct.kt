package co.tpcreative.supersafe.ui.settings
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.View
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
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.preference.MyPreferenceAlbumSettings
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.AlbumSettingsViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumSettingsAct : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_settings)
        albumSettings = this
        initUI()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
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
                        if (main != mainCategory.categories_hex_name && trash != mainCategory.categories_hex_name) {
                            onShowChangeCategoriesNameDialog(EnumStatus.CHANGE, name)
                        }
                    } else if (preference.key == getString(R.string.key_album_lock)) {
                        val name = preference.summary.toString()
                        onShowChangeCategoriesNameDialog(EnumStatus.SET, null)
                    } else if (preference.key == getString(R.string.key_album_cover)) {
                        if (mainCategory.pin == "") {
                           Navigator.onMoveAlbumCover(activity!!, mainCategory)
                        }
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*change categories name*/
            mName = findPreference(getString(R.string.key_name)) as MyPreferenceAlbumSettings?
            mName?.onPreferenceChangeListener = createChangeListener()
            mName?.onPreferenceClickListener = createActionPreferenceClickListener()
            mName?.summary = mainCategory.categories_name
            mLockAlbum = findPreference(getString(R.string.key_album_lock)) as MyPreferenceAlbumSettings?
            mLockAlbum?.onPreferenceChangeListener = createChangeListener()
            mLockAlbum?.onPreferenceClickListener = createActionPreferenceClickListener()
            val isPin: String? = mainCategory.pin
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
                    if (mainCategory.pin == "") {
                        val items: ItemModel? = SQLHelper.getItemId(mainCategory.items_id)
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
                                        if (items.getThumbnail().isFileExist()) {
                                            mAlbumCover?.imageViewCover?.rotation = items.degrees.toFloat()
                                            Glide.with(context!!)
                                                    .load(items.getThumbnail().readFile())
                                                    .apply(options)
                                                    .into(mAlbumCover?.imageViewCover!!)
                                            mAlbumCover?.imageViewCover?.visibility = View.VISIBLE
                                            mAlbumCover?.imgViewSuperSafe?.visibility = View.INVISIBLE
                                            Utils.Log(TAG,"Call here...........1")
                                        } else {
                                            mAlbumCover?.imageViewCover?.setImageResource(0)
                                            val myColor = Color.parseColor(mainCategory.image)
                                            mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                                            mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(context, mainCategory.icon))
                                            mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                                            mAlbumCover?.imageViewCover?.visibility = View.VISIBLE
                                            Utils.Log(TAG,"Call here...........2")
                                        }
                                        Utils.Log(TAG,"Call here...........")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } else {
                            Utils.Log(TAG,"Call here...........???")
                            mAlbumCover?.imageViewCover?.setImageResource(0)
                            val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(mainCategory.mainCategories_Local_Id)
                            if (mainCategories != null) {
                                mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(context, mainCategories.icon))
                                mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                                mAlbumCover?.imageViewCover?.visibility = View.VISIBLE
                                try {
                                    val myColor = Color.parseColor(mainCategories.image)
                                    mAlbumCover?.imageViewCover?.setBackgroundColor(myColor)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                mAlbumCover?.imgViewSuperSafe?.setImageDrawable(SQLHelper.getDrawable(context, mainCategory.icon))
                                mAlbumCover?.imgViewSuperSafe?.visibility = View.VISIBLE
                                mAlbumCover?.imageViewCover?.visibility = View.VISIBLE
                                try {
                                    val myColor = Color.parseColor(mainCategory.image)
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
                            val myColor = Color.parseColor(mainCategory.image)
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

        private fun onShowChangeCategoriesNameDialog(enumStatus: EnumStatus?, name: String?) {
            var title: String? = ""
            var content: String? = ""
            var positiveAction: String? = ""
            val isPin: String? = mainCategory.pin
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
                else -> Utils.Log(TAG,"Nothing")
            }
            val builder: MaterialDialog = MaterialDialog(activity!!)
                    .title(text = title!!)
                    .message(text = content!!)
                    .cancelable(false)
                    .negativeButton(text =  getString(R.string.cancel))
                    .positiveButton(text = positiveAction!!)
                    .negativeButton { it.dismiss() }
                    .input(maxLength = Utils.MAX_LENGTH,inputType = inputType,hint = null, prefill = name,allowEmpty = false){ dialog,input ->
                        when (enumStatus) {
                            EnumStatus.CHANGE -> {
                                Utils.Log(TAG, "Value")
                                val value = input.toString()
                                val base64Code: String = Utils.getHexCode(value)
                                val item: MainCategoryModel? = SQLHelper.getTrashItem()
                                val result: String? = item?.categories_hex_name
                                val main: String = Utils.getHexCode(getString(R.string.key_main_album))
                                if (base64Code == result) {
                                    Utils.onBasicAlertNotify(activity!!,"Alert","This name already existing")
                                    dialog.dismiss()
                                } else if (base64Code == main) {
                                    Utils.onBasicAlertNotify(activity!!,"Alert","This name already existing")
                                    dialog.dismiss()
                                } else {
                                    mainCategory.categories_name = value
                                    val response: Boolean = SQLHelper.onChangeCategories(mainCategory)
                                    if (response) {
                                        Utils.onBasicAlertNotify(activity!!,"Alert","Changed album successful")
                                        albumSettings.title = mainCategory.categories_name
                                        mName?.summary = mainCategory.categories_name

                                        if (!mainCategory.isFakePin) {
                                            ServiceManager.getInstance()?.onPreparingSyncCategoryData()
                                        }
                                    } else {
                                        Utils.onBasicAlertNotify(activity!!,"Alert","Album name already existing.")
                                    }
                                    if (!(mainCategory.isFakePin)) {
                                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                                    }
                                    dialog.dismiss()
                                }
                            }
                            EnumStatus.SET -> {
                                if (isPin != "") {
                                    if (isPin == input.toString()) {
                                        mainCategory.pin = ""
                                        SQLHelper.updateCategory(mainCategory)
                                        mLockAlbum?.summary = getString(R.string.unlocked)
                                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                                        dialog.dismiss()
                                    } else {
                                        activity?.let {
                                            Utils.onBasicAlertNotify(it,getString(R.string.key_alert),getString(R.string.wrong_password))
                                        }
                                        dialog.getInputField().setText("")
                                    }
                                } else {
                                     mainCategory.pin = input.toString()
                                     SQLHelper.updateCategory(mainCategory)
                                    mLockAlbum?.summary = getString(R.string.locked)
                                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                                    dialog.dismiss()
                                }
                            }
                            else -> Utils.Log(TAG,"Nothing")
                        }
                    }
            builder.show()
        }
    }

    fun onSetUpPreference() {
        val fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ALBUM_COVER -> {
                if (resultCode == Activity.RESULT_OK) {
                    getReload()
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                    Utils.Log(TAG, "onActivityResult...")
                }
            }
        }
    }

    companion object {
        private val TAG = AlbumSettingsAct::class.java.simpleName
        lateinit var viewModel: AlbumSettingsViewModel
        lateinit var albumSettings : AlbumSettingsAct
        val mainCategory : MainCategoryModel
            get() {
                return viewModel.mainCategoryModel
            }
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