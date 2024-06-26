package co.tpcreative.supersafe.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class ImageModel(var id: Long, var  name: String?, var path: String?, var isSelected: Boolean) : Parcelable {

}