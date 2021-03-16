package com.example.walkingtours;


import android.location.Geocoder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class FenceDataDownloader implements Runnable {

    private static final String TAG = "FenceDataDownloader";
    private final Geocoder geocoder;
    private final FenceMgr fenceMgr;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";
    private MapsActivity mapsActivity;

    FenceDataDownloader(MapsActivity mapsActivity, FenceMgr fenceMgr) {
        this.fenceMgr = fenceMgr;
        geocoder = new Geocoder(mapsActivity);
        this.mapsActivity = mapsActivity;
    }




    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: Response code: " + connection.getResponseCode());
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            List<String> paths = processData(buffer.toString());

            mapsActivity.runOnUiThread(() -> mapsActivity.tourPath(paths));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private List<String> processData(String result) {
        List<String> paths = new ArrayList<>();

        if (result == null)
            return null;

        ArrayList<FenceData> fences = new ArrayList<>();
        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fObj = jArr.getJSONObject(i);
                String id = fObj.getString("id");
                String address = fObj.getString("address");
                double latitude = fObj.getDouble("latitude");
                double longitude = fObj.getDouble("longitude");
                float rad = (float) fObj.getDouble("radius");
                String description = fObj.getString("description");
                //int type = fObj.getInt("type");
                String color = fObj.getString("fenceColor");
                String image = fObj.getString("image");

                //LatLng ll = getLatLong(address);

                FenceData fd = new FenceData(id, address, latitude, longitude, rad, description, color, image);
                fences.add(fd);

            }
            JSONArray path = jObj.getJSONArray("path");

            for (int i = 0; i < path.length(); i++){
                paths.add(path.getString(i));
            }

            fenceMgr.addFences(fences);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }
/*
    private LatLng getLatLong(String address) {
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            Address a = addressList.get(0);
            return new LatLng(a.getLatitude(), a.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

 */
}