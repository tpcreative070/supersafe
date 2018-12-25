package co.tpcreative.supersafe.ui.albumcover;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleViewHolder;
import com.snatik.storage.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Encrypter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Event;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Theme;

public class AlbumCoverCell extends SimpleCell<Event,AlbumCoverCell.ViewHolder> {


    private ItemSelectedListener listener;

    private static final String TAG = AlbumCoverCell.class.getSimpleName();


    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.drawable.baseline_music_note_white_48)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH);
    private Context context;
    private AlbumCoverAdapter.ItemSelectedListener itemSelectedListener;
    private Encrypter encrypter;
    private Storage storage = new Storage(SuperSafeApplication.getInstance());

    public AlbumCoverCell(@NonNull Event item) {
        super(item);
    }

    protected void setListener(ItemSelectedListener listener){
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.album_cover_item;
    }

    /*
    - Return a ViewHolder instance
     */
    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, View cellView) {
        return new ViewHolder(cellView);
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder viewHolder,final int i, @NonNull Context context, Object o) {
          final Event event = getItem();

          switch (event.getEvent()){
              case ITEMS:{

                  break;
              }
              case MAIN_CATEGORIES:{



                  break;
              }
          }

          viewHolder.rlHome.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if (listener!=null){
                      listener.onClickItem(i);
                  }
              }
          });
    }
    /**
     - Our ViewHolder class.
     - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     * */
    static class ViewHolder extends SimpleViewHolder {
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        @BindView(R.id.imgSelect)
        ImageView imgSelect;
        @BindView(R.id.view_alpha)
        View view_alpha;
        int mPosition;
        @BindView(R.id.rlHome)
        RelativeLayout rlHome;
        Event event;
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public  interface ItemSelectedListener{
        void onClickItem(int position);
    }
}

