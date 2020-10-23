package co.tpcreative.supersafe.ui.accountmanager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AppLists
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import java.util.*

class AccountManagerPresenter : Presenter<BaseView<EmptyModel>?>() {
    var mList: MutableList<AppLists>?
    fun getData() {
        val view: BaseView<EmptyModel>? = view()
        val qrScanner: Boolean? = Utils.appInstalledOrNot(getString(R.string.qrscanner_package_name)!!)
        //boolean gpsSpeed = Utils.appInstalledOrNot(getString(R.string.gpsspeed_package_name));
        mList?.add(AppLists("ic_qrscanner_launcher", "QRScanner", "Scan code quickly by your hands", qrScanner!!, getString(R.string.qrscanner_package_name), getString(R.string.qrscanner_link)))
        //mList.add(new AppLists("ic_gpsspeedkmh_launcher","GPSSpeedKmh","Calculate speed without internet",gpsSpeed,getString(R.string.gpsspeed_package_name),getString(R.string.gpsspeed_link)));
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    companion object {
        private val TAG = AccountManagerPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<AppLists>()
    }
}