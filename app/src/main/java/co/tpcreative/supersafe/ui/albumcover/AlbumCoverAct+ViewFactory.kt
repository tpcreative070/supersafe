package co.tpcreative.supersafe.ui.albumcover
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.ThemeApp
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_album_cover.*
import kotlinx.android.synthetic.main.layout_premium_header.*

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
    presenter?.getData()
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
        val builder = getContext()?.let { MaterialDialog.Builder(it) }
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
        val dialog = builder?.show()
        builder?.setOnShowListener {
            val positive = dialog?.findViewById<AppCompatButton?>(android.R.id.button1)
            val negative = dialog?.findViewById<AppCompatButton?>(android.R.id.button2)
            val textView: AppCompatTextView? = dialog?.findViewById<AppCompatTextView?>(android.R.id.message)
            if (positive != null && negative != null && textView != null) {
                positive.setTextColor(ContextCompat.getColor(getContext()!!, themeApp!!.getAccentColor()))
                negative.setTextColor(ContextCompat.getColor(getContext()!!, themeApp.getAccentColor()))
                textView.setTextSize(16f)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
