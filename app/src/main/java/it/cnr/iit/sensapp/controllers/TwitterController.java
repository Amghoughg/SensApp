package it.cnr.iit.sensapp.controllers;

import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import it.cnr.iit.sensapp.model.Post;
import it.cnr.iit.sensapp.utils.ChartData;
import retrofit2.Call;

/**
 * Created by mattia on 26/01/18.
 */

public class TwitterController {

    public void downloadLastTweets(long userId, final LastTweetsListener listener){

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        final Date monthAgo = calendar.getTime();

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        Call<List<Tweet>> tweetsCall = statusesService.userTimeline(userId, null,
                3200, null, null, null, false,
                true, true);

        tweetsCall.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> results) {

                List<Post> recentTweets = new ArrayList<>();

                for(Tweet tweet : results.data){

                    Date tweetDate = getTwitterDate(tweet.createdAt);

                    if(tweetDate != null && monthAgo != null && !tweetDate.before(monthAgo)){
                        recentTweets.add(new Post(tweet));
                    }else
                        break;
                }

                listener.onDownload(recentTweets);
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });
    }

    public interface LastTweetsListener{
        void onDownload(List<Post> posts);
    }

    public static Date getTwitterDate(String date) {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";

        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.getDefault());
        sf.setLenient(true);

        Date parsed;
        try {
            parsed = sf.parse(date);
        } catch (ParseException e) {
            parsed = null;
        }

        return parsed;
    }

    public static List<ChartData.FrequencyData> getMostUsedTags(List<String> tags,
                                                                int firstNElements,
                                                                boolean withHash){

        Set<String> uniqueTags = new HashSet<>(tags);

        List<ChartData.FrequencyData> tagsFrequency = new ArrayList<>();

        for(String tag : uniqueTags)
            tagsFrequency.add(new ChartData.FrequencyData(tag, Collections.frequency(tags, tag),
                    withHash));

        Collections.sort(tagsFrequency);
        Collections.reverse(tagsFrequency);

        return tagsFrequency.subList(0,
                firstNElements > tagsFrequency.size() ? tagsFrequency.size() : firstNElements);
    }

    public static RetweetStats getReteetStats(List<Tweet> tweets){

        RetweetStats stats = new RetweetStats();
        float myTweetsCount = 0.0f;

        for(Tweet tweet : tweets){

            if(tweet.retweetedStatus == null){

                myTweetsCount++;

                if(tweet.retweetCount != 0){
                    stats.retweetsPerc += 1.0f;
                    stats.tweetsWithRetweets.add(tweet.id);
                }

                stats.retweetsDistribution.add(tweet.retweetCount);
            }
        }

        if(myTweetsCount != 0) stats.retweetsPerc = (stats.retweetsPerc / myTweetsCount);

        Collections.sort(stats.retweetsDistribution);
        Collections.reverse(stats.retweetsDistribution);

        return stats;
    }

    public static class RetweetStats{

        public float retweetsPerc = 0.0f;
        public List<Integer> retweetsDistribution = new ArrayList<>();
        public List<Long> tweetsWithRetweets = new ArrayList<>();

    }
}
