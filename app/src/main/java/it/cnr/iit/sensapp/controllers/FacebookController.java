package it.cnr.iit.sensapp.controllers;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.cnr.iit.sensapp.model.Post;

public class FacebookController {

    private List<Post> posts = new ArrayList<>();

    /**
     * me/posts?fields=message_tags,shares,likes.limit(100),sharedposts.limit(100),comments.limit(100),place&limit=200
     */
    public void downloadLastFacebookPosts(final FacebookListener listener){

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "message_tags,shares,likes.limit(100)," +
                "sharedposts.limit(100),comments.limit(100),place");
        parameters.putString("since", String.valueOf(monthAgo));
        parameters.putString("limit", "200");

        new FBGraphRequest("/me/posts", parameters, new FBRequestInterface() {
            @Override
            public void onRequestComplete(List<JSONArray> responses) {

                for(JSONArray resp : responses){

                    for(int i=0; i<resp.length(); i++)

                        try {
                            posts.add(new Post(resp.getJSONObject(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }

                listener.onLastPostsDownloaded(posts);
            }
        }).start();
    }

    private class FBGraphRequest extends Thread{

        private List<JSONArray> responses = new ArrayList<>();
        private Bundle params;
        private FBRequestInterface listener;
        private String endPoint;

        FBGraphRequest(String endPoint, Bundle params, FBRequestInterface listener){
            this.params = params;
            this.listener = listener;
            this.endPoint = endPoint;
        }

        @Override
        public void run() {

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(), endPoint,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {

                            JSONArray data = new JSONArray();

                            try {
                                data = response.getJSONObject().getJSONArray("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(data.length() != 0) responses.add(data);

                            GraphRequest nextRequest = response.getRequestForPagedResults(
                                    GraphResponse.PagingDirection.NEXT);
                            if(nextRequest != null){
                                nextRequest.setCallback(this);
                                nextRequest.executeAndWait();
                            }else {
                                listener.onRequestComplete(responses);
                            }
                        }
                    });

            request.setParameters(params);
            request.executeAndWait();

        }
    }

    private interface FBRequestInterface{
        void onRequestComplete(List<JSONArray> responses);
    }

    public interface FacebookListener{
        void onLastPostsDownloaded(List<Post> posts);
    }

}
