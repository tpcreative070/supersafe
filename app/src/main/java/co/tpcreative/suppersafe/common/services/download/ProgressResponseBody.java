
package co.tpcreative.suppersafe.common.services.download;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by PC on 9/1/2017.
 */

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final ProgressResponseBodyListener progressListener;
    private BufferedSource bufferedSource;
    public static final String TAG = ProgressResponseBody.class.getSimpleName();

    public ProgressResponseBody(ResponseBody responseBody, ProgressResponseBodyListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }


    private synchronized Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long allBytes = responseBody.contentLength();
            Long startTime = System.currentTimeMillis();

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);

                //Long elapsedTime = System.nanoTime() - startTime;
                //Long allTimeForDownloading = (elapsedTime * responseBody.contentLength() / bytesRead);
                //Long remainingTime = allTimeForDownloading - elapsedTime;

                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                final float percent = bytesRead == -1 ? 100f : (((float) totalBytesRead / (float) responseBody.contentLength()) * 100);
                if (progressListener != null) {
                    try {
                        if (percent > 1) {
                            if (percent > 99) {
                                progressListener.onAttachmentDownloadUpdate((int) percent);
                                progressListener.onAttachmentDownloadedSuccess();
                            } else {
                                progressListener.onAttachmentDownloadUpdate((int) percent);
                            }

                            progressListener.onAttachmentTotalDownload(allBytes, totalBytesRead);

                            Long elapsedTime = System.currentTimeMillis() - startTime;

                            progressListener.onAttachmentElapsedTime(elapsedTime);

                            Long allTimeForDownloading = (elapsedTime * allBytes / totalBytesRead);

                            progressListener.onAttachmentAllTimeForDownloading(allTimeForDownloading);

                            Long remainingTime = allTimeForDownloading - elapsedTime;

                            progressListener.onAttachmentRemainingTime(remainingTime);

                            double speedInKBps = 0.0D;
                            long timeInSecs = elapsedTime / 1000; //converting millis to seconds as 1000m in 1 second
                            if (timeInSecs != 0) {
                                speedInKBps = (totalBytesRead / timeInSecs) / 1024D;
                                progressListener.onAttachmentSpeedPerSecond(speedInKBps);
                            }

                        }

                    } catch (Exception ae) {
                        progressListener.onAttachmentDownloadedError(ae.getMessage());
                    }
                }

                //Log.d(TAG,"byte read :" + (totalBytesRead/1024) + "MB");
                //Log.d(TAG, "contentLength : " + responseBody.contentLength());
                //Log.d(TAG, "elapsedtime minutes : " + elapsedTime / 1000000000);
                //Log.d(TAG, "alltimefordownloading minutes : " + allTimeForDownloading / 1000000000);
                //Log.d(TAG, "remainingtime minutes : " + remainingTime / 1000000000.0);

                return bytesRead;
            }
        };
    }

    public interface ProgressResponseBodyListener {
        void onAttachmentDownloadedSuccess();

        void onAttachmentDownloadedError(String message);

        void onAttachmentDownloadUpdate(int percent);

        void onAttachmentElapsedTime(long elapsed);

        void onAttachmentAllTimeForDownloading(long all);

        void onAttachmentRemainingTime(long all);

        void onAttachmentSpeedPerSecond(double all);

        void onAttachmentTotalDownload(long totalByte, long totalByteDownloaded);
    }

}
