package com.enroutetechnologies.enroute;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SadruddinHashmani on 2016-11-12.
 */

public class YelpConnection {

    private String mConsumerKey = "b5iuZZDpEMqGSMMXM_GhCg";
    private String mConsumerSecret = "1zdvEkbi3ToCQ5VtkNjl813eSeU";
    private String mToken = "3d24sy-UTDV3zXwcXJlSiRS635a2HzPM";
    private String mTokenSecret = "BamVqH-13iphIMF9vx5Fc6zjN7A";
    private SearchResponse searchResponse;
    private ArrayList pointsOfInterests = new ArrayList();
    public int counter = 0;

    YelpAPIFactory apiFactory = new YelpAPIFactory(mConsumerKey, mConsumerSecret, mToken, mTokenSecret);
    YelpAPI yelpAPI = apiFactory.createAPI();

    public ArrayList getPointsOfInterests(final String searchItem, ArrayList<LatLng> directionPoint,
                                          double radius) throws IOException {

        ArrayList mCleanData = getCleanData(directionPoint);

        for(int i = 0; i < mCleanData.size(); i++){

            final LatLng record = (LatLng) mCleanData.get(i);
            double latitude = record.latitude;
            double longitude = record.longitude;

            Map<String, String> params = new HashMap<>();

            // general search params
            params.put("term", searchItem);
            params.put("limit", "5");
            params.put("radius_filter", String.valueOf(radius));

            // set up location boundary
            CoordinateOptions coordinate = CoordinateOptions.builder()
                    .latitude(latitude)
                    .longitude(longitude).build();

            final Call<SearchResponse> call = yelpAPI.search(coordinate, params);

            final Callback<SearchResponse> callback = new Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                    counter += 1;
                    searchResponse = response.body();
                    ArrayList<Business> businesses = searchResponse.businesses();

                    for (Business business : businesses) {
                        PointOfInterest poi = new PointOfInterest(business.id(),
                                business.name(), business.displayPhone(), business.imageUrl(),
                                business.isClosed(), business.location(), business.rating());

                        pointsOfInterests.add(poi);
                    }

                }
                @Override
                public void onFailure(Call<SearchResponse> call, Throwable t) {
                    counter += 1;
                    Log.i("YELP", "FAILED");
                    Log.i("YELP", t.getMessage());
                    searchResponse = null;
                }
            };

            call.enqueue(callback);

        }

        return pointsOfInterests;

    }

    public ArrayList getCleanData(ArrayList dirtyList){
        ArrayList cleanList = new ArrayList();
        for(int i = 0; i < dirtyList.size(); i+=(dirtyList.size()/20)){
            cleanList.add(dirtyList.get(i));
        }
        return cleanList;
    }

    public ArrayList getPointsOfInterests() {
        return pointsOfInterests;
    }

}

