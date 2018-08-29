package co.tpcreative.suppersafe.ui.cloudmanager;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.ftinc.kit.util.SizeUtils;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.ServiceManager;


public class CloudManagerActivity extends BaseActivity {

    private static String TAG = CloudManagerActivity.class.getSimpleName();
    private SlidrConfig mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_manager);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int primary = getResources().getColor(R.color.colorPrimary);
        int secondary = getResources().getColor(R.color.colorPrimaryDark);

        mConfig = new SlidrConfig.Builder()
                .primaryColor(primary)
                .secondaryColor(secondary)
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(this, mConfig);

    }

    @OnClick(R.id.btnCreateFile)
    public void onClickedCreatedFile(View view){
        createFile();
    }


    @OnClick(R.id.btnDisconnect)
    public void onClickedDisconnect(View view){
        ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
            @Override
            public void onCompletedDisconnect() {
                Log.d(TAG,"Disconnected");
                finish();

            }

            @Override
            public void onCompletedSignOut() {
                Log.d(TAG,"SignOut");
                finish();
            }

            @Override
            public void onError() {
                Log.d(TAG,"Error");
            }
        });
    }

    private void createFile() {

        if (ServiceManager.getInstance().getDriveResourceClient()==null){
            return;
        }

        final Task<DriveFolder> rootFolderTask = ServiceManager.getInstance().getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = ServiceManager.getInstance().getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = rootFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write("Hello World!");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("HelloWorld.jpg")
                            .setMimeType("image/jpeg")
                            .build();

                    return ServiceManager.getInstance().getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                        @Override
                        public void onCompletedDisconnect() {

                        }
                        @Override
                        public void onCompletedSignOut() {
                            finish();
                        }
                        @Override
                        public void onError() {

                            finish();
                        }
                    });
                });
        // [END create_file]
    }

}
