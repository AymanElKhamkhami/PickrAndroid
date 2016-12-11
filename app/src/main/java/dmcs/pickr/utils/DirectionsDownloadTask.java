package dmcs.pickr.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Ayman on 29/05/2016.
 */
public class DirectionsDownloadTask extends AsyncTask<String, Void, String> {

    private MapHelper mapHelper;
    private ProgressDialog progressDialog;

    public DirectionsDownloadTask(MapHelper mapHelper)
    {
        super();
        this.mapHelper = mapHelper;
    }

    public void onPreExecute()
    {
        progressDialog = new ProgressDialog(mapHelper.activity);
        progressDialog.setMessage("Calculating directions");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... url) {
        // For storing data from web service
        String data = "";

        try{
            // Fetching the data from web service
            data = downloadUrl(url[0]);
        }catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        progressDialog.dismiss();

        DirectionsParserTask directionsParserTask = new DirectionsParserTask(mapHelper);

        // Invokes the thread for parsing the JSON data
        directionsParserTask.execute(result);

    }

    /** A method to download json data from url */
    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
