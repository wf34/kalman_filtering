package wf34.kf.gpsacquire;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class GpsHandler extends Handler implements LocationListener {
    private LocationManager location_manager;

    public GpsHandler(Service service, Looper looper) {
        super(looper);
        location_manager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d(this.getClass().toString(), "service running" + longitude + " " + latitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void handleMessage(Message msg) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        while(true) {
            try {
                Thread.sleep(1000);
                location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
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
