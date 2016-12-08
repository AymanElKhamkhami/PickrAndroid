package dmcs.pickr.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import dmcs.pickr.models.Preferences;
import dmcs.pickr.models.UserDetails;

/**
 * Created by Ayman on 01/12/2016.
 */
public class JSONParser {

    public JSONParser() {
        super();
    }

    public boolean parseUserAuthentication(JSONObject object) {
        boolean userAtuh = false;
        try {
            userAtuh = object.getBoolean("Value");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("JSONParser parseUserAuthentication", e.getMessage());
        }

        return userAtuh;
    }

    public boolean parseUserExistence(JSONObject object) {
        boolean userExists = false;
        try {
            userExists = object.getBoolean("Value");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("JSONParser parseUserExistence", e.getMessage());
        }

        return userExists;
    }

    public UserDetails parseUser(JSONObject object)
    {
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


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d("JSONParser parseUserDetails", e.getMessage());
        }

        return user;

    }
}
