package co.tpcreative.supersafe.ui.help;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.snatik.storage.security.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.Categories;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.HelpAndSupport;

public class HelpAndSupportPresenter extends Presenter<BaseView>{

    protected List<HelpAndSupport> mList;
    protected HelpAndSupport content;

    private static final String TAG = HelpAndSupportPresenter.class.getSimpleName();

    public HelpAndSupportPresenter(){
        mList = new ArrayList<>();
        content = new HelpAndSupport();
    }

    public void onGetList(){
        mList.clear();

        Categories categories = new Categories(0,getString(R.string.faq));
        mList.add(new HelpAndSupport(categories,getString(R.string.i_have_a_new_phone),getString(R.string.i_have_a_new_phone_content),null));

        mList.add(new HelpAndSupport(categories,getString(R.string.how_do_export_my_files),getString(R.string.how_do_export_my_files_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.how_do_i_recover_items_from_trash),getString(R.string.how_do_i_recover_items_from_trash_content),null));

        mList.add(new HelpAndSupport(categories,getString(R.string.i_forgot_the_password_how_to_unlock_my_albums),getString(R.string.i_forgot_the_password_how_to_unlock_my_albums_content),null));

        mList.add(new HelpAndSupport(categories,getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it),getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it_content),null));

        categories = new Categories(1,getString(R.string.contact_support));
        mList.add(new HelpAndSupport(categories,getString(R.string.contact_support),getString(R.string.contact_support_content),null));
    }

    public void onGetDataIntent(Activity activity){
        BaseView view = view();
        try{
            Bundle bundle = activity.getIntent().getExtras();
            content = (HelpAndSupport) bundle.get(HelpAndSupport.class.getSimpleName());
            view.onSuccessful("Successful", EnumStatus.RELOAD);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

    public void onSendGmail(String email,String content){
        BaseView view = view();
        String body = String.format(getString(R.string.support_help_email),email,content);
        String title = String.format(getString(R.string.support_help_title));
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(SecurityUtil.user_name)
                .withPassword(SecurityUtil.password)
                .withMailto(SecurityUtil.tpcreative)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject(title)
                .withBody(body)
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Log.d(TAG,"Successful");
                        view.onSuccessful("Successful",EnumStatus.SEND_EMAIL);
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                        view.onError("Error",EnumStatus.SEND_EMAIL);
                    }
                })
                .send();
    }



}
