package co.tpcreative.suppersafe.common;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    public boolean isInLeft;
    public boolean isOutLeft;
    public boolean isCurrentScreen;

    public boolean isLoaded = false;
    public boolean isDead = false;
    private Object object = new Object();

    protected abstract int getLayoutId();




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isDead = false;
        View view = inflater.inflate(getLayoutId(), container, false);
        unbinder = ButterKnife.bind(this, view);
        work();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        synchronized (object) {
            isLoaded = true;
            object.notifyAll();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        isDead = true;
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
        hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        remove();
        isLoaded = false;
    }

    protected void remove(){}

    protected void hide(){}

    protected void work() {

    }

}
