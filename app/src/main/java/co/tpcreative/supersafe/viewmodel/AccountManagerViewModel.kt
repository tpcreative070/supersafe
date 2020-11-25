package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AppLists
import kotlinx.coroutines.Dispatchers

class AccountManagerViewModel : BaseViewModel<AppLists>() {
    fun getData() = liveData(Dispatchers.Main){
        val qrScanner: Boolean? = Utils.appInstalledOrNot(getString(R.string.qrscanner_package_name)!!)
        //boolean gpsSpeed = Utils.appInstalledOrNot(getString(R.string.gpsspeed_package_name));
        dataList.add(AppLists("ic_qrscanner_launcher", "QRScanner", "Scan code quickly by your hands", qrScanner!!, getString(R.string.qrscanner_package_name), getString(R.string.qrscanner_link)))
        //mList.add(new AppLists("ic_gpsspeedkmh_launcher","GPSSpeedKmh","Calculate speed without internet",gpsSpeed,getString(R.string.gpsspeed_package_name),getString(R.string.gpsspeed_link)));
        emit(dataList)
    }
}