package wf34.kf.gpsacquire;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;


public class GpsLoggerService extends Service implements LocationListener {
    private CSVWriter writer;

    @Override
    public void onCreate() {
        writer = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        Bundle extras = intent.getExtras();
        if (null != extras) {
            String file = extras.getString(getResources().getString(R.string.dump_destination_key));
            Log.d(this.getClass().toString(), file);
            try {
                writer = new CSVWriter(new FileWriter(file),
                                       CSVWriter.DEFAULT_SEPARATOR,
                                       CSVWriter.NO_QUOTE_CHARACTER);
            } catch (IOException e) {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("extras were not provided");
        }
        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String[] data = {"time", "longitude", "latitude"};
        writer.writeNext(data);
        location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); //GPS_
        location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d(this.getClass().toString(), "service running" + longitude + " " + latitude);
        String[] current_data = {Long.toString(System.currentTimeMillis()),
                Double.toString(longitude),
                Double.toString(latitude)};
        writer.writeNext(current_data);
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
}
