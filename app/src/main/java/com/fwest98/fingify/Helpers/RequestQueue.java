package com.fwest98.fingify.Helpers;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.fwest98.fingify.R;

public class RequestQueue {
    public static boolean isToastTriggered = false;
    private static com.android.volley.RequestQueue requestQueue;
    private static Context applicationContext;

    private static synchronized com.android.volley.RequestQueue getRequestQueue(Context context) {
        if(requestQueue == null) {
            applicationContext = context.getApplicationContext();
            requestQueue = Volley.newRequestQueue(applicationContext);
        }
        return requestQueue;
    }

    public static <T> void addToRequestQueue(ApiRequest<T> request, Context context) {
        getRequestQueue(context).add(request);

        Response.Listener<T> currentListener = request.getListener();
        request.setListener(response -> {
            if(request.hasNewApiVersion && !isToastTriggered) {
                Toast.makeText(context, context.getString(R.string.apirequests_newapiversion), Toast.LENGTH_LONG).show();
                isToastTriggered = true;
            }

            currentListener.onResponse(response);
        });
    }
}
