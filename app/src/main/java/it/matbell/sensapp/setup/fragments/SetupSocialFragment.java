package it.matbell.sensapp.setup.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import it.matbell.sensapp.R;
import it.matbell.sensapp.setup.SetupActivity;

/**
 * Created by mattia on 16/12/2017.
 */
public class SetupSocialFragment extends Fragment{

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    private static final String[] PERMISSIONS = {"email", "user_posts"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_social_fragment, container, false);

        callbackManager = CallbackManager.Factory.create();

        loginButton = rootView.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(PERMISSIONS));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FB", "Access token: "+loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e("FB", "FB On cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("FB", "FB On error: "+exception);
            }
        });

        printKeyHash();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public CallbackManager getCallbackManager(){ return this.callbackManager; }

    private void printKeyHash(){
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity()
                            .getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", getActivity().getPackageName()+": "
                        +Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

            ((SetupActivity)getActivity()).onSetupComplete();

        } catch (PackageManager.NameNotFoundException e) {
            Log.d(getClass().getName(), e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
    }
}
