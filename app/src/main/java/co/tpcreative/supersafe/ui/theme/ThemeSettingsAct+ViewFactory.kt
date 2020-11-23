package co.tpcreative.supersafe.ui.theme
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.ui.trash.TrashAct
import co.tpcreative.supersafe.viewmodel.ThemeSettingsViewModel
import co.tpcreative.supersafe.viewmodel.TrashViewModel
import kotlinx.android.synthetic.main.activity_theme_settings.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun ThemeSettingsAct.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    tvPremiumDescription?.text = getString(R.string.customize_your_theme)
    getData()
}

fun ThemeSettingsAct.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ThemeSettingsAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 4)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
    recyclerView?.itemAnimator = DefaultItemAnimator()
    recyclerView?.adapter = adapter
}

fun ThemeSettingsAct.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
    })
}

private fun ThemeSettingsAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ThemeSettingsViewModel::class.java)
}
