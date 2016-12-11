package dmcs.pickr.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Arrays;

import dmcs.pickr.R;
import dmcs.pickr.utils.AutocompleteAdapter;
import dmcs.pickr.utils.MapHelper;

public class Details extends FragmentActivity implements OnMapReadyCallback {



    private MapHelper mapHelper;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 88;

    private Activity activity = this;
    private AutocompleteAdapter adapterStart;
    private LatLng centerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapHelper = new MapHelper(MY_PERMISSIONS_REQUEST_LOCATION, this);

        ArrayList<Integer> arr = new ArrayList<>(Arrays.asList(1,2,3,4,5)) ;
        for(int i=0; i<arr.size(); i++) {
            arr.remove(0);
            arr.remove(0);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mapHelper.checkLocationPermission();
        }
        setContentView(R.layout.details);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng lodz = new LatLng(51.7592, 19.4560);

        mapHelper.mMap = googleMap;
        mapHelper.mMap.moveCamera(CameraUpdateFactory.newLatLng(lodz));
        mapHelper.mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        mapHelper.mMap.getUiSettings().setRotateGesturesEnabled(true);
        mapHelper.mMap.getUiSettings().setZoomControlsEnabled(true);
        mapHelper.mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Provide user location once the application starts
        centerLocation = lodz;

        //
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mapHelper.mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mapHelper.mMap.setMyLocationEnabled(true);
        }


        mapHelper.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                if (mapHelper.markersCount == 0) {
                    mapHelper.placeMarker(point, 1, mapHelper.getAddressFromPosition(point));
                }

                else if (mapHelper.markersCount == 1) {
                    mapHelper.placeMarker(point, 2, mapHelper.getAddressFromPosition(point));
                }

                else {
                    mapHelper.placeMarker(point, 3, mapHelper.getAddressFromPosition(point));
                }
            }
        });


        mapHelper.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.showInfoWindow();
                return true;
            }
        });

        mapHelper.mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });

        mapHelper.mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setSnippet(mapHelper.getAddressFromPosition(marker.getPosition()));
                //marker.showInfoWindow();
                if (mapHelper.markersCount > 1)
                    mapHelper.calculateRoute(mapHelper.startMarker.getPosition(), mapHelper.destMarker.getPosition());
            }
        });




    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mapHelper.mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }


}
