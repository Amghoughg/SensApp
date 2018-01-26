package it.cnr.iit.sensapp.setup.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.morphingbutton.MorphingButton;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import it.cnr.iit.sensapp.PreferencesController;
import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.setup.AutoStartController;
import it.cnr.iit.sensapp.setup.SetupActivity;
import it.cnr.iit.sensapp.setup.Utils;
import it.cnr.iit.sensapp.setup.instagramlogin.InstagramApp;
import it.cnr.iit.sensapp.setup.instagramlogin.InstagramSession;
import it.cnr.iit.sensapp.utils.CircleTransformation;
import it.mcampana.instadroid.RequestListener;
import it.mcampana.instadroid.endpoints.UsersEndpoint;
import it.mcampana.instadroid.model.User;

/**
 * Created by mattia on 24/01/18.
 */

public class SetupInstagramFragment extends Fragment {

    private static final String CLIENT_ID = "288a3323226b423e9fccded0883c7575";
    private static final String CLIENT_SECRET = "e813c8b188444af98c483ce514bf269e";
    private static final String CALLBACK_URL = "http://mcampana.iit.cnr.it/sensing/";

    private static final int BUTTON_ANIM_DURATION = 500;

    private MorphingButton grantButton;
    private WebView webView;
    private InstagramApp instagramApp;
    private TextView body, title;
    private ImageView profileImage;
    private CardView cardView;
    private TextView connectedTV;
    private AVLoadingIndicatorView loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_instagram_fragment, container, false);

        grantButton = rootView.findViewById(R.id.grant_permission_button);
        profileImage = rootView.findViewById(R.id.profile_image);
        cardView = rootView.findViewById(R.id.cardview);
        connectedTV = rootView.findViewById(R.id.connected_label);
        loader = rootView.findViewById(R.id.avi);
        body = rootView.findViewById(R.id.body);
        title = rootView.findViewById(R.id.title);
        webView = rootView.findViewById(R.id.webview);

        instagramApp = new InstagramApp(getContext(), webView, CLIENT_ID, CLIENT_SECRET,
                CALLBACK_URL);
        instagramApp.setListener(listener);

        grantButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                title.setVisibility(View.INVISIBLE);
                body.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                grantButton.setVisibility(View.INVISIBLE);
                if(!instagramApp.hasAccessToken()) instagramApp.authorize(loader);
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && getContext() != null && getContext().getResources() != null) {
            Utils.morphToSquare(getActivity(), grantButton, BUTTON_ANIM_DURATION,
                    getContext().getResources().getString(R.string.instagram_login));
        }
    }

    private InstagramApp.OAuthAuthenticationListener listener = new InstagramApp.OAuthAuthenticationListener() {
        @Override
        public void onSuccess() {

            final InstagramSession session = new InstagramSession(getContext());
            Log.d("IO", "Access token: "+session.getAccessToken());
            webView.setVisibility(View.INVISIBLE);
            grantButton.setVisibility(View.VISIBLE);
            body.setVisibility(View.INVISIBLE);
            cardView.setVisibility(View.VISIBLE);



            UsersEndpoint.getSelf(getActivity(), session.getAccessToken(), new RequestListener<User>() {
                @Override
                public void onResponse(User response) {

                    connectedTV.setText(response.fullName);
                    Picasso.with(getActivity()).load(response.profilePicture).transform(
                            new CircleTransformation()).into(profileImage);

                    PreferencesController.storeInstagramProfile(getContext(), response,
                            session.getAccessToken());

                    Picasso.with(getContext())
                            .load(response.profilePicture)
                            .transform(new CircleTransformation())
                            .into(profileImage, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    loader.hide();

                                    ((SetupActivity)getActivity()).onOperationSuccess(
                                            grantButton, BUTTON_ANIM_DURATION);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            //if(AutoStartController.requestIsNeeded())
                                                //((SetupActivity)getActivity()).nextFragment(1);
                                            //else
                                                ((SetupActivity)getActivity()).onSetupComplete();

                                        }
                                    }, BUTTON_ANIM_DURATION*4);
                                }

                                @Override
                                public void onError() {

                                }
                            });
                }

                @Override
                public void onError(String error) {

                }
            });

        }

        @Override
        public void onFail(String error) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            ((SetupActivity)getActivity()).onOperationFailure(grantButton, BUTTON_ANIM_DURATION,
                    BUTTON_ANIM_DURATION*3,
                    getContext().getResources().getString(R.string.grant_permissions));
        }
    };
}
