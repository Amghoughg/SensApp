package it.cnr.iit.sensapp.setup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

import it.cnr.iit.sensapp.MainTwitter;
import it.cnr.iit.sensapp.PreferencesController;
import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import retrofit2.Call;

public class TwitterLoginActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_twitter_activity);

        avi = findViewById(R.id.avi);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();

        Twitter.initialize(config);

        loginButton = findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                fetchUserProfile();
                System.out.println("Token: "+result.data.getAuthToken().token);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchUserProfile(){

        avi.show();

        final Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);

        user.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {
                fillViews(userResult.data);
            }

            @Override
            public void failure(TwitterException exc) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc);
            }
        });
    }

    private void fillViews(User user){

        findViewById(R.id.title).setVisibility(View.INVISIBLE);
        findViewById(R.id.body).setVisibility(View.INVISIBLE);

        ((TextView)findViewById(R.id.connected_label)).setText(user.name);

        Picasso.with(this)
                .load(user.profileImageUrl.replace("_normal", ""))
                .transform(new CircleTransformation())
                .into((ImageView)findViewById(R.id.profile_image),
                        new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        avi.hide();
                        findViewById(R.id.cardview).setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onSetupComplete();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onError() {}
        });
    }

    //==============================================================================================
    // Setup complete
    //==============================================================================================
    public void onSetupComplete(){

        PreferencesController.storeSetupComplete(this);

        Intent intent = new Intent(this, MainTwitter.class);
        intent.putExtra("setupComplete", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
