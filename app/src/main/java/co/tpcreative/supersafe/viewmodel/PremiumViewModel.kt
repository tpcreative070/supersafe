package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.CheckoutRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import com.anjlab.android.iab.v3.PurchaseData
import kotlinx.coroutines.Dispatchers

class PremiumViewModel(private val userViewModel: UserViewModel) : BaseViewModel<EmptyModel>() {

    fun checkout(data : PurchaseData) = liveData(Dispatchers.Main) {
        val mResult = userViewModel.checkoutItems(getRequestCheckout(data))
        when(mResult.status){
            Status.SUCCESS -> emit(mResult)
            else -> emit(Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message ?: "",null))
        }
    }

    fun getRequestCheckout(data : PurchaseData) : CheckoutRequest{
        return CheckoutRequest(Utils.getUserId(), data.autoRenewing, data.orderId, data.productId, data.purchaseState.name, data.purchaseToken)
    }
}