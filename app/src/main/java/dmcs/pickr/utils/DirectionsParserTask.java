package dmcs.pickr.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Ayman on 29/05/2016.
 */
public class DirectionsParserTask extends AsyncTask<String, Integer, DirectionsParserTask.Route> {

    public MapHelper mapHelper;
    private ProgressDialog progressDialog;
    public double distance;
    public int duration;
    public ArrayList<Polygon> polygons = new ArrayList<>();


    public DirectionsParserTask(MapHelper mapHelper) {
        super();
        this.mapHelper = mapHelper;
    }

    public void onPreExecute() {
        progressDialog = new ProgressDialog(mapHelper.activity);
        progressDialog.setMessage("Setting route");
        progressDialog.show();
    }

    @Override
    protected Route doInBackground(String... jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            // Starts parsing data
            routes = parser.parse(jObject);
            distance = new BigDecimal(parser.distance / 1000).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            duration = (parser.duration / 60);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Route result = new Route();
        result.route = drawRoute(routes);
        result.buffer = bufferRoute(result.route, 0.1); // Radius in Km

        return result;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(Route result) {

        progressDialog.dismiss();

        PolylineOptions route = result.route;
        route.width(15);
        route.color(0x7F0083FF);
        mapHelper.route = mapHelper.mMap.addPolyline(route);

        Toast.makeText(mapHelper.activity.getBaseContext(), distance + " km / " + duration + " mins", Toast.LENGTH_LONG).show();

        PolygonOptions buffer = result.buffer;
        buffer.strokeWidth(0);
        buffer.fillColor(0x7F729E47);
        mapHelper.range = mapHelper.mMap.addPolygon(buffer);

        mapHelper.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getRouteCenter(buffer),100));

    }


    public PolylineOptions drawRoute(List<List<HashMap<String, String>>> steps) {

        ArrayList points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < steps.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = steps.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);

        }

        return lineOptions;
    }


    public PolygonOptions bufferRoute(PolylineOptions route, double radius) {

        for (int i = 0; i < route.getPoints().size() - 1; i++) {
            // Draw a circle around each point of the route
            PolygonOptions circle1 = drawCircle(route.getPoints().get(i), radius);
            PolygonOptions circle2 = drawCircle(route.getPoints().get(i + 1), radius);

            // Draw a convex hull between every two successive circles
            PolygonOptions convexHull = drawConvexHull(circle1, circle2);

            List<LatLng> latLngs = convexHull.getPoints();
            List<Coordinate> coords = new ArrayList<>();

            for (int j=0; j<latLngs.size(); j++) {
                coords.add(new Coordinate(latLngs.get(j).latitude, latLngs.get(j).longitude));
                if(j==latLngs.size()-1)
                    coords.add(new Coordinate(latLngs.get(0).latitude, latLngs.get(0).longitude));
            }

            Coordinate[] coordinates = coords.toArray(new Coordinate[coords.size()]);

            GeometryFactory fact = new GeometryFactory();
            LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
            Polygon poly = new Polygon(linear, null, fact);

            polygons.add(poly);
        }

        return unionPolygons(polygons);
    }


    public PolygonOptions drawCircle(LatLng center, double radius) {
        PolygonOptions circle = new PolygonOptions();

        // radius
        double d = radius / 3963.189;

        // radians
        double lat1 = Math.toRadians(center.latitude);
        double lng1 = Math.toRadians(center.longitude);

        int step = 10;

        for (int i = 0; i < 360; i += step) {
            double tc = Math.toRadians(i);
            double y = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(tc));
            double x = (lng1 + Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1), Math.cos(d) - Math.sin(lat1) * Math.sin(y)));
            // MOD function
            LatLng point = new LatLng(Math.toDegrees(y), Math.toDegrees(x));
            circle.add(point);
        }

        return circle;
    }


    public PolygonOptions drawConvexHull(PolygonOptions p1, PolygonOptions p2) {

        ArrayList<LatLng> points = new ArrayList<>();
        points.addAll(p1.getPoints());
        points.addAll(p2.getPoints());

        PolygonOptions convHull = new PolygonOptions();

        int minPoint = -1, maxPoint = -1;
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).longitude < minX) {
                minX = points.get(i).longitude;
                minPoint = i;
            }
            if (points.get(i).longitude > maxX) {
                maxX = points.get(i).longitude;
                maxPoint = i;
            }
        }

        LatLng A = points.get(minPoint);
        LatLng B = points.get(maxPoint);
        convHull.add(A);
        convHull.add(B);
        points.remove(A);
        points.remove(B);

        ArrayList<LatLng> leftSet = new ArrayList();
        ArrayList<LatLng> rightSet = new ArrayList();

        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            if (pointLocation(A, B, p) == -1)
                leftSet.add(p);
            else if (pointLocation(A, B, p) == 1)
                rightSet.add(p);
        }

        hullSet(A, B, rightSet, (ArrayList<LatLng>) convHull.getPoints());
        hullSet(B, A, leftSet, (ArrayList<LatLng>) convHull.getPoints());

        points.clear();

        return convHull;
    }


    static PolygonOptions unionPolygons(Collection<Polygon> geometries ){
        Geometry all = null;
        PolygonOptions allPolygon = new PolygonOptions();

        for(Iterator<Polygon> i = geometries.iterator(); i.hasNext(); ){
            Polygon geometry = i.next();
            if( geometry == null ) continue;
            if( all == null ){
                all = geometry;
            }
            else {
                all = all.union( geometry );
            }
        }

        List<Coordinate> bufferCoordinates = Arrays.asList(all.getCoordinates());

        for (Coordinate c : bufferCoordinates) {
            allPolygon.add(new LatLng(c.x, c.y));
        }

        return allPolygon;
    }


    public LatLngBounds getRouteCenter(PolygonOptions buffer) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : buffer.getPoints()) {
            builder.include(p);
        }

        return  builder.build();
    }


    public double distance(LatLng A, LatLng B, LatLng C) {
        double ABx = B.longitude - A.longitude;
        double ABy = B.latitude - A.latitude;
        double num = ABx * (A.latitude - C.latitude) - ABy * (A.longitude - C.longitude);
        if (num < 0)
            num = -num;
        return num;
    }


    public void hullSet(LatLng A, LatLng B, ArrayList<LatLng> set, ArrayList<LatLng> hull) {
        int insertPosition = hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1) {
            LatLng p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        double dist = Integer.MIN_VALUE;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++) {
            LatLng p = set.get(i);
            double distance = distance(A, B, p);
            if (distance > dist) {
                dist = distance;
                furthestPoint = i;
            }
        }
        LatLng P = set.get(furthestPoint);
        set.remove(furthestPoint);
        hull.add(insertPosition, P);

        // Determine who's to the left of AP
        ArrayList<LatLng> leftSetAP = new ArrayList<>();
        for (int i = 0; i < set.size(); i++) {
            LatLng M = set.get(i);
            if (pointLocation(A, P, M) == 1) {
                leftSetAP.add(M);
            }
        }

        // Determine who's to the left of PB
        ArrayList<LatLng> leftSetPB = new ArrayList<>();
        for (int i = 0; i < set.size(); i++) {
            LatLng M = set.get(i);
            if (pointLocation(P, B, M) == 1) {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);

    }


    public int pointLocation(LatLng A, LatLng B, LatLng P) {
        double cp1 = (B.longitude - A.longitude) * (P.latitude - A.latitude) - (B.latitude - A.latitude) * (P.longitude - A.longitude);
        if (cp1 > 0)
            return 1;
        else if (cp1 == 0)
            return 0;
        else
            return -1;
    }


    public boolean pointInPolygon(LatLng point, List<LatLng> area) {
        //Ray-cast algorithm is here onward
        int k, j = area.size() - 1;
        boolean oddNodes = false; //to check whether number of intersections is odd

        for (k = 0; k < area.size(); k++) {
            //fetch adjucent points of the polygon
            LatLng polyK = area.get(k);
            LatLng polyJ = area.get(j);

            //check the intersections
            if (((polyK.longitude > point.longitude) != (polyJ.longitude > point.longitude)) &&
                    (point.latitude < (polyJ.latitude - polyK.latitude) * (point.longitude - polyK.longitude) / (polyJ.longitude - polyK.longitude) + polyK.latitude))
                oddNodes = !oddNodes; //switch between odd and even
            j = k;
        }

        return oddNodes;
    }


    static public boolean isPointInsidePolygon(ArrayList<LatLng> polygonPoints, LatLng q) {
        // This code was copied, with minor changes, from
        //    http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
        // where it (the code, not the algorithm) is attributed to Randolph Franklin.
        // The idea behind the algorithm is to imagine a ray projecting
        // from the point toward the right, and then count how many times
        // that ray intersects an edge of the polygon.
        // If the number is odd, the point is inside the polygon.

        boolean returnValue = false;
        int i, j;

        for (i = 0, j = polygonPoints.size() - 1; i < polygonPoints.size(); j = i++) {

            LatLng pi = polygonPoints.get(i);
            double xi = pi.latitude;
            double yi = pi.longitude;
            LatLng pj = polygonPoints.get(j);
            double xj = pj.latitude;
            double yj = pj.longitude;

            if (
                    (((yi <= q.longitude) && (q.longitude < yj)) || ((yj <= q.longitude) && (q.longitude < yi)))
                            && (q.latitude < (xj - xi) * (q.longitude - yi) / (yj - yi) + xi)

                    ) {
                returnValue = !returnValue;
            }
        }
        return returnValue;
    }


    static public class Route {
        PolylineOptions route;
        PolygonOptions buffer;
    }

}

