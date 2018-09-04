package co.tpcreative.suppersafe.ui.albumdetail;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Encrypter;
import co.tpcreative.suppersafe.common.adapter.BaseAdapter;
import co.tpcreative.suppersafe.common.adapter.BaseHolder;
import co.tpcreative.suppersafe.common.controller.SingletonEncryptData;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.model.Album;
import co.tpcreative.suppersafe.model.Items;

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
        int mPosition;

        @Override
        public void bind(Items data, int position) {
            super.bind(data, position);
            String path = data.thumbnailPath;
            File file = new File(path);
            try{
                if (!file.exists() || !file.isFile()){
                    return;
                }
                storage.setEncryptConfiguration(SupperSafeApplication.getInstance().getConfigurationFile());
                Glide.with(context)
                        .load(storage.readFile(path))
                        .apply(options)
                        .into(imgAlbum);
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
