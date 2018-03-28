package it.cnr.iit.sensapp.setup.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.dd.morphingbutton.MorphingButton;

import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.setup.SetupActivity;
import it.cnr.iit.sensapp.setup.Utils;

public class SetupTermsFragment extends Fragment {

    private static final int BUTTON_ANIM_DURATION = 500;
    private static final String URL = "http://mcampana.iit.cnr.it/sensing/terms/";

    private MorphingButton grantButton;
    private WebView webView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_webview_fragment, container, false);

        grantButton = rootView.findViewById(R.id.grant_permission_button);
        webView = rootView.findViewById(R.id.webview);

        grantButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(getActivity() != null)
                    ((SetupActivity)getActivity()).onOperationSuccess(grantButton,
                            BUTTON_ANIM_DURATION);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Activity main = getActivity();
                        if(main != null)
                            ((SetupActivity) main).nextFragment(1);
                    }
                }, BUTTON_ANIM_DURATION*2);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getUserVisibleHint()) {

            if(getContext() != null && getContext().getResources() != null)
                Utils.morphToSquare(getActivity(), grantButton, BUTTON_ANIM_DURATION,
                        getContext().getResources().getString(R.string.accept));

            webView.loadUrl(URL);
        }
    }
}
