package it.matbell.sensapp.setup.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.morphingbutton.MorphingButton;

import it.matbell.sensapp.R;
import it.matbell.sensapp.setup.AutoStartController;
import it.matbell.sensapp.setup.SetupActivity;
import it.matbell.sensapp.setup.Utils;

/**
 * Created by mattia on 15/12/17.
 */

public class SetupAutoStartFragment extends Fragment {

    private MorphingButton grantButton, finishButton;
    private static final int BUTTON_ANIM_DURATION = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_autostart, container, false);

        grantButton = rootView.findViewById(R.id.grant_permission_button);
        finishButton = rootView.findViewById(R.id.finish_button);

        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishButton.setEnabled(true);
                AutoStartController.requestAutostart(getContext());
                Utils.enableGreen(getActivity(), finishButton, getContext().getResources()
                        .getString(R.string.finish));
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetupActivity)getActivity()).onSetupComplete();
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Utils.morphToSquareSide(getActivity(), grantButton, BUTTON_ANIM_DURATION,
                    getContext().getResources().getString(R.string.open_settings));

            Utils.morphToGreenSquareSide(getActivity(), finishButton, BUTTON_ANIM_DURATION,
                    getContext().getResources().getString(R.string.finish));

            finishButton.setEnabled(false);
        }
    }
}
