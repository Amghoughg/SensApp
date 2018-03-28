package it.cnr.iit.sensapp.setup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Arrays;
import java.util.List;

import it.cnr.iit.sensapp.MainActivity;
import it.cnr.iit.sensapp.PreferencesController;
import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.controllers.FacebookController;
import it.cnr.iit.sensapp.model.Post;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import it.matbell.ask.logs.FileLogger;
import it.matbell.ask.model.Loggable;
import retrofit2.Call;

public class SocialLoginActivity extends AppCompatActivity
        implements FacebookController.FacebookListener {

    private static final String TAG = "SocialLogin";

    private TwitterLoginButton twitterLoginButton;
    private AVLoadingIndicatorView avi;

    private TwitterLoginInfo twitterLoginInfo;
    private FacebookController.FacebookLoginInfo facebookLoginInfo;

    private boolean fblogin = false;

    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    // See https://developers.facebook.com/docs/facebook-login/permissions/
    private static final String[] FB_PERMISSIONS = {"email", "public_profile", "user_friends",
            "user_birthday", "user_actions.books", "user_actions.fitness", "user_actions.music",
            "user_actions.video", "user_likes", "user_photos", "user_posts", "user_tagged_places",
            "user_videos", "user_events"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_social_activity);

        avi = findViewById(R.id.avi);

        //TWITTER-----------------------------------------------------------------------------------
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();

        Twitter.initialize(config);
        twitterLoginButton = findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(twitterCallback);

        //FB----------------------------------------------------------------------------------------
        fbLoginButton = findViewById(R.id.fb_login_button);
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.setReadPermissions(Arrays.asList(FB_PERMISSIONS));
        fbLoginButton.registerCallback(callbackManager, facebookCallback);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(fblogin){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else {
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fillViews(String imageUrl, String name){

        findViewById(R.id.title).setVisibility(View.INVISIBLE);
        findViewById(R.id.body).setVisibility(View.INVISIBLE);

        ((TextView)findViewById(R.id.connected_label)).setText(name);

        Picasso.with(this)
                .load(imageUrl)
                .transform(new CircleTransformation())
                .into(findViewById(R.id.profile_image), new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        avi.hide();
                        findViewById(R.id.cardview).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {}
        });
    }

    //==============================================================================================
    // TWITTER
    //==============================================================================================

    private Callback<TwitterSession> twitterCallback = new Callback<TwitterSession>(){
        @Override
        public void success(Result<TwitterSession> result) {
            twitterLoginInfo = new TwitterLoginInfo();
            twitterLoginInfo.accessToken = result.data.getAuthToken().token;
            twitterLoginInfo.tokenSecret = result.data.getAuthToken().secret;
            Log.d(TAG, "TWITTER TOKEN: "+twitterLoginInfo.accessToken);

            // Do something with result, which provides a TwitterSession for making API calls
            downloadTwitterUser();

        }

        @Override
        public void failure(TwitterException exception) {
            // Do something on failure
        }
    };

    private void downloadTwitterUser(){

        avi.show();

        final Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);

        user.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {

                twitterLoginInfo.userId = userResult.data.id;
                twitterLoginInfo.email = userResult.data.email;
                twitterLoginInfo.name = userResult.data.name;
                twitterLoginInfo.screenName = userResult.data.screenName;

                FileLogger logger = FileLogger.getInstance();
                logger.setBaseDir(getResources().getString(R.string.app_name));
                logger.store("osn_accounts.csv", twitterLoginInfo, false);

                fillViews(userResult.data.profileImageUrl.replace("_normal", ""),
                        userResult.data.name);

                twitterLoginButton.setEnabled(false);
                twitterLoginButton.setBackgroundColor(
                        getResources().getColor(R.color.grey, getTheme()));
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });
    }

    private class TwitterLoginInfo implements Loggable{

        long userId;
        String name, screenName, email;
        String accessToken, tokenSecret;

        @Override
        public String getDataToLog() {
            return userId + FileLogger.SEP + name + FileLogger.SEP + screenName + FileLogger.SEP
                    + email + FileLogger.SEP + accessToken + FileLogger.SEP + tokenSecret;
        }
    }

    //==============================================================================================
    // FACEBOOK
    //==============================================================================================
    public void onFbLoginClicked(View view){
        avi.show();
        fblogin = true;
        fbLoginButton.performClick();
    }

    FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>(){

        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "FB Login success.");
            new FacebookController().downloadFacebookUserInfo(SocialLoginActivity.this);
        }

        @Override
        public void onCancel() {
            Log.e(TAG, "FB Login canceled.");
            fblogin = false;
            avi.hide();
        }

        @Override
        public void onError(FacebookException exception) {
            Log.e(TAG, "FB Login error: "+exception.toString());
        }
    };

    @Override
    public void onLastPostsDownloaded(List<Post> posts) {}

    @Override
    public void onFacebookLoginInfo(FacebookController.FacebookLoginInfo loginInfo) {

        this.facebookLoginInfo = loginInfo;

        if(loginInfo != null) {

            fillViews(loginInfo.profilePicture, loginInfo.fullName);
            findViewById(R.id.fb_login_custom_button).setEnabled(false);
            findViewById(R.id.fb_login_custom_button).setBackgroundColor(
                    getResources().getColor(R.color.grey, getTheme()));
        }

        fblogin = false;
    }

    @Override
    public void onFacebookVideoInfoDownloaded(int movies, int tvShows) {}

    @Override
    public void onFacebookMusicInfoDownloaded(int music) {}

    @Override
    public void onFacebookBooksInfoDownloaded(int books) {}

    @Override
    public void onFacebookUploadedPhotos(int photos) {}

    @Override
    public void onFacebookTaggedPhotos(int photos) {}

    @Override
    public void onFacebookUploadedVideos(int videos) {}

    @Override
    public void onFacebookTaggedVideos(int videos) {}

    @Override
    public void onFacebookPlacesDownloaded(String firstPlace, String secondPlace, String thirdPlace) {}

    @Override
    public void onFacebookLastEventDownload(String url) {}

    @Override
    public void onFacebookPagesDownload(int total, String name, String url) {}

    @Override
    public void onFacebookWalkDownload(int total) {}

    @Override
    public void onFacebookRunDownload(int total) {}

    @Override
    public void onFacebookBikeDownload(int total) {}

    //==============================================================================================
    // Setup complete
    //==============================================================================================
    public void onFinishClicked(View view){

        if(facebookLoginInfo == null && twitterLoginInfo == null){

            new FancyGifDialog.Builder(this)
                    .setTitle("No Social Networks?")
                    .setMessage("Please, log-in to at least one Social Network.")
                    .setPositiveBtnBackground("#966E5C")
                    .setPositiveBtnText("Ok")
                    .setNegativeBtnText("Cancel")
                    .setGifResource(R.drawable.alone)
                    .isCancellable(true)
                    .OnNegativeClicked(new FancyGifDialogListener() {
                        @Override
                        public void OnClick() {}
                    })
                    .build();

        }else {
            PreferencesController.storeSocialLogin(this, facebookLoginInfo != null,
                    (twitterLoginInfo != null) ? twitterLoginInfo.userId : -1);
            onSetupComplete();
        }
    }

    public void onSetupComplete(){

        PreferencesController.storeSetupComplete(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("setupComplete", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
