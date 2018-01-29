package it.cnr.iit.sensapp;

import android.app.Application;

import com.twitter.sdk.android.core.Twitter;

/**
 * Created by mattia on 27.01.18.
 */

public class CustomApplication extends Application {

    public void onCreate() {
        super.onCreate();

        Twitter.initialize(this);
    }

}
