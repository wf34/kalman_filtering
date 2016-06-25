package wf34.kf.gpsacquire;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

//import com.opencsv.CSVReader;

public class MapActivity extends ActionBarActivity {
    private static final String TAG = "MapActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private boolean can_start;
    private boolean can_stop;
    private boolean can_choose;
    private MenuItem start_button;
    private MenuItem stop_button;
    private MenuItem choose_button;

    private Intent gps_service;

    private ArrayList<Polyline> polylines = new ArrayList<>();
    private ArrayList<LatLng> original_points = new ArrayList<>();

    // TODO: member persistence through SharedPreferences

    public MapActivity() {
        can_start = true;
        can_choose = true;
        can_stop = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        start_button = menu.findItem(R.id.action_start);
        stop_button = menu.findItem(R.id.action_stop);
        choose_button = menu.findItem(R.id.action_choose);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        start_button.setEnabled(can_start);
        stop_button.setEnabled(can_stop);
        choose_button.setEnabled(can_choose);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void flip_buttons() {
        can_start = !can_start;
        can_stop = !can_stop;
        can_choose = !can_choose;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                Log.d(TAG, "start");
                start_service();
                flip_buttons();
                invalidateOptionsMenu();
                return true;

            case R.id.action_stop:
                Log.d(TAG, "stop");
                shutdown_service();
                flip_buttons();
                invalidateOptionsMenu();
                return true;

            case R.id.action_choose:
                Log.d(TAG, "choose");
                draw_line("2016.06.14_21.42.csv"); //  "2016.06.11_21.07.csv"
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void draw_line(String filename) {
        for(Polyline polyline : polylines) {
            polyline.remove();
        }
        original_points.clear();

        String filepath = get_destination_folder() + "/" + filename;
        File file = new File(filepath);
        String next[];
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file)));
            reader.readNext(); // header
            for(;;) {
                next = reader.readNext();
                if(next != null) {
                    original_points.add(new LatLng(Double.parseDouble(next[2]),
                                                   Double.parseDouble(next[1])));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        PolylineOptions polyLineOptions = new PolylineOptions();
        polyLineOptions.addAll(original_points);
        polyLineOptions.width(2);
        polyLineOptions.color(Color.BLUE);
        Polyline polyline = mMap.addPolyline(polyLineOptions);
        polylines.add(polyline);
        mMap.addMarker(new MarkerOptions().position(original_points.get(0))
                                          .title("Start"));
        mMap.addMarker(new MarkerOptions().position(original_points.get(original_points.size() - 1))
                .title("Finish"));
    }

    private String get_destination_folder() {
        File sdCard = Environment.getExternalStorageDirectory();
        File folder = new File (sdCard.getAbsolutePath() + "/gps_dumps");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    private void start_service() {
        gps_service = new Intent(this, GpsLoggerService.class);
        String folder = get_destination_folder();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String filename = folder + "/" + currentDateTimeString + ".csv";
        gps_service.putExtra(getResources().getString(R.string.dump_destination_key),
                             filename);
        startService(gps_service);
    }

    private void shutdown_service() {
        stopService(gps_service);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.7961664, 37.6009312), 14.0f));
    }
}
