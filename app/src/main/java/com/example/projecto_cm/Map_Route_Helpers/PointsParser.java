package com.example.projecto_cm.Map_Route_Helpers;

import android.graphics.Color;
import android.os.AsyncTask;

import com.example.projecto_cm.Fragments.Frag_Show_Trip_Route;
import com.example.projecto_cm.Interfaces.Interface_On_Trip_Route_Ready;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    Interface_On_Trip_Route_Ready taskCallback;
    String directionMode;

    public PointsParser(Frag_Show_Trip_Route mContext, String directionMode) {
        this.taskCallback = mContext;
        this.directionMode = directionMode;
    }

    /**
     * Parsing the data in non-ui thread
     * this than calls DataParser class
     * and after that it calls onPostExecute from this class
     * @param jsonData
     * @return
     */
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);

            DataParser parser = new DataParser();

            // Starts parsing data
            routes = parser.parse(jObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return routes;
    }

    /**
     * Executes in UI thread, after the parsing process
     * this is executed after DataParser class executes its methods
     * @param result
     */
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {

        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        if(result != null){

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {

                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
            }
        }

        // Drawing polyline in the Google Map for the i-th route if points inserted are able to be connected
        if (lineOptions != null) {
            taskCallback.show_route(lineOptions);
        }
    }
}
