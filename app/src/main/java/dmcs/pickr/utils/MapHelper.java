package dmcs.pickr.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmcs.pickr.activities.Details;
import dmcs.pickr.activities.Search;

/**
 * Created by Ayman on 11/12/2016.
 */
public class MapHelper {


    public int requestCode;
    public Activity activity;

    public GoogleMap mMap;
    public Polyline route;
    public Polygon range;
    public int markersCount = 0;
    public Marker startMarker;
    public Marker destMarker;
    public ArrayList<Marker> waypointsMarkers = new ArrayList<>();
    public boolean draggable = false;
    public String titleStart = "Start";
    public String titleDestination = "Destination";


    public MapHelper () {}


    public MapHelper(int requestCode, Activity activity) {
        this.requestCode = requestCode;
        this.activity = activity;
    }


    public void placeMarker(LatLng position, int label, String snippet ) {

//        String snippet = "";
//
//        if(name!=null && !name.equals(""))
//            snippet = name;
//
//        if(address!=null && !address.equals(""))
//            snippet = address;

        if(activity instanceof Details) {
            titleStart = "Pick up";
            titleDestination = "Drop off";
        }

        if (label == 1) {

            if(activity instanceof Search) {
                ((Search) activity).autocompleteViewDestination.setEnabled(true);
                draggable = true;
            }

            if (markersCount == 0) {
                startMarker = mMap.addMarker(new MarkerOptions().position(position).title(titleStart).draggable(draggable).snippet(snippet));
                markersCount++;
            } else {
                startMarker.setPosition(position);
                startMarker.setSnippet(snippet);
                if (destMarker != null)
                    calculateRoute(startMarker.getPosition(), destMarker.getPosition());
            }
        } else if (label == 2) {
            if (markersCount == 1) {
                destMarker = mMap.addMarker(new MarkerOptions().position(position).title(titleDestination).draggable(draggable).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).snippet(snippet));
                calculateRoute(startMarker.getPosition(), destMarker.getPosition());
                markersCount++;
            } else {
                destMarker.setPosition(position);
                destMarker.setSnippet(snippet);
                calculateRoute(startMarker.getPosition(), destMarker.getPosition());
            }


        } else if (label == 3) {
            Marker waypoint = mMap.addMarker(new MarkerOptions().position(position).title("Waypoint").draggable(draggable).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).snippet(snippet));
            //waypoint.showInfoWindow();
            waypointsMarkers.add(waypoint);
            markersCount++;
            calculateRoute(startMarker.getPosition(), destMarker.getPosition());
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }


    public void getPositionFromID(String placeID, int label) {
        if (placeID != null || !placeID.equals("")) {
            // Instantiating DownloadTask to get places from Google Geocoding service
            // in a non-ui thread
            GeocodeDownloadTask downloadTask = new GeocodeDownloadTask(this, label);

            // Start downloading the geocoding places
            downloadTask.execute(getGeocodeUrl(placeID,null));
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);
            }
            return false;
        } else {
            return true;
        }
    }


    public void calculateRoute(LatLng fromPosition, LatLng toPosition) {
        if (route != null) {
            route.remove();
        }

        if (range != null) {
            range.remove();
        }


        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(fromPosition, toPosition);
        DirectionsDownloadTask directionsDownloadTask = new DirectionsDownloadTask(this);

        // Start downloading json data from Google Directions API
        directionsDownloadTask.execute(url);
    }


    public String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for (int i = 0; i < waypointsMarkers.size(); i++) {
            LatLng point = (getWaypointsPositions(waypointsMarkers)).get(i);
            if (i == 0)
                waypoints = "&waypoints=optimize:true|";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }


    public String getGeocodeUrl(String placeID, LatLng coordinates) {

        String url = "https://maps.googleapis.com/maps/api/geocode/json?";
        String latlng = "";
        String place_id = "";
        String sensor;
        String key;

        try {
            // encoding special characters like space in the user input place
            if(placeID!=null)
                placeID = URLEncoder.encode(placeID, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //String address = "address=" + location;
        if(coordinates!=null)
            latlng = "latlng=" + coordinates.latitude + "," + coordinates.longitude;

        if(placeID!=null)
            place_id = "place_id=" + placeID;

        sensor = "sensor=false";
        key = "key=AIzaSyDGTmb6jrO_6kck9EWGNsblaa-NNqMr87M";

        // url , from where the geocoding data is fetched
        url = url + place_id + latlng + "&" + sensor + "&" + key;

        return url;

        //Test: https://maps.googleapis.com/maps/api/geocode/json?place_id=ChIJ82wLoMXKG0cRFEWkkH-av4E&key=AIzaSyDGTmb6jrO_6kck9EWGNsblaa-NNqMr87M
    }


    public ArrayList<LatLng> getWaypointsPositions(ArrayList<Marker> waypointsMarkers) {
        ArrayList<LatLng> positions = new ArrayList<>();

        for (Marker m : waypointsMarkers) {
            positions.add(m.getPosition());
        }

        return positions;
    }


    public String getAddressFromPosition(LatLng point) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity.getBaseContext(), Locale.getDefault());
        String address = "";

        try {
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            //String knownName = addresses.get(0).getFeatureName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

}
