package co.tpcreative.supersafe.ui.move_gallery;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.snatik.storage.Storage;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.views.SquaredImageView;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.GalleryAlbum;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;


public class MoveGalleryAdapter extends BaseAdapter<GalleryAlbum, BaseHolder> {

    private Context mContext;
    private Storage storage;
    private MoveGalleryAdapter.ItemSelectedListener ls;
    private String TAG = MoveGalleryAdapter.class.getSimpleName();

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher_round)
            .priority(Priority.HIGH);

    public MoveGalleryAdapter(LayoutInflater inflater, Context context, MoveGalleryAdapter.ItemSelectedListener itemSelectedListener) {
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
        return new MoveGalleryAdapter.ItemHolder(inflater.inflate(R.layout.item_move_gallery, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickGalleryItem(int position);
    }

    public class ItemHolder extends BaseHolder<GalleryAlbum> {

        @BindView(R.id.rl_item)
        RelativeLayout rl_item;
        @BindView(R.id.image)
        SquaredImageView imgAlbum;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvPhotos)
        TextView tvPhotos;
        @BindView(R.id.tvVideos)
        TextView tvVideos;
        @BindView(R.id.tvAudios)
        TextView tvAudios;
        @BindView(R.id.tvOthers)
        TextView tvOthers;
        private int mPosition;
        private GalleryAlbum data;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(GalleryAlbum mData, int position) {
            super.bind(mData, position);
            this.data = mData;
            final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestId(data.main.categories_local_id,false,data.main.isFakePin);
            if (items != null) {
                EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
                switch (formatTypeFile) {
                    case AUDIO: {
                        Glide.with(mContext)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options)
                                .into(imgAlbum);
                        break;
                    }
                    case FILES:{
                        Glide.with(mContext)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options)
                                .into(imgAlbum);
                        break;
                    }
                    default: {
                        try {
                            if (storage.isFileExist(""+items.thumbnailPath)){
                                Glide.with(mContext)
                                        .load(storage.readFile(items.thumbnailPath))
                                        .apply(options)
                                        .into(imgAlbum);
                            }
                            else{
                                imgAlbum.setImageResource(0);
                                int myColor = Color.parseColor(data.main.image);
                                imgAlbum.setBackgroundColor(myColor);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } else {
                imgAlbum.setImageResource(0);
                try {
                    int myColor = Color.parseColor(data.main.image);
                    imgAlbum.setBackgroundColor(myColor);
                }
                catch (Exception e){

                }
            }

            String photos = String.format(mContext.getString(R.string.photos_default),""+data.photos);

            String videos = String.format(mContext.getString(R.string.videos_default),""+data.videos);

            String audios = String.format(mContext.getString(R.string.audios_default),""+data.audios);

            String others = String.format(mContext.getString(R.string.others_default),""+data.audios);

            tvPhotos.setText(photos);
            tvVideos.setText(videos);
            tvAudios.setText(audios);
            tvTitle.setText(data.main.categories_name);
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
