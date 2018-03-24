package it.cnr.iit.sensapp.askcontroller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class NotificationController {

    public static final String CHANNEL_ID = "it.cnr.iit.sensapp.channelID";
    public static final String CHANNEL_NAME = "MyDigitalFootprint";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createChannel(Context context){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
                createNotificationChannel(channel);
    }
}
