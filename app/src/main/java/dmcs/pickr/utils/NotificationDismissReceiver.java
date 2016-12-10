package dmcs.pickr.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Ayman on 04/12/2016.
 */
public class NotificationDismissReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        int notifId = intent.getExtras().getInt("NotificationID");

        Log.d("DISMISSED NOTIFICATION",String.valueOf(notifId));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,  0, new Intent(), 0);
        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
        mb.setContentIntent(resultPendingIntent);
    }
}
