package co.tpcreative.supersafe.ui.help;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.HelpAndSupport;
import co.tpcreative.supersafe.ui.resetpin.ResetPinActivity;

public class HelpAndSupportActivity extends BaseActivity implements BaseView,HelpAndSupportCell.ItemSelectedListener{

    private HelpAndSupportPresenter presenter;
    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private static final String TAG = HelpAndSupportActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);

        presenter = new HelpAndSupportPresenter();
        presenter.bindView(this);
        presenter.onGetList();
        addRecyclerHeaders();
        bindData();

    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    private void addRecyclerHeaders() {
        SectionHeaderProvider<HelpAndSupport> sh = new SimpleSectionHeaderProvider<HelpAndSupport>() {
            @NonNull
            @Override
            public View getSectionHeaderView(@NonNull HelpAndSupport history, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.help_support_item_header, null, false);
                TextView textView = view.findViewById(R.id.tvHeader);
                textView.setText(history.getCategoryName());
                return view;
            }

            @Override
            public boolean isSameSection(@NonNull HelpAndSupport history, @NonNull HelpAndSupport nextHistory) {
                return history.getCategoryId() == nextHistory.getCategoryId();
            }

            // Optional, whether the header is sticky, default false
            @Override
            public boolean isSticky() {
                return false;
            }
        };
        recyclerView.setSectionHeader(sh);
    }

    private void bindData() {
        List<HelpAndSupport> Galaxys = presenter.mList;
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        List<HelpAndSupportCell> cells = new ArrayList<>();
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (HelpAndSupport galaxy : Galaxys) {
            HelpAndSupportCell cell = new HelpAndSupportCell(galaxy);
            cell.setListener(this);
            cells.add(cell);
        }
        recyclerView.addCells(cells);

    }


    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(HelpAndSupportActivity.class.getSimpleName());
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onClickItem(int position) {
        Utils.Log(TAG,"position :"+position);
        Navigator.onMoveHelpAndSupportContent(this,presenter.mList.get(position));
    }

}
