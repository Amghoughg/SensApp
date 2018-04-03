package it.cnr.iit.sensapp.askcontroller;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import it.cnr.iit.sensapp.MainActivity;
import it.cnr.iit.sensapp.R;
import it.matbell.ask.ASK;

public class ForegroundService extends Service {

    public static final String TAG = "FGService";

    int mStartMode = START_STICKY;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used

    private DisplayStatusBCastReceiver receiver;

    @Override
    public void onCreate() {
        // The service is being created
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        Log.d(TAG, "Start service");

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationController.createChannel(getApplicationContext());
            builder = new Notification.Builder(this, NotificationController.CHANNEL_ID);
        }else
            builder = new Notification.Builder(this);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        builder.setSmallIcon(R.mipmap.ic_foot)
                .setContentText("Sensing is on")
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(101, builder.build());

        /*IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);

        if(receiver == null) {
            receiver = new DisplayStatusBCastReceiver();
            registerReceiver(receiver, screenStateFilter);
        }*/

        ASK ask = new ASK(getApplicationContext(), getApplicationContext().getString(R.string.ask_conf));
        ask.start();

        return mStartMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        //TaskController.getInstance().stopTask();
        //ASK ask = new ASK(getApplicationContext(),
        //        getApplicationContext().getString(R.string.ask_conf));
        //ask.stop();
        unregisterReceiver(receiver);
    }
}
