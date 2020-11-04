package co.tpcreative.supersafe.common.controller

class SingletonFakePinComponent {
    private var ls: SingletonPrivateFragmentListener? = null
    fun setListener(ls: SingletonPrivateFragmentListener?) {
        this.ls = ls
    }

    fun onUpdateView() {
        if (SingletonManager.getInstance().isVisitLockScreen()) {
            return
        }
        ls.let {
            it?.onUpdateView()
        }
    }

    interface SingletonPrivateFragmentListener {
        fun onUpdateView()
    }

    companion object {
        private var instance: SingletonFakePinComponent? = null
        fun getInstance(): SingletonFakePinComponent {
            if (instance == null) {
                instance = SingletonFakePinComponent()
            }
            return instance!!
        }
    }
}