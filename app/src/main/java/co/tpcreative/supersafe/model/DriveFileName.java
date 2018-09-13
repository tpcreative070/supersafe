package co.tpcreative.supersafe.model;

import com.google.android.gms.drive.DriveFile;

public class DriveFileName {

    public final DriveFile mDriveFile;
    public final String name;
    public DriveFileName(DriveFile mDriveFile,String name){
        this.mDriveFile = mDriveFile;
        this.name = name;
    }
}
