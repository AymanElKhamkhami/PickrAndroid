package dmcs.pickr.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dmcs.pickr.R;
import dmcs.pickr.utils.JSONParser;
import dmcs.pickr.services.PickrWebService;
import dmcs.pickr.models.UserDetails;

public class Login extends AppCompatActivity implements View.OnClickListener {


    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTETRNAL = 99;

    EditText et_pass;
    AutoCompleteTextView et_email;
    Button btn_login, btn_cancel;
    LoginButton btn_facebook;
    TextView txt_register;
    String username, password;
    String[] history;

    Context context;
    CallbackManager callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.login);
        context=this;

        checkPermissions();

        if(checkActiveSession()) {
            Intent intent = new Intent(Login.this, Home.class);
            startActivityForResult(intent, 0);
        }

        //subscribeToPushService("testTopic");


        callback = CallbackManager.Factory.create();

        btn_facebook = (LoginButton) findViewById(R.id.button_fb_login);
        btn_login = (Button) findViewById(R.id.button_login);
        btn_cancel = (Button) findViewById(R.id.button_cancel);
        et_email = (AutoCompleteTextView) findViewById(R.id.editText);
        et_pass = (EditText) findViewById(R.id.editText2);
        txt_register = (TextView) findViewById(R.id.link_register);

        txt_register.setPaintFlags(txt_register.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );

        history = getInputHistory();

        btn_facebook.setReadPermissions(Arrays.asList("email")); //, "name", "first_name", "last_name", "gender", "picture", "age_range"
        btn_facebook.registerCallback(callback, new FacebookCallback<LoginResult>() {

            String facebookEmail;

            @Override
            public void onSuccess(LoginResult loginResult) {

                // Now check the user existence in Pickr database
                if(AccessToken.getCurrentAccessToken() != null) {

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    Log.v("LoginActivity", response.toString());

                                    // Application code
                                    try {

                                        facebookEmail = object.getString("email");
                                        //Now check in Pickr database
                                        new FacebookLoginTask().execute(facebookEmail);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(context, "Problem retrieving Facebook details. Please try again.", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender, birthday");
                    request.setParameters(parameters);
                    request.executeAsync();

                }

                //Intent i = new Intent(Login.this, Home.class);
            }

            @Override
            public void onCancel() {

                Toast.makeText(context, "Login cancelled ",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(context, "Error while logging in. Please try again.",Toast.LENGTH_LONG).show();
            }
        });


        setAutoCompleteSource();

        btn_login.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        txt_register.setOnClickListener(this);

    }


    private void subscribeToPushService(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        Log.d("AndroidBash", "Subscribed");
        //Toast.makeText(Login.this, "Subscribed to" + topic, Toast.LENGTH_SHORT).show();

        String token = FirebaseInstanceId.getInstance().getToken();

        // Log and toast
        Log.d("AndroidBash", token);
        //Toast.makeText(Login.this, token, Toast.LENGTH_SHORT).show();
    }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_login:
                username = et_email.getText().toString();
                password = et_pass.getText().toString();
                if(username.isEmpty() || password.isEmpty())
                    Toast.makeText(context, "Please provide an email and password ",Toast.LENGTH_LONG).show();
                else
                    new LoginTask().execute(username, password);
                break;

            case R.id.button_cancel:
                et_email.setText("");
                et_pass.setText("");
                break;

            case R.id.link_register:
                Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
                break;

            default:
                break;
        }

    }


    public void addUserToSession(UserDetails user) {

        JSONObject json = new JSONObject();
        JSONObject jsonPrefs = new JSONObject();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        try {
            jsonPrefs.put("smoking", user.getPreferences().isSmoking());
            jsonPrefs.put("music", user.getPreferences().isMusic());
            jsonPrefs.put("pets", user.getPreferences().isPets());
            jsonPrefs.put("talking", user.getPreferences().getTalking());

            json.put("email", user.getEmail());
            json.put("username", user.getUsername());
            json.put("reputation", user.getReputation());
            json.put("preferences", jsonPrefs);
            json.put("carModel", user.getCarModel());
            json.put("firstName", user.getFirstName());
            json.put("surname", user.getSurname());
            json.put("gender", user.getGender());
            json.put("mobile", user.getMobile());
            json.put("picture", user.getPicture());
            json.put("address", user.getAddress());
            json.put("mode", user.getMode());

            editor.putString("userDetails", json.toString());
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public boolean checkActiveSession() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.contains("userDetails");
        //return preferences.getString(key, null);
    }


    private void setAutoCompleteSource() {
        //AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.editText);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, history);//.toArray(new String[history.size()]));
        et_email.setThreshold(1);
        et_email.setAdapter(adapter);
    }


    private void addInputToHistory(String input) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> inputsHistory = preferences.getStringSet("emailHistory", new HashSet<String>());

        inputsHistory.add(input);

        editor.putStringSet("emailHistory", inputsHistory);
        editor.commit();
    }


    private String[] getInputHistory() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> inputsHistory = preferences.getStringSet("emailHistory", new HashSet<String>());
        String [] history = inputsHistory.toArray(new String[inputsHistory.size()]);

        return history;
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Login.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        callback.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            this.finish();
        }
    }


    ///LoginTask Inner class
    private class LoginTask extends AsyncTask<String, String, Boolean> {
        ProgressDialog pdLoading = new ProgressDialog(Login.this);
        UserDetails user = new UserDetails();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLogging in...");
            pdLoading.setCancelable(true);
            pdLoading.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            PickrWebService api = new PickrWebService();
            boolean userAuth = false;

            try {

                /// Call the User Authentication Method in API
                JSONObject jsonObj = api.UserAuthentication(params[0], params[1]);

                //Parse the JSON Object to boolean
                JSONParser parser = new JSONParser();
                userAuth = parser.parseUserAuthentication(jsonObj);

                if(userAuth) {
                    jsonObj = api.GetUser(params[0]);
                    user = parser.parseUser(jsonObj);
                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLogin", e.getMessage());

            }

            return userAuth;


        }

        @Override
        protected void onPostExecute(Boolean result) {

            //Check user validity
            if (result) {
                addUserToSession(user);
                Intent i = new Intent(Login.this, Home.class);
                i.putExtra("user",user);
                pdLoading.dismiss();
                //Login.this.finish();
                //startActivity(i);
                addInputToHistory(et_email.getText().toString());
                startActivityForResult(i, 0);
            }
            else
            {
                Toast.makeText(context, "Invalid email or password ",Toast.LENGTH_LONG).show();
                pdLoading.dismiss();
            }


        }

    }


    ///FacebookLoginTask Inner class
    private class FacebookLoginTask extends AsyncTask<String, String, Boolean> {
        ProgressDialog pdLoading = new ProgressDialog(Login.this);
        UserDetails user = new UserDetails();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLogging in...");
            pdLoading.setCancelable(true);
            pdLoading.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            PickrWebService api = new PickrWebService();
            boolean userExists = false;

            try {

                /// Call the User Authentication Method in API
                JSONObject jsonObj = api.CheckUserExistence(params[0]);

                //Parse the JSON Object to boolean
                JSONParser parser = new JSONParser();
                userExists = parser.parseUserExistence(jsonObj);

                if(userExists) {
                    jsonObj = api.GetUser(params[0]);
                    user = parser.parseUser(jsonObj);
                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLogin", e.getMessage());

            }

            return userExists;


        }

        @Override
        protected void onPostExecute(Boolean result) {

            //Check user validity
            if (result) {
                addUserToSession(user);
                Intent i = new Intent(Login.this, Home.class);
                i.putExtra("user",user);
                pdLoading.dismiss();
                startActivityForResult(i, 0);
            }
            else
            {
                Toast.makeText(context, "You are not registered. Please create a new account with the same email you are using for Facebook login and try again.",Toast.LENGTH_LONG).show();
                pdLoading.dismiss();
            }


        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTETRNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void checkPermissions() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTETRNAL);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

}
