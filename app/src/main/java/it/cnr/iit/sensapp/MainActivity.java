package it.cnr.iit.sensapp;

import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daasuu.cat.CountAnimationTextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.cnr.iit.sensapp.controllers.InstagramStats;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import it.mcampana.instadroid.RequestListener;
import it.mcampana.instadroid.endpoints.UsersEndpoint;
import it.mcampana.instadroid.model.Media;
import it.mcampana.instadroid.model.User;

import static it.cnr.iit.sensapp.controllers.InstagramStats.MEDIA_CAROUSEL;
import static it.cnr.iit.sensapp.controllers.InstagramStats.MEDIA_TYPES;

public class MainActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = PreferencesController.getInstagramUser(this);

        ((ScrollView)findViewById(R.id.scrollView)).setSmoothScrollingEnabled(true);

        fillProfileResume();
        tagsChart();
    }

    private void fillProfileResume(){

        if(user != null) {

            Picasso.with(this)
                    .load(user.profilePicture)
                    .transform(new CircleTransformation())
                    .into((ImageView) findViewById(R.id.profile_image));

            String nickname = "(" + user.username + ")";

            ((TextView)findViewById(R.id.name)).setText(user.fullName);
            ((TextView)findViewById(R.id.nickname)).setText(nickname);

            ((CountAnimationTextView) findViewById(R.id.posts_counter))
                    .setInterpolator(new AccelerateInterpolator())
                    .countAnimation(0, user.counts.media);
            ((CountAnimationTextView) findViewById(R.id.followers_counter))
                    .setInterpolator(new AccelerateInterpolator())
                    .countAnimation(0, user.counts.followedBy);
            ((CountAnimationTextView) findViewById(R.id.following_counter))
                    .setInterpolator(new AccelerateInterpolator())
                    .countAnimation(0, user.counts.follows);
        }
    }

    private void tagsChart(){
        final PieChart tagsChart = findViewById(R.id.tags_chart);
        final PieChart mediaChart = findViewById(R.id.content_type_chart);

        int nLastPosts = 20;
        int nMostUsedTags = 4;

        TextView title = findViewById(R.id.tags_chart_title);
        String titleString = "The " + nMostUsedTags + " " + getString(R.string.tags_stats_title) +
                " " + nLastPosts + " " + getString(R.string.tags_stats_title_2);
        title.setText(titleString);

        title = findViewById(R.id.content_type_chart_title);
        titleString = getString(R.string.types_stats_title) + " " + nLastPosts + " "
                + getString(R.string.tags_stats_title_2);
        title.setText(titleString);

        UsersEndpoint.getSelfRecentMedia(this,
                PreferencesController.getInstagramToken(this), null, null,
                nLastPosts, new RequestListener<List<Media>>() {
                    @Override
                    public void onResponse(List<Media> response) {

                        List<String> tags = new ArrayList<>();
                        List<String> types = new ArrayList<>();

                        for(Media media : response){
                            tags.addAll(media.tags);

                            if(media.type.equals(InstagramStats.MEDIA_IMAGE) &&
                                    media.carouselMedia != null)
                                types.add(MEDIA_CAROUSEL);
                            else
                                types.add(media.type);
                        }

                        showRecentTagsChart(tagsChart, tags);
                        showRecentContentTypeChart(mediaChart, types);
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void showRecentTagsChart(PieChart chart, List<String> tags) {

        chart.setTransparentCircleRadius(50f);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setRotationEnabled(false);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chart.getDescription().setEnabled(false);

        chart.setUsePercentValues(true);
        chart.setHoleRadius(40f);

        Legend l = chart.getLegend();
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        l.setTextSize(15f);

        chart.offsetLeftAndRight(50);

        chart.setDrawEntryLabels(false);

        List<InstagramStats.TagFrequency> tagsFrequency = InstagramStats.getMostUsedTags(tags, 4);

        List<PieEntry> vals = new ArrayList<>();
        for(InstagramStats.TagFrequency tagFrequency : tagsFrequency){
            vals.add(new PieEntry(tagFrequency.frequency, tagFrequency.tag));
        }

        PieDataSet dataSet = new PieDataSet(vals, null);
        dataSet.setColors(Color.parseColor("#DD6E42"),
                Color.parseColor("#4E598C"), Color.parseColor("#FF5964"),
                Color.parseColor("#99D5C9"), Color.parseColor("#35A7FF"));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new CustomPercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();

    }

    private void showRecentContentTypeChart(PieChart chart, List<String> types){

        chart.setTransparentCircleRadius(50f);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setRotationEnabled(false);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chart.getDescription().setEnabled(false);

        chart.setUsePercentValues(true);
        chart.setHoleRadius(40f);

        Legend l = chart.getLegend();
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        l.setTextSize(15f);

        chart.offsetLeftAndRight(-100);

        chart.setDrawEntryLabels(false);

        HashMap<String, Integer> typesFrequency = InstagramStats.getMostPublishedElementTypes(types);

        List<PieEntry> vals = new ArrayList<>();
        for(String type : MEDIA_TYPES){
            vals.add(new PieEntry(typesFrequency.get(type), type));
        }

        PieDataSet dataSet = new PieDataSet(vals, null);
        dataSet.setColors(Color.parseColor("#DD6E42"),
                Color.parseColor("#4E598C"), Color.parseColor("#FF5964"));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new CustomPercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }

    private class CustomPercentFormatter implements IValueFormatter {


        private DecimalFormat mFormat;

        public CustomPercentFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0");
        }

        public CustomPercentFormatter(DecimalFormat format) {
            this.mFormat = format;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {

            if (value == 0.0f)
                return "";

            return mFormat.format(value) + " %";
        }
    }
}
