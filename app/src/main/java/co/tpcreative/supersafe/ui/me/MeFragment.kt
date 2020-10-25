package co.tpcreative.supersafe.ui.me
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.SyncData
import co.tpcreative.supersafe.model.ThemeApp
import org.greenrobot.eventbus.EventBus

class MeFragment : BaseFragment(), BaseView<EmptyModel> {
    @BindView(R.id.nsv)
    var nestedScrollView: NestedScrollView? = null

    @BindView(R.id.imgSettings)
    var imgSettings: AppCompatImageView? = null

    @BindView(R.id.imgPro)
    var imgPro: AppCompatImageView? = null

    @BindView(R.id.tvEmail)
    var tvEmail: AppCompatTextView? = null

    @BindView(R.id.tvStatus)
    var tvStatus: AppCompatTextView? = null

    @BindView(R.id.tvEnableCloud)
    var tvEnableCloud: AppCompatTextView? = null

    @BindView(R.id.llAboutLocal)
    var llAboutLocal: LinearLayout? = null
    private var presenter: MePresenter? = null

    @BindView(R.id.tvPremiumLeft)
    var tvPremiumLeft: AppCompatTextView? = null

    @BindView(R.id.tvAudios)
    var tvAudios: AppCompatTextView? = null

    @BindView(R.id.tvPhotos)
    var tvPhotos: AppCompatTextView? = null

    @BindView(R.id.tvVideos)
    var tvVideos: AppCompatTextView? = null

    @BindView(R.id.tvOther)
    var tvOther: AppCompatTextView? = null

    @BindView(R.id.tvAvailableSpaces)
    var tvAvailableSpaces: AppCompatTextView? = null

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(
                R.layout.fragment_me, viewGroup, false) as ConstraintLayout
    }

    protected override fun work() {
        super.work()
        nestedScrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                Utils.Log(TAG, "hide")
            } else {
                Utils.Log(TAG, "show")
            }
        })
        presenter = MePresenter()
        presenter?.bindView(this)
        presenter?.onShowUserInfo()
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.verified!!) {
                tvStatus?.setText(getString(R.string.view_user_info))
            } else {
                tvStatus?.setText(getString(R.string.verify_change))
            }
            tvEmail?.setText(presenter?.mUser?.email)
        }
    }

    fun onUpdatedView() {
        val isPremium: Boolean = Utils.isPremium()
        if (isPremium) {
            tvPremiumLeft?.setText(getString(R.string.you_are_in_premium_features))
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            tvPremiumLeft?.setTextColor(ContextCompat.getColor(activity!!,themeApp?.getPrimaryColor()!!))
            if (presenter?.mUser?.driveConnected!!) {
                tvEnableCloud?.setText(getString(R.string.no_limited_cloud_sync_storage))
            } else {
                tvEnableCloud?.setText(getString(R.string.enable_cloud_sync))
            }
        } else {
            if (presenter?.mUser?.driveConnected!!) {
                val value: String?
                val syncData: SyncData? = presenter?.mUser?.syncData
                if (syncData != null) {
                    val result: Int = Navigator.LIMIT_UPLOAD - syncData.left
                    value = kotlin.String.format(getString(R.string.monthly_used), result.toString() + "", "" + Navigator.LIMIT_UPLOAD)
                } else {
                    value = kotlin.String.format(getString(R.string.monthly_used), "0", "" + Navigator.LIMIT_UPLOAD)
                }
                tvEnableCloud?.setText(value)
            } else {
                tvEnableCloud?.setText(getString(R.string.enable_cloud_sync))
            }
            val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
            tvPremiumLeft?.setText(sourceString?.toSpanned())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Utils.Log(TAG, "visit :$isVisibleToUser")
        if (isVisibleToUser) {
            EventBus.getDefault().post(EnumStatus.HIDE_FLOATING_BUTTON)
            onUpdatedView()
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
        presenter?.onCalculate()
        presenter?.onShowUserInfo()
        onUpdatedView()
        try {
            if (presenter?.mUser != null) {
                if (presenter?.mUser?.verified!!) {
                    tvStatus?.setText(getString(R.string.view_user_info))
                } else {
                    tvStatus?.setText(getString(R.string.verify_change))
                }
                tvEmail?.setText(presenter?.mUser?.email)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Utils.Log(TAG, "OnResume")
    }

    @OnClick(R.id.llSettings)
    fun onSettings(view: View?) {
        Navigator.onSettings(getActivity()!!)
    }

    @OnClick(R.id.llAccount)
    fun onVerifyAccount(view: View?) {
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.verified!!) {
                Navigator.onManagerAccount(getActivity()!!)
            } else {
                Navigator.onVerifyAccount(getActivity()!!)
            }
        }
    }

    @OnClick(R.id.llEnableCloud)
    fun onEnableCloud(view: View?) {
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.verified!!) {
                if (!(presenter?.mUser?.driveConnected)!!) {
                    Navigator.onCheckSystem(getActivity()!!, null)
                } else {
                    Navigator.onManagerCloud(getActivity()!!)
                }
            } else {
                Navigator.onVerifyAccount(getActivity()!!)
            }
        }
    }

    @OnClick(R.id.llPremium)
    fun onClickedPremium(view: View?) {
        context?.let { Navigator.onMoveToPremium(it) }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return super.getContext()
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter?.photos)
                tvPhotos?.setText(photos)
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter?.videos)
                tvVideos?.setText(videos)
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter?.audios)
                tvAudios?.setText(audios)
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter?.others)
                tvOther?.setText(others)
                val availableSpaces: String? = ConvertUtils.byte2FitMemorySize(Utils.getAvailableSpaceInBytes())
                tvAvailableSpaces?.setText(availableSpaces)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        private val TAG = MeFragment::class.java.simpleName
        fun newInstance(index: Int): MeFragment? {
            val fragment = MeFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.setArguments(b)
            return fragment
        }
    }
}