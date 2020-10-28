package co.tpcreative.supersafe.ui.move_gallery
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.common.views.VerticalSpaceItemDecoration
import co.tpcreative.supersafe.model.MainCategoryModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_gallery.view.*

fun MoveGalleryFragment.iniUI(){
    mConfig = mListener?.getConfiguration()
    if (mConfig == null) {
        return
    }
    mAdapterAlbumGrid = MoveGalleryAdapter(activity!!.layoutInflater, activity, this)
    presenter = MoveGalleryPresenter()
    presenter?.bindView(this)
    Utils.Log(TAG, Gson().toJson(mConfig))
    presenter?.getData(mConfig!!.localCategoriesId, mConfig!!.isFakePIN)
}

fun MoveGalleryFragment.openAlbum() {
    if (mConfig == null) {
        dialog?.dismiss()
        return
    }
    val screenHeight: Int = Utils.getScreenHeight(activity!!)
    if (dialog != null && dialog?.isShowing!!) {
        dialog?.dismiss()
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
        mAdapterAlbumGrid?.setDataSource(presenter?.mList)
    } else {
        mGalleryView?.layoutManager = LinearLayoutManager(activity)
        mGalleryView?.addItemDecoration(VerticalSpaceItemDecoration(dp2px(mConfig!!.spaceSize.toFloat())))
        mGalleryView?.itemAnimator = DefaultItemAnimator()
        mGalleryView?.adapter = mAdapterAlbumGrid
        mAdapterAlbumGrid?.setDataSource(presenter?.mList)
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

fun MoveGalleryFragment.onShowDialog() {
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
                    Toast.makeText(activity, "This name already existing", Toast.LENGTH_SHORT).show()
                } else {
                    val response: Boolean = SQLHelper.onAddCategories(base64Code, value, mConfig?.isFakePIN!!)
                    if (response) {
                        Toast.makeText(activity, "Created album successful", Toast.LENGTH_SHORT).show()
                        presenter?.getData(mConfig?.localCategoriesId, mConfig?.isFakePIN!!)
                        SingletonPrivateFragment.getInstance()?.onUpdateView()
                        ServiceManager.getInstance()?.onPreparingSyncCategoryData()
                    } else {
                        Toast.makeText(activity, "Album name already existing", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    builder.show()
}

fun MoveGalleryFragment.dp2px(dp: Float): Int {
    return (dp * activity?.resources?.displayMetrics?.density!! + 0.5f).toInt()
}

fun MoveGalleryFragment.getGallerWidth(container: ViewGroup?): Int {
    return Utils.getScreenWidth(activity!!) - (container?.paddingLeft!! - container?.paddingRight)
}
