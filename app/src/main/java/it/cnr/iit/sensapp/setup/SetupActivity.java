package it.cnr.iit.sensapp.setup;

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

import it.cnr.iit.sensapp.R;
import it.cnr.iit.sensapp.setup.fragments.SetupAppStatisticsFragment;
import it.cnr.iit.sensapp.setup.fragments.SetupAutoStartFragment;
import it.cnr.iit.sensapp.setup.fragments.SetupPermissionsFragment;
import it.cnr.iit.sensapp.setup.fragments.SetupPowerFragment;
import it.cnr.iit.sensapp.setup.fragments.SetupPrivacyFragment;
import it.cnr.iit.sensapp.setup.fragments.SetupTermsFragment;

public class SetupActivity extends AppCompatActivity {

    public static final int REQ_IGNORE_BATTERY_OPT = 1;

    private Fragment[] fragments = new Fragment[5];

    private CustomViewPager mPager;

    public static Class<?> socialLoginClass = SocialLoginActivity.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_activity);

        Utils.printKeyHash(this);

        fragments[0] = new SetupTermsFragment();
        fragments[1] = new SetupPrivacyFragment();
        fragments[2] = new SetupPermissionsFragment();
        //fragments[3] = new SetupPowerFragment();
        fragments[3] = new SetupAppStatisticsFragment();
        //fragments[5] = new SetupInstagramFragment();
        fragments[4] = new SetupAutoStartFragment();

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

    public void nextFragment(int step){
        mPager.setCurrentItem(mPager.getCurrentItem() + step);
    }

    //==============================================================================================
    // MorphingButton management
    //==============================================================================================
    public void onOperationFailure(final MorphingButton button, final int duration, int delay,
                                    final String idleString){

        Utils.morphToFailure(this, button, duration);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.morphToSquare(SetupActivity.this, button, duration, idleString);

            }
        }, delay);
    }

    public void onOperationSuccess(final MorphingButton button, final int duration){

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

        final SetupPermissionsFragment fragment = (SetupPermissionsFragment) fragments[2];

        switch (requestCode) {
            case 0: {

                if (allPermissionsGranted(grantResults)) {

                    onOperationSuccess(fragment.getGrantButton(), fragment.getAnimationDuration());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                nextFragment(1);
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

        if (requestCode == REQ_IGNORE_BATTERY_OPT) {

            SetupPowerFragment fragment = (SetupPowerFragment) fragments[3];

            switch (resultCode){

                case RESULT_OK:

                    onOperationSuccess(fragment.getGrantButton(), fragment.getAnimationDuration());

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextFragment(1);

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


}
