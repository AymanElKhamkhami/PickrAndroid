package dmcs.pickr.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import dmcs.pickr.models.Preferences;
import dmcs.pickr.models.RideDetails;
import dmcs.pickr.models.UserDetails;

/**
 * Created by Ayman on 01/12/2016.
 */
public class RESTJSONParser {

    public RESTJSONParser() {
        super();
    }

    public boolean parseUserAuthentication(JSONObject object) {
        boolean userAtuh = false;
        try {
            userAtuh = object.getBoolean("Value");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("RESTJSONParser parseUserAuthentication", e.getMessage());
        }

        return userAtuh;
    }


    public boolean parseDatabaseCommit(JSONObject object) {
        boolean commit = false;
        try {
            commit = object.getBoolean("Value");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("RESTJSONParser parseDatabaseCommit", e.getMessage());
        }

        return commit;
    }


    public boolean parseUserExistence(JSONObject object) {
        boolean userExists = false;
        try {
            userExists = object.getBoolean("Value");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("RESTJSONParser parseUserExistence", e.getMessage());
        }

        return userExists;
    }


    public UserDetails parseUser(JSONObject object) {
        UserDetails user = new UserDetails();
        Preferences pref = new Preferences();

        try {
            JSONObject jsonObj = object.getJSONArray("Value").getJSONObject(0);

            user.setEmail(jsonObj.getString("Email"));
            user.setUsername(jsonObj.getString("Username"));
            user.setPassword(jsonObj.getString("Password"));

            if(jsonObj.has("Reputation"))
                user.setReputation(jsonObj.getInt("Reputation"));

            if(jsonObj.has("CarModel"))
                user.setCarModel(jsonObj.getString("CarModel"));

            user.setFirstName(jsonObj.getString("FirstName"));
            user.setSurname(jsonObj.getString("Surname"));
            user.setMobile(jsonObj.getString("Mobile"));
            user.setPicture(jsonObj.getString("Picture"));
            user.setMode(jsonObj.getString("Mode"));


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("RESTJSONParser parseUserDetails", e.getMessage());
        }

        return user;

    }


    public RideDetails parseRideDetails(JSONObject object) {

        RideDetails rideDetails = new RideDetails();

        try {
            JSONObject jsonObj = object.getJSONArray("Value").getJSONObject(0);

            rideDetails.setPicture(jsonObj.getString("Picture"));
            rideDetails.setFirstname(jsonObj.getString("FirstName"));
            rideDetails.setSurname(jsonObj.getString("Surname"));
            rideDetails.setEmail(jsonObj.getString("Email"));


            if(jsonObj.has("Mobile")) rideDetails.setMobile(jsonObj.getString("Mobile"));
            else rideDetails.setMobile("");


            if(jsonObj.has("CarModel")) rideDetails.setCarModel(jsonObj.getString("CarModel"));
            else rideDetails.setCarModel("");

            rideDetails.setPickupTime(jsonObj.getString("PickupTime"));
            rideDetails.setPickupLatitude(jsonObj.getDouble("PickupLatitude"));
            rideDetails.setPickupLongitude(jsonObj.getDouble("PickupLongitude"));
            rideDetails.setDropoffLatitude(jsonObj.getDouble("DropoffLatitude"));
            rideDetails.setDropoffLongitude(jsonObj.getDouble("DropoffLongitude"));


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("RESTJSONParser parseUserDetails", e.getMessage());
        }

        return rideDetails;
    }

}
