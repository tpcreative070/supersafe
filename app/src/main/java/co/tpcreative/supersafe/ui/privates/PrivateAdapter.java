package co.tpcreative.supersafe.ui.privates;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Theme;
import co.tpcreative.supersafe.model.room.InstanceGenerator;


public class PrivateAdapter extends BaseAdapter<MainCategories, BaseHolder> {

    private Context context;
    private Storage storage;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = PrivateAdapter.class.getSimpleName();
    Theme theme = Theme.getInstance().getThemeInfo();
    Drawable note1 = SuperSafeApplication.getInstance().getResources().getDrawable(theme.getAccentColor());
    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(theme.getPrimaryColor())
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(theme.getAccentColor())
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

        private MainCategories data;
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        int mPosition;

        @Override
        public void bind(MainCategories data, int position) {
            super.bind(data, position);
            this.data = data;
            if (data.pin.equals("")) {
                final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(data.items_id);
                if (items != null) {
                    EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
                    switch (formatTypeFile) {
                        case AUDIO: {
                            Glide.with(context)
                                    .load(note1)
                                    .apply(options)
                                    .into(imgAlbum);
                            imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                            break;
                        }
                        case FILES:{
                            Glide.with(context)
                                    .load(note1)
                                    .apply(options)
                                    .into(imgAlbum);
                            imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_insert_drive_file_white_48));
                            break;
                        }

                        default: {
                            try {
                                if (storage.isFileExist("" + items.thumbnailPath)) {
                                    imgAlbum.setRotation(items.degrees);
                                    Glide.with(context)
                                            .load(storage.readFile(items.thumbnailPath))
                                            .apply(options)
                                            .into(imgAlbum);
                                    imgIcon.setVisibility(View.INVISIBLE);
                                } else {
                                    imgAlbum.setImageResource(0);
                                    int myColor = Color.parseColor(data.image);
                                    imgAlbum.setBackgroundColor(myColor);
                                    imgIcon.setImageDrawable(MainCategories.getInstance().getDrawable(context, data.icon));
                                    imgIcon.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                } else {
                    imgAlbum.setImageResource(0);
                    imgIcon.setImageDrawable(MainCategories.getInstance().getDrawable(context, data.icon));
                    imgIcon.setVisibility(View.VISIBLE);
                    try {
                        int myColor = Color.parseColor(data.image);
                        imgAlbum.setBackgroundColor(myColor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                imgAlbum.setImageResource(0);
                imgIcon.setImageResource(R.drawable.baseline_https_white_48);
                imgIcon.setVisibility(View.VISIBLE);
                try {
                    int myColor = Color.parseColor(data.image);
                    imgAlbum.setBackgroundColor(myColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tvTitle.setText(data.categories_name);
            this.mPosition = position;
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickItem(mPosition);
            }
        }

        @OnClick(R.id.overflow)
        public void onClickedOverFlow(View view) {
            if (data.categories_hex_name.equals(Utils.getHexCode(context.getString(R.string.key_trash)))){
                showPopupMenu(view, R.menu.menu_trash_album,mPosition);
            }
            else if(data.categories_hex_name.equals(Utils.getHexCode(context.getString(R.string.key_main_album)))){
                showPopupMenu(view, R.menu.menu_main_album,mPosition);
            }
            else{
                if (data.pin.equals("")){
                    showPopupMenu(view, R.menu.menu_album,mPosition);
                }
                else{
                    showPopupMenu(view, R.menu.menu_main_album,mPosition);
                }
            }
        }
    }

    private void showPopupMenu(View view,int menu, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(menu, popup.getMenu());
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
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onSetting(position);
                    }
                    return true;
                case R.id.action_delete:
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onDeleteAlbum(position);
                    }
                    return true;
                case R.id.action_empty_trash :
                    if (itemSelectedListener!=null){
                        itemSelectedListener.onEmptyTrash(position);
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

        void onEmptyTrash(int position);
    }

}
