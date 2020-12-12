package co.tpcreative.supersafe.common.extension

import com.google.gson.Gson
import java.io.Serializable

fun Serializable.toJson() : String{
    return Gson().toJson(this)
}

fun Any.toJson() : String{
    return Gson().toJson(this)
}

fun <T>String.toObject(clazz: Class<T>) : T{
    return Gson().fromJson(this,clazz)
}

fun <T>String.toObjectMayBeNull(clazz: Class<T>) : T?{
    try {
        return Gson().fromJson(this,clazz)
    }catch (e:Exception){
        return null
    }

}
