package it.cnr.iit.sensapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.daasuu.cat.CountAnimationTextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import it.cnr.iit.sensapp.askcontroller.ForegroundService;
import it.cnr.iit.sensapp.controllers.FacebookController;
import it.cnr.iit.sensapp.controllers.MyTwitterApiClient;
import it.cnr.iit.sensapp.controllers.MyTwitterCustomInterface;
import it.cnr.iit.sensapp.controllers.TwitterController;
import it.cnr.iit.sensapp.controllers.UIController;
import it.cnr.iit.sensapp.model.Post;
import it.cnr.iit.sensapp.utils.ChartData;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        FacebookController.FacebookListener, TwitterController.LastTweetsListener{

    private User twitterUser;
    private AVLoadingIndicatorView generalAvi, tagsAvi, retweetsAvi, retweetersAvi, socialActivityAvi;
    private List<User> retweeters = new ArrayList<>();
    private MostRetweetersAdapter adapter;
    private List<Long> tweetsWithRetweets = new ArrayList<>();
    private List<String> retweetersIds = new ArrayList<>();
    private int downloadedListOfRetweeters = 0;
    private int retweetersCounter = 0;
    private int retweetersDownloaderCount =0;
    private NestedScrollView scrollView;
    private SwipeRefreshLayout refreshLayout;


    private FacebookController facebookController = new FacebookController();
    private TwitterController twitterController = new TwitterController();

    private List<Post> fbPosts, twitterPosts;
    private long twitterId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        generalAvi = findViewById(R.id.general_info_avi);
        tagsAvi = findViewById(R.id.tags_avi);
        retweetsAvi = findViewById(R.id.retweets_avi);
        retweetersAvi = findViewById(R.id.retweeters_avi);
        socialActivityAvi = findViewById(R.id.social_activity_avi);
        scrollView = findViewById(R.id.scrollView);
        refreshLayout = findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(this);

        generalAvi.show();
        tagsAvi.show();
        retweetsAvi.show();
        retweetersAvi.show();

        //fillUserInfo();
        //startService();

        if(PreferencesController.isFbLogged(this)) {
            facebookController.downloadFacebookUserInfo(this);
            facebookController.downloadLastFacebookPosts(this);
        }

        twitterId = PreferencesController.isTwitterLogged(this);
        if(twitterId != -1) {
            downloadTwitterGeneralInfo();
            twitterController.downloadLastTweets(twitterId, this);
        }
    }

    @Override
    public void onRefresh() {

        /*retweeters = new ArrayList<>();
        tweetsWithRetweets = new ArrayList<>();
        retweetersIds = new ArrayList<>();
        downloadedListOfRetweeters = 0;
        retweetersCounter = 0;
        retweetersDownloaderCount = 0;

        findViewById(R.id.general_info_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.tags_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.retweets_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.retweeters_container).setVisibility(View.INVISIBLE);

        generalAvi.show();
        tagsAvi.show();
        retweetsAvi.show();
        retweetersAvi.show();

        fillUserInfo();
        startService();

        scrollView.scrollTo(0, 0);
        scrollView.fullScroll(View.FOCUS_UP);
        scrollView.fullScroll(NestedScrollView.FOCUS_UP);*/
    }

    private void startService(){
        Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
        startService(startIntent);
    }

    private void downloadTwitterGeneralInfo(){

        final Call<User> userCall = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);

        userCall.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {

                twitterUser = userResult.data;

                Picasso.with(MainActivity.this)
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
                3200, null, null, null, false,
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

    //==============================================================================================
    // SOCIAL ACTIVITY
    //==============================================================================================
    private void showSocialActivity(){

        if(PreferencesController.isFbLogged(this) && twitterId != -1
                && (fbPosts == null || twitterPosts == null)) return;

        if(fbPosts == null) fbPosts = new ArrayList<>();
        if(twitterPosts == null) twitterPosts = new ArrayList<>();

        PieChart chart = findViewById(R.id.social_activity_chart);

        List<ChartData.FrequencyData> frequency = new ArrayList<>();
        frequency.add(new ChartData.FrequencyData("Facebook", fbPosts.size(), false));
        frequency.add(new ChartData.FrequencyData("Twitter", twitterPosts.size(), false));


        UIController.createPieChart(chart, this, Legend.LegendOrientation.VERTICAL,
                Legend.LegendVerticalAlignment.TOP, Legend.LegendHorizontalAlignment.LEFT,
                frequency, Color.parseColor("#4E598C"),
                Color.parseColor("#99D5C9"));

        socialActivityAvi.hide();
        findViewById(R.id.social_activity_container).setVisibility(View.VISIBLE);
    }

    //==============================================================================================
    // MOST USED TAGS
    //==============================================================================================
    private void showMostUsedTags(List<Tweet> tweets){

        PieChart chart = findViewById(R.id.tags_chart);

        List<String> tags = new ArrayList<>();
        for(Tweet tweet : tweets)
            for(HashtagEntity hashTag : tweet.entities.hashtags)
                tags.add(hashTag.text);

        List<ChartData.FrequencyData> tagsFrequency = TwitterController.getMostUsedTags(
                tags, 5, true);

        UIController.createPieChart(chart, this, Legend.LegendOrientation.VERTICAL,
                Legend.LegendVerticalAlignment.TOP, Legend.LegendHorizontalAlignment.LEFT,
                tagsFrequency,Color.parseColor("#DD6E42"),
                Color.parseColor("#4E598C"), Color.parseColor("#FF5964"),
                Color.parseColor("#99D5C9"), Color.parseColor("#35A7FF"));

        tagsAvi.hide();
        findViewById(R.id.tags_container).setVisibility(View.VISIBLE);
    }

    //==============================================================================================
    // RETWEETS STATS
    //==============================================================================================
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

    //==============================================================================================
    // RETWEETERS
    //==============================================================================================
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
                    Log.e("MainActivity", ""+exception);
                    refreshLayout.setRefreshing(false);
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
                        Log.e("MainActivity", ""+exception);
                        refreshLayout.setRefreshing(false);
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
        findViewById(R.id.retweeters_container).setVisibility(View.VISIBLE);
        retweetersAvi.hide();
        refreshLayout.setRefreshing(false);
    }


    //==============================================================================================
    // FACEBOOK
    //==============================================================================================
    @Override
    public void onLastPostsDownloaded(List<Post> posts) {
        this.fbPosts = posts;
        showSocialActivity();
    }

    @Override
    public void onFacebookLoginInfo(FacebookController.FacebookLoginInfo loginInfo) {
        generalAvi.hide();
        findViewById(R.id.general_info_container).setVisibility(View.VISIBLE);

        ((CountAnimationTextView) findViewById(R.id.friends_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, loginInfo.friends);

        Picasso.with(MainActivity.this)
                .load(loginInfo.profilePicture)
                .transform(new CircleTransformation())
                .into((ImageView) findViewById(R.id.profile_image));
    }

    //==============================================================================================
    // TWITTER
    //==============================================================================================
    @Override
    public void onDownload(List<Post> posts) {
        this.twitterPosts = posts;
        showSocialActivity();
    }
}
