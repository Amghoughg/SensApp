package it.cnr.iit.sensapp.setup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.morphingbutton.MorphingButton;

import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.setup.AutoStartController;
import it.cnr.iit.sensapp.setup.SetupActivity;
import it.cnr.iit.sensapp.setup.Utils;

/**
 * Created by mattia on 18/01/18.
 */
public class SetupAppStatisticsFragment extends Fragment {

    private MorphingButton grantButton;
    public static final int BUTTON_ANIM_DURATION = 500;
    private boolean next = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_appstats_fragment, container, false);

        grantButton = rootView.findViewById(R.id.grant_permission_button);

        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!next) {

                    openSettingsActivity();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Utils.enableGreen(getActivity(), grantButton, getContext().getResources()
                                    .getString(R.string.next));

                            next = true;

                        }
                    }, BUTTON_ANIM_DURATION);

                }else{
                    if(AutoStartController.requestIsNeeded())
                        ((SetupActivity)getActivity()).nextFragment(1);
                    else{
                        Intent intent = new Intent(getContext(),
                                ((SetupActivity)getActivity()).socialLoginClass);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Utils.morphToSquare(getActivity(), grantButton, BUTTON_ANIM_DURATION,
                    getContext().getResources().getString(R.string.grant_permissions));
        }
    }

    private void openSettingsActivity(){

        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

}
