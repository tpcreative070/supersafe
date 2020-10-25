package co.tpcreative.supersafe.ui.me
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson

class MePresenter : Presenter<BaseView<EmptyModel>>() {
    var mUser: User? = null
    var videos = 0
    var photos = 0
    var audios = 0
    var others = 0
    fun onShowUserInfo() {
        mUser = Utils.getUserInfo()
        Utils.onWriteLog(Gson().toJson(mUser), EnumStatus.USER_INFO)
    }

    fun onCalculate() {
        try {
            val view: BaseView<EmptyModel>? = view()
            photos = 0
            videos = 0
            audios = 0
            others = 0
            val mList: MutableList<ItemModel>? = SQLHelper.getListAllItems(false, false)
            if (mList != null) {
                for (index in mList) {
                    val enumTypeFile = EnumFormatType.values()[index.formatType]
                    when (enumTypeFile) {
                        EnumFormatType.IMAGE -> {
                            photos += 1
                        }
                        EnumFormatType.VIDEO -> {
                            videos += 1
                        }
                        EnumFormatType.AUDIO -> {
                            audios += 1
                        }
                        EnumFormatType.FILES -> {
                            others += 1
                        }
                    }
                }
            }
            view?.onSuccessful("Successful", EnumStatus.RELOAD)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = MePresenter::class.java.simpleName
    }
}