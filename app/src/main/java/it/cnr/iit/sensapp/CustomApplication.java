package it.cnr.iit.sensapp;

import android.app.Application;

import com.twitter.sdk.android.core.Twitter;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

@AcraCore(buildConfigClass = BuildConfig.class, reportFormat= StringFormat.JSON)
@AcraHttpSender(
        httpMethod = HttpSender.Method.PUT,
        uri = "http://mcampana.iit.cnr.it:5984/acra-mydigitalfootprint/_design/acra-storage/_update/report",
        basicAuthLogin = "mydigitalfootprint",
        basicAuthPassword = "mydigitalfootprint"
)
public class CustomApplication extends Application {

    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
        Twitter.initialize(this);
    }

}
