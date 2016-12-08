package dmcs.pickr.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import dmcs.pickr.Alert;

/**
 * Created by Ayman on 04/12/2016.
 */
public class AlertSnoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, Alert.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        Toast.makeText(context, "Alert re-started....", Toast.LENGTH_LONG).show();
        Log.d("Alert:","Alert re-started....");
    }
}
