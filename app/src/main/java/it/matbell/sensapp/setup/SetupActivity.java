package it.matbell.sensapp.setup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.dd.morphingbutton.MorphingButton;

import it.matbell.sensapp.MainActivity;
import it.matbell.sensapp.R;
import it.matbell.sensapp.setup.fragments.SetupAutoStartFragment;
import it.matbell.sensapp.setup.fragments.SetupPermissionsFragment;
import it.matbell.sensapp.setup.fragments.SetupPowerFragment;
import it.matbell.sensapp.setup.fragments.SetupSocialFragment;

public class SetupActivity extends AppCompatActivity {

    public static final int REQ_IGNORE_BATTERY_OPT = 1;
    public static final String SETUP_PREF_KEY = "setupComplete";

    private Fragment[] fragments = new Fragment[4];

    private CustomViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_activity);

        fragments[0] = new SetupPermissionsFragment();
        fragments[1] = new SetupPowerFragment();
        fragments[2] = new SetupAutoStartFragment();
        fragments[3] = new SetupSocialFragment();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setScrollDurationFactor(5);
    }

    //==============================================================================================
    // ViewPager
    //==============================================================================================
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

    public void nextPage(int step){
        mPager.setCurrentItem(mPager.getCurrentItem() + step);
    }

    //==============================================================================================
    // MorphingButton management
    //==============================================================================================
    private void onOperationFailure(final MorphingButton button, final int duration, int delay,
                                    final String idleString){

        Utils.morphToFailure(this, button, duration);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.morphToSquare(SetupActivity.this, button, duration, idleString);

            }
        }, delay);
    }

    private void onOperationSuccess(final MorphingButton button, final int duration){

        Utils.morphToSuccess(this, button, duration);
    }

    //==============================================================================================
    // Permissions
    //==============================================================================================
    private boolean allPermissionsGranted(int[] granted){

        for(int perm : granted)
            if(perm == PackageManager.PERMISSION_DENIED) return false;

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        final SetupPermissionsFragment fragment = (SetupPermissionsFragment) fragments[0];

        switch (requestCode) {
            case 0: {

                if (allPermissionsGranted(grantResults)) {

                    onOperationSuccess(fragment.getGrantButton(), fragment.getAnimationDuration());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                nextPage(1);
                            }
                        }, fragment.getAnimationDuration() * 2);
                    }

                } else {

                    onOperationFailure(fragment.getGrantButton(), fragment.getAnimationDuration(),
                            fragment.getAnimationDuration()*3,
                            getResources().getString(R.string.grant_permissions));
                }
            }
        }
    }

    //==============================================================================================
    // Avoid doze mode
    //==============================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(mPager.getCurrentItem() == 3)
            ((SetupSocialFragment)fragments[3]).getCallbackManager()
                .onActivityResult(requestCode, resultCode, data);

        else if (requestCode == REQ_IGNORE_BATTERY_OPT) {

            SetupPowerFragment fragment = (SetupPowerFragment) fragments[1];

            switch (resultCode){

                case RESULT_OK:

                    onOperationSuccess(fragment.getGrantButton(), fragment.getAnimationDuration());

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if(!AutoStartController.requestIsNeeded())
                                nextPage(2);
                            else
                                nextPage(1);
                        }
                    }, fragment.getAnimationDuration() * 2);

                    break;

                case RESULT_CANCELED:

                    onOperationFailure(fragment.getGrantButton(), fragment.getAnimationDuration(),
                            fragment.getAnimationDuration()*3,
                            getResources().getString(R.string.ignore_battery_opt));

                    break;
            }
        }
    }

    public void onSetupComplete(){

        getSharedPreferences(getApplication().getPackageName(),
                Context.MODE_PRIVATE).edit().putBoolean(SETUP_PREF_KEY, true).apply();
        startSensingKit();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(SETUP_PREF_KEY, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void startSensingKit(){
        /*AndroidSensingKit androidSensingKit = new AndroidSensingKit(this,
                getString(R.string.ask_conf));
        androidSensingKit.start();*/
    }
}
