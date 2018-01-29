package it.cnr.iit.sensapp.controllers;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

/**
 * Created by mattia on 28.01.18.
 */

public class MyTwitterApiClient extends TwitterApiClient {

    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    /**
     * Provide CustomService with defined endpoints
     */
    public MyTwitterCustomInterface getCustomService() {
        return getService(MyTwitterCustomInterface.class);
    }
}