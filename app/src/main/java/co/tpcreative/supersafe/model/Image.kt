package co.tpcreative.supersafe.model

import android.os.Parcel
import android.os.Parcelable


class Image : Parcelable {
    var id: Long?
    var name: String?
    var path: String?
    var isSelected = false

    constructor(id: Long, name: String?, path: String?, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.path = path
        this.isSelected = isSelected
    }

    override fun describeContents(): Int {
        return 0
    }


    override fun writeToParcel(dest: Parcel?, flags: Int) {
        id?.let { dest?.writeLong(it) }
        dest?.writeString(name)
        dest?.writeString(path)
    }

    private constructor(`in`: Parcel?) {
        id = `in`?.readLong()
        name = `in`?.readString()
        path = `in`?.readString()
    }

    companion object {
        val CREATOR: Parcelable.Creator<Image?>? = object : Parcelable.Creator<Image?> {
            override fun createFromParcel(source: Parcel?): Image? {
                return co.tpcreative.supersafe.model.Image(source)
            }
            override fun newArray(size: Int): Array<Image?>? {
                return arrayOfNulls<Image?>(size)
            }
        }
    }
}