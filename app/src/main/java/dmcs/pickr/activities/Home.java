package dmcs.pickr.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import dmcs.pickr.R;
import dmcs.pickr.models.Preferences;
import dmcs.pickr.models.UserDetails;

public class Home extends AppCompatActivity {

    ImageView img_profile;
    TextView txt_hello;
    Button btn_logout;
    UserDetails sessionUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        img_profile = (ImageView) findViewById(R.id.image_profile);
        txt_hello = (TextView) findViewById(R.id.text_hello);
        btn_logout = (Button) findViewById(R.id.button_logout);

        if (getSessionUser() != null) {
            sessionUser = getSessionUser();


            new DownloadImageTask(img_profile).execute(sessionUser.picture);

            subscribeToPushService(sessionUser.getEmail().replace("@", "%"));
        }


        /*Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            UserDetails user = (UserDetails) bundle.getSerializable("user");
            txt_hello.setText("Hello " + user.getFirstName() + " " + user.getSurname() + "!");
        }*/ /*else {
            Intent intent = new Intent(Home.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/




        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Intent intent = new Intent(Home.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove("userDetails");
                                editor.apply();

                                if(AccessToken.getCurrentAccessToken() != null)
                                    LoginManager.getInstance().logOut();

                                startActivity(intent);
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
        });

    }


    public UserDetails getSessionUser() {

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


    private void subscribeToPushService(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);

        Log.d("AndroidBash", "Subscribed");
        //Toast.makeText(Login.this, "Subscribed to" + topic, Toast.LENGTH_SHORT).show();

        String token = FirebaseInstanceId.getInstance().getToken();

        // Log and toast
        Log.d("AndroidBash", token);
        //Toast.makeText(Login.this, token, Toast.LENGTH_SHORT).show();
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ProgressDialog pdLoading = new ProgressDialog(Home.this);

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tDownloading data...");
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

            txt_hello.setText("Hello " + sessionUser.getFirstName() + " " + sessionUser.getSurname() + "!");
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit the app?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setResult(0);
                        finish();
                        //Home.this.finish();
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
    protected void onStop() {
        setResult(0);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setResult(0);
        super.onDestroy();
    }

}
