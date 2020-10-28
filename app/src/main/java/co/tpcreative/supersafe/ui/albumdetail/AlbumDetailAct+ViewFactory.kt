package co.tpcreative.supersafe.ui.albumdetail
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.footer_items_detail_album.*

fun AlbumDetailAct.initUI(){
    TAG = this::class.java.simpleName
    storage = Storage(this)
    storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    initSpeedDial(true)
    presenter = AlbumDetailPresenter()
    presenter?.bindView(this)
    onInit()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    collapsing_toolbar.title = presenter?.mainCategories?.categories_name
    val mList: MutableList<ItemModel>? = presenter?.mainCategories?.isFakePin?.let { SQLHelper.getListItems(presenter?.mainCategories?.categories_local_id, it) }
    val items: ItemModel? = SQLHelper.getItemId(presenter?.mainCategories?.items_id)
    if (items != null && mList != null && mList.size > 0) {
        when (EnumFormatType.values()[items.formatType]) {
            EnumFormatType.AUDIO -> {
                try {
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            EnumFormatType.FILES -> {
                try {
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                if (storage?.isFileExist(items.thumbnailPath)!!) {
                    backdrop?.rotation = items.degrees.toFloat()
                    Glide.with(this)
                            .load(storage?.readFile(items.thumbnailPath))
                            .apply(options!!)
                            .into(backdrop!!)
                } else {
                    backdrop?.setImageResource(0)
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                }
            }
        }
    } else {
        backdrop?.setImageResource(0)
        val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(presenter?.mainCategories?.mainCategories_Local_Id)
        if (mainCategories != null) {
            try {
                val myColor = Color.parseColor(mainCategories.image)
                backdrop?.setBackgroundColor(myColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val myColor = Color.parseColor(presenter?.mainCategories?.image)
                backdrop?.setBackgroundColor(myColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    llBottom?.visibility = View.GONE
    /*Root Fragment*/
    imgShare.setOnClickListener {
        if (countSelected > 0) {
            storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafeShare())
            presenter?.status = EnumStatus.SHARE
            onShowDialog(presenter?.status)
        }
    }

    imgExport.setOnClickListener {
        onExport()
    }

    imgDelete.setOnClickListener {
        if (countSelected > 0) {
            presenter?.status = EnumStatus.DELETE
            onShowDialog(presenter?.status)
        }
    }

    imgMove.setOnClickListener {
       openAlbum()
    }

    recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            Utils.Log(TAG, "Scrolling change listener")
            if (actionMode != null) {
                speedDial?.visibility = View.INVISIBLE
            }
        }
    })
}

fun AlbumDetailAct.onExport(){
    if (countSelected > 0) {
        storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture())
        presenter?.status = EnumStatus.EXPORT
        var isSaver = false
        var spaceAvailable: Long = 0
        for (i in presenter?.mList?.indices!!) {
            val items: ItemModel? = presenter?.mList?.get(i)
            if (items?.isSaver!! && items?.isChecked) {
                isSaver = true
                spaceAvailable += items.size?.toLong()!!
            }
        }
        val availableSpaceOS: Long = Utils.getAvailableSpaceInBytes()
        if (availableSpaceOS < spaceAvailable) {
            val request_spaces = spaceAvailable - availableSpaceOS
            val result: String? = ConvertUtils.byte2FitMemorySize(request_spaces)
            val message: String = kotlin.String.format(getString(R.string.your_space_is_not_enough_to), "export. ", "Request spaces: $result")
            Utils.showDialog(this, message = message)
        } else {
            if (isSaver) {
                onEnableSyncData()
            } else {
                onShowDialog(presenter?.status)
            }
        }
    }
}