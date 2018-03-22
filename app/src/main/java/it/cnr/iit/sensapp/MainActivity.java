package it.cnr.iit.sensapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.cnr.iit.sensapp.askcontroller.ForegroundService;
import it.cnr.iit.sensapp.controllers.FacebookController;
import it.cnr.iit.sensapp.controllers.TwitterController;
import it.cnr.iit.sensapp.controllers.UIController;
import it.cnr.iit.sensapp.model.Post;
import it.cnr.iit.sensapp.utils.ChartData;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        FacebookController.FacebookListener, TwitterController.LastTweetsListener{

    private User twitterUser;
    private AVLoadingIndicatorView generalAvi, tagsAvi, retweetsAvi, socialActivityAvi, fbStatsAvi,
    fbActivitiesAvi, fbPhotosAvi, fbVideosAvi;
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
        socialActivityAvi = findViewById(R.id.social_activity_avi);
        fbStatsAvi = findViewById(R.id.fb_stats_avi);
        fbActivitiesAvi = findViewById(R.id.fb_activities_avi);
        fbPhotosAvi = findViewById(R.id.fb_photos_avi);
        fbVideosAvi = findViewById(R.id.fb_videos_avi);

        scrollView = findViewById(R.id.scrollView);
        refreshLayout = findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(this);

        generalAvi.show();
        tagsAvi.show();
        retweetsAvi.show();
        fbStatsAvi.show();
        ((AVLoadingIndicatorView) findViewById(R.id.fb_places_avi)).show();
        ((AVLoadingIndicatorView) findViewById(R.id.fb_event_avi)).show();
        ((AVLoadingIndicatorView) findViewById(R.id.fb_fitness_avi)).show();

        downloadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    @Override
    public void onRefresh() {

        findViewById(R.id.general_info_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.tags_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.retweets_container).setVisibility(View.INVISIBLE);

        generalAvi.show();
        tagsAvi.show();
        retweetsAvi.show();
        fbStatsAvi.show();
        fbActivitiesAvi.show();
        fbPhotosAvi.show();
        fbVideosAvi.show();

        scrollView.scrollTo(0, 0);
        scrollView.fullScroll(View.FOCUS_UP);
        scrollView.fullScroll(NestedScrollView.FOCUS_UP);

        downloadData();
    }

    private void downloadData(){

        if(PreferencesController.isFbLogged(this)) {
            facebookController.downloadFacebookUserInfo(this);
            facebookController.downloadLastFacebookPosts(this);

            facebookController.downloadRecentVideosInfo(this);
            facebookController.downloadRecentMusicInfo(this);
            facebookController.downloadRecentBooksInfo(this);

            facebookController.downloadUploadedPhotos(this);
            facebookController.downloadPhotosWhereIAmTagged(this);

            facebookController.downloadUploadedVideos(this);
            facebookController.downloadVideosWhereIAmTagged(this);

            facebookController.downloadTaggedPlaces(this);

            facebookController.downloadLastEvent(this);

            facebookController.downloadLastPages(this);

            facebookController.downloadWalk(this);
            facebookController.downloadRun(this);
            facebookController.downloadBikes(this);

        }else{
            findViewById(R.id.fb_stats_card).setVisibility(View.GONE);
        }

        twitterId = PreferencesController.isTwitterLogged(this);
        if(twitterId != -1) {
            downloadTwitterGeneralInfo();
            twitterController.downloadLastTweets(twitterId, this);
        }else{
            findViewById(R.id.twitter_tags_card).setVisibility(View.GONE);
            findViewById(R.id.twitter_retweets_card).setVisibility(View.GONE);
        }

    }

    private void startService(){
        Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
        //startService(startIntent);
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

        refreshLayout.setRefreshing(false);
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

        String retweetPerc = String.format(Locale.getDefault(), "%.2f%%", stats.retweetsPerc);

        findViewById(R.id.retweets_container).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.retweets_p_tv)).setText(retweetPerc);

        UIController.createBarChart((BarChart)findViewById(R.id.retweets_chart), this,
                stats.retweetsDistribution);

        retweetsAvi.hide();
    }


    //==============================================================================================
    // FACEBOOK
    //==============================================================================================
    @Override
    public void onLastPostsDownloaded(List<Post> posts) {
        this.fbPosts = posts;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSocialActivity();
                showFacebookStats(fbPosts);
            }
        });
    }

    @Override
    public void onFacebookLoginInfo(FacebookController.FacebookLoginInfo loginInfo) {
        generalAvi.hide();
        findViewById(R.id.general_info_container).setVisibility(View.VISIBLE);
        findViewById(R.id.name).setVisibility(View.VISIBLE);
        if(loginInfo.fullName != null)
            ((TextView)findViewById(R.id.name)).setText(loginInfo.fullName);

        ((CountAnimationTextView) findViewById(R.id.friends_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, loginInfo.friends);

        Picasso.with(MainActivity.this)
                .load(loginInfo.profilePicture)
                .transform(new CircleTransformation())
                .into((ImageView) findViewById(R.id.profile_image));
    }

    @Override
    public void onFacebookVideoInfoDownloaded(int movies, int tvShows) {
        fbActivitiesAvi.hide();
        findViewById(R.id.fb_activities_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.movies_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, movies);
        ((CountAnimationTextView) findViewById(R.id.tv_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, movies);
    }

    @Override
    public void onFacebookMusicInfoDownloaded(int music) {
        fbActivitiesAvi.hide();
        findViewById(R.id.fb_activities_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.music_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, music);
    }

    @Override
    public void onFacebookBooksInfoDownloaded(int books) {
        fbActivitiesAvi.hide();
        findViewById(R.id.fb_activities_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.books_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, books);
    }

    @Override
    public void onFacebookUploadedPhotos(int photos) {
        fbPhotosAvi.hide();
        findViewById(R.id.fb_photos_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.uploaded_photos_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, photos);
    }

    @Override
    public void onFacebookTaggedPhotos(int photos) {
        fbPhotosAvi.hide();
        findViewById(R.id.fb_photos_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.tagged_photos_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, photos);
    }

    @Override
    public void onFacebookUploadedVideos(int videos) {
        fbVideosAvi.hide();
        findViewById(R.id.fb_videos_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.uploaded_videos_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, videos);
    }

    @Override
    public void onFacebookTaggedVideos(int videos) {
        fbVideosAvi.hide();
        findViewById(R.id.fb_videos_container).setVisibility(View.VISIBLE);
        ((CountAnimationTextView) findViewById(R.id.tagged_videos_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, videos);
    }

    @Override
    public void onFacebookPlacesDownloaded(String firstPlace, String secondPlace, String thirdPlace) {
        ((AVLoadingIndicatorView) findViewById(R.id.fb_places_avi)).hide();
        findViewById(R.id.fb_places_container).setVisibility(View.VISIBLE);

        if(firstPlace == null) findViewById(R.id.first_place_container).setVisibility(View.GONE);
        else{
            ((TextView) findViewById(R.id.first_place)).setText(firstPlace);
        }

        if(secondPlace == null) findViewById(R.id.second_place_container).setVisibility(View.GONE);
        else{
            ((TextView) findViewById(R.id.second_place)).setText(secondPlace);
        }

        if(thirdPlace == null) findViewById(R.id.third_place_container).setVisibility(View.GONE);
        else{
            ((TextView) findViewById(R.id.third_place)).setText(thirdPlace);
        }

    }

    @Override
    public void onFacebookLastEventDownload(String url) {
        ((AVLoadingIndicatorView) findViewById(R.id.fb_event_avi)).hide();

        Picasso.with(this).load(url).into((ImageView) findViewById(R.id.event_cover));
    }

    @Override
    public void onFacebookPagesDownload(int total, String name, String url) {

        ((AVLoadingIndicatorView) findViewById(R.id.fb_pages_avi)).hide();
        findViewById(R.id.fb_pages_container).setVisibility(View.VISIBLE);

        ((CountAnimationTextView) findViewById(R.id.total_pages_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, total);

        if(url != null)
            Picasso.with(this).load(url).into((ImageView) findViewById(R.id.lat_page_image));

        if(name != null){
            ((TextView) findViewById(R.id.last_page_label)).setText(name + " is the\nmost recent");
        }else
            findViewById(R.id.last_page_label).setVisibility(View.INVISIBLE);

    }

    @Override
    public void onFacebookWalkDownload(int total) {

        ((AVLoadingIndicatorView) findViewById(R.id.fb_fitness_avi)).hide();
        findViewById(R.id.fb_fitness_container).setVisibility(View.VISIBLE);

        ((CountAnimationTextView) findViewById(R.id.walk_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, total);
    }

    @Override
    public void onFacebookRunDownload(int total) {

        ((AVLoadingIndicatorView) findViewById(R.id.fb_fitness_avi)).hide();
        findViewById(R.id.fb_fitness_container).setVisibility(View.VISIBLE);

        ((CountAnimationTextView) findViewById(R.id.run_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, total);

    }

    @Override
    public void onFacebookBikeDownload(int total) {

        ((AVLoadingIndicatorView) findViewById(R.id.fb_fitness_avi)).hide();
        findViewById(R.id.fb_fitness_container).setVisibility(View.VISIBLE);

        ((CountAnimationTextView) findViewById(R.id.bike_counter))
                .setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, total);

    }


    //==============================================================================================
    // FACEBOOK STATS
    //==============================================================================================
    private void showFacebookStats(List<Post> posts){

        Log.d("MAIN", "FB: "+ posts.size());

        FacebookController.FBStats stats = FacebookController.getStats(posts);

        String likesPerc = String.format(Locale.getDefault(), "%.2f%%", stats.likesPerc);
        String sharedPerc = String.format(Locale.getDefault(), "%.2f%%", stats.sharedPerc);

        findViewById(R.id.fb_stats_container).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.fb_likes_perc)).setText(likesPerc);
        ((TextView) findViewById(R.id.fb_share_perc)).setText(sharedPerc);

        UIController.createBarChart((BarChart)findViewById(R.id.fb_share_chart), this,
                stats.shareDistribution);

        UIController.createBarChart((BarChart)findViewById(R.id.fb_likes_chart), this,
                stats.likesDistribution);

        fbStatsAvi.hide();
    }

    //==============================================================================================
    // TWITTER
    //==============================================================================================
    @Override
    public void onDownload(List<Post> posts) {
        this.twitterPosts = posts;

        Log.d("MAIN", "TW: "+ posts.size());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSocialActivity();
            }
        });
    }
}
