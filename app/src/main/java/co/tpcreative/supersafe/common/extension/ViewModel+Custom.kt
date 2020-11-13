package co.tpcreative.supersafe.common.extension

import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication

fun ViewModel.getString(res : Int) : String{
    return SuperSafeApplication.getInstance().getString(res)
}

fun ViewModel.postData(){

}