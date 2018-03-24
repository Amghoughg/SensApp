package it.cnr.iit.sensapp.askcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import it.cnr.iit.sensapp.R;
import it.matbell.ask.ASK;

public class DisplayStatusBCastReceiver extends BroadcastReceiver {

    private static final String TAG = "DisplayBCastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent != null && intent.getAction() != null){

            ASK ask = new ASK(context, context.getString(R.string.ask_conf));

            switch (intent.getAction()){

                case Intent.ACTION_SCREEN_OFF:
                    Log.i(TAG,"Screen went OFF");
                    ask.stop();
                    break;

                case Intent.ACTION_USER_PRESENT:
                    Log.i(TAG,"User is present");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ask.start();
                        }
                    }, 5000);
                    break;

            }
        }
    }
}
