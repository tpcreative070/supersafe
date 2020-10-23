package co.tpcreative.supersafe.ui.albumcover
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import butterknife.ButterKnife
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Encrypter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumEvent
import co.tpcreative.supersafe.model.Event
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import com.snatik.storage.Storage

class AlbumCoverCell(item: Event) : SimpleCell<Event, AlbumCoverCell.ViewHolder>(item) {
    private var listener: ItemSelectedListener? = null
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.drawable.baseline_music_note_white_48)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)
    private val context: Context? = null
    private val itemSelectedListener: ItemSelectedListener? = null
    private val encrypter: Encrypter? = null
    private val storage: Storage? = Storage(SuperSafeApplication.getInstance())
    protected fun setListener(listener: ItemSelectedListener?) {
        this.listener = listener
    }

    protected override fun getLayoutRes(): Int {
        return R.layout.album_cover_item
    }

    /*
    - Return a ViewHolder instance
     */
    protected override fun onCreateViewHolder(parent: ViewGroup, cellView: View): ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    protected override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val event: Event = getItem()
        when (event.getEvent()) {
            EnumEvent.ITEMS -> {
            }
            EnumEvent.MAIN_CATEGORIES -> {
            }
        }
        viewHolder.rlHome?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (listener != null) {
                    listener?.onClickItem(i)
                }
            }
        })
    }

    /**
     * - Our ViewHolder class.
     * - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     */
     class ViewHolder(itemView: View) : SimpleViewHolder(itemView) {
        @BindView(R.id.imgAlbum)
        var imgAlbum: AppCompatImageView? = null

        @BindView(R.id.imgIcon)
        var imgIcon: AppCompatImageView? = null

        @BindView(R.id.imgSelect)
        var imgSelect: AppCompatImageView? = null

        @BindView(R.id.view_alpha)
        var view_alpha: View? = null
        var mPosition = 0

        @BindView(R.id.rlHome)
        var rlHome: RelativeLayout? = null
        var event: Event? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    companion object {
        private val TAG = AlbumCoverCell::class.java.simpleName
    }
}