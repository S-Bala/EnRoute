package com.enroutetechnologies.enroute;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import static android.R.attr.width;
import static com.enroutetechnologies.enroute.R.id.map;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        HTTPSRequest.Listener,
        FormFragment.Listener,
        android.location.LocationListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ArrayList<Marker> mMarkers = new ArrayList<>();

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    LocationManager locationManager;
    String locationProvider;

    private int txtnumber = 0;

    private GoogleMap mMap;

    private EditText mFrom;
    private EditText mTo;
    private EditText mSearch;
    private SupportMapFragment mMapFragment;
    private GMapV2Direction mGMapV2Direction;
    private Place mFromPlace;
    private Place mToPlace;
    private Button mButton;
    private HTTPSRequest mHTTPSRequest;
    private YelpConnection yelpConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState == null) {showDialog();}
        mGMapV2Direction = new GMapV2Direction();
        mHTTPSRequest = new HTTPSRequest(this, this);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mMapFragment.getMapAsync(this);
        this.initializeLocationManager();


        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void initializeLocationManager() {

        //get the location manager
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //define the location manager criteria
        Criteria criteria = new Criteria();

        this.locationProvider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);


        //initialize the location
        if (location != null) {

            onLocationChanged(location);
        }
    }

    public String getURL(LatLng start, LatLng end, String type) {
        String url = "http://maps.googleapis.com/maps/api/directions/" + type + "?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=driving";
        return url;

    }

    public void autoComplete() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void mapDirection(String response) {
        Document doc = mGMapV2Direction.getDocument(response);
        final ArrayList<LatLng> directionPoint = mGMapV2Direction.getDirection(doc);
        ArrayList<PointOfInterest> yelpPointsofInterests = null;

        yelpConnection = new YelpConnection();
        String searchItem = mSearch.getText().toString();
        try {
            yelpPointsofInterests = yelpConnection.getPointsOfInterests(searchItem, directionPoint, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
        dialog.setTitle("Loading...");
        dialog.setMessage("Please wait.");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while(yelpConnection.counter < 21){}
                ArrayList<PointOfInterest> pointOfInterestsArray = yelpConnection.getPointsOfInterests();
                timer.cancel();
                timer.purge();
                dialog.dismiss();
            }
        }, 1000);

        PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.BLUE);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        Polyline polylin = mMap.addPolyline(rectLine);
        zoomFunction();
    }

    public void zoomFunction(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 0.5f));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (txtnumber == 0) {
                    mFromPlace = place;
                    mFrom.setText(mFromPlace.getName());
                } else if (txtnumber == 1) {
                    mToPlace = place;
                    mTo.setText(mToPlace.getName());
                }
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(14.0f).build();
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
//                mMap.moveCamera(cameraUpdate);
//                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
//                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void requestSuccess(String response) {
        if (isJSONValid(response)){
            try {
                Log.i("GMaps Response", response);
                JSONObject jObject  = new JSONObject(response);
//                JSONObject routes = jObject.getJSONObject("geocoded_waypoints");
//                Log.i("HELLOO", routes.toString());
//                JSONObject legs = routes.getJSONObject("legs");
//                Log.i("HELLOO123", legs.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mapDirection(response);
        }
//        Log.i("Response", response);
        mapDirection(response);
    }

    @Override
    public void requestFailure(String response) {
        showDialog();
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public void showDialog() {
        // Create an instance of the dialog fragment and show it
        FormFragment dialog = new FormFragment();
        dialog.show(getFragmentManager(), "FormFragment");
        dialog.setCancelable(false);
        dialog.setRetainInstance(true);
    }

    @Override
    public void onDialogPositiveClick(DialogInterface dialog, EditText text) {
        mSearch = text;
        if (mFromPlace != null && mToPlace != null) {
            mHTTPSRequest.getRequest(getURL(mFromPlace.getLatLng(), mToPlace.getLatLng(), "xml"),dialog);
            mMarkers.add(mMap.addMarker(new MarkerOptions().position(mFromPlace.getLatLng()).title(mFromPlace.getName().toString())));
            mMarkers.add(mMap.addMarker(new MarkerOptions().position(mToPlace.getLatLng()).title(mToPlace.getName().toString())));
        }

    }

    @Override
    public void onDialogNegativeClick(DialogInterface dialog) {
        dialog.cancel();
    }

    @Override
    public void onFromClick(EditText editText) {
        mFrom = editText;
        txtnumber = 0;
        try {
            synchronized (this) {
                this.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        autoComplete();
    }

    @Override
    public void onToClick(EditText editText) {
        mTo = editText;
        txtnumber = 1;
        try {
            synchronized (this) {
                this.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        autoComplete();
    }

    @Override
    public void onSearchClick(EditText editText) {
        mSearch = editText;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.locationManager.requestLocationUpdates(this.locationProvider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.locationManager.removeUpdates(this);
    }

    //------------------------------------------
    //	Summary: Location Listener  methods
    //------------------------------------------
    @Override
    public void onLocationChanged(Location location) {

        Log.i("called", "onLocationChanged");


        if (mMap != null) {
            //when the location changes, update the map by zooming to the location
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            this.mMap.moveCamera(center);

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            this.mMap.animateCamera(zoom);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
