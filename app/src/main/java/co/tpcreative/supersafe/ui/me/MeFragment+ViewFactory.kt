package co.tpcreative.supersafe.ui.me
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.SyncData
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.fragment_me.*

fun MeFragment.initUI(){
    TAG = this::class.java.simpleName
    nsv?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
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
            tvStatus?.text = getString(R.string.view_user_info)
        } else {
            tvStatus?.text = getString(R.string.verify_change)
        }
        tvEmail?.text = presenter?.mUser?.email
    }

    llSettings.setOnClickListener {
        Navigator.onSettings(activity!!)
    }

    llAccount.setOnClickListener {
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.verified!!) {
                Navigator.onManagerAccount(activity!!)
            } else {
                Navigator.onVerifyAccount(activity!!)
            }
        }
    }

    llEnableCloud.setOnClickListener {
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.verified!!) {
                if (!(presenter?.mUser?.driveConnected)!!) {
                    Navigator.onCheckSystem(activity!!, null)
                } else {
                    Navigator.onManagerCloud(activity!!)
                }
            } else {
                Navigator.onVerifyAccount(activity!!)
            }
        }
    }

    llPremium.setOnClickListener {
        context?.let { Navigator.onMoveToPremium(it) }
    }
}

fun MeFragment.onUpdatedView() {
    val isPremium: Boolean = Utils.isPremium()
    if (isPremium) {
        tvPremiumLeft?.text = getString(R.string.you_are_in_premium_features)
        if (presenter?.mUser?.driveConnected!!) {
            tvEnableCloud?.text = getString(R.string.no_limited_cloud_sync_storage)
        } else {
            tvEnableCloud?.text = getString(R.string.enable_cloud_sync)
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
            tvEnableCloud?.text = value
        } else {
            tvEnableCloud?.text = getString(R.string.enable_cloud_sync)
        }
        val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
        tvPremiumLeft?.text = sourceString?.toSpanned()
    }
}