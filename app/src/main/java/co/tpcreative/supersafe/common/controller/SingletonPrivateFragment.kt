package co.tpcreative.supersafe.common.controller

class SingletonPrivateFragment {
    private var ls: SingletonPrivateFragmentListener? = null
    fun setListener(ls: SingletonPrivateFragmentListener?) {
        this.ls = ls
    }

    fun onUpdateView() {
        if (SingletonManager.getInstance().isVisitLockScreen()) {
            return
        }
        if (ls != null) {
            ls?.onUpdateView()
        }
    }

    interface SingletonPrivateFragmentListener {
        fun onUpdateView()
    }

    companion object {
        private var instance: SingletonPrivateFragment? = null
        fun getInstance(): SingletonPrivateFragment? {
            if (instance == null) {
                instance = SingletonPrivateFragment()
            }
            return instance
        }
    }
}