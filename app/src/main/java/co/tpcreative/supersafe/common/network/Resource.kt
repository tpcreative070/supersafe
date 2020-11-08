package co.tpcreative.supersafe.common.network

data class Resource<out T>(val status: Status, val data: T?, val message: String?,val code : Int?) {
    companion object{
        fun<T> success(data:T,code : Int?): Resource<T> = Resource(status= Status.SUCCESS,data = data,message = null,code = code)
        fun<T> error(data:T?,message: String?,code : Int?): Resource<T> = Resource(status= Status.ERROR,data = data,message = message, code = code)
        fun<T> loading(data:T?,code : Int?): Resource<T> = Resource(status= Status.LOADING,data = data,message = null,code = code)
    }
}