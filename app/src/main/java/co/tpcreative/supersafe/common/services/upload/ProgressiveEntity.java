
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.supersafe.common.services.upload;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ProgressiveEntity implements HttpEntity {

    private final ProgressListener listener;
    private final HttpEntity yourEntity;
    public static final String TAG = ProgressiveEntity.class.getSimpleName();

    public ProgressiveEntity(final HttpEntity entity, final ProgressListener progressListener) {
        this.yourEntity = entity;
        this.listener = progressListener;
    }

    @Override
    public void consumeContent() throws IOException {
        //EntityUtils.consume(yourEntity);
        yourEntity.consumeContent();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return yourEntity.getContent();
    }

    @Override
    public Header getContentEncoding() {
        return yourEntity.getContentEncoding();
    }

    @Override
    public long getContentLength() {
        return yourEntity.getContentLength();
    }

    @Override
    public Header getContentType() {
        return yourEntity.getContentType();
    }

    @Override
    public boolean isChunked() {
        return yourEntity.isChunked();
    }

    @Override
    public boolean isRepeatable() {
        return yourEntity.isRepeatable();
    }

    @Override
    public boolean isStreaming() {
        return yourEntity.isStreaming();
    } // CONSIDER put a _real_ delegator into here!

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        yourEntity.writeTo(new ProxyOutputStream(outstream, this.listener));
    }

    public class ProxyOutputStream extends FilterOutputStream {

        private long transferred;
        private final ProgressListener progressListener;
        Long startTime = System.currentTimeMillis();

        public ProxyOutputStream(final OutputStream proxy, final ProgressListener progressListener) {
            super(proxy);
            this.transferred = 0;
            this.progressListener = progressListener;
        }

        @Override
        public void write(byte[] bts) throws IOException {
            out.write(bts);

        }

        @Override
        public void write(byte[] bts, int st, int end) throws IOException {
            out.write(bts, st, end);
            this.transferred += end;
            Long elapsedTime = System.currentTimeMillis() - startTime;
            double speedInKBps = 0.0D;
            try {
                long timeInSecs = elapsedTime / 1000; //converting millis to seconds as 1000m in 1 second
                speedInKBps = (transferred / timeInSecs) / 1024D;
                this.progressListener.transferSpeed(speedInKBps);

            } catch (ArithmeticException ae) {
            }
            this.progressListener.transferred(transferred);
        }

        @Override
        public void write(int idx) throws IOException {
            out.write(idx);
            this.transferred++;
            Long elapsedTime = System.currentTimeMillis() - startTime;
            double speedInKBps = 0.0D;
            try {
                long timeInSecs = elapsedTime / 1000; //converting millis to seconds as 1000m in 1 second
                speedInKBps = (transferred / timeInSecs) / 1024D;
                this.progressListener.transferSpeed(speedInKBps);

            } catch (ArithmeticException ae) {
            }
            this.progressListener.transferred(transferred);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

    } // CONSIDER import this class (and risk more Jar File Hell)

    public interface ProgressListener {
        void transferred(long num);
        void transferSpeed(double speed);
    }


}