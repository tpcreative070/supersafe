package co.tpcreative.supersafe.ui.privates
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.dialog.DialogListener
import co.tpcreative.supersafe.common.dialog.DialogManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.ui.privates.PrivateAdapter
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.greenrobot.eventbus.EventBus

class PrivateFragment : BaseFragment(), BaseView<EmptyModel>, PrivateAdapter.ItemSelectedListener, SingletonPrivateFragment.SingletonPrivateFragmentListener {
    private var recyclerView: RecyclerView? = null
    private var presenter: PrivatePresenter? = null
    private var adapter: PrivateAdapter? = null
    var isClicked = false

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        val view: ConstraintLayout = inflater?.inflate(
                R.layout.fragment_private, viewGroup, false) as ConstraintLayout
        recyclerView = view.findViewById(R.id.recyclerView)
        initRecycleView(inflater)
        SingletonPrivateFragment.getInstance()?.setListener(this)
        return view
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun work() {
        presenter = PrivatePresenter()
        presenter?.bindView(this)
        presenter?.getData()
        adapter?.setDataSource(presenter?.mList)
        super.work()
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return super.getContext()
    }

    fun initRecycleView(layoutInflater: LayoutInflater?) {
        adapter = layoutInflater?.let { PrivateAdapter(it, context, this) }
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(context, 2)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.addItemDecoration(GridSpacingItemDecoration(2, 10, true))
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = adapter
    }

    override fun onClickItem(position: Int) {
        if (isClicked) {
            Utils.Log(TAG, "Deny onClick $position")
            return
        }
        try {
            val value: String = Utils.getHexCode(getString(R.string.key_trash))
            if (value == presenter?.mList?.get(position)?.categories_hex_name) {
                Navigator.onMoveTrash(activity!!)
            } else {
                val mainCategories: MainCategoryModel? = presenter?.mList?.get(position)
                val pin: String? = mainCategories?.pin
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
        presenter?.mList?.get(position)?.let { Navigator.onAlbumSettings(activity!!, it) }
    }

    override fun onDeleteAlbum(position: Int) {
        Utils.Log(TAG, "Delete album")
        DialogManager.getInstance()?.onStartDialog(context!!, R.string.confirm, R.string.are_you_sure_you_want_to_move_this_album_to_trash, object : DialogListener {
            override fun onClickButton() {
                presenter?.onDeleteAlbum(position)
            }

            override fun dismiss() {}
        })
    }

    override fun onEmptyTrash(position: Int) {
        try {
            DialogManager.getInstance()?.onStartDialog(context!!, R.string.delete_all, R.string.are_you_sure_you_want_to_empty_trash, object : DialogListener {
                override fun onClickButton() {
                    presenter?.onEmptyTrash()
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
        if (presenter != null) {
            presenter?.getData()
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    private fun dpToPx(dp: Int): Int {
        val r: Resources = getResources()
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            EventBus.getDefault().post(EnumStatus.SHOW_FLOATING_BUTTON)
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                try {
                    getActivity()?.runOnUiThread(Runnable {
                        if (adapter != null) {
                            adapter?.setDataSource(presenter?.mList)
                            EventBus.getDefault().post(EnumStatus.PRIVATE_DONE)
                            Utils.Log(TAG, "Reload")
                        }
                    })
                } catch (e: Exception) {
                    e.message
                }
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    fun onShowChangeCategoriesNameDialog(mainCategories: MainCategoryModel?) {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(getActivity()!!)
                .title(getString(R.string.album_is_locked))
                .content(getString(R.string.enter_a_password_for_this_album))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .negativeText(getString(R.string.cancel))
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        dialog.dismiss()
                        isClicked = false
                    }
                })
                .positiveText(getString(R.string.open))
                .input(null, null, object : MaterialDialog.InputCallback {
                    override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                        isClicked = false
                        if (mainCategories?.pin == input.toString()) {
                            Navigator.onMoveAlbumDetail(getActivity()!!, mainCategories)
                            dialog.dismiss()
                        } else {
                            Utils.showInfoSnackbar(getView()!!, R.string.wrong_password, true)
                            dialog.inputEditText?.setText("")
                        }
                    }
                })
        builder.show()
    }

    companion object {
        private val TAG = PrivateFragment::class.java.simpleName
        fun newInstance(index: Int): PrivateFragment? {
            val fragment = PrivateFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.setArguments(b)
            return fragment
        }
    }
}