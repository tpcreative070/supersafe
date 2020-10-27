package co.tpcreative.supersafe.ui.me
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.fragment_me.*
import org.greenrobot.eventbus.EventBus

class MeFragment : BaseFragment(), BaseView<EmptyModel> {

    var presenter: MePresenter? = null
    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(
                R.layout.fragment_me, viewGroup, false) as ConstraintLayout
    }

    override fun work() {
        super.work()
        initUI()
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

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        Utils.Log(TAG, "visit :$menuVisible")
        if (menuVisible) {
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
                    tvStatus?.text = getString(R.string.view_user_info)
                } else {
                    tvStatus?.text = getString(R.string.verify_change)
                }
                tvEmail?.text = presenter?.mUser?.email
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Utils.Log(TAG, "OnResume")
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
                tvPhotos?.text =photos
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter?.videos)
                tvVideos?.text = videos
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter?.audios)
                tvAudios?.text = audios
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter?.others)
                tvOther?.text = others
                val availableSpaces: String? = ConvertUtils.byte2FitMemorySize(Utils.getAvailableSpaceInBytes())
                tvAvailableSpaces?.text = availableSpaces
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        fun newInstance(index: Int): MeFragment? {
            val fragment = MeFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}