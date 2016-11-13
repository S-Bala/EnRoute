package com.enroutetechnologies.enroute;

import android.content.Context;
import android.util.Log;
import android.content.DialogInterface;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by Sanhar on 2016-11-12.
 */

public class HTTPSRequest {
    Context mContext;
    Listener mListener;
    HTTPSRequest(Listener listener, Context context){
        mListener = listener;
        mContext = context;
    }

    public void getRequest(String url) {

        Log.d("YOOO", url);

    }

    public void getRequest(String url, final DialogInterface dialog){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.cancel();
                        mListener.requestSuccess(response);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
                mListener.requestFailure(error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public interface Listener{
        void requestSuccess(String response);
        void requestFailure(String response);

    }
}
