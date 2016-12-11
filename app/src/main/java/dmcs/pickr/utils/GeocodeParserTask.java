package dmcs.pickr.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


/**
 * Created by Ayman on 31/05/2016.
 */
public class GeocodeParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {

    JSONObject jObject;
    private Activity activity;
    private int label;
    private MapHelper mapHelper;


    public GeocodeParserTask(MapHelper mapHelper, int label)
    {
        super();
        this.mapHelper = mapHelper;
        this.label = label;
    }

    // Invoked by execute() method of this object
    @Override
    protected List<HashMap<String,String>> doInBackground(String... jsonData) {

        List<HashMap<String, String>> places = null;
        GeocodeJSONParser parser = new GeocodeJSONParser();

        try{
            jObject = new JSONObject(jsonData[0]);
            /** Getting the parsed data as a an ArrayList */
            places = parser.parse(jObject);

        }catch(Exception e){
            Log.d("Exception",e.toString());
        }
        return places;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(List<HashMap<String,String>> list){

        for(int i=0;i<list.size();i++){

            // Getting a place from the places list
            HashMap<String, String> hmPlace = list.get(i);

            double lat = Double.parseDouble(hmPlace.get("lat"));
            double lng = Double.parseDouble(hmPlace.get("lng"));
            String name = hmPlace.get("short_name");
            String address = hmPlace.get("formatted_address");

            LatLng latLng = new LatLng(lat, lng);
            mapHelper.placeMarker(latLng, label, name);

        }
    }
}


