package co.tpcreative.supersafe.common.controller

class SingletonManager {
    fun isVisitLockScreen(): Boolean {
        return isVisitLockScreen
    }

    fun setVisitLockScreen(visitLockScreen: Boolean) {
        isVisitLockScreen = visitLockScreen
    }

    fun isVisitFakePin(): Boolean {
        return isVisitFakePin
    }

    fun setVisitFakePin(visitFakePin: Boolean) {
        isVisitFakePin = visitFakePin
    }

    fun isReloadMainTab(): Boolean {
        return isReloadMainTab
    }

    fun setReloadMainTab(reloadMainTab: Boolean) {
        isReloadMainTab = reloadMainTab
    }

    private var isVisitLockScreen = false
    private var isVisitFakePin = false
    private var isReloadMainTab = false

    companion object {
        private var instance: SingletonManager? = null
        fun getInstance(): SingletonManager {
            if (instance == null) {
                synchronized(SingletonManager::class.java) {
                    if (instance == null) {
                        instance = SingletonManager()
                    }
                }
            }
            return instance!!
        }
    }
}