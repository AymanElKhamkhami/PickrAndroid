package dmcs.pickr.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dmcs.pickr.activities.Alert;

/**
 * Created by Ayman on 04/12/2016.
 */
public class AlarmSchedulerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int alarmId = intent.getExtras().getInt("ALARM_ID");
        String senderEmail = intent.getExtras().getString("SENDER_EMAIL");
        String senderName = intent.getExtras().getString("SENDER_NAME");
        String pickupTime = intent.getExtras().getString("PICKUP_TIME");

        Intent alarmIntent = new Intent(context, Alert.class);
        alarmIntent.putExtra("ALARM_ID", alarmId);
        alarmIntent.putExtra("SENDER_EMAIL", senderEmail);
        alarmIntent.putExtra("SENDER_NAME", senderName);
        alarmIntent.putExtra("PICKUP_TIME", pickupTime);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);

        //Toast.makeText(context, "Alert re-started....", Toast.LENGTH_LONG).show();
        Log.d("Alert:","Alert started....");
    }
}
