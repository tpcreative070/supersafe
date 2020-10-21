package co.tpcreative.supersafe.common.controller

import co.tpcreative.supersafe.common.controllerimport.PremiumManager

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
        open fun onUpdateView()
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