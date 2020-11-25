package co.tpcreative.supersafe.ui.move_album
import android.content.Context
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
import co.tpcreative.supersafe.model.GalleryAlbum
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.viewmodel.MoveAlbumViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import de.mrapp.android.util.ThreadUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MoveAlbumFragment : BaseFragment(), MoveAlbumAdapter.ItemSelectedListener {
    var mAlbumColumnNumber = 0
    var mAdapterAlbumGrid: MoveAlbumAdapter? = null
    var mConfig: Configuration? = null
    var dialog: BottomSheetDialog? = null
    var mBehavior: BottomSheetBehavior<*>? = null
    var mListener: OnGalleryAttachedListener? = null
    lateinit var viewModel : MoveAlbumViewModel

    interface OnGalleryAttachedListener {
        fun getConfiguration(): Configuration?
        fun getListItems(): MutableList<ItemModel>?
        fun onMoveAlbumSuccessful()
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
                    ThreadUtil.runOnUiThread(Runnable { getData(mConfig?.localCategoriesId, mConfig?.isFakePIN ?: false) })
                    Utils.Log(TAG, "Updated UI => Warning categories id is null")
                }
            }
            else -> Utils.Log(TAG,"Nothing")
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
        moveAlbum(position)
        Utils.Log(TAG, "Position :$position")
    }

    val itemDataList : MutableList<ItemModel>
    get() {
        return if (mListener != null) {
            mListener?.getListItems() ?: mutableListOf()
        } else mutableListOf()
    }

    val dataSource : MutableList<GalleryAlbum> = mutableListOf()

    companion object {
        fun newInstance(): Fragment {
            return MoveAlbumFragment()
        }
    }
}