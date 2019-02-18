package co.tpcreative.supersafe.ui.albumdetail;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ConvertUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.Theme;

public class AlbumDetailVerticalAdapter extends BaseAdapter<Items, BaseHolder> {

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(200 ,200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_100)
            .priority(Priority.HIGH);
    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private Storage storage;
    private String TAG = AlbumDetailVerticalAdapter.class.getSimpleName();
    final Theme theme = Theme.getInstance().getThemeInfo();
    Drawable note1 = SuperSafeApplication.getInstance().getResources().getDrawable( theme.getAccentColor());


    public AlbumDetailVerticalAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
        this.itemSelectedListener = itemSelectedListener;
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.custom_item_verical, parent, false));
    }


    public interface ItemSelectedListener {
        void onClickItem(int position);
        void onLongClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<Items> {

        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSizeCreatedDate)
        TextView tvSizeCreatedDate;
        @BindView(R.id.view_alpha)
        View alpha;
        int mPosition;


        public ItemHolder(View itemView) {
            super(itemView);
        }


        @Override
        public void bind(Items data, int position) {
            super.bind(data, position);
            mPosition = position;
            Utils.Log(TAG,"Position "+ position);

            if (data.isChecked) {
                alpha.setAlpha(0.5f);

            } else {
                alpha.setAlpha(0.0f);
            }

            Utils.Log(TAG,"date time "+data);

            try {
                String path = data.thumbnailPath;
                EnumFormatType formatTypeFile = EnumFormatType.values()[data.formatType];
                tvTitle.setText(data.title);
                String value = ConvertUtils.byte2FitMemorySize(Long.parseLong(data.size));
                tvSizeCreatedDate.setText(value+" created "+ Utils.getCurrentDate(data.originalName));
                switch (formatTypeFile) {
                    case AUDIO: {
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(imgAlbum);
                        break;
                    }
                    case VIDEO: {
                        if (storage.isFileExist(path)){
                            imgAlbum.setRotation(data.degrees);
                            Glide.with(context)
                                    .load(storage.readFile(path))
                                    .apply(options).into(imgAlbum);
                        }
                        break;
                    }
                    case IMAGE: {
                        if (storage.isFileExist(path)){
                            imgAlbum.setRotation(data.degrees);
                            Glide.with(context)
                                    .load(storage.readFile(path))
                                    .apply(options).into(imgAlbum);
                        }
                        break;
                    }
                    case FILES:{
                        Glide.with(context)
                                .load(note1)
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

        @OnLongClick(R.id.rlHome)
        public boolean onLongClickedItem(View view){
            if (itemSelectedListener!=null){
                itemSelectedListener.onLongClickItem(mPosition);
            }
            return true;
        }

    }


}
