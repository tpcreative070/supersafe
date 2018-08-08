package co.tpcreative.suppersafe.ui.albumdetail;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.adapter.BaseAdapter;
import co.tpcreative.suppersafe.common.adapter.BaseHolder;
import co.tpcreative.suppersafe.model.Album;

public class AlbumDetailAdapter extends BaseAdapter<Album, BaseHolder> {

    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = AlbumDetailAdapter.class.getSimpleName();

    public AlbumDetailAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        this.itemSelectedListener = itemSelectedListener;
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
            this.mPosition = position;
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
