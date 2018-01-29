package it.cnr.iit.sensapp.controllers;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.sensapp.utils.ChartData;

/**
 * Created by mattia on 26/01/18.
 */

public class TwitterController {

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

        if(myTweetsCount != 0) stats.retweetsPerc = (stats.retweetsPerc / myTweetsCount)*100.0f;

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
