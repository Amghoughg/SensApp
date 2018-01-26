package it.cnr.iit.sensapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import it.cnr.iit.sensapp.setup.SetupActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int WAIT_TIME_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Class<?> classToJump = (PreferencesController.isSetupComplete(
                        SplashScreen.this) ? MainActivity.class : SetupActivity.class);

                startActivity(new Intent(SplashScreen.this, classToJump));

            }
        }, WAIT_TIME_MS);
    }
}
