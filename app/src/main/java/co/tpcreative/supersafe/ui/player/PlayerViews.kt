package co.tpcreative.supersafe.ui.player
import co.tpcreative.supersafe.common.presenter.BaseView

interface PlayerViews : BaseView<Any?> {
    open fun onPlay()
}