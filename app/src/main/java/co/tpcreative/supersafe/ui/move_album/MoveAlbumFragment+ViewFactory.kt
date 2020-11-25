package co.tpcreative.supersafe.ui.move_album
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.common.views.VerticalSpaceItemDecoration
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.viewmodel.MoveAlbumViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_gallery.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun MoveAlbumFragment.iniUI(){
    setupViewModel()
    mConfig = mListener?.getConfiguration()
    if (mConfig == null) {
        return
    }
    mAdapterAlbumGrid = MoveAlbumAdapter(activity!!.layoutInflater, activity, this)
    Utils.Log(TAG, Gson().toJson(mConfig))
    getData(mConfig!!.localCategoriesId, mConfig!!.isFakePIN)
    Utils.Log(TAG,"Init data")
}

fun MoveAlbumFragment.openAlbum() {
    if (mConfig == null) {
        dialog?.dismiss()
        Utils.Log(TAG,"Null...")
        return
    }
    val screenHeight: Int = Utils.getScreenHeight(activity!!)
    if (dialog != null && dialog?.isShowing!!) {
        dialog?.dismiss()
        Utils.Log(TAG,"Null...???")
        return
    }
    dialog = BottomSheetDialog(activity!!)
    val view: View = LayoutInflater.from(activity).inflate(R.layout.layout_gallery, null)
    val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight)
    view.layoutParams = lp
    val mGalleryView: RecyclerView? = view.recycler_view
    val llCreateAlbum: LinearLayout? = view.llCreateAlbum
    llCreateAlbum?.setOnClickListener(View.OnClickListener { onShowDialog() })
    if (mConfig?.dialogMode!! >= Configuration.DIALOG_GRID) {
        mGalleryView?.setLayoutManager(GridLayoutManager(activity, mAlbumColumnNumber))
        mGalleryView?.addItemDecoration(GridSpacingItemDecoration(mAlbumColumnNumber, dp2px(mConfig!!.spaceSize.toFloat()), true))
        mGalleryView?.itemAnimator = DefaultItemAnimator()
        mGalleryView?.adapter = mAdapterAlbumGrid
        mAdapterAlbumGrid?.setDataSource(dataSource)
    } else {
        mGalleryView?.layoutManager = LinearLayoutManager(activity)
        mGalleryView?.addItemDecoration(VerticalSpaceItemDecoration(dp2px(mConfig!!.spaceSize.toFloat())))
        mGalleryView?.itemAnimator = DefaultItemAnimator()
        mGalleryView?.adapter = mAdapterAlbumGrid
        mAdapterAlbumGrid?.setDataSource(dataSource)
    }
    dialog?.setContentView(view)
    mBehavior = BottomSheetBehavior.from(view.parent as View)
    if (mConfig?.dialogHeight!! < 0) {
        mBehavior?.peekHeight = if (mConfig?.dialogHeight!! <= Configuration.DIALOG_HALF) screenHeight / 2 else screenHeight
    } else {
        if (mConfig?.dialogHeight!! >= screenHeight) screenHeight else mConfig?.dialogHeight?.let { mBehavior?.setPeekHeight(it) }
    }
    dialog?.show()
}

fun MoveAlbumFragment.onShowDialog() {
    val builder: MaterialDialog = MaterialDialog(activity!!)
            .title(text = getString(R.string.create_album))
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text =  getString(R.string.ok))
            .input(inputType = InputType.TYPE_CLASS_TEXT,allowEmpty = false) { dialog ,input ->
                val value = input.toString()
                val base64Code: String = Utils.getHexCode(value)
                val item: MainCategoryModel? = SQLHelper.getTrashItem()
                val result: String? = item?.categories_hex_name
                if (base64Code == result) {
                    activity?.let {
                        Utils.onBasicAlertNotify(it,"Alert","This name already existing")
                    }
                } else {
                    val response: Boolean = SQLHelper.onAddCategories(base64Code, value, mConfig?.isFakePIN!!)
                    if (response) {
                        activity?.let {
                            Utils.onBasicAlertNotify(it,"Alert","Created album successful")
                        }
                        getData(mConfig?.localCategoriesId, mConfig?.isFakePIN!!)
                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                        ServiceManager.getInstance()?.onPreparingSyncCategoryData()
                    } else {
                        activity?.let {
                            Utils.onBasicAlertNotify(it,"Alert","Album name already existing")
                        }
                    }
                }
            }
    builder.show()
}

fun MoveAlbumFragment.dp2px(dp: Float): Int {
    return (dp * activity?.resources?.displayMetrics?.density!! + 0.5f).toInt()
}

fun MoveAlbumFragment.getGallerWidth(container: ViewGroup?): Int {
    return Utils.getScreenWidth(activity!!) - (container?.paddingLeft!! - container?.paddingRight)
}

fun MoveAlbumFragment.moveAlbum(position : Int){
    viewModel.onMoveItemsToAlbum(position,itemDataList).observe(this, Observer {
        ServiceManager.getInstance()?.onPreparingSyncData()
        SingletonPrivateFragment.getInstance()?.onUpdateView()
        dialog?.dismiss()
        Utils.onPushEventBus(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
        if (mListener != null) {
            mListener?.onMoveAlbumSuccessful()
        }
    })
}

fun MoveAlbumFragment.getData(categories_local_id: String?, isFakePIN: Boolean){
    viewModel.getData(categories_local_id,isFakePIN).observe(this, Observer {
        dataSource.addAll(it)
    })
}

private fun MoveAlbumFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(MoveAlbumViewModel::class.java)
}
