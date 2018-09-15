package co.tpcreative.supersafe.ui.albumdetail;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import java.security.NoSuchAlgorithmException;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Encrypter;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;

public class AlbumDetailAdapter extends BaseAdapter<Items, BaseHolder> {

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.drawable.baseline_music_note_white_48)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH);
    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private Encrypter encrypter;
    private Storage storage;
    private String TAG = AlbumDetailAdapter.class.getSimpleName();

    public AlbumDetailAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
        this.itemSelectedListener = itemSelectedListener;
        try {
            encrypter = new Encrypter();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.album_detail_item, parent, false));
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album_detail, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);

        void onAddToFavoriteSelected(int position);

        void onPlayNextSelected(int position);
    }

    public class ItemHolder extends BaseHolder<Items> {

        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.imgVideoCam)
        ImageView imgVideoCam;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        int mPosition;
        private Items data;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Items data, int position) {
            super.bind(data, position);
            mPosition = position;
            try {
                String path = data.thumbnailPath;
                storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                EnumFormatType formatTypeFile = EnumFormatType.values()[data.formatType];
                switch (formatTypeFile) {
                    case AUDIO: {
                        imgVideoCam.setVisibility(View.VISIBLE);
                        imgVideoCam.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                        tvTitle.setVisibility(View.VISIBLE);
                        tvTitle.setText(data.title);
                        //imgAlbum.setImageDrawable(context.getResources().getDrawable(R.drawable.image_background_audio_video));
                        Glide.with(context)
                                .load(R.drawable.ic_video_audio_v3)
                                .apply(options).into(imgAlbum);
                        Utils.Log(TAG,"audio");
                        break;
                    }
                    case VIDEO: {
                        imgVideoCam.setVisibility(View.VISIBLE);
                        imgVideoCam.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_videocam_white_36));
                        tvTitle.setVisibility(View.INVISIBLE);
                        Glide.with(context)
                                .load(storage.readFile(path))
                                .apply(options).into(imgAlbum);
                        break;
                    }
                    default: {
                        tvTitle.setVisibility(View.INVISIBLE);
                        imgVideoCam.setVisibility(View.INVISIBLE);
                        Glide.with(context)
                                .load(storage.readFile(path))
                                .apply(options).into(imgAlbum);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickItem(mPosition);
            }
        }

    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        int position;

        public MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onAddToFavoriteSelected(position);
                    }
                    return true;
                case R.id.action_play_next:
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onPlayNextSelected(position);
                    }
                    return true;
                default:
            }
            return false;
        }
    }

}
