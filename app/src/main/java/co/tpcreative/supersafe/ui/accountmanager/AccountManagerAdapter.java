package co.tpcreative.supersafe.ui.accountmanager;

import android.content.Context;
import android.graphics.Color;
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

import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Encrypter;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.AppLists;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Theme;

public class AccountManagerAdapter extends BaseAdapter<AppLists, BaseHolder> {

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
    private String TAG = AccountManagerAdapter.class.getSimpleName();

    public AccountManagerAdapter(LayoutInflater inflater, Context context,ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        storage = new Storage(context);
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
        return new ItemHolder(inflater.inflate(R.layout.app_items, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<AppLists> {

        @BindView(R.id.imgIconApp)
        ImageView imgIconApp;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvDescription)
        TextView tvDescription;
        @BindView(R.id.tvStatus)
        TextView tvStatus;
        int mPosition;
        AppLists items;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(AppLists data, int position) {
            super.bind(data, position);
            mPosition = position;
            items = data;
            try {
                imgIconApp.setImageDrawable(MainCategories.getInstance().getDrawable(context,data.ic_name));
                tvTitle.setText(data.title);
                tvDescription.setText(data.description);
                if (data.isInstalled){
                    tvStatus.setText(SuperSafeApplication.getInstance().getString(R.string.installed));
                    tvStatus.setTextColor(SuperSafeApplication.getInstance().getResources().getColor(R.color.material_green_300));
                }
                else{
                    tvStatus.setText(SuperSafeApplication.getInstance().getString(R.string.learn_more));
                    tvStatus.setTextColor(SuperSafeApplication.getInstance().getResources().getColor(R.color.colorButton));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.llHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickItem(mPosition);
            }
        }

    }

}
