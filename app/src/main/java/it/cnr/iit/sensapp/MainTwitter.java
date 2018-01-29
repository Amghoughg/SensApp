package it.cnr.iit.sensapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daasuu.cat.CountAnimationTextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.cnr.iit.sensapp.controllers.MyTwitterApiClient;
import it.cnr.iit.sensapp.controllers.MyTwitterCustomInterface;
import it.cnr.iit.sensapp.controllers.TwitterController;
import it.cnr.iit.sensapp.controllers.UIController;
import it.cnr.iit.sensapp.utils.ChartData;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import retrofit2.Call;

public class MainTwitter extends AppCompatActivity {

    private User twitterUser;
    private AVLoadingIndicatorView generalAvi, tagsAvi, retweetsAvi;
    private List<User> retweeters = new ArrayList<>();
    private MostRetweetersAdapter adapter;
    private List<Long> tweetsWithRetweets = new ArrayList<>();
    private List<String> retweetersIds = new ArrayList<>();
    private int downloadedListOfRetweeters = 0;
    private int retweetersCounter = 0;
    private int retweetersDownloaderCount =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_main);

        generalAvi = findViewById(R.id.general_info_avi);
        tagsAvi = findViewById(R.id.tags_avi);
        retweetsAvi = findViewById(R.id.retweets_avi);

        generalAvi.show();
        tagsAvi.show();
        retweetsAvi.show();

        fillUserInfo();
    }

    private void fillUserInfo(){

        final Call<User> userCall = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);

        userCall.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {

                twitterUser = userResult.data;

                Picasso.with(MainTwitter.this)
                        .load(twitterUser.profileImageUrl.replace("_normal", ""))
                        .transform(new CircleTransformation())
                        .into((ImageView) findViewById(R.id.profile_image));

                String nickname = "(" + twitterUser.screenName + ")";
                ((TextView)findViewById(R.id.name)).setText(twitterUser.name);
                ((TextView)findViewById(R.id.nickname)).setText(nickname);

                ((CountAnimationTextView) findViewById(R.id.pins_counter))
                        .setInterpolator(new AccelerateInterpolator())
                        .countAnimation(0, twitterUser.statusesCount);
                ((CountAnimationTextView) findViewById(R.id.followers_counter))
                        .setInterpolator(new AccelerateInterpolator())
                        .countAnimation(0, twitterUser.followersCount);
                ((CountAnimationTextView) findViewById(R.id.following_counter))
                        .setInterpolator(new AccelerateInterpolator())
                        .countAnimation(0, twitterUser.friendsCount);

                generalAvi.hide();
                findViewById(R.id.general_info_container).setVisibility(View.VISIBLE);

                fetchLastTweets();
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });

    }

    private void fetchLastTweets(){

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        Call<List<Tweet>> tweetsCall = statusesService.userTimeline(twitterUser.id, null,
                200, null, null, null, false,
                true, true);

        tweetsCall.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> results) {

                showMostUsedTags(results.data);
                showRetweetStats(results.data);
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });
    }

    private void showMostUsedTags(List<Tweet> tweets){

        PieChart chart = findViewById(R.id.tags_chart);

        List<String> tags = new ArrayList<>();
        for(Tweet tweet : tweets)
            for(HashtagEntity hashTag : tweet.entities.hashtags)
                tags.add(hashTag.text);

        List<ChartData.FrequencyData> tagsFrequency = TwitterController.getMostUsedTags(
                tags, 5, true);

        UIController.createPieChart(chart, this, Legend.LegendOrientation.VERTICAL,
                Legend.LegendVerticalAlignment.CENTER, Legend.LegendHorizontalAlignment.LEFT,
                tagsFrequency,Color.parseColor("#DD6E42"),
                Color.parseColor("#4E598C"), Color.parseColor("#FF5964"),
                Color.parseColor("#99D5C9"), Color.parseColor("#35A7FF"));

        tagsAvi.hide();
        findViewById(R.id.tags_container).setVisibility(View.VISIBLE);
    }

    private void showRetweetStats(List<Tweet> tweets){

        TwitterController.RetweetStats stats = TwitterController.getReteetStats(tweets);
        tweetsWithRetweets = stats.tweetsWithRetweets;

        String retweetPerc = String.format(Locale.getDefault(), "%.2f%%", stats.retweetsPerc);

        findViewById(R.id.retweets_container).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.retweets_p_tv)).setText(retweetPerc);

        UIController.createBarChart((BarChart)findViewById(R.id.retweets_chart), this,
                stats.retweetsDistribution);

        downloadRetweeters();

        retweetsAvi.hide();
    }

    private void downloadRetweeters(){

        TwitterSession activeSession = TwitterCore.getInstance()
                .getSessionManager().getActiveSession();

        MyTwitterApiClient apiClient = new MyTwitterApiClient(activeSession);
        MyTwitterCustomInterface customService = apiClient.getCustomService();

        for(Long tweetId : tweetsWithRetweets){

            Call<RetweetersResponse> call  = customService.getRetweeters(tweetId);
            call.enqueue(new Callback<RetweetersResponse>() {
                @Override
                public void success(Result<RetweetersResponse> result) {
                    newDownloadedRetweeters(result.data.ids);
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.e("MainTwitter", ""+exception);
                }
            });
        }
    }

    private void newDownloadedRetweeters(List<String> retweeters){

        TwitterSession activeSession = TwitterCore.getInstance()
                .getSessionManager().getActiveSession();

        MyTwitterApiClient apiClient = new MyTwitterApiClient(activeSession);
        MyTwitterCustomInterface customService = apiClient.getCustomService();

        retweetersIds.addAll(retweeters);
        downloadedListOfRetweeters++;

        if(downloadedListOfRetweeters >= tweetsWithRetweets.size()){

            List<ChartData.FrequencyData> firstRetweeters = TwitterController.getMostUsedTags(
                    retweetersIds, 5, false);

            retweetersCounter = firstRetweeters.size();

            for(ChartData.FrequencyData data : firstRetweeters){

                Call<User> call = customService.getUserById(Long.parseLong(data.tag));
                call.enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        newDownloadedRetweeterInfo(result.data);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.e("MainTwitter", ""+exception);
                    }
                });

            }
        }
    }

    private void newDownloadedRetweeterInfo(User user){
        retweeters.add(user);
        retweetersDownloaderCount++;

        if(retweetersDownloaderCount >= retweetersCounter) showMostRetweeters();
    }

    private void showMostRetweeters(){

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MostRetweetersAdapter(this, retweeters, retweetersIds);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }


}
