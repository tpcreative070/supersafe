package co.tpcreative.supersafe.ui.me
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.SyncData
import co.tpcreative.supersafe.viewmodel.MeViewModel
import kotlinx.android.synthetic.main.fragment_me.*

fun MeFragment.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    nsv?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        if (scrollY > oldScrollY) {
            Utils.Log(TAG, "hide")
        } else {
            Utils.Log(TAG, "show")
        }
    })
    if (Utils.isVerifiedAccount()) {
        tvStatus?.text = getString(R.string.view_user_info)
    } else {
        tvStatus?.text = getString(R.string.verify_change)
    }
    tvEmail?.text = Utils.getUserId()

    llSettings.setOnClickListener {
        Navigator.onSettings(activity!!)
    }

    llAccount.setOnClickListener {
        if (Utils.isVerifiedAccount()) {
            Navigator.onManagerAccount(activity!!)
        } else {
            Navigator.onVerifyAccount(activity!!)
        }
    }

    llEnableCloud.setOnClickListener {
        if (Utils.isVerifiedAccount()) {
            if (Utils.isConnectedToGoogleDrive()) {
                Navigator.onManagerCloud(activity!!)
            } else {
                Navigator.onCheckSystem(activity!!, null)
            }
        } else {
            Navigator.onVerifyAccount(activity!!)
        }
    }

    llPremium.setOnClickListener {
        context?.let { Navigator.onMoveToPremium(it) }
    }

    viewModel.photos.observe(this, Observer {
        tvPhotos.text = String.format(getString(R.string.photos_default), "$it")
    })
    viewModel.videos.observe(this, Observer {
        tvVideos.text = kotlin.String.format(getString(R.string.videos_default), "$it")
    })
    viewModel.audios.observe(this, Observer {
        tvAudios.text = kotlin.String.format(getString(R.string.audios_default), "$it")
    })
    viewModel.others.observe(this, Observer {
        tvOther.text = kotlin.String.format(getString(R.string.others_default), "$it")
    })

}

fun MeFragment.onUpdatedView() {
    val isPremium: Boolean = Utils.isPremium()
    if (isPremium) {
        tvPremiumLeft?.text = getString(R.string.you_are_in_premium_features)
        if (Utils.isConnectedToGoogleDrive()) {
            tvEnableCloud?.text = getString(R.string.no_limited_cloud_sync_storage)
        } else {
            tvEnableCloud?.text = getString(R.string.enable_cloud_sync)
        }
    } else {
        if (Utils.isConnectedToGoogleDrive()) {
            val value: String?
            val syncData: SyncData? = Utils.getSyncData()
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

fun MeFragment.getData(){
    viewModel.getData().observe(this, Observer {
        val availableSpaces: String? = ConvertUtils.byte2FitMemorySize(Utils.getAvailableSpaceInBytes())
        tvAvailableSpaces?.text = availableSpaces
    })
}

private fun MeFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(MeViewModel::class.java)
}