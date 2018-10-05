package co.tpcreative.supersafe.ui.theme;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import co.tpcreative.supersafe.model.EnumStatusProgress;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.Theme;
import de.hdodenhof.circleimageview.CircleImageView;

public class ThemeSettingsAdapter extends BaseAdapter<Theme, BaseHolder> {
    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = ThemeSettingsAdapter.class.getSimpleName();
    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(60,60)
            .priority(Priority.HIGH);

    public ThemeSettingsAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
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
        return new ItemHolder(inflater.inflate(R.layout.theme_item, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<Theme> {

        @BindView(R.id.imgTheme)
        CircleImageView imgTheme;
        @BindView(R.id.imgChecked)
        ImageView imgChecked;
        int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Theme data, int position) {
            super.bind(data, position);
            mPosition = position;
            //imgTheme.setBackgroundColor(data.getPrimaryColor());
            Glide.with(context)
                    .load(context.getResources().getDrawable(data.getPrimaryColor()))
                    .apply(options).into(imgTheme);
            if (data.isCheck){
                imgChecked.setVisibility(View.VISIBLE);
            }
            else {
                imgChecked.setVisibility(View.INVISIBLE);
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