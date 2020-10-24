package co.tpcreative.supersafe.ui.help
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.HelpAndSupport
import co.tpcreative.supersafe.ui.helpimport.HelpAndSupportCell
import com.jaychang.srv.SimpleRecyclerView
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class HelpAndSupportActivity : BaseActivity(), BaseView<EmptyModel>, HelpAndSupportCell.ItemSelectedListener {
    private var presenter: HelpAndSupportPresenter? = null
    @BindView(R.id.recyclerView)
    var recyclerView: SimpleRecyclerView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = HelpAndSupportPresenter()
        presenter?.bindView(this)
        presenter?.onGetList()
        addRecyclerHeaders()
        bindData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    private fun addRecyclerHeaders() {
        val sh: SectionHeaderProvider<HelpAndSupport> = object : SimpleSectionHeaderProvider<HelpAndSupport>() {
            override fun getSectionHeaderView(history: HelpAndSupport, i: Int): View {
                val view: View = LayoutInflater.from(getContext()).inflate(R.layout.help_support_item_header, null, false)
                val textView: AppCompatTextView? = view.findViewById<AppCompatTextView>(R.id.tvHeader)
                textView?.setText(history.getCategoryName())
                return view
            }

            override fun isSameSection(history: HelpAndSupport, nextHistory: HelpAndSupport): Boolean {
                return history.getCategoryId() == nextHistory.getCategoryId()
            }

            override fun isSticky(): Boolean {
                return false
            }
        }
        recyclerView?.setSectionHeader(sh)
    }

    private fun bindData() {
        val Galaxys: MutableList<HelpAndSupport>? = presenter?.mList
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        val cells: MutableList<HelpAndSupportCell>? = ArrayList()
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        if (Galaxys != null) {
            for (galaxy in Galaxys) {
                val cell = HelpAndSupportCell(galaxy)
                cell.setListener(this)
                cells?.add(cell)
            }
        }
        recyclerView?.addCells(cells!!)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "position :$position")
        presenter?.mList?.get(position)?.let { Navigator.onMoveHelpAndSupportContent(this, it) }
    }

    companion object {
        private val TAG = HelpAndSupportActivity::class.java.simpleName
    }
}