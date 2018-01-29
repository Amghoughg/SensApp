package it.cnr.iit.sensapp;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mattia on 28.01.18.
 */

public class NetworkController {

    static final String TAG = NetworkController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static NetworkController mInstance;

    private NetworkController(){}

    public static synchronized NetworkController getInstance() {

        if(mInstance == null) mInstance = new NetworkController();

        return mInstance;
    }

    RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Context context, Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(context).add(req);
    }

    public <T> void addToRequestQueue(Context context, Request<T> req) {
        req.setTag(TAG);
        getRequestQueue(context).add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
