package co.tpcreative.supersafe.common.dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import co.tpcreative.supersafe.R;


public class DialogManager {


    private static DialogManager instance;

    public static DialogManager getInstance() {
        if (instance==null){
            instance = new DialogManager();
        }
        return instance;
    }

    public void onStartDialog(Context context, int title, int content, DialogListener ls){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(context)
                .title(context.getString(title))
                .theme(Theme.LIGHT)
                .content(content)
                .titleColor(context.getResources().getColor(R.color.black))
                .negativeText(context.getString(R.string.cancel))
                .positiveText(context.getString(R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (ls!=null){
                            ls.onClickButton();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (ls!=null){
                            ls.dismiss();
                        }
                    }
                });
        builder.show();
    }

}
