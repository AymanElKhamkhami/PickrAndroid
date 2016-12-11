package dmcs.pickr.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ayman on 30/05/2016.
 */
public class AutocompleteAdapter extends ArrayAdapter<String> implements Filterable {

    public ArrayList<String> resultList = new ArrayList<>();
    public ArrayList<String> placeIDs = new ArrayList<>();

    private Context mContext;
    private int mResource;
    private LatLng centerLocation;

    AutocompleteJSONParser parser = new AutocompleteJSONParser();

    public AutocompleteAdapter(Context context, int resource, LatLng location) {
        super(context, resource);
        mContext = context;
        mResource = resource;
        centerLocation = location;
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    resultList.clear();
                    placeIDs.clear();

                    HashMap<String, String> resultsMap = parser.autocomplete(constraint.toString(), centerLocation);

                    if(resultsMap.size()>0){
                        for (String key : resultsMap.keySet()) {
                            resultList.add(resultsMap.get(key));
                            placeIDs.add(key);
                        }
                    }

                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {


                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

}
