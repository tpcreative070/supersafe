package co.tpcreative.supersafe.ui.me
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.BaseFragment
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.viewmodel.MeViewModel
import kotlinx.android.synthetic.main.fragment_me.*

class MeFragment : BaseFragment(){
    lateinit var viewModel : MeViewModel
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
            Utils.onPushEventBus(EnumStatus.HIDE_FLOATING_BUTTON)
            onUpdatedView()
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
        getData()
        onUpdatedView()
        try {
            if (Utils.isVerifiedAccount()) {
                tvStatus?.text = getString(R.string.view_user_info)
            } else {
                tvStatus?.text = getString(R.string.verify_change)
            }
            tvEmail?.text = Utils.getUserId()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Utils.Log(TAG, "OnResume")
    }
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