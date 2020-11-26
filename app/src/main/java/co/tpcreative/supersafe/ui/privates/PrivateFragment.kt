package co.tpcreative.supersafe.ui.privates
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.dialog.DialogListener
import co.tpcreative.supersafe.common.dialog.DialogManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.viewmodel.PrivateViewModel
import de.mrapp.android.util.ThreadUtil.runOnUiThread

class PrivateFragment : BaseFragment(), PrivateAdapter.ItemSelectedListener, SingletonPrivateFragment.SingletonPrivateFragmentListener {
    var adapter: PrivateAdapter? = null
    var isClicked = false
    lateinit var viewModel : PrivateViewModel

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        val view: ConstraintLayout = inflater?.inflate(
                R.layout.fragment_private, viewGroup, false) as ConstraintLayout
        SingletonPrivateFragment.getInstance()?.setListener(this)
        return view
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun work() {
        super.work()
        initUI()
    }

    override fun onClickItem(position: Int) {
        if (isClicked) {
            Utils.Log(TAG, "Deny onClick $position")
            return
        }
        try {
            val value: String = Utils.getHexCode(getString(R.string.key_trash))
            if (value == dataSource[position].categories_hex_name) {
                Navigator.onMoveTrash(activity!!)
            } else {
                val mainCategories: MainCategoryModel = dataSource[position]
                val pin: String? = mainCategories.pin
                isClicked = true
                if (pin == "") {
                    Navigator.onMoveAlbumDetail(activity!!, mainCategories)
                } else {
                    onShowChangeCategoriesNameDialog(mainCategories)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSetting(position: Int) {
       Navigator.onAlbumSettings(activity!!, dataSource[position])
    }

    override fun onDeleteAlbum(position: Int) {
        DialogManager.getInstance()?.onStartDialog(context!!, R.string.confirm, R.string.are_you_sure_you_want_to_move_this_album_to_trash, object : DialogListener {
            override fun onClickButton() {
                deletedAlbum(position)
            }
            override fun dismiss() {}
        })
    }

    override fun onEmptyTrash(position: Int) {
        try {
            DialogManager.getInstance()?.onStartDialog(context!!, R.string.delete_all, R.string.are_you_sure_you_want_to_empty_trash, object : DialogListener {
                override fun onClickButton() {
                    emptyData()
                }
                override fun dismiss() {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        isClicked = false
        Utils.Log(TAG, "onResume")
    }

    override fun onUpdateView() {
        runOnUiThread(Runnable { getData() })
        Utils.Log(TAG,"Updated..............")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            Utils.onPushEventBus(EnumStatus.SHOW_FLOATING_BUTTON)
        }
    }

    private val dataSource : MutableList<MainCategoryModel>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    companion object {
        private val TAG = PrivateFragment::class.java.simpleName
        fun newInstance(index: Int): PrivateFragment {
            val fragment = PrivateFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}