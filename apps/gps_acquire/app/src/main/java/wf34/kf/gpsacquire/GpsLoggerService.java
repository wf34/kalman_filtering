package wf34.kf.gpsacquire;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;


public class GpsLoggerService extends Service  {

    private GpsHandler handler;
    private HandlerThread thread;

    @Override
    public void onCreate() {
        thread = new HandlerThread("ServiceStartArguments",
                                   Process.THREAD_PRIORITY_MORE_FAVORABLE);
        thread.start();
        handler = new GpsHandler(this, thread.getLooper());
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
        thread = null;
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
