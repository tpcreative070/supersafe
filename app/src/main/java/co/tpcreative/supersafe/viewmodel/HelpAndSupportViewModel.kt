package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers

class HelpAndSupportViewModel(private val emailOutlookViewModel: EmailOutlookViewModel) :BaseViewModel<HelpAndSupportModel>() {

    var content : String = Utils.getUserId() ?:""
        set(value) {
            field = value
            validationContent(value)
        }

    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    private fun validationContent(mValue : String){
        if (mValue.isBlank()){
            putError(EnumValidationKey.EDIT_TEXT_CONTENT, "Request enter content")
        }
        else{
            putError(EnumValidationKey.EDIT_TEXT_CONTENT)
        }
    }

    fun getData(activity: Activity?) = liveData(Dispatchers.Main) {
        try {
            val bundle: Bundle? = activity?.intent?.extras
            val content = bundle?.get(HelpAndSupportModel::class.java.simpleName) as HelpAndSupportModel
            emit(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getData() = liveData(Dispatchers.Main) {
        dataList.clear()
        var categories = Categories(0, getString(R.string.faq))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.i_have_a_new_phone), getString(R.string.i_have_a_new_phone_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.what_about_google_drive), getString(R.string.what_about_google_drive_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.how_do_export_my_files), getString(R.string.how_do_export_my_files_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.how_do_i_recover_items_from_trash), getString(R.string.how_do_i_recover_items_from_trash_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.i_forgot_the_password_how_to_unlock_my_albums), getString(R.string.i_forgot_the_password_how_to_unlock_my_albums_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it), getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it_content), null))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.what_is_the_secret_pin_and_how_do_i_use_it), getString(R.string.what_is_the_secret_pin_and_how_do_i_use_it_content), null))
        categories = Categories(1, getString(R.string.contact_support))
        dataList.add(HelpAndSupportModel(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null))
        emit(dataList)
    }

    fun sendEmail() = liveData(Dispatchers.Main){
       emit(emailOutlookViewModel.sendEmail(EnumStatus.NONE,content=content))
    }
}