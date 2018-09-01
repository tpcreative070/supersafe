package co.tpcreative.suppersafe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.jaychang.sa.AuthCallback;
import com.jaychang.sa.AuthData;
import com.jaychang.sa.AuthDataHolder;
import com.jaychang.sa.SocialUser;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.suppersafe.demo.oauthor.GoogleAuthActivity;

/**
 * Simple list-based Activity to redirect to one of the other Activities. The code here is
 * uninteresting, {@link GoogleSignInActivity} is a good place to start if you are curious about
 * {@code GoogleSignInApi}.
 */
public class ChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = ChooserActivity.class.getSimpleName();

    private static final Class[] CLASSES = new Class[]{
            GoogleSignInActivity.class,
            SignInActivityWithDrive.class,
            IdTokenActivity.class,
            ServerAuthCodeActivity.class,
            RestApiActivity.class,
            GoogleAuthActivity.class
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_sign_in_activity,
            R.string.desc_sign_in_activity_scopes,
            R.string.desc_id_token_activity,
            R.string.desc_auth_code_activity,
            R.string.desc_rest_activity,
            R.string.sign_in_by_oauth

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.list_view);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked = CLASSES[position];
        if (clicked==GoogleAuthActivity.class){
            List<String> requiredScopes = new ArrayList<>();
             requiredScopes.add(DriveScopes.DRIVE);
             requiredScopes.add(DriveScopes.DRIVE_FILE);
        AuthDataHolder.getInstance().googleAuthData = new AuthData(requiredScopes, new AuthCallback() {
            @Override
            public void onSuccess(SocialUser socialUser) {
                Log.d(TAG,"onSuccess : " + socialUser.accessToken);
                Log.d(TAG,"user :" + new Gson().toJson(socialUser));
            }
            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG,"onError");
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"onCancel");
            }
        });
        GoogleAuthActivity.start(this);
        }
        else{
            startActivity(new Intent(this, clicked));
        }
    }

    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(mClasses[position].getSimpleName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);

            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }
}
