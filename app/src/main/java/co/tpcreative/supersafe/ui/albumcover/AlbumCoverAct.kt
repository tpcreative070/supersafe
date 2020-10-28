package co.tpcreative.supersafe.ui.albumcover
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.activity_album_cover.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumCoverAct : BaseActivity(), BaseView<EmptyModel>, CompoundButton.OnCheckedChangeListener, AlbumCoverAdapter.ItemSelectedListener, AlbumCoverDefaultAdapter.ItemSelectedListener {
    var presenter: AlbumCoverPresenter? = null
    var adapterDefault: AlbumCoverDefaultAdapter? = null
    var adapterCustom: AlbumCoverAdapter? = null
    var isReload = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_cover)
        initUI()
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

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "position...$position")
        try {
            presenter?.mMainCategories?.items_id = presenter?.mList?.get(position)?.items_id
            presenter?.mMainCategories?.mainCategories_Local_Id = ""
            presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
            presenter?.getData()
            isReload = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClickedDefaultItem(position: Int) {
        Utils.Log(TAG, "position...$position")
        try {
            presenter?.mMainCategories?.items_id = ""
            presenter?.mMainCategories?.mainCategories_Local_Id = presenter?.mListMainCategories?.get(position)?.mainCategories_Local_Id
            presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
            presenter?.getData()
            isReload = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                if (isReload) {
                    val intent: Intent = getIntent()
                    setResult(Activity.RESULT_OK, intent)
                    Utils.Log(TAG, "onBackPressed")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        when (compoundButton.id) {
            R.id.btnSwitch -> {
                if (!Utils.isPremium()) {
                    btnSwitch?.isChecked = false
                    onShowPremium()
                } else {
                    presenter?.mMainCategories?.isCustom_Cover = b
                    presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
                    llRecyclerView?.visibility = (if (b) View.VISIBLE else View.INVISIBLE)
                    Utils.Log(TAG, "action here")
                }
            }
        }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                if (presenter?.mMainCategories != null) {
                    if (Utils.isPremium()) {
                        title = (presenter?.mMainCategories?.categories_name)
                        presenter?.mMainCategories?.isCustom_Cover?.let { btnSwitch?.setChecked(it) }
                        llRecyclerView?.visibility = (if (presenter?.mMainCategories?.isCustom_Cover!!) View.VISIBLE else View.INVISIBLE)
                    } else {
                        title = (presenter?.mMainCategories?.categories_name)
                        presenter!!.mMainCategories?.isCustom_Cover = false
                        presenter?.mMainCategories?.isCustom_Cover?.let { btnSwitch?.setChecked(it) }
                        llRecyclerView?.visibility = (if (presenter?.mMainCategories?.isCustom_Cover!!) View.VISIBLE else View.INVISIBLE)
                        SQLHelper.updateCategory(presenter?.mMainCategories!!)
                    }
                }
            }
            EnumStatus.GET_LIST_FILE -> {
                Utils.Log(TAG, "load data")
                adapterDefault?.setDataSource(presenter?.mListMainCategories)
                adapterCustom?.setDataSource(presenter?.mList)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onBackPressed() {
        if (isReload) {
            val intent: Intent = getIntent()
            setResult(Activity.RESULT_OK, intent)
            Utils.Log(TAG, "onBackPressed")
        }
        super.onBackPressed()
    }
}