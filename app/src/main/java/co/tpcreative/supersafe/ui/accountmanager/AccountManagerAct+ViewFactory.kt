package co.tpcreative.supersafe.ui.accountmanager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.User
import kotlinx.android.synthetic.main.activity_account_manager.*

fun AccountManagerAct.intUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = AccountManagerPresenter()
    presenter?.bindView(this)
    presenter?.getData()
    onUpdatedView()
    scrollView?.smoothScrollTo(0, 0)
    initRecycleView()
    btnUpgrade.setOnClickListener {
        Navigator.onMoveToPremium(this)
    }
    rlPremiumComplimentary.setOnClickListener {
        Navigator.onMoveToPremium(this)
    }

    btnSignOut.setOnClickListener {
        Utils.Log(TAG, "sign out")
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    mUser.verified = false
                    mUser.driveConnected = false
                    Utils.setUserPreShare(mUser)
                    onBackPressed()
                }
                override fun onError() {}
                override fun onCancel() {}
            })
        }
    }
}

fun AccountManagerAct.initRecycleView() {
    adapter = AccountManagerAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView?.adapter = adapter
}