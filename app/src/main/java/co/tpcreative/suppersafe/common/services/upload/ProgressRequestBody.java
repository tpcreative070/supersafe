package co.tpcreative.suppersafe.common.services.upload;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private final String TAG = ProgressRequestBody.class.getSimpleName();

    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;
    private String type ;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        void onError();
        void onFinish();
    }

    public ProgressRequestBody(final File file, final  UploadCallbacks listener) {
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        // i want to upload only images
        if (type==null){
            return MediaType.parse("image/*");
        }

        return MediaType.parse(type);
    }

    public void setContentType(String type){
        this.type = type;
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = this.mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }
        @Override
        public void run() {
            final int percent = (int)(100 * mUploaded / mTotal);
            if (percent==100){
                mListener.onFinish();
            }
            else{
                mListener.onProgressUpdate(percent);
            }
        }
    }
}
