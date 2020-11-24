package co.tpcreative.supersafe.ui.albumcover
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager
import co.tpcreative.supersafe.common.views.addGridOfDecoration
import co.tpcreative.supersafe.common.views.clearDecorations
import co.tpcreative.supersafe.model.AlbumCoverModel
import co.tpcreative.supersafe.model.EnumTypeObject
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailAdapter
import co.tpcreative.supersafe.viewmodel.AlbumCoverViewModel
import com.google.gson.Gson
import de.mrapp.android.dialog.MaterialDialog
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_album_cover.*
import kotlinx.android.synthetic.main.activity_album_cover.progress_bar
import kotlinx.android.synthetic.main.activity_album_cover.recyclerView
import kotlinx.android.synthetic.main.activity_album_cover.toolbar
import kotlinx.android.synthetic.main.layout_premium_header.*

fun AlbumCoverAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    tvPremiumDescription?.text = getString(R.string.premium_cover_description)
    rlSwitch.setOnClickListener {
        btnSwitch?.isChecked = !btnSwitch?.isChecked!!
    }

    viewModel.isLoading.observe(this, Observer {
        if (it) {
            progress_bar.visibility = View.VISIBLE
            llCoverAlbum.visibility = View.INVISIBLE
        } else {
            progress_bar.visibility = View.INVISIBLE
            llCoverAlbum.visibility = View.VISIBLE
        }
    })
    getData()
}

fun AlbumCoverAct.onShowPremium() {
    try {
        val builder =  MaterialDialog.Builder(this, Utils.getCurrentTheme())
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        builder.setHeaderBackground(themeApp?.getAccentColor()!!)
        builder.setTitle(getString(R.string.this_is_premium_feature))
        builder.setMessage(getString(R.string.upgrade_now))
        builder.setCustomHeader(R.layout.custom_header)
        builder.setPadding(40, 40, 40, 0)
        builder.setMargin(60, 0, 60, 0)
        builder.showHeader(true)
        builder.setPositiveButton(getString(R.string.get_premium)) { dialogInterface, i ->  Navigator.onMoveToPremium(this)  }
        builder.setNegativeButton(getText(R.string.later)) { dialogInterface, i -> }
        builder.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun AlbumCoverAct.getData(){
    Utils.Log(TAG, "Loading data...")
    viewModel.getData(this).observe(this, Observer {
        Utils.Log(TAG, Gson().toJson(it))
        showValueToUI()
    })
}

fun AlbumCoverAct.getReload(isCustom: Boolean){
    viewModel.isLoading.postValue(true)
    viewModel.getData(isCustom).observe(this, Observer {
        dataSourceCustom =  it.filter { it.type == EnumTypeObject.ITEM } as MutableList<AlbumCoverModel>
        dataSourceDefault = it.filter { it.type == EnumTypeObject.CATEGORY } as MutableList<AlbumCoverModel>
        setupRecyclerView(isCustom,it)
        viewModel.isLoading.postValue(false)
    })
}

private fun AlbumCoverAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(AlbumCoverViewModel::class.java)
}

fun AlbumCoverAct.setupRecyclerView(isCustom : Boolean,data: MutableList<AlbumCoverModel>){
    recyclerView.clearDecorations()
    sectionedAdapter = SectionedRecyclerViewAdapter()
    val parameterName = SectionParameters.builder()
            .itemResourceId(R.layout.album_cover_item)
            .headerResourceId(R.layout.album_cover_item_header)
            .build()

    sectionedAdapter.addSection(AlbumCoverSection(this, "Default items", mainCategory,dataSourceDefault, this,parameterName))
    if (isCustom){
        sectionedAdapter.addSection(AlbumCoverSection(this, "Custom items", mainCategory,dataSourceCustom, this,parameterName))
    }
    val glm = NpaGridLayoutManager(this, 3)
    glm.spanSizeLookup = object : SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if (sectionedAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                3
            } else 1
        }
    }
    recyclerView?.addGridOfDecoration(AlbumDetailAdapter.SPAN_COUNT_THREE,3)
    recyclerView.layoutManager = glm
    recyclerView.adapter = sectionedAdapter
}

fun AlbumCoverAct.showValueToUI(){
    if (Utils.isPremium()){
        title = mainCategory.categories_name
        btnSwitch.isChecked = mainCategory.isCustom_Cover
    }else {
        title = mainCategory.categories_name
        mainCategory.isCustom_Cover = false;
        btnSwitch.isChecked = mainCategory.isCustom_Cover;
    }
    getReload(mainCategory.isCustom_Cover)
}

