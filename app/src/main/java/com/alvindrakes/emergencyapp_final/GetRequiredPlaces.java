package com.alvindrakes.emergencyapp_final;

import android.graphics.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.alvindrakes.emergencyapp_final.directionhelpers.FetchURL;
import com.alvindrakes.emergencyapp_final.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class GetRequiredPlaces extends AsyncTask<Object, String, String> {

    GoogleMap mMap;
    String url;
    InputStream is;
    BufferedReader bufferedReader;
    StringBuilder stringBuilder;
    String data;
    Marker markerName;

    private static String TAG = "Getting places";

    public GetRequiredPlaces(CustomerMapActivity customerMapActivity) {
    }

    @Override
    protected String doInBackground(Object... params) {

        mMap = (GoogleMap) params[0];
        url = (String) params[1];

        try {
            URL myUrl = new URL(url);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)myUrl.openConnection();
            httpsURLConnection.connect();
            is = httpsURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            data = stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {

        Log.d(TAG, "onPostExecute: " + s);

        if (s != null ) {
                try
                {

                    mMap.clear();  // clear all markers when creating new one

                    JSONObject parentObject = new JSONObject(s);
                    JSONArray resultsArray = parentObject.getJSONArray("results");

                    for (int i = 0; i < resultsArray.length(); i++) {

                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");

                        String latitude = locationObject.getString("lat");
                        String longitude = locationObject.getString("lng");

                        JSONObject nameObject = resultsArray.getJSONObject(i);

                        String emergencyPlaceName = nameObject.getString("name");
                        String vicinity = nameObject.getString("vicinity");

                        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                        mMap.addMarker(new MarkerOptions().snippet("Address: " + vicinity).position(latLng).title(emergencyPlaceName));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10)); // zoom out to show markers on map
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
}





