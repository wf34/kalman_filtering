package wf34.kf.gpsacquire;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Process;
import android.widget.Toast;

public class GpsLoggerService extends Service {

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            while(true) {
                try {
                    Thread.sleep(1000);
                    Log.d(this.getClass().toString(), "service running");
                } catch (InterruptedException e) {
                    // Restore interrupt status.
                    Thread.currentThread().interrupt();
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }
    private ServiceHandler handler;
    private HandlerThread thread;

    @Override
    public void onCreate() {
        thread = new HandlerThread("ServiceStartArguments",
                                   Process.THREAD_PRIORITY_MORE_FAVORABLE);
        thread.start();
        handler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = handler.obtainMessage();
        msg.arg1 = startId;
        handler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Looper looper = handler.getLooper();
        handler = null;
        looper.quit();
        thread.interrupt();
        looper = null;
        thread = null;
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
