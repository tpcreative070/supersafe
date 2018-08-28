package co.tpcreative.suppersafe.demo;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
        final String sFilename = "HelloWorld.jpg";
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
                        Log.d(TAG,"finish");
                        finish();
                    }
                } )
                .addOnFailureListener( this, new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        Log.i( TAG, "ERROR: File not found: " + sFilename );
                    }
                } );
    }

}
