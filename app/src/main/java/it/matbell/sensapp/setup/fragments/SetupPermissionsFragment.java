package it.matbell.sensapp.setup.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.morphingbutton.MorphingButton;

import it.matbell.sensapp.R;
import it.matbell.sensapp.setup.SetupActivity;
import it.matbell.sensapp.setup.Utils;

/**
 * Created by mattia on 14/12/2017.
 */

public class SetupPermissionsFragment extends Fragment {

    private MorphingButton grantButton;
    private static final int BUTTON_ANIM_DURATION = 500;

    private static final String[] RUNTIME_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR
    };
    private static final int REQ_CODE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.setup_permissions, container, false);


        grantButton = rootView.findViewById(R.id.grant_permission_button);

        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });


        return rootView;
    }

    public MorphingButton getGrantButton(){ return grantButton; }
    public int getAnimationDuration(){ return BUTTON_ANIM_DURATION; }


    private void checkPermissions(){

        boolean require = false;

        for(String permission : RUNTIME_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(
                    getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                require = true;
                break;
            }
        }

        if(require)
            ActivityCompat.requestPermissions(getActivity(), RUNTIME_PERMISSIONS, REQ_CODE);
        else
            ((SetupActivity)getActivity()).nextPage(1);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getUserVisibleHint()) {
            Utils.morphToSquare(getActivity(), grantButton, BUTTON_ANIM_DURATION,
                    getContext().getResources().getString(R.string.grant_permissions));
        }
    }
}

