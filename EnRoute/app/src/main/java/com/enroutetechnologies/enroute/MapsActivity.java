package com.enroutetechnologies.enroute;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import static com.enroutetechnologies.enroute.R.id.map;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        HTTPSRequest.Listener{

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

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
    private Document mDocument;
    private HTTPSRequest mHTTPSRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mGMapV2Direction = new GMapV2Direction();
        mHTTPSRequest = new HTTPSRequest(this,this);
        mFrom = (EditText)findViewById(R.id.from);
        mTo = (EditText)findViewById(R.id.to);
        mSearch = (EditText)findViewById(R.id.search);
        mButton = (Button)findViewById(R.id.submit);

        mFrom.setOnClickListener(onClickListener);
        mTo.setOnClickListener(onClickListener);
        mSearch.setOnClickListener(onClickListener);
        mButton.setOnClickListener(onClickListener);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mMapFragment.getMapAsync(this);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.from:
                    txtnumber = 0;
                    autoComplete();
                    break;
                case R.id.to:
                    txtnumber = 1;
                    autoComplete();
                    break;
                case R.id.search:
                    txtnumber = 2;
                    break;
                case R.id.submit:
                    if(mFromPlace != null && mToPlace != null){
                        mHTTPSRequest.getRequest(getURL(mFromPlace.getLatLng(),mToPlace.getLatLng(),"xml"));
                        mHTTPSRequest.getRequest(getURL(mFromPlace.getLatLng(),mToPlace.getLatLng(),"json"));
//                        mDocument = mGMapV2Direction.getDocument(mFromPlace.getLatLng(),mToPlace.getLatLng(),mGMapV2Direction.MODE_DRIVING);
//                        mapDirection();
                    }
                    break;
            }
        }
    };

    public String getURL(LatLng start, LatLng end, String type){
        String url = "http://maps.googleapis.com/maps/api/directions/"+ type +"?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=driving";
        return url;

    }
    public void autoComplete(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void mapDirection(String response){
        Document doc = mGMapV2Direction.getDocument(response);

        ArrayList<LatLng> directionPoint = mGMapV2Direction.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(3).color(R.color.Red);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        Polyline polylin = mMap.addPolyline(rectLine);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (txtnumber == 0){
                    mFromPlace = place;
                    mFrom.setText(mFromPlace.getName());
                } else if (txtnumber == 1){
                    mToPlace = place;
                    mTo.setText(mToPlace.getName());
                }
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(14.0f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.moveCamera(cameraUpdate);
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
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
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
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
        Log.i("Response", response);
        if (isJSONValid(response)){

        } else {
            mapDirection(response);
        }
    }

    @Override
    public void requestFailure(String response) {

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
}
