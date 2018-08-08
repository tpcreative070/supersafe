package co.tpcreative.suppersafe.ui.privates;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.FabWithLabelView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.ceryle.fitgridview.FitGridView;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.BaseFragment;
import co.tpcreative.suppersafe.common.Navigator;

import co.tpcreative.suppersafe.ui.lockscreen.EnterPinActivity;

public class PrivateFragment extends BaseFragment implements PrivateView{

    //@BindView(R.id.gridView)
    GridView gridView;
    private PrivateAdapter adapter;
    private PrivatePresenter presenter;
    private static final String TAG = PrivateFragment.class.getSimpleName();

    public PrivateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(
                R.layout.fragment_private, viewGroup, false);
        gridView = view.findViewById(R.id.gridView);
        return view;
    }


    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void work() {
        super.work();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridView.setNestedScrollingEnabled(true);
        }
        presenter = new PrivatePresenter();
        presenter.bindView(this);
        presenter.getData();
        adapter = new PrivateAdapter(getContext(),presenter.mList);
        gridView.setAdapter(adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
