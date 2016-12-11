package dmcs.pickr.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ayman on 30/05/2016.
 */
public class GeocodeJSONParser {

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

//    {"address_components":
//        [{"long_name":"Finestra","short_name":"Finestra","types":["establishment","point_of_interest"]},
//        {"long_name":"33","short_name":"33","types":["street_number"]},
//        {"long_name":"Radwańska","short_name":"Radwańska","types":["route"]},
//        {"long_name":"Polesie","short_name":"Polesie","types":["political","sublocality","sublocality_level_1"]},
//        {"long_name":"Łódź","short_name":"Łódź","types":["locality","political"]},
//        {"long_name":"Łódź","short_name":"Łódź","types":["administrative_area_level_2","political"]},
//        {"long_name":"województwo łódzkie","short_name":"województwo łódzkie","types":["administrative_area_level_1","political"]},
//        {"long_name":"Poland","short_name":"PL","types":["country","political"]},
//        {"long_name":"90-540","short_name":"90-540","types":["postal_code"]}],
//        "formatted_address":"Finestra, Radwańska 33, 90-540 Łódź, Poland",
//            "geometry":{"location":{"lat":51.7518922,"lng":19.4514083},
//        "location_type":"APPROXIMATE",
//                "viewport":{"northeast":{"lat":51.7532411802915,"lng":19.4527572802915},"southwest":{"lat":51.7505432197085,"lng":19.4500593197085}}},"place_id":"ChIJBfGoAdk0GkcRR9wjBMW7S3s","types":["establishment","food","point_of_interest","restaurant"]}

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();
        String formatted_address = "";
        String short_name = "";
        String lat="";
        String lng="";

        try {
            // Extracting formatted address, if available
            if(!jPlace.isNull("formatted_address")){
                formatted_address = jPlace.getString("formatted_address");
            }


                short_name = jPlace.getJSONArray("address_components").getJSONObject(0).getString("short_name");


            lat = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            lng = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.put("formatted_address", formatted_address);
            place.put("short_name", short_name);
            place.put("lat", lat);
            place.put("lng", lng);

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }


}


