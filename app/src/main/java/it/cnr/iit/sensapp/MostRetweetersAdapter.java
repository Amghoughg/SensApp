package it.cnr.iit.sensapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.User;

import java.util.Collections;
import java.util.List;

import it.cnr.iit.sensapp.utils.CircleTransformation;

public class MostRetweetersAdapter extends RecyclerView.Adapter<MostRetweetersAdapter.MyViewHolder>{

    private List<User> retweeters;
    private List<String> retweetersIds;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView retweeterImage;
        TextView retweeterNameTV, numberOfRetweetsTV;

        MyViewHolder(View view) {
            super(view);

            retweeterImage = view.findViewById(R.id.retweeter_profile_image);
            retweeterNameTV = view.findViewById(R.id.retweeter_name);
            numberOfRetweetsTV = view.findViewById(R.id.number_of_retweets);
        }
    }

    public MostRetweetersAdapter(Context context, List<User> retweeters, List<String> retweetersIds) {
        this.context = context;
        this.retweeters = retweeters;
        this.retweetersIds = retweetersIds;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.twitter_most_retweeters_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = retweeters.get(position);
        holder.retweeterNameTV.setText(user.name);

        Picasso.with(context)
                .load(user.profileImageUrl.replace("_normal", ""))
                .transform(new CircleTransformation())
                .into(holder.retweeterImage);

        String numberRetweets = Collections.frequency(retweetersIds, user.idStr) + " retweets";
        holder.numberOfRetweetsTV.setText(numberRetweets);
    }

    @Override
    public int getItemCount() {
        return (retweeters != null) ? retweeters.size() : 0;
    }

}
