package dmcs.pickr.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import dmcs.pickr.R;
import dmcs.pickr.models.Preferences;
import dmcs.pickr.models.RideDetails;
import dmcs.pickr.models.UserDetails;
import dmcs.pickr.services.PickrWebService;
import dmcs.pickr.utils.AutocompleteAdapter;
import dmcs.pickr.utils.MapHelper;
import dmcs.pickr.utils.RESTJSONParser;

public class Details extends FragmentActivity implements OnMapReadyCallback {



    private MapHelper mapHelper;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 88;

    private Activity activity = this;
    private AutocompleteAdapter adapterStart;
    private LatLng centerLocation, pickupPoint, dropoffPoint;


    private TextView txt_name, txt_email, txt_phone, txt_car, txt_pickup;
    private ImageView img_profile;


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


        txt_name = (TextView) findViewById(R.id.text_name);
        txt_email = (TextView) findViewById(R.id.text_email);
        txt_phone = (TextView) findViewById(R.id.text_mobile);
        txt_car = (TextView) findViewById(R.id.text_carmodel);
        txt_pickup = (TextView) findViewById(R.id.text_pickup);
        img_profile = (ImageView) findViewById(R.id.image_profile);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            int id = bundle.getInt("ID");
            new RideDetailsTask(id).execute();
        }


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



        mapHelper.placeMarker(pickupPoint, 1, mapHelper.getAddressFromPosition(pickupPoint));

        mapHelper.placeMarker(dropoffPoint, 2, mapHelper.getAddressFromPosition(dropoffPoint));



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


    private class RideDetailsTask extends AsyncTask<String, String, RideDetails> {
        ProgressDialog pdLoading = new ProgressDialog(Details.this);
        int requestId;


        public RideDetailsTask(int requestId) {
            super();
            this.requestId = requestId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(true);
            pdLoading.show();

        }

        @Override
        protected RideDetails doInBackground(String... params) {
            PickrWebService api = new PickrWebService();
            RideDetails rideDetails = new RideDetails();

            try {

                JSONObject jsonObj = api.GetRideDetails(requestId, getSessionUser().getMode().equals("passenger") ? "driver" : "passenger");

                //Parse the JSON Object to boolean
                RESTJSONParser parser = new RESTJSONParser();
                rideDetails = parser.parseRideDetails(jsonObj);


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncRideDetails", e.getMessage());

            }

            return rideDetails;


        }

        @Override
        protected void onPostExecute(RideDetails result) {

            txt_name.setText(result.getFirstname() + " " + result.getSurname());
            txt_email.setText(result.getEmail());
            txt_phone.setText(result.getMobile().isEmpty() ? "Mobile: NA" : result.getMobile());
            txt_car.setText(result.getCarModel().isEmpty() ? "" : result.getCarModel());
            String[] splittedDate = splitDate(result.getPickupTime());
            String pickupText = splittedDate[1].equals("01/01/1900") ?  "Pending request" : "Pick up on " + splittedDate[0] + " " + splittedDate[1] + " at " + splittedDate[2];
            txt_pickup.setText(pickupText);

            pickupPoint = new LatLng(result.getPickupLatitude(), result.getPickupLongitude());
            dropoffPoint = new LatLng(result.getDropoffLatitude(), result.getDropoffLongitude());

            pdLoading.dismiss();

            new DownloadImageTask(img_profile).execute(result.getPicture());
        }

    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ProgressDialog pdLoading = new ProgressDialog(Details.this);

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            pdLoading.dismiss();
            bmImage.setImageBitmap(result);
            bmImage.setBackgroundColor(0x00000000);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(Details.this);

        }
    }


    private UserDetails getSessionUser() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            JSONObject json = new JSONObject(preferences.getString("userDetails", ""));
            Log.d("JSON", json.toString());
            JSONObject jsonPrefs = json.getJSONObject("preferences");
            Preferences userPrefs = new Preferences(jsonPrefs.getBoolean("smoking"), jsonPrefs.getBoolean("music"), jsonPrefs.getBoolean("pets"), jsonPrefs.getInt("talking"));
            UserDetails user = new UserDetails(json.getString("email"), json.getString("username"), "", json.getInt("reputation"), userPrefs, json.getString("carModel"), json.getString("firstName"), json.getString("surname"), json.getString("gender"), json.getString("mobile"), json.getString("picture"), json.getString("address"), json.getString("mode"));
            return user;

        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }

    }


    private String[] splitDate(String rawDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = null;
        String nameOfDay="", date ="", time="";

        try {
            d = format.parse(rawDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        nameOfDay = dayFormat.format(d);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        date = dateFormat.format(d);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        time = timeFormat.format(d);

        return new String[] {nameOfDay, date, time};
    }



}
