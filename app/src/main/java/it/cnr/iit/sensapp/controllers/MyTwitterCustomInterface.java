package it.cnr.iit.sensapp.controllers;

import com.twitter.sdk.android.core.models.User;

import it.cnr.iit.sensapp.RetweetersResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mattia on 28.01.18.
 */

public interface MyTwitterCustomInterface {

    @GET("/1.1/statuses/retweeters/ids.json")
    Call<RetweetersResponse> getRetweeters(@Query("id") long id);

    @GET("/1.1/users/show.json")
    Call<User> getUserById(@Query("id") long id);
}