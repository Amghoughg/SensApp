package it.cnr.iit.sensapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.cnr.iit.sensapp.askcontroller.ForegroundService;
import it.matbell.ask.ASK;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG ,"Intent received");

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG ,"Starting ASK");
            /*ASK ask = new ASK(context, context.getString(R.string.ask_conf));
            ask.start();*/
            Intent startIntent = new Intent(context, ForegroundService.class);
            context.startService(startIntent);
        }
    }
}
