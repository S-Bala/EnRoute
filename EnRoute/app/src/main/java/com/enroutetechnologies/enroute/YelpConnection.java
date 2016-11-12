package com.enroutetechnologies.enroute;

import android.util.Log;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
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

    YelpAPIFactory apiFactory = new YelpAPIFactory(mConsumerKey, mConsumerSecret, mToken, mTokenSecret);
    YelpAPI yelpAPI = apiFactory.createAPI();

    public void getRequest(String searchItem, double latitude, double longitude, double radius) throws IOException {

        Map<String, String> params = new HashMap<>();

        // general search params
        params.put("term", searchItem);
        params.put("limit", "3");
        params.put("radius_filter", String.valueOf(radius));

        // set up location boundary
        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(latitude)
                .longitude(longitude).build();

        Call<SearchResponse> call = yelpAPI.search(coordinate, params);

        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();
                Log.i("YELP", searchResponse.toString());
            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i("YELP", "FAILED");
                Log.i("YELP", t.getMessage());
            }
        };

        call.enqueue(callback);






    }


}


