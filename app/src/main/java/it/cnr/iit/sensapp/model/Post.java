package it.cnr.iit.sensapp.model;

import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static it.cnr.iit.sensapp.model.Post.TYPE.FB;
import static it.cnr.iit.sensapp.model.Post.TYPE.TW;

public class Post {

    enum TYPE{FB, TW}

    public String id;
    public List<String> tags = new ArrayList<>();
    public List<User> taggedUsers = new ArrayList<>();
    public Location location;
    public List<User> likes = new ArrayList<>();
    public List<Comment> comments = new ArrayList<>();
    public TYPE type;

    public Post(Tweet tweet){

        this.id = String.valueOf(tweet.id);

        if(tweet.entities != null){

            if(tweet.entities.hashtags != null) {
                for (HashtagEntity hashtagEntity : tweet.entities.hashtags)
                    this.tags.add(hashtagEntity.text);
            }

            if(tweet.entities.userMentions != null){

                for(MentionEntity mentionEntity : tweet.entities.userMentions)
                    this.taggedUsers.add(new User(String.valueOf(mentionEntity.id),
                            mentionEntity.name));
            }
        }

        if(tweet.coordinates != null){
            this.location = new Location(tweet.coordinates.getLatitude(),
                    tweet.coordinates.getLongitude());
        }

        if(tweet.place != null){
            if(location == null) location = new Location();

            location.country = tweet.place.country;
            location.name = tweet.place.fullName;
            if(tweet.place.placeType.equals("city")) location.city = tweet.place.name;
        }

        for(int i=0; i<tweet.favoriteCount; i++) likes.add(new User());
        for(int i=0; i<tweet.retweetCount; i++) comments.add(new Comment());

        this.type = TW;
    }

    public Post(JSONObject jsonObject) throws JSONException{

        if(jsonObject.has("id")) this.id = jsonObject.getString("id");

        if(jsonObject.has("place"))
            location = new Location(jsonObject.getJSONObject("place"));

        if(jsonObject.has("likes")){

            JSONArray likesObject = jsonObject.getJSONObject("likes").getJSONArray("data");

            for(int i=0; i<likesObject.length(); i++)
                this.likes.add(new User(likesObject.getJSONObject(i)));
        }

        if(jsonObject.has("comments")){

            JSONArray commentsObject = jsonObject.getJSONObject("comments").getJSONArray("data");

            for(int i=0; i < commentsObject.length(); i++)
                this.comments.add(new Comment(commentsObject.getJSONObject(i)));
        }

        if(jsonObject.has("message_tags")){

            JSONArray msgTagsArray = jsonObject.getJSONArray("message_tags");

            for(int i=0; i < msgTagsArray.length(); i++)

                if(msgTagsArray.getJSONObject(i).getString("type").equals("user"))
                    this.taggedUsers.add(new User(msgTagsArray.getJSONObject(i)));
        }

        this.type = FB;
    }

    public class User{
        public String id;
        public String name;

        public User(){}

        public User(String id, String name){
            this.id = id;
            this.name = name;
        }

        public User(JSONObject jsonObject) throws JSONException{

            this.id = jsonObject.getString("id");
            this.name = jsonObject.getString("name");
        }
    }

    public class Location{
        public double latitude, longitude;
        public String city, country, name;

        public Location(){}

        public Location(double latitude, double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Location(JSONObject jsonObject) throws JSONException{

            JSONObject location = jsonObject.getJSONObject("location");

            if(location.has("city")) city = location.getString("city");
            if(location.has("country")) country = location.getString("country");
            if(jsonObject.has("name")) name = jsonObject.getString("name");
            if(location.has("latitude")) latitude = location.getDouble("latitude");
            if(location.has("longitude")) longitude = location.getDouble("longitude");
        }
    }

    public class Comment{
        public String message;
        public User user;

        public Comment(){}

        public Comment(JSONObject jsonObject) throws JSONException{

            this.message = jsonObject.getString("message");
            this.user = new User(jsonObject.getJSONObject("from"));
        }
    }
}
