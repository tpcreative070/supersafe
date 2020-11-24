package co.tpcreative.supersafe.ui.albumcover
import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AlbumCoverModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.viewmodel.AlbumCoverViewModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_album_cover.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumCoverAct : BaseActivity(),CompoundButton.OnCheckedChangeListener,AlbumCoverSection.ClickListener {
    var isReload = false
    lateinit var viewModel : AlbumCoverViewModel
    var sectionedAdapter: SectionedRecyclerViewAdapter =  SectionedRecyclerViewAdapter()
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
            else -> Utils.Log(TAG,"Nothing")
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
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onHeaderMoreButtonClicked(section: AlbumCoverSection, itemAdapterPosition: Int) {

    }

    override fun onItemRootViewClicked(section: AlbumCoverSection, itemAdapterPosition: Int) {
        val mIndexOfHeader = sectionedAdapter.getAdapterForSection(section).sectionPosition
        if (mIndexOfHeader>0){
            /*Updated data to db*/
            val mItemId =  dataSourceCustom[itemAdapterPosition].item?.items_id
            mainCategory.items_id = mItemId
            mainCategory.mainCategories_Local_Id = ""
            SQLHelper.updateCategory(mainCategory)

            /*Updated all view of default item to unchecked*/
            for (index in dataSourceDefault.indices){
                if (dataSourceDefault[index].category?.isChecked == true){
                    dataSourceDefault[index].category?.isChecked = false
                    sectionedAdapter.getAdapterForSection(Navigator.CATEGORY_TAG.toString()).notifyItemChanged(index)
                }
            }

            /*Updated previous of checked to unchecked*/
            section.previousPositionOfItem?.let {
                dataSourceCustom[it.toInt()].item?.isChecked = false
                sectionedAdapter.getAdapterForSection(section).notifyItemChanged(it.toInt())
            }
            /*Updated current view to checked*/
            dataSourceCustom[itemAdapterPosition].item?.isChecked = true
            sectionedAdapter.getAdapterForSection(section).notifyItemChanged(itemAdapterPosition)
        }else{
            /*Updated data to db*/
            val mCategoryId = dataSourceDefault[itemAdapterPosition].category?.mainCategories_Local_Id
            mainCategory.items_id = ""
            mainCategory.mainCategories_Local_Id = mCategoryId
            SQLHelper.updateCategory(mainCategory)

            /*Updated all view of custom item to unchecked*/
            for (index in dataSourceCustom.indices){
                if (dataSourceCustom[index].item?.isChecked == true){
                    dataSourceCustom[index].item?.isChecked = false
                    sectionedAdapter.getAdapterForSection(Navigator.ITEM_TAG.toString()).notifyItemChanged(index)
                }
            }

            /*Updated previous of checked*/
            section.previousPositionOfCategory?.let {
                dataSourceDefault[it.toInt()].category?.isChecked = false
                sectionedAdapter.getAdapterForSection(section).notifyItemChanged(it.toInt())
            }
            /*Updated current view to checked*/
            dataSourceDefault[itemAdapterPosition].category?.isChecked = true
            sectionedAdapter.getAdapterForSection(section).notifyItemChanged(itemAdapterPosition)
        }
        isReload = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isReload) {
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
                    mainCategory.isCustom_Cover = b
                    /*Set default item*/
                    if (!b && mainCategory.items_id?.isNotEmpty() == true){
                        mainCategory.isCustom_Cover = false
                        mainCategory.items_id = ""
                        mainCategory.mainCategories_Local_Id = dataSourceDefault[0].category?.mainCategories_Local_Id
                    }
                    SQLHelper.updateCategory(mainCategory)
                    getReload(mainCategory.isCustom_Cover)
                    isReload = true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isReload) {
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    val mainCategory : MainCategoryModel
        get(){
            return viewModel.mainCategoryModel
        }

    var dataSourceCustom : MutableList<AlbumCoverModel> = mutableListOf()

    var dataSourceDefault : MutableList<AlbumCoverModel> = mutableListOf()
}