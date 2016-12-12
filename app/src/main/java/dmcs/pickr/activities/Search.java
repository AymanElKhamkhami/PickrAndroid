package dmcs.pickr.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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

import java.util.ArrayList;
import java.util.Arrays;

import dmcs.pickr.R;
import dmcs.pickr.utils.AutocompleteAdapter;
import dmcs.pickr.utils.MapHelper;

public class Search extends FragmentActivity implements OnMapReadyCallback {



    private MapHelper mapHelper;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 88;

    private Activity activity = this;
    private AutocompleteAdapter adapterStart;
    private AutocompleteAdapter adapterDestination;
    public AutoCompleteTextView autocompleteViewStart;
    public AutoCompleteTextView autocompleteViewDestination;
    private ImageView clearBtn;
    private ImageView clearBtn2;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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


        // Setting the Search Box for start point
        autocompleteViewStart = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        adapterStart = new AutocompleteAdapter(this, R.layout.autocomplete_list_item, centerLocation);
        autocompleteViewStart.setAdapter(adapterStart);

        autocompleteViewStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String placeID = adapterStart.placeIDs.get(position);
                mapHelper.getPositionFromID(placeID, 1);
                autocompleteViewStart.setText("");

                View v = activity.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        autocompleteViewStart.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mapHelper.getPositionFromID(v.getText().toString(), 1);
                    autocompleteViewStart.setText("");

                    View view = activity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });

        autocompleteViewStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autocompleteViewStart.getText().toString().equals(""))
                    clearBtn.setVisibility(View.INVISIBLE);
                else
                    clearBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        clearBtn = (ImageView) findViewById(R.id.calc_clear_txt_Prise);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteViewStart.setText("");
                clearBtn.setVisibility(View.INVISIBLE);
            }
        });


        // Setting the Search Box for destination
        autocompleteViewDestination = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestination);
        adapterDestination = new AutocompleteAdapter(this, R.layout.autocomplete_list_item, centerLocation);
        autocompleteViewDestination.setAdapter(adapterDestination);

        autocompleteViewDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String placeID = adapterDestination.placeIDs.get(position);
                mapHelper.getPositionFromID(placeID, 2);
                autocompleteViewDestination.setText("");

                View v = activity.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        autocompleteViewDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mapHelper.getPositionFromID(v.getText().toString(), 2);
                    autocompleteViewDestination.setText("");

                    View view = activity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        autocompleteViewDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autocompleteViewDestination.getText().toString().equals(""))
                    clearBtn2.setVisibility(View.INVISIBLE);
                else
                    clearBtn2.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        clearBtn2 = (ImageView) findViewById(R.id.calc_clear_txt_Prise2);

        clearBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteViewDestination.setText("");
                clearBtn.setVisibility(View.INVISIBLE);
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
