/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.tpcreative.suppersafe.demo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;

import java.util.List;

import co.tpcreative.suppersafe.ChooserActivity;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.demo.events.ListenChangeEventsForFilesActivity;
import co.tpcreative.suppersafe.demo.events.SubscribeChangeEventsForFilesActivity;

/**
 * An activity to list all available demo activities.
 */
public class HomeActivity extends AppCompatActivity {

    private static  final String TAG = HomeActivity.class.getSimpleName();


    private final Class[] sActivities = new Class[] {
            CreateEmptyFileActivity.class, DeleteSpecificFolderActivity.class,
            ChooserActivity.class,UploadFileAndViewActivity.class,
            CreateFileActivity.class, CreateFolderActivity.class, CreateFileInFolderActivity.class,
            CreateFolderInFolderActivity.class, CreateFileInAppFolderActivity.class,
            CreateFileWithCreatorActivity.class, RetrieveMetadataActivity.class,
            RetrieveContentsActivity.class, RetrieveContentsWithProgressDialogActivity.class,
            EditMetadataActivity.class, AppendContentsActivity.class, RewriteContentsActivity.class,
            PinFileActivity.class, InsertUpdateCustomPropertyActivity.class,
            DeleteCustomPropertyActivity.class, QueryFilesActivity.class,
            QueryFilesInFolderActivity.class, QueryNonTextFilesActivity.class,
            QuerySortedFilesActivity.class, QueryFilesSharedWithMeActivity.class,
            QueryFilesWithTitleActivity.class, QueryFilesWithCustomPropertyActivity.class,
            QueryStarredTextFilesActivity.class, QueryTextOrHtmlFilesActivity.class,
            ListenChangeEventsForFilesActivity.class, SubscribeChangeEventsForFilesActivity.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String[] titles = getResources().getStringArray(R.array.titles_array);
        ListView mListViewSamples = (ListView) findViewById(R.id.listViewSamples);
        mListViewSamples.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));
        mListViewSamples.setOnItemClickListener((arg0, arg1, i, arg3) -> {
            Intent intent = new Intent(getBaseContext(), sActivities[i]);
            startActivity(intent);
        });

        onAddPermission();
    }

    public void onAddPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Storage storage = new Storage(getApplicationContext());
                            String path = ""+ storage.getExternalStorageDirectory()+"/Android/data/"+getPackageName()+"/files/";
                            Log.d(TAG,storage.isDirectoryExists(path) ? "Existing " : "Not existing");
                            storage.createDirectory(path);
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }
}
