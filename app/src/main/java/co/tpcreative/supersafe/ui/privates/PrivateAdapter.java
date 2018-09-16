package co.tpcreative.supersafe.ui.privates;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.Album;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class PrivateAdapter extends BaseAdapter<MainCategories, BaseHolder> {

    private Context context;
    private Storage storage;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = PrivateAdapter.class.getSimpleName();

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH);

    public PrivateAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.private_item, parent, false));
    }

    public class ItemHolder extends BaseHolder<MainCategories> {

        public ItemHolder(View itemView) {
            super(itemView);
        }

        private Album data;
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        int mPosition;

        @Override
        public void bind(MainCategories data, int position) {
            super.bind(data, position);
            final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestId(data.getLocalId());
            if (items!=null){
                Glide.with(context)
                    .load(storage.readFile(items.thumbnailPath))
                    .apply(options)
                    .into(imgAlbum);
            }
            else{
                imgAlbum.setBackgroundResource(data.getImageResource());
            }
            tvTitle.setText(data.getName());
            this.mPosition = position;
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view){
            if (itemSelectedListener!=null){
                itemSelectedListener.onClickItem(mPosition);
            }
        }

        @OnClick(R.id.overflow)
        public void onClickedOverFlow(View view){
            showPopupMenu(view,mPosition);
        }

    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
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
                case R.id.action_settings:
                    if (itemSelectedListener!=null){
                        itemSelectedListener.onSetting(position);
                    }
                    return true;
                case R.id.action_delete:
                    if (itemSelectedListener!=null){
                        itemSelectedListener.onDeleteAlbum(position);
                    }
                    return true;
                default:
            }
            return false;
        }
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
        void onSetting(int position);
        void onDeleteAlbum(int position);
    }

}
