package co.tpcreative.suppersafe.demo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

import co.tpcreative.suppersafe.R;

public class DeleteSpecificFolderActivity extends BaseDemoActivity {

    private static final String TAG = DeleteSpecificFolderActivity.class.getSimpleName();

    @Override
    protected void onDriveClientReady() {
       // DriveResource driveResource = getDriveResourceClient().getMetadata(getDriveClient());
        onDelete();
    }

    public void onDeleteSpecificFolder(DriveFile file){
        getDriveResourceClient()
                .delete(file)
                .addOnSuccessListener(this,
                        aVoid -> {
                            showMessage(getString(R.string.file_deleted));
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to delete file", e);
                    showMessage(getString(R.string.delete_failed));
                    finish();
                });
    }

    public void onDelete(){

        final String sFilename = "NewFolder";
        Query query = new Query.Builder()
                .addFilter( Filters.eq( SearchableField.TITLE, sFilename ) )
                .build();

        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask.addOnSuccessListener( this,
                new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess( MetadataBuffer metadataBuffer ) {
                        for( Metadata m : metadataBuffer) {
                            DriveResource driveResource = m.getDriveId().asDriveResource();
                            Log.i( TAG, "Deleting file: " + sFilename + "  DriveId:(" + m.getDriveId() + ")" );
                            getDriveResourceClient().delete( driveResource );
                        }
                    }
                } )
                .addOnFailureListener( this, new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        Log.i( TAG, "ERROR: File not found: " + sFilename );
                    }
                } );
    }


    public void getOAuthor(String account) {
        // Try to perform a Drive API request, for instance:
        // File file = service.files().insert(body, mediaContent).execute();
        Set<String> requiredScopes = new HashSet<>(1);
        requiredScopes.add(DriveScopes.DRIVE);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, requiredScopes);
        credential.setSelectedAccountName(account);
        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();



    }


}
