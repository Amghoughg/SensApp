package it.matbell.sensapp.setup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by mattia on 15/12/17.
 */
public class AutoStartController {

    public static boolean requestIsNeeded(){

        return Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                Build.MANUFACTURER.equalsIgnoreCase("vivo") ||
                Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.MANUFACTURER.equalsIgnoreCase("letv") ||
                Build.MANUFACTURER.equalsIgnoreCase("honor");
    }


    public static void requestAutostart(Context context){

        if(Build.MANUFACTURER.equalsIgnoreCase("oppo"))
            autoStartOppo(context);

        else if(Build.MANUFACTURER.equalsIgnoreCase("vivo"))
            autoLaunchVivo(context);

        else if(Build.MANUFACTURER.equalsIgnoreCase("xiaomi"))
            autoStartXiaomi(context);

        else if(Build.MANUFACTURER.equalsIgnoreCase("letv"))
            autoStartLetvi(context);

        else if(Build.MANUFACTURER.equalsIgnoreCase("honor"))
            autoStartHonor(context);

    }

    private static void autoStartXiaomi(Context context){

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        context.startActivity(intent);

    }

    private static void autoStartLetvi(Context context){

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"));
        context.startActivity(intent);

    }

    private static void autoStartHonor(Context context){

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        context.startActivity(intent);

    }

    private static void autoLaunchVivo(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                context.startActivity(intent);
            } catch (Exception ex) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                    context.startActivity(intent);
                } catch (Exception exx) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void autoStartOppo(Context context){

        try {
            Intent intent = new Intent();
            intent.setClassName("com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity");
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity");
                context.startActivity(intent);

            } catch (Exception ex) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.coloros.safecenter",
                            "com.coloros.safecenter.startupapp.StartupAppListActivity");
                    context.startActivity(intent);
                } catch (Exception exx) {

                }
            }
        }

    }

}
