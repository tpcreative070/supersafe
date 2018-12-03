package co.tpcreative.supersafe.ui.albumcover;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Theme;

public class AlbumCoverAdapter extends BaseAdapter<Items, BaseHolder> {

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
    private MainCategories categories;
    private String TAG = AlbumCoverAdapter.class.getSimpleName();

    public AlbumCoverAdapter(LayoutInflater inflater, Context context,MainCategories mainCategories, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
        this.categories = mainCategories;
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
        return new ItemHolder(inflater.inflate(R.layout.album_cover_item, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<Items> {
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
        Items items;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Items data, int position) {
            super.bind(data, position);
            mPosition = position;
            Utils.Log(TAG,"load data");
            items = data;
            if (data.isChecked) {
                view_alpha.setAlpha(0.5f);
                imgSelect.setVisibility(View.VISIBLE);

            } else {
                view_alpha.setAlpha(0.0f);
                imgSelect.setVisibility(View.INVISIBLE);
            }

            try {
                storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
                switch (formatTypeFile) {
                    case AUDIO: {
                        Theme theme = Theme.getInstance().getThemeInfo();
                        Drawable note1 = context.getResources().getDrawable(theme.getAccentColor());
                        Glide.with(context)
                                .load(note1)
                                .apply(options)
                                .into(imgAlbum);
                        imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_insert_drive_file_white_48));
                        break;
                    }
                    case FILES:{
                        Theme theme = Theme.getInstance().getThemeInfo();
                        Drawable note1 = context.getResources().getDrawable(theme.getAccentColor());
                        Glide.with(context)
                                .load(note1)
                                .apply(options)
                                .into(imgAlbum);
                        imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_files));
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
                                Utils.Log(TAG,"load data 2");
                            } else {
                                imgAlbum.setImageResource(0);
                                int myColor = Color.parseColor(categories.image);
                                imgAlbum.setBackgroundColor(myColor);
                                imgIcon.setImageDrawable(MainCategories.getInstance().getDrawable(context, categories.icon));
                                imgIcon.setVisibility(View.VISIBLE);
                                Utils.Log(TAG,"load data 3");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

}
