package co.tpcreative.supersafe.ui.albumcover
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.ThemeApp
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_album_cover.*
import kotlinx.android.synthetic.main.layout_premium_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


fun AlbumCoverAct.initUI(){
    TAG = this::class.java.simpleName
    presenter = AlbumCoverPresenter()
    presenter?.bindView(this)
    presenter?.getData(this)
    initRecycleViewDefault(layoutInflater)
    initRecycleViewCustom(layoutInflater)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    tvPremiumDescription?.text = getString(R.string.premium_cover_description)
    rlSwitch.setOnClickListener {
        btnSwitch?.isChecked = !btnSwitch?.isChecked!!
    }
    mainScope.launch {
        //SingletonManagerProcessing.getInstance()?.onStartProgressing(this@initUI,R.string.loading)
        progress_bar.visibility = View.VISIBLE
        val mResult  = async {
            presenter!!.getData()
        }
        mResult.await()
        Utils.Log(TAG,"Fished...")
    }
}

fun AlbumCoverAct.initRecycleViewDefault(layoutInflater: LayoutInflater) {
    adapterDefault = AlbumCoverDefaultAdapter(layoutInflater,applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 3)
    recyclerViewDefault?.layoutManager = mLayoutManager
    recyclerViewDefault?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
    recyclerViewDefault?.itemAnimator = DefaultItemAnimator()
    recyclerViewDefault?.adapter = adapterDefault
    recyclerViewDefault?.isNestedScrollingEnabled = false
}

fun AlbumCoverAct.initRecycleViewCustom(layoutInflater: LayoutInflater) {
    adapterCustom = AlbumCoverAdapter(layoutInflater,applicationContext, presenter?.mMainCategories, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 3)
    recyclerViewCustom?.layoutManager = mLayoutManager
    recyclerViewCustom?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
    recyclerViewCustom?.itemAnimator = DefaultItemAnimator()
    recyclerViewCustom?.adapter = adapterCustom
    recyclerViewCustom?.isNestedScrollingEnabled = false
}

fun AlbumCoverAct.onShowPremium() {
    try {
        val builder = getContext()?.let { MaterialDialog.Builder(it,Utils.getCurrentTheme()) }
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        builder?.setHeaderBackground(themeApp?.getAccentColor()!!)
        builder?.setTitle(getString(R.string.this_is_premium_feature))
        builder?.setMessage(getString(R.string.upgrade_now))
        builder?.setCustomHeader(R.layout.custom_header)
        builder?.setPadding(40, 40, 40, 0)
        builder?.setMargin(60, 0, 60, 0)
        builder?.showHeader(true)
        builder?.setPositiveButton(getString(R.string.get_premium)) { dialogInterface, i -> getContext()?.let { Navigator.onMoveToPremium(it) } }
        builder?.setNegativeButton(getText(R.string.later)) { dialogInterface, i -> }
        builder?.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun AlbumCoverAct.onUpdatedCustomData(position : Int? = null,isRequestPrevious : Boolean){
    if (!isRequestPrevious){
        position?.let {mResultPosition ->
            if (adapterCustom?.getDataSource()!!.size > mResultPosition) {
                adapterCustom?.notifyItemChanged(mResultPosition)
                presenter?.defaultPreviousPosition?.let {
                    presenter?.mListMainCategories?.get(it)?.isChecked = !(presenter?.mListMainCategories?.get(it)?.isChecked!!)
                    onUpdatedDefaultData(it,true)
                }
            }
        }
    }
    presenter?.previousPosition?.let {mResultPosition ->
        if (adapterCustom!!.getDataSource()!!.size > mResultPosition){
            adapterCustom?.notifyItemChanged(mResultPosition)
            if (isRequestPrevious){
                presenter!!.previousPosition = null
            }else{
                presenter!!.previousPosition = position
            }
            presenter?.defaultPreviousPosition?.let {
                presenter?.mListMainCategories?.get(it)?.isChecked = !(presenter?.mListMainCategories?.get(it)?.isChecked!!)
                onUpdatedDefaultData(it,true)
            }
        }
    } ?: run {
        presenter?.previousPosition = position
    }
}

fun AlbumCoverAct.onUpdatedDefaultData(position : Int? = null, isRequestPrevious : Boolean) {
    if (!isRequestPrevious){
        position?.let {mResultPosition ->
            if (adapterDefault?.getDataSource()!!.size > mResultPosition) {
                adapterDefault?.notifyItemChanged(mResultPosition)
                presenter?.previousPosition?.let {
                    presenter?.mList?.get(it)?.isChecked = !(presenter?.mList?.get(it)?.isChecked!!)
                    onUpdatedCustomData(it,true)
                }
            }
        }
    }
    presenter?.defaultPreviousPosition?.let { mResultPosition ->
        if (adapterDefault!!.getDataSource()!!.size > mResultPosition){
            adapterDefault?.notifyItemChanged(mResultPosition)
            if (isRequestPrevious){
                presenter!!.defaultPreviousPosition = null
            }else{
                presenter!!.defaultPreviousPosition = position
            }
            presenter?.previousPosition?.let {
                presenter?.mList?.get(it)?.isChecked = !(presenter?.mList?.get(it)?.isChecked!!)
                onUpdatedCustomData(it,true)
                presenter?.previousPosition = null
            }
        }
    } ?: run {
        this.presenter?.defaultPreviousPosition = position
    }
}
