package co.tpcreative.supersafe.ui.privates;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.BaseFragment;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.dialog.DialogListener;
import co.tpcreative.supersafe.common.dialog.DialogManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;

public class PrivateFragment extends BaseFragment implements BaseView, PrivateAdapter.ItemSelectedListener, SingletonPrivateFragment.SingletonPrivateFragmentListener {

    private static final String TAG = PrivateFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private PrivatePresenter presenter;
    private PrivateAdapter adapter;
    public boolean isClicked;

    public static PrivateFragment newInstance(int index) {
        PrivateFragment fragment = new PrivateFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    public PrivateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(
                R.layout.fragment_private, viewGroup, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        initRecycleView(inflater);
        SingletonPrivateFragment.getInstance().setListener(this);
        return view;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void work() {
        presenter = new PrivatePresenter();
        presenter.bindView(this);
        presenter.getData();
        adapter.setDataSource(presenter.mList);
        super.work();
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new PrivateAdapter(layoutInflater, getContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 10, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        if (isClicked){
            Utils.Log(TAG,"Deny onClick "+ position);
            return;
        }
        Log.d(TAG, "Position :" + position);
        try {
            String value = Utils.getHexCode(getString(R.string.key_trash));
            if (value.equals(presenter.mList.get(position).categories_hex_name)) {
                Navigator.onMoveTrash(getActivity());
            } else {
                final MainCategories mainCategories = presenter.mList.get(position);
                final String pin = mainCategories.pin;
                isClicked = true;
                if (pin.equals("")) {
                    Navigator.onMoveAlbumDetail(getActivity(), mainCategories);
                } else {
                    onShowChangeCategoriesNameDialog(mainCategories);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSetting(int position) {
        Navigator.onAlbumSettings(getActivity(), presenter.mList.get(position));
    }

    @Override
    public void onDeleteAlbum(int position) {
        Utils.Log(TAG, "Delete album");
        DialogManager.getInstance().onStartDialog(getContext(), R.string.confirm, R.string.are_you_sure_you_want_to_move_this_album_to_trash, new DialogListener() {
            @Override
            public void onClickButton() {
                presenter.onDeleteAlbum(position);
            }
            @Override
            public void dismiss() {

            }
        });
    }

    @Override
    public void onEmptyTrash(int position) {
        try {
            DialogManager.getInstance().onStartDialog(getContext(), R.string.delete_all, R.string.are_you_sure_you_want_to_empty_trash, new DialogListener() {
                @Override
                public void onClickButton() {
                    presenter.onEmptyTrash();
                }
                @Override
                public void dismiss() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;
        Utils.Log(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onUpdateView() {
        if (presenter != null) {
            presenter.getData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "visit :" + isVisibleToUser);
        if (isVisibleToUser) {
            EventBus.getDefault().post(EnumStatus.SHOW_FLOATING_BUTTON);
        }
    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case RELOAD: {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (adapter != null) {
                                adapter.setDataSource(presenter.mList);
                                EventBus.getDefault().post(EnumStatus.PRIVATE_DONE);
                                Utils.Log(TAG,"Reload");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    public void onShowChangeCategoriesNameDialog(final MainCategories mainCategories) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.album_is_locked))
                .content(getString(R.string.enter_a_password_for_this_album))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .negativeText(getString(R.string.cancel))
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        isClicked = false;
                    }
                })
                .positiveText(getString(R.string.open))
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        isClicked = false;
                        if (mainCategories.pin.equals(input.toString())) {
                            Navigator.onMoveAlbumDetail(getActivity(), mainCategories);
                            dialog.dismiss();
                        } else {
                            Utils.showInfoSnackbar(getView(),R.string.wrong_password,true);
                            dialog.getInputEditText().setText("");
                        }
                    }
                });
        builder.show();
    }
}
