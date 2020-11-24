package co.tpcreative.supersafe.ui.albumcover
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.SquaredImageView
import co.tpcreative.supersafe.common.views.SquaredView
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import kotlinx.android.synthetic.main.album_cover_item.view.*
import kotlinx.android.synthetic.main.album_cover_item_header.view.*

class AlbumCoverSection  constructor(@NonNull val context : Context, @NonNull val title: String, @NonNull val mainCategoryModel: MainCategoryModel, @NonNull val list: MutableList<AlbumCoverModel>,
                                            @NonNull val clickListener: ClickListener, sectionParameters: SectionParameters) : Section(sectionParameters) {

    val TAG = this::class.java.simpleName
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_200)
            .priority(Priority.HIGH)
    var themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    override fun getContentItemsTotal(): Int {
        return list.size
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return ItemViewHolder(view!!)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ItemViewHolder
        Utils.Log(TAG,"Changed $position")
        viewHolder.mPosition = position
        try {
            val data: AlbumCoverModel = list[position]
            when(data.type){
                EnumTypeObject.CATEGORY -> {
                    Utils.Log(TAG,"category type... ${data.type.name}")
                    val categoryData = data.category!!
                    Utils.Log(TAG,"category is... ${categoryData.isChecked}")
                    if (categoryData.isChecked) {
                        viewHolder.viewAlpha.alpha = 0.5f
                        viewHolder.imgIcon.setColorFilter(ContextCompat.getColor(SuperSafeApplication.getInstance(),themeApp!!.getAccentColor()), PorterDuff.Mode.SRC_IN)
                        viewHolder.imgSelect.visibility = View.VISIBLE
                    } else {
                        viewHolder.imgIcon.setColorFilter(ContextCompat.getColor(SuperSafeApplication.getInstance(), R.color.white), PorterDuff.Mode.SRC_IN)
                        viewHolder.viewAlpha.alpha = 0.0f
                        viewHolder.imgSelect.visibility = View.INVISIBLE
                    }
                    try {
                        viewHolder.imgAlbum.setImageResource(0)
                        val myColor = Color.parseColor(categoryData.image)
                        viewHolder.imgAlbum.setBackgroundColor(myColor)
                        viewHolder.imgIcon.setImageDrawable(SQLHelper.getDrawable(context, categoryData.icon))
                        viewHolder.imgIcon.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else ->{
                    val itemData = data.item!!
                    if (itemData.isChecked) {
                        viewHolder.viewAlpha.alpha = 0.5f
                        viewHolder.imgSelect.visibility = View.VISIBLE
                    } else {
                        viewHolder.viewAlpha.alpha = 0.0f
                        viewHolder.imgSelect.visibility = View.INVISIBLE
                    }
                    when (EnumFormatType.values()[itemData.formatType]) {
                        EnumFormatType.AUDIO -> {
                            val note1 = ContextCompat.getDrawable(context,themeApp!!.getAccentColor())
                            Glide.with(context)
                                    .load(note1)
                                    .apply(options!!)
                                    .into(viewHolder.imgAlbum)
                            viewHolder.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_insert_drive_file_white_48))
                        }
                        EnumFormatType.FILES -> {
                            val note1 = ContextCompat.getDrawable(context,themeApp!!.getAccentColor())
                            Glide.with(context)
                                    .load(note1)
                                    .apply(options!!)
                                    .into(viewHolder.imgAlbum)
                            viewHolder.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_files))
                        }
                        else -> {
                            try {
                                if (itemData.getThumbnail().isFileExist()) {
                                    itemData.degrees.toFloat().let { viewHolder.imgAlbum.setRotation(it) }
                                    Glide.with(context)
                                            .load(itemData.getThumbnail().readFile())
                                            .apply(options!!)
                                            .into(viewHolder.imgAlbum)
                                    viewHolder.imgIcon.visibility = View.INVISIBLE
                                } else {
                                    viewHolder.imgAlbum.setImageResource(0)
                                    val myColor = Color.parseColor(mainCategoryModel.image)
                                    viewHolder.imgAlbum.setBackgroundColor(myColor)
                                    viewHolder.imgIcon.setImageDrawable(SQLHelper.getDrawable(context, mainCategoryModel.icon))
                                    viewHolder.imgIcon.visibility = View.VISIBLE
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewHolder.rlHome.setOnClickListener {
            clickListener.onItemRootViewClicked(this,position)
        }
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return HeaderViewHolder(view!!)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val headerHolder = holder as HeaderViewHolder
        headerHolder.tvTitle.text = title
    }

    interface ClickListener {
        fun onHeaderMoreButtonClicked(section: AlbumCoverSection, itemAdapterPosition: Int)
        fun onItemRootViewClicked(section: AlbumCoverSection, itemAdapterPosition: Int)
    }
}

internal class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvTitle: AppCompatTextView = view.tvHeader
}

internal class ItemViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
    val imgAlbum: SquaredImageView = rootView.imgAlbum
    val imgIcon: SquaredImageView = rootView.imgIcon
    val imgSelect: SquaredImageView = rootView.imgSelect
    val viewAlpha: SquaredView = rootView.view_alpha
    var rlHome: RelativeLayout = rootView.rlHome
    var mPosition = 0
}

