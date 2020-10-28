package co.tpcreative.supersafe.ui.theme
import android.view.LayoutInflater
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_theme_settings.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun ThemeSettingsAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    presenter = ThemeSettingsPresenter()
    presenter?.bindView(this)
    presenter?.getData()
    tvPremiumDescription?.text = getString(R.string.customize_your_theme)
}

fun ThemeSettingsAct.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ThemeSettingsAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 4)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}