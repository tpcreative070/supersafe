package co.tpcreative.supersafe.ui.breakinalerts;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import java.io.File;
import java.util.Locale;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.adapter.BaseAdapter;
import co.tpcreative.supersafe.common.adapter.BaseHolder;
import co.tpcreative.supersafe.model.BreakInAlertsModel;

public class BreakInAlertsAdapter extends BaseAdapter<BreakInAlertsModel, BaseHolder> {
    private Activity myActivity;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = BreakInAlertsAdapter.class.getSimpleName();

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(70,70)
            .priority(Priority.HIGH);

    public BreakInAlertsAdapter(LayoutInflater inflater, Activity activity, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.myActivity = activity;
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public BaseHolder<BreakInAlertsModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.break_in_alerts_item, parent, false));
    }

    public class ItemHolder extends BaseHolder<BreakInAlertsModel> {

        @BindView(R.id.imgPicture)
        ImageView imgPicture;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.tvPin)
        TextView tvPin;
        private int position ;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(BreakInAlertsModel data, int position) {
            super.bind(data, position);
            this.position = position;
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(locale).build();

            tvTime.setText(TimeAgo.using(data.time,messages));
            tvPin.setText(data.pin);
            Glide.with(myActivity)
                    .load(new File(data.fileName))
                    .apply(options).into(imgPicture);
        }

        @OnClick(R.id.llHome)
        public void onClickedItem(View view){
            if (itemSelectedListener!=null){
                itemSelectedListener.onClickItem(position);
            }
        }
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

}
