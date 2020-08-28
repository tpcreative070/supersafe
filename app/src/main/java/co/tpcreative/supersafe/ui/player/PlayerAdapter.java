package co.tpcreative.supersafe.ui.player;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.snatik.storage.Storage;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.ThemeApp;

public class PlayerAdapter extends BaseAdapter<ItemEntity, BaseHolder> {
    private Context mContext;
    private Storage storage;
    private PlayerAdapter.ItemSelectedListener ls;
    private String TAG = PlayerAdapter.class.getSimpleName();
    private ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();

    public PlayerAdapter(LayoutInflater inflater, Context context, PlayerAdapter.ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.mContext = context;
        storage = new Storage(context);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        ls = itemSelectedListener;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return dataSource.size();
    }
    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlayerAdapter.ItemHolder(inflater.inflate(R.layout.item_player, parent, false));
    }
    public interface ItemSelectedListener {
        void onClickGalleryItem(int position);
    }
    public class ItemHolder extends BaseHolder<ItemEntity> {
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.imgPlaying)
        ImageView imgPlaying;
        private int mPosition;
        private ItemEntity data;
        public ItemHolder(View itemView) {
            super(itemView);
        }
        @Override
        public void bind(ItemEntity mData, int position) {
            super.bind(mData, position);
            this.data = mData;
            if (data.isChecked){
                imgPlaying.setVisibility(View.VISIBLE);
            }
            else{
                imgPlaying.setVisibility(View.INVISIBLE);
            }
            imgPlaying.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(themeApp.getAccentColor()), PorterDuff.Mode.SRC_IN);
            Utils.Log(TAG,"position :"+ data.isChecked);
            tvTitle.setText(data.title);
            this.mPosition = position;
        }
        @OnClick(R.id.rl_item)
        public void onClickedItem(View view){
            if (ls!=null){
                ls.onClickGalleryItem(mPosition);
            }
        }
    }
}
