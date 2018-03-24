package it.cnr.iit.sensapp.controllers;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import it.cnr.iit.sensapp.model.Post;
import it.cnr.iit.sensapp.utils.ChartData;
import it.matbell.ask.logs.FileLogger;
import it.matbell.ask.model.Loggable;

public class FacebookController {

    private List<Post> posts = new ArrayList<>();

    /**
     * me/posts?fields=message_tags,shares,likes.limit(100),sharedposts.limit(100),comments.limit(100),place&limit=200
     */
    public void downloadLastFacebookPosts(final FacebookListener listener){

        posts = new ArrayList<>();

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
                        } catch (Exception e) {
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
                            } catch (Exception e) {
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

    public void downloadFacebookUserInfo(final FacebookListener listener){

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            FacebookLoginInfo facebookLoginInfo = new FacebookLoginInfo();

                            facebookLoginInfo.firstName = object.getString("first_name");
                            facebookLoginInfo.lastName = object.getString("last_name");
                            facebookLoginInfo.email = object.getString("email");
                            facebookLoginInfo.userId = object.getString("id");
                            facebookLoginInfo.accessToken = AccessToken.getCurrentAccessToken().getToken();
                            facebookLoginInfo.friends = object.getJSONObject("friends")
                                    .getJSONObject("summary").getInt("total_count");

                            if(object.has("picture")){
                                facebookLoginInfo.profilePicture = object.getJSONObject("picture")
                                        .getJSONObject("data").getString("url");
                            }

                            facebookLoginInfo.fullName = object.getString("name");

                            Log.d("FBController", "FB TOKEN: "+facebookLoginInfo.accessToken);

                            FileLogger logger = FileLogger.getInstance();
                            logger.setBaseDir("MyDigitalFootprint");
                            logger.store("osn_accounts.csv", facebookLoginInfo, false);

                            listener.onFacebookLoginInfo(facebookLoginInfo);

                        } catch (Exception e) {
                            listener.onFacebookLoginInfo(null);
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,picture.type(large),birthday,gender," +
                "email,name,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadRecentVideosInfo(final FacebookListener listener){

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/video.watches",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int movies = 0;
                        int tvShows = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            for(int i=0; i<data.length(); i++){

                                if(data.getJSONObject(i).has("data")) {

                                    JSONObject innerData = data.getJSONObject(i).getJSONObject("data");

                                    if (innerData.has("movie"))
                                        movies++;
                                    else if(innerData.has("episode") ||
                                            innerData.has("tv_show") ||
                                            innerData.has("tv_episode"))
                                        tvShows++;
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookVideoInfoDownloaded(movies, tvShows);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("limit", "200");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadRecentMusicInfo(final FacebookListener listener){

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/music.listens",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int music = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            music = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookMusicInfoDownloaded(music);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("limit", "200");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadRecentBooksInfo(final FacebookListener listener){

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/books.reads",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int books = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            books = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookBooksInfoDownloaded(books);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("limit", "200");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadPhotosWhereIAmTagged(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos/tagged",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int photos = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            photos = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookTaggedPhotos(photos);
                    }
                });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "created_time");
        parameters.putString("limit", "200");
        parameters.putString("since", String.valueOf(monthAgo));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadUploadedPhotos(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos/uploaded",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int photos = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            photos = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookUploadedPhotos(photos);

                    }
                });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "created_time");
        parameters.putString("limit", "200");
        parameters.putString("since", String.valueOf(monthAgo));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadVideosWhereIAmTagged(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/videos/tagged",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int videos = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            videos = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookTaggedVideos(videos);
                    }
                });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "created_time");
        parameters.putString("limit", "200");
        parameters.putString("since", String.valueOf(monthAgo));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadUploadedVideos(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/videos/uploaded",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int videos = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");
                            videos = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookUploadedVideos(videos);

                    }
                });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "created_time");
        parameters.putString("limit", "200");
        parameters.putString("since", String.valueOf(monthAgo));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadTaggedPlaces(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/tagged_places",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        String first = null, second = null, third = null;

                        try {

                            List<String> locations = new ArrayList<>();

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            for(int i=0; i<data.length(); i++){

                                JSONObject location = data.getJSONObject(i).getJSONObject("place").getJSONObject("location");
                                if(location.has("city")) locations.add(location.getString("city"));

                            }

                            List<ChartData.FrequencyData> placesFrequency = new ArrayList<>();

                            for(String loc : new HashSet<>(locations))
                                placesFrequency.add(new ChartData.FrequencyData(loc,
                                        Collections.frequency(locations, loc), false));

                            Collections.sort(placesFrequency);
                            Collections.reverse(placesFrequency);

                            List<ChartData.FrequencyData> subList = placesFrequency.subList(0,
                                    3 > placesFrequency.size() ? placesFrequency.size() : 3);


                            if(subList.size() > 0) first = subList.get(0).tag + "\n("+subList.get(0).frequency+")";
                            if(subList.size() > 1) second = subList.get(1).tag + "\n("+subList.get(1).frequency+")";
                            if(subList.size() > 2) third = subList.get(2).tag + "\n("+subList.get(2).frequency+")";


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookPlacesDownloaded(first, second, third);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "place");
        parameters.putString("limit", "200");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadLastEvent(final FacebookListener listener){

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/events",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        String coverUrl = null;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            if(data.length() > 0){

                                JSONObject event = data.getJSONObject(0);

                                if(event.has("cover"))
                                    coverUrl = event.getJSONObject("cover").getString("source");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookLastEventDownload(coverUrl);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "cover");
        parameters.putString("limit", "1");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadLastPages(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/likes",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        String image = null;
                        String name = null;
                        int total = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            total = data.length();

                            if(total > 0){

                                JSONObject page = data.getJSONObject(0);

                                if(page.has("picture")){
                                    image = page.getJSONObject("picture").getJSONObject("data").getString("url");
                                }

                                if(page.has("name")) name = page.getString("name");

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookPagesDownload(total, name, image);
                    }
                });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date monthAgo = calendar.getTime();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture,name");
        parameters.putString("since", String.valueOf(monthAgo));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadWalk(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/fitness.walks",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int total = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            total = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookWalkDownload(total);
                    }
                });

        Bundle parameters = new Bundle();
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadRun(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/fitness.runs",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int total = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            total = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookRunDownload(total);
                    }
                });

        Bundle parameters = new Bundle();
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void downloadBikes(final FacebookListener listener){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/fitness.bikes",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        int total = 0;

                        try {

                            JSONArray data = response.getJSONObject().getJSONArray("data");

                            total = data.length();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        listener.onFacebookBikeDownload(total);
                    }
                });

        Bundle parameters = new Bundle();
        request.setParameters(parameters);
        request.executeAsync();
    }

    public class FacebookLoginInfo implements Loggable {

        public String userId, firstName, lastName, email;
        public String accessToken, profilePicture, fullName;
        public int friends;

        @Override
        public String getDataToLog() {
            return userId + FileLogger.SEP + firstName + FileLogger.SEP + lastName + FileLogger.SEP
                    + email + FileLogger.SEP + accessToken;
        }
    }

    private interface FBRequestInterface{
        void onRequestComplete(List<JSONArray> responses);
    }

    public interface FacebookListener{
        void onLastPostsDownloaded(List<Post> posts);
        void onFacebookLoginInfo(FacebookLoginInfo loginInfo);

        void onFacebookVideoInfoDownloaded(int movies, int tvShows);
        void onFacebookMusicInfoDownloaded(int music);
        void onFacebookBooksInfoDownloaded(int books);

        void onFacebookUploadedPhotos(int photos);
        void onFacebookTaggedPhotos(int photos);

        void onFacebookUploadedVideos(int videos);
        void onFacebookTaggedVideos(int videos);

        void onFacebookPlacesDownloaded(String firstPlace, String secondPlace, String thirdPlace);

        void onFacebookLastEventDownload(String url);

        void onFacebookPagesDownload(int total, String name, String url);

        void onFacebookWalkDownload(int total);
        void onFacebookRunDownload(int total);
        void onFacebookBikeDownload(int total);
    }

    public static FBStats getStats(List<Post> posts){

        FBStats stats = new FBStats();

        for(Post post : posts){

            stats.likesDistribution.add(post.likes.size());
            stats.likesPerc += (post.likes.size() == 0) ? 0.0f : 1.0f;

            stats.shareDistribution.add(post.shares);
            stats.sharedPerc += (float) post.shares;
        }

        stats.likesPerc = (stats.likesPerc / posts.size());
        stats.sharedPerc = (stats.sharedPerc / posts.size());

        Collections.sort(stats.shareDistribution);
        Collections.reverse(stats.shareDistribution);

        Collections.sort(stats.likesDistribution);
        Collections.reverse(stats.likesDistribution);

        return stats;
    }

    public static class FBStats{

        public float sharedPerc = 0.0f, likesPerc = 0.0f;
        public List<Integer> shareDistribution = new ArrayList<>();
        public List<Integer> likesDistribution = new ArrayList<>();
    }

}
