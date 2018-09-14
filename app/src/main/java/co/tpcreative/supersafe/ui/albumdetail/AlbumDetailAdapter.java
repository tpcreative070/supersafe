package co.tpcreative.supersafe.ui.albumdetail;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Encrypter;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;

public class AlbumDetailAdapter extends BaseAdapter<Items, BaseHolder> {

    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private Encrypter encrypter;
    private Storage storage;
    private String TAG = AlbumDetailAdapter.class.getSimpleName();

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400,400)
            .placeholder(R.drawable.ic_camera)
            .error(R.drawable.ic_aspect_ratio)
            .priority(Priority.HIGH);

    public AlbumDetailAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
        this.itemSelectedListener = itemSelectedListener;
        try {
            encrypter = new Encrypter();
        }
        catch (NoSuchAlgorithmException e){
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

    public class ItemHolder extends BaseHolder<Items> {

        public ItemHolder(View itemView) {
            super(itemView);
        }

        private Items data;
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.imgVideoCam)
        ImageView imgVideoCam;
        int mPosition;

        @Override
        public void bind(Items data, int position) {
            super.bind(data, position);
            mPosition = position;
            try{
                String path = data.thumbnailPath;
                File file = new File(""+path);
                if (!file.exists() || !file.isFile()){
                    return;
                }
                storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                Glide.with(context)
                        .load(storage.readFile(path))
                        .apply(options)
                        .into(imgAlbum);
                
                EnumFormatType enumTypeFile = EnumFormatType.values()[data.formatType];
                switch (enumTypeFile){
                    case VIDEO:{
                        imgVideoCam.setVisibility(View.VISIBLE);
                        break;
                    }
                    default:{
                        imgVideoCam.setVisibility(View.INVISIBLE);
                        break;
                    }
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        @OnClick(R.id.rlHome)
        public void onClicked(View view){
            if (itemSelectedListener!=null){
                itemSelectedListener.onClickItem(mPosition);
            }
        }

    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album_detail, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
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
                    if (itemSelectedListener!=null){
                        itemSelectedListener.onAddToFavoriteSelected(position);
                    }
                    return true;
                case R.id.action_play_next:
                    if (itemSelectedListener!=null){
                        itemSelectedListener.onPlayNextSelected(position);
                    }
                    return true;
                default:
            }
            return false;
        }
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
        void onAddToFavoriteSelected(int position);
        void onPlayNextSelected(int position);
    }

}
