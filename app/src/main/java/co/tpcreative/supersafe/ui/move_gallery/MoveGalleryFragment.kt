package co.tpcreative.supersafe.ui.move_gallery
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MoveGalleryFragment : BaseFragment(), MoveGalleryAdapter.ItemSelectedListener, MoveGalleryView {
    var mAlbumColumnNumber = 0
    var mAdapterAlbumGrid: MoveGalleryAdapter? = null
    var mConfig: Configuration? = null
    var dialog: BottomSheetDialog? = null
    var mBehavior: BottomSheetBehavior<*>? = null
    var mListener: OnGalleryAttachedListener? = null
    var presenter: MoveGalleryPresenter? = null

    interface OnGalleryAttachedListener {
        open fun getConfiguration(): Configuration?
        open fun getListItems(): MutableList<ItemModel>?
        open fun onMoveAlbumSuccessful()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniUI()
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