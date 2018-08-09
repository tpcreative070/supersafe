package co.tpcreative.suppersafe.ui.albumdetail;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Encrypter;
import co.tpcreative.suppersafe.common.adapter.BaseAdapter;
import co.tpcreative.suppersafe.common.adapter.BaseHolder;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.model.Album;

public class AlbumDetailAdapter extends BaseAdapter<Album, BaseHolder> {

    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private Encrypter encrypter;
    private Storage storage;
    private String TAG = AlbumDetailAdapter.class.getSimpleName();

    RequestOptions options = new RequestOptions()
            .centerCrop()
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

    public class ItemHolder extends BaseHolder<Album> {

        public ItemHolder(View itemView) {
            super(itemView);
        }
        private Album data;
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        int mPosition;

        @Override
        public void bind(Album data, int position) {
            super.bind(data, position);
            imgAlbum.setImageDrawable(context.getResources().getDrawable(data.getImageResource()));

            String path = SupperSafeApplication.getInstance().getKeepSafety()+"picture.jpg";

          //  byte[] bitmapdata = SupperSafeApplication.getInstance().getStorage().readFile(path);
          //  Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
          //  imgAlbum.setImageBitmap(bitmap);

//            Glide.with(context)
//                    .asBitmap()
//                    .load(bitmap)
//                    .apply(options)
//                    .into(imgAlbum);

//            String path = SupperSafeApplication.getInstance().getKeepSafety()+"newFile.jpg";
//            String outPut = SupperSafeApplication.getInstance().getKeepSafety()+"newFilePut.jpg";
//            File file = new File(outPut);
//            if (!file.exists()){
//                storage.createFile(file.getAbsolutePath(),"");
//            }
//            encrypter.decryptFile(Uri.parse(path),outPut,"hgS+m1eNmYvrlAuRPvXJrA==");
//            this.mPosition = position;
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view){
            if (itemSelectedListener!=null){
                itemSelectedListener.onClickItem(mPosition);
            }
        }

        @OnClick(R.id.overflows)
        public void onClickedOverFlows(View view){
            showPopupMenu(view,mPosition);
            Log.d(TAG,"OverFlow");
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
