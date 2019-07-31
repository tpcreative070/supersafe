package co.tpcreative.supersafe.ui.albumcover;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.ThemeApp;

public class AlbumCoverDefaultAdapter extends BaseAdapter<MainCategories, BaseHolder> {

    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();

    private String TAG = AlbumCoverDefaultAdapter.class.getSimpleName();

    public AlbumCoverDefaultAdapter(LayoutInflater inflater, Context context,ItemSelectedListener itemSelectedListener) {
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
        return new ItemHolder(inflater.inflate(R.layout.album_cover_item, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickedDefaultItem(int position);
    }

    public class ItemHolder extends BaseHolder<MainCategories> {
        @BindView(R.id.imgAlbum)
        ImageView imgAlbum;
        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        @BindView(R.id.imgSelect)
        ImageView imgSelect;
        @BindView(R.id.view_alpha)
        View view_alpha;
        int mPosition;
        MainCategories categories;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(MainCategories data, int position) {
            super.bind(data, position);
            mPosition = position;
            Utils.Log(TAG,"load data");
            categories = data;
            if (data.isChecked) {
                view_alpha.setAlpha(0.5f);
                imgIcon.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(themeApp.getAccentColor()), android.graphics.PorterDuff.Mode.SRC_IN);
                imgSelect.setVisibility(View.VISIBLE);

            } else {
                imgIcon.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                view_alpha.setAlpha(0.0f);
                imgSelect.setVisibility(View.INVISIBLE);
            }
            try {
                imgAlbum.setImageResource(0);
                int myColor = Color.parseColor(categories.image);
                imgAlbum.setBackgroundColor(myColor);
                imgIcon.setImageDrawable(MainCategories.getInstance().getDrawable(context, categories.icon));
                imgIcon.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickedDefaultItem(mPosition);
            }
        }
    }

}
