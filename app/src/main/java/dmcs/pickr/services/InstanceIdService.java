package dmcs.pickr.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import dmcs.pickr.models.Preferences;
import dmcs.pickr.models.RideDetails;
import dmcs.pickr.models.UserDetails;
import dmcs.pickr.utils.RESTJSONParser;

/**
 * Created by Ayman on 04/12/2016.
 */
public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        PickrWebService api = new PickrWebService();
        boolean updated;


        UserDetails user = getSessionUser();

        if(user!=null) {
            try {

                JSONObject jsonObj = api.UpdateDeviceToken(getSessionUser().getEmail(), token);

                //Parse the JSON Object to boolean
                RESTJSONParser parser = new RESTJSONParser();
                updated = parser.parseDatabaseCommit(jsonObj);

                Log.d(TAG, "Token update status: " + updated);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncRideDetails", e.getMessage());

            }
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

}
