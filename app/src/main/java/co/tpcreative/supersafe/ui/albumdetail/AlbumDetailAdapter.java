package co.tpcreative.supersafe.ui.albumdetail;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.snatik.storage.Storage;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.ThemeApp;

public class AlbumDetailAdapter extends BaseAdapter<ItemModel, BaseHolder> {
    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(200 ,200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_200)
            .priority(Priority.HIGH);
    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private Storage storage;
    private String TAG = AlbumDetailAdapter.class.getSimpleName();
    final ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
    Drawable note1 = SuperSafeApplication.getInstance().getResources().getDrawable( themeApp.getAccentColor());

    public AlbumDetailAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
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
        return new ItemHolder(inflater.inflate(R.layout.album_detail_item, parent, false));
    }


    public interface ItemSelectedListener {
        void onClickItem(int position);
        void onLongClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<ItemModel> {
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.imgVideoCam)
        ImageView imgVideoCam;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.progressingBar)
        ProgressBar progressingBar;
        @BindView(R.id.imgCheck)
        ImageView imgCheck;
        @BindView(R.id.view_alpha)
        View view_alpha;
        @BindView(R.id.imgSelect)
        ImageView imgSelect;
        int mPosition;
        @BindView(R.id.rlHome)
        RelativeLayout rlHome;
        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(ItemModel data, int position) {
            super.bind(data, position);
            mPosition = position;
            Utils.Log(TAG,"Position "+ position);
            if (data.isChecked) {
                view_alpha.setAlpha(0.5f);
                imgSelect.setVisibility(View.VISIBLE);
            } else {
                view_alpha.setAlpha(0.0f);
                imgSelect.setVisibility(View.INVISIBLE);
            }
            try {
                String path = data.thumbnailPath;
                EnumFormatType formatTypeFile = EnumFormatType.values()[data.formatType];
                switch (formatTypeFile) {
                    case AUDIO: {
                        imgVideoCam.setVisibility(View.VISIBLE);
                        imgVideoCam.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_music_note_white_48));
                        tvTitle.setVisibility(View.VISIBLE);
                        tvTitle.setText(data.title);
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(imgAlbum);
                        break;
                    }
                    case VIDEO: {
                        imgVideoCam.setVisibility(View.VISIBLE);
                        imgVideoCam.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_videocam_white_36));
                        tvTitle.setVisibility(View.INVISIBLE);
                        if (storage.isFileExist(path)){
                            imgAlbum.setRotation(data.degrees);
                            Glide.with(context)
                                    .load(storage.readFile(path))
                                    .apply(options).into(imgAlbum);
                        }
                        break;
                    }
                    case IMAGE: {
                        tvTitle.setVisibility(View.INVISIBLE);
                        imgVideoCam.setVisibility(View.INVISIBLE);
                        if (storage.isFileExist(path)){
                            imgAlbum.setRotation(data.degrees);
                            Glide.with(context)
                                    .load(storage.readFile(path))
                                    .apply(options).into(imgAlbum);
                        }
                        break;
                    }
                    case FILES:{
                        imgVideoCam.setVisibility(View.VISIBLE);
                        imgVideoCam.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_insert_drive_file_white_48));
                        tvTitle.setVisibility(View.VISIBLE);
                        tvTitle.setText(data.title);
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(imgAlbum);
                        break;
                    }
                }

                progressingBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(themeApp.getAccentColor()),
                        PorterDuff.Mode.SRC_IN);
                EnumStatusProgress progress = EnumStatusProgress.values()[data.statusProgress];
                switch (progress){
                    case PROGRESSING:{
                        imgCheck.setVisibility(View.INVISIBLE);
                        progressingBar.setVisibility(View.VISIBLE);
                        break;
                    }
                    case DONE:{
                        if (data.isSaver){
                            imgCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_attach_money_white_48));
                            imgCheck.setVisibility(View.VISIBLE);
                        }
                        else{
                            imgCheck.setVisibility(View.VISIBLE);
                        }
                        progressingBar.setVisibility(View.INVISIBLE);
                        break;
                    }
                    default:{
                        imgCheck.setVisibility(View.INVISIBLE);
                        progressingBar.setVisibility(View.INVISIBLE);
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
