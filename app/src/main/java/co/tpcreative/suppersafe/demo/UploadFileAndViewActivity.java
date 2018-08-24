package co.tpcreative.suppersafe.demo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.OpenFileCallback;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.controller.ManagerService;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.DriveFileName;
import co.tpcreative.suppersafe.model.DriveType;


public class UploadFileAndViewActivity extends BaseDemoActivity {

    private static final String TAG = UploadFileAndViewActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CREATOR = 2;
    private DriveType driveType;

    private DriveFolder mDriveFolder;

    private List<DriveFileName> mListFile;

    /*Download File*/

    /**
     * Progress bar to show the current download progress of the file.
     */

    private ExecutorService mExecutorService;


    private final String FOLDER_NAME = "SUPPER_SAFE";
    private final String FOLDER_NAME_IN_APP = "SUPPER_SAFE_IN_APP";

    @BindView(R.id.edtFolder)
    EditText edtFolder;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.tvSize)
    TextView tvSize;

    private boolean isFolderExisting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file_and_view);
    }

    @Override
    protected void onDriveClientReady() {
        checkFolderInApp();
        edtFolder.setText(FOLDER_NAME);
        /*Init Download File*/
        mProgressBar.setMax(100);
        mExecutorService = Executors.newSingleThreadExecutor();
        mListFile = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }

    @OnClick(R.id.btnSelectFile)
    public void onSelectFile(View view){
        Log.d(TAG,"select file");
        driveType = DriveType.ROOT;
        Navigator.onMoveToAlbum(UploadFileAndViewActivity.this);
    }

    @OnClick(R.id.btnListFile)
    public void onListFile(View view){
        listFiles();
    }

    @OnClick(R.id.btnUploadAppFolder)
    public void onUploadFileToInAppFolder(){
        driveType = DriveType.IN_APP_FOLDER;
        Navigator.onMoveToAlbum(UploadFileAndViewActivity.this);
    }

    @OnClick(R.id.btnUploadFolder)
    public void onUploadFileToFolder(View view){
        driveType = DriveType.IN_FOLDER;
        Navigator.onMoveToAlbum(UploadFileAndViewActivity.this);
    }

    @OnClick(R.id.btnListFileInFolder)
    public void onListFileInFolder(View view){
        listFilesInFolder(mDriveFolder);
    }

    @OnClick(R.id.btnDownloadFile)
    public void onDownLoadFile(View view){
        if (mListFile.size()>0){
            Log.d(TAG,"Can download");
            for (DriveFileName index : mListFile){
                onDownloadFile(index);
            }
        }else{
            listFilesInFolder(mDriveFolder);
        }
    }

    @OnClick(R.id.btnSpecificFolder)
    public void onGetSpecificFolder(View view){
        getSpecificFolder();
        listFilesInFolder(mDriveFolder);
    }

    @OnClick(R.id.btnCreateFoler)
    public void onCreateFolder(View view){
        if (Utils.isValid(edtFolder.getText().toString())){
            isFolderExisting = false;
            checkFolder();
        }
    }

    @OnClick(R.id.btnCreateFolderInApp)
    public void onCreateFolderInApp(){
        checkFolderInApp();
    }

    @OnClick(R.id.btnUploadToInAppSpecificFolder)
    public void onUploadToInAppSpecificFolder(View view){
        checkFolderInApp();
        driveType = DriveType.IN_APP_SPECIFIC_FOLDER;
        Navigator.onMoveToAlbum(UploadFileAndViewActivity.this);
    }

    @OnClick(R.id.btnSize)
    public void onCallInfo(){
        ManagerService.getInstance().getMyService().getAction();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"Selected album :");
        switch (requestCode){
           case REQUEST_CODE_CREATOR:
               Log.i(TAG, "creator request code");
               // Called after a file is saved to Drive.
               if (resultCode == RESULT_OK) {
                   Log.i(TAG, "Image successfully saved.");
               }
               break;
           case Constants.REQUEST_CODE :
               if (resultCode == Activity.RESULT_OK && data != null) {
                   switch (driveType){
                       case ROOT:
                           ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                           for (int i = 0, l = images.size(); i < l; i++) {
                               String path = images.get(i).path;
                               createFile(new File(path));
                               Log.d(TAG,"Selected album :" + path);
                           }
                           break;
                       case IN_APP_FOLDER:
                           images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                           for (int i = 0, l = images.size(); i < l; i++) {
                               String path = images.get(i).path;
                               createFileInApp(new File(path));
                               Log.d(TAG,"Selected album :" + path);
                           }
                           break;

                       case IN_FOLDER:
                           images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                           for (int i = 0, l = images.size(); i < l; i++) {
                               String path = images.get(i).path;
                               createFileInFolder(new File(path),mDriveFolder);
                               Log.d(TAG,"Selected album :" + path);
                           }
                           break;

                       case IN_APP_SPECIFIC_FOLDER:
                           images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                           for (int i = 0, l = images.size(); i < l; i++) {
                               String path = images.get(i).path;
                               createFileToInAppSpecificFolder(new File(path),mDriveFolder);
                               Log.d(TAG,"Selected album :" + path);
                           }
                           break;
                   }
               }
               break;
       }
    }


    private void onDownloadFile(DriveFileName file) {
        // [START read_with_progress_listener]
        OpenFileCallback openCallback = new OpenFileCallback() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                        Log.d(TAG, String.format("Loading progress: %d percent", progress));
                        mProgressBar.setProgress(progress);
                    }
                });
            }

            @Override
            public void onContents(@NonNull DriveContents driveContents) {
                // onProgress may not be called for files that are already
                // available on the device. Mark the progress as complete
                // when contents available to ensure status is updated.
                mProgressBar.setProgress(100);
                // Read contents
                // [START_EXCLUDE]

                InputStream input = driveContents.getInputStream();
                try {
                    String path_folder_name = SupperSafeApplication.getInstance().getSupperSafe();
                    File root = new File(path_folder_name+file.name);
                    if (!root.exists()){
                        File parentFolder = new File(path_folder_name);
                        if (!parentFolder.exists()) {
                            parentFolder.mkdirs();
                        }
                        root.createNewFile();
                    }
                    try (OutputStream output = new FileOutputStream(root)) {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;
                        while ((read = input.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        input.close();
                        showMessage(getString(R.string.content_loaded));
                        /*Response Data*/
                        getDriveResourceClient().discardContents(driveContents);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e);
                showMessage(getString(R.string.read_failed));
                // [END_EXCLUDE]
            }
        };

        getDriveResourceClient().openFile(file.mDriveFile, DriveFile.MODE_READ_ONLY, openCallback);
        // [END read_with_progress_listener]
    }

    // [START create_folder]
    private void createFolder() {
        getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(edtFolder.getText().toString())
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(false)
                            .build();
                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(this,
                        driveFolder -> {
                            mDriveFolder = driveFolder;
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFolder.getDriveId().encodeToString()));
                            showMessage(getString(R.string.file_created,
                                    driveFolder.getDriveId().encodeToString()));
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }

    // [START create_folder]
    private void createFolderInApp() {
        getDriveResourceClient()
                .getAppFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(FOLDER_NAME_IN_APP)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(false)
                            .build();
                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(this,
                        driveFolder -> {
                            mDriveFolder = driveFolder;
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFolder.getDriveId().encodeToString()));
                            showMessage(getString(R.string.file_created,
                                    driveFolder.getDriveId().encodeToString()));
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }

    // [START create_folder]
    private void checkFolderInApp() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        StringBuilder stringBuilder = new StringBuilder();
        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer -> {
                            boolean isFolderExisting =false;
                            for (Metadata index : metadataBuffer){
                                String info = "Name :" +index.getTitle() + " Id :" + index.getDriveId() + " Resource Id :" + index.getDriveId().getResourceId();
                                stringBuilder.append(info);
                                stringBuilder.append("\n");
                                if (index.getTitle().toUpperCase().equals(FOLDER_NAME_IN_APP.toUpperCase())){
                                    isFolderExisting = true;
                                    mDriveFolder = index.getDriveId().asDriveFolder();
                                }
                            }

                            if (isFolderExisting){
                                showMessage(FOLDER_NAME_IN_APP + " Already existing");
                            }
                            else {
                                createFolderInApp();
                            }
                            Log.d(TAG,stringBuilder.toString());
                        })
                .addOnFailureListener(this, e -> {
                    // Handle failure...
                    // [START_EXCLUDE]
                    Log.e(TAG, "Error retrieving files", e);
                    showMessage(getString(R.string.query_failed));
                    finish();
                    // [END_EXCLUDE]
                });
        // [END query_results]
    }

    private void createFileToInAppSpecificFolder(final File file,final DriveFolder mDriveFolder) {
        if (mDriveFolder==null){
            return;
        }
        // [START create_file]
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(createContentsTask)
                .continueWithTask(task -> {
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e1) {
                        Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType(getMimeType(file.getAbsolutePath()))
                            .build();

                    return getDriveResourceClient().createFile(mDriveFolder, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                            listFiles();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }



    // [START create_folder]
    private void getSpecificFolder() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer -> {
                            for (Metadata index : metadataBuffer){
                                if (index.getTitle().toUpperCase().equals(FOLDER_NAME.toUpperCase())){
                                    mDriveFolder = index.getDriveId().asDriveFolder();
                                    showMessage(FOLDER_NAME + " Already existing");
                                }
                            }
                        })
                .addOnFailureListener(this, e -> {
                    // Handle failure...
                    // [START_EXCLUDE]
                    Log.e(TAG, "Error retrieving files", e);
                    showMessage(getString(R.string.query_failed));
                    finish();
                    // [END_EXCLUDE]
                });
        // [END query_results]
    }


    // [START create_folder]
    private void checkFolder() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        StringBuilder stringBuilder = new StringBuilder();
        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer -> {
                            for (Metadata index : metadataBuffer){
                                String info = "Name :" +index.getTitle() + " Id :" + index.getDriveId() + " Resource Id :" + index.getDriveId().getResourceId();
                                stringBuilder.append(info);
                                stringBuilder.append("\n");
                                if (index.getTitle().toUpperCase().equals(edtFolder.getText().toString().toUpperCase())){
                                    isFolderExisting = true;
                                    mDriveFolder = index.getDriveId().asDriveFolder();
                                }
                            }

                            if (!isFolderExisting){
                                createFolder();
                            }
                            else{
                                showMessage("Folder is existing !!!");
                            }
                            Log.d(TAG,stringBuilder.toString());

                        })
                .addOnFailureListener(this, e -> {
                    // Handle failure...
                    // [START_EXCLUDE]
                    Log.e(TAG, "Error retrieving files", e);
                    showMessage(getString(R.string.query_failed));
                    finish();
                    // [END_EXCLUDE]
                });
        // [END query_results]
    }

    private void createFile(final File file) {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = rootFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();

                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e1) {
                        Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType(getMimeType(file.getAbsolutePath()))
                            .build();

                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                            listFiles();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }


    private void createFileInFolder(final File file,final DriveFolder mDriveFolder) {
        if (mDriveFolder==null){
            return;
        }
        // [START create_file]
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(createContentsTask)
                .continueWithTask(task -> {
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e1) {
                        Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType(getMimeType(file.getAbsolutePath()))
                            .build();

                    return getDriveResourceClient().createFile(mDriveFolder, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                            listFiles();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }

    private void createFileInApp(final File file) {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    DriveFolder parent = rootFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();

                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e1) {
                        Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType(getMimeType(file.getAbsolutePath()))
                            .build();

                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Log.d(TAG,getString(R.string.file_created,
                                    driveFile.getDriveId()));
                            showMessage(getString(R.string.file_created,
                                    driveFile.getDriveId().encodeToString()));
                            listFiles();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }


    private void listFiles() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"))
                .build();
        // [START query_files]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        // [END query_files]
        // [START query_results]

        StringBuilder stringBuilder = new StringBuilder();

        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer -> {
                            // Handle results...
                            // [START_EXCLUDE]

                            for (Metadata index : metadataBuffer){
                                String info = "Name :" +index.getTitle() + " Id :" + index.getDriveId() + " Resource Id :" + index.getDriveId().getResourceId();
                                stringBuilder.append(info);
                                stringBuilder.append("\n");
                            }

                            Log.d(TAG,stringBuilder.toString());


                            // [END_EXCLUDE]
                        })
                .addOnFailureListener(this, e -> {
                    // Handle failure...
                    // [START_EXCLUDE]
                    Log.e(TAG, "Error retrieving files", e);
                    showMessage(getString(R.string.query_failed));
                    finish();
                    // [END_EXCLUDE]
                });
        // [END query_results]
    }

    private void listFilesInFolder(DriveFolder folder) {
        if (folder==null){
            return;
        }
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"))
                .build();
        // [START query_children]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().queryChildren(folder, query);
        // END query_children]
        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer ->{
                            mListFile.clear();
                            for (Metadata index : metadataBuffer){
                                String info = "Name :" +index.getTitle() + " Id :" + index.getDriveId() + " Resource Id :" + index.getDriveId().getResourceId();
                                Log.d(TAG,"info :"+ info);
                                mListFile.add(new DriveFileName(index.getDriveId().asDriveFile(),index.getTitle()));
                            }
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error retrieving files", e);
                    showMessage(getString(R.string.query_failed));
                    finish();
                });
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


}
