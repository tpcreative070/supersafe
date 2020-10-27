package co.tpcreative.supersafe.ui.move_gallery
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.common.views.VerticalSpaceItemDecoration
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MoveGalleryFragment : BaseFragment(), MoveGalleryAdapter.ItemSelectedListener, MoveGalleryView {
    private var mAlbumColumnNumber = 0
    private var mAdapterAlbumGrid: MoveGalleryAdapter? = null
    private var mConfig: Configuration? = null
    private var dialog: BottomSheetDialog? = null
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var mListener: OnGalleryAttachedListener? = null
    private var presenter: MoveGalleryPresenter? = null

    interface OnGalleryAttachedListener {
        open fun getConfiguration(): Configuration?
        open fun getListItems(): MutableList<ItemModel>?
        open fun onMoveAlbumSuccessful()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.UPDATE_MOVE_NEW_ALBUM -> {
                if (mConfig != null) {
                    presenter?.getData(mConfig?.localCategoriesId, mConfig?.isFakePIN!!)
                    Utils.Log(TAG, "Updated UI => Warning categories id is null")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            mAlbumColumnNumber = getGallerWidth(container) / dp2px(mConfig?.photoMaxWidth!! * 1.5f)
            inflater.inflate(R.layout.layout_fragment_root, container, false)
        } catch (e: Exception) {
            mAlbumColumnNumber = getGallerWidth(container) / dp2px(120 * 1.5f)
            inflater.inflate(R.layout.layout_fragment_root, container, false)
        }
    }

    override fun getLayoutId(): Int {
        TODO("Not yet implemented")
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return  inflater?.inflate(R.layout.layout_fragment_root, viewGroup, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            Utils.Log(TAG, "Register Listener")
            activity as OnGalleryAttachedListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnGalleryAttachedListener")
        }
    }

    override fun onClickGalleryItem(position: Int) {
        presenter?.onMoveItemsToAlbum(position)
        Utils.Log(TAG, "Position :$position")
    }

    fun openAlbum() {
        if (mConfig == null) {
            dialog?.dismiss()
            return
        }
        val screenHeight: Int = Utils.getScreenHeight(activity!!)
        if (dialog != null && dialog?.isShowing()!!) {
            dialog?.dismiss()
            return
        }
        dialog = BottomSheetDialog(activity!!)
        val view: View = LayoutInflater.from(activity).inflate(R.layout.layout_gallery, null)
        val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight)
        view.layoutParams = lp
        val mGalleryView: RecyclerView? = view.findViewById<View?>(R.id.recycler_view) as RecyclerView?
        val llCreateAlbum: LinearLayout? = view.findViewById<LinearLayout>(R.id.llCreateAlbum)
        llCreateAlbum?.setOnClickListener(View.OnClickListener { onShowDialog() })
        if (mConfig?.dialogMode!! >= Configuration.DIALOG_GRID) {
            mGalleryView?.setLayoutManager(GridLayoutManager(activity, mAlbumColumnNumber))
            mGalleryView?.addItemDecoration(GridSpacingItemDecoration(mAlbumColumnNumber, dp2px(mConfig!!.spaceSize.toFloat()), true))
            mGalleryView?.setItemAnimator(DefaultItemAnimator())
            mGalleryView?.setAdapter(mAdapterAlbumGrid)
            mAdapterAlbumGrid?.setDataSource(presenter?.mList)
        } else {
            mGalleryView?.setLayoutManager(LinearLayoutManager(activity))
            mGalleryView?.addItemDecoration(VerticalSpaceItemDecoration(dp2px(mConfig!!.spaceSize.toFloat())))
            mGalleryView?.setItemAnimator(DefaultItemAnimator())
            mGalleryView?.setAdapter(mAdapterAlbumGrid)
            mAdapterAlbumGrid?.setDataSource(presenter?.mList)
        }
        dialog?.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        if (mConfig?.dialogHeight!! < 0) {
            mBehavior?.setPeekHeight(if (mConfig?.dialogHeight!! <= Configuration.DIALOG_HALF) screenHeight / 2 else screenHeight)
        } else {
            if (mConfig?.dialogHeight!! >= screenHeight) screenHeight else mConfig?.dialogHeight?.let { mBehavior?.setPeekHeight(it) }
        }
        dialog?.show()
    }

    fun onShowDialog() {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(activity!!)
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(ContextCompat.getColor(SuperSafeApplication.getInstance(),R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, object : MaterialDialog.InputCallback {
                    override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
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
                })
        builder.show()
    }

    private fun dp2px(dp: Float): Int {
        return (dp * activity?.resources?.displayMetrics?.density!! + 0.5f) as Int
    }

    private fun getGallerWidth(container: ViewGroup?): Int {
        return Utils.getScreenWidth(activity!!) - (container?.getPaddingLeft()!! - container?.getPaddingRight())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                if (mAdapterAlbumGrid != null) {
                    mAdapterAlbumGrid?.setDataSource(presenter?.mList)
                }
            }
            EnumStatus.MOVE -> {
                dialog?.dismiss()
                EventBus.getDefault().post(EnumStatus.UPDATED_VIEW_DETAIL_ALBUM)
                if (mListener != null) {
                    mListener?.onMoveAlbumSuccessful()
                }
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: ItemModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<ItemModel>?) {}
    override fun getListItems(): MutableList<ItemModel>? {
        return if (mListener != null) {
            mListener?.getListItems()
        } else null
    }

    companion object {
        fun newInstance(): Fragment? {
            return MoveGalleryFragment()
        }
    }
}