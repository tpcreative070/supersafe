package co.tpcreative.supersafe.common.controller

class GalleryCameraMediaManager {
    fun isProgressing(): Boolean {
        return isProgressing
    }

    fun setProgressing(progressing: Boolean) {
        isProgressing = progressing
    }

    private var isProgressing = false

    companion object {
        private var instance: GalleryCameraMediaManager? = null
        fun getInstance(): GalleryCameraMediaManager? {
            if (instance == null) {
                instance = GalleryCameraMediaManager()
            }
            return instance
        }
    }
}