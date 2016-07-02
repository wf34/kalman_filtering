package wf34.kf.gpsacquire;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;


public class GpsLoggerService extends Service implements GpsStatus.NmeaListener, LocationListener {
    private CSVWriter writer;
    private Nmea nmea_parser;
    private LocationManager location_manager;

    @Override
    public void onCreate() {
        writer = null;
        nmea_parser = null;
        location_manager = null;
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
        String[] data = {"time", "latitude", "longitude"};
        writer.writeNext(data);
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
        nmea_parser = new Nmea();
        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location_manager.addNmeaListener(this);
        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); //NETWORK_
        location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        location_manager.removeUpdates(this);
        location_manager.removeNmeaListener(this);
        location_manager = null;
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.getClass().toString(), "loc service running");
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
    public void onNmeaReceived(long timestamp, String message) {
        Log.d(this.getClass().toString(), message);
        Nmea.GPSPosition position = nmea_parser.parse(message);

        double latitude = position.lat;
        double longitude = position.lon;

        // Log.d(this.getClass().toString(), "service running "  + latitude + " " + longitude);
        String[] current_data = {Long.toString(System.currentTimeMillis()),
                Double.toString(latitude),
                Double.toString(longitude)};
        writer.writeNext(current_data);
    }
}
