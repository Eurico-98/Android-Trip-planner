package com.example.projecto_cm.Map_Route_Helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.example.projecto_cm.Fragments.Frag_Show_Trip_Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchURL extends AsyncTask<String, Void, String> {

    Frag_Show_Trip_Route mContext;
    String directionMode = "driving";

    public FetchURL(Frag_Show_Trip_Route mContext) {
        this.mContext = mContext;
    }

    /**
     * For storing data from web service
     * this is the second step in getting the trip route
     * @param strings
     * @return
     */
    @Override
    protected String doInBackground(String... strings) {

        // For storing data from web service
        String data = "";
        directionMode = strings[1];

        try {
            // Fetching the data from web service
            data = downloadUrl(strings[0]);

        } catch (Exception ignored) {}

        return data;
    }

    /**
     * Invokes the thread for parsing the JSON data
     * this is the third step in getting the trip route it calls PointsParser class
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        PointsParser parserTask = new PointsParser(mContext, directionMode);
        // Invokes the thread for parsing the JSON data
        parserTask.execute(s);
    }

    /**
     * to downaload URL
     * this is the first step in getting the trip route
     * @param strUrl
     * @return
     * @throws IOException
     */
    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {

            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception ignored) {}
        finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }
}

