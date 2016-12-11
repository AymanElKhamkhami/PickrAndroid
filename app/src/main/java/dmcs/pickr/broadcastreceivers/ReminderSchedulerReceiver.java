package dmcs.pickr.broadcastreceivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import dmcs.pickr.activities.Details;
import dmcs.pickr.activities.Login;
import dmcs.pickr.R;

/**
 * Created by Ayman on 04/12/2016.
 */
public class ReminderSchedulerReceiver extends BroadcastReceiver {

    private static final String NOTIF_CODE = "3";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Get intent attributes
        String senderName = intent.getExtras().getString("SENDER");
        String date = intent.getExtras().getString("DATE");
        int id = intent.getExtras().getInt("REQUEST ID");
        int reminderId = intent.getExtras().getInt("REMINDER ID");

        //Fire notification
        createNotification(context, "Reminder", senderName, date, id);

        //Disable alarm
        PendingIntent piDismiss = PendingIntent.getBroadcast(context.getApplicationContext(), reminderId /*My code set for reminder pending intents*/, new Intent().putExtra("NotificationID", Integer.valueOf(id + "1")), 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(piDismiss);

        Log.d("Reminder:","Reminder started....");
    }


    private void createNotification(Context context, String messageTitle, String messageBody, String rawDate, int id) {

        int notifId = Integer.parseInt(id + NOTIF_CODE + String.valueOf(ThreadLocalRandom.current().nextInt(0, 99)));

        Intent openIntent = new Intent(context, Login.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piOpen = PendingIntent.getActivity(context, 0 /* Request code */, openIntent, PendingIntent.FLAG_ONE_SHOT);

        //set intents and pending intents to call service on click of "dismiss" action button of notification
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        dismissIntent.putExtra("NotificationID", notifId);
        //The request code must be unique, otherwise the extras will be overriden
        PendingIntent piDismiss = PendingIntent.getBroadcast(context.getApplicationContext(), notifId/* Request code */, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //dismissIntent.setAction(ACTION_DISMISS);
        //PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        Intent detailsIntent = new Intent(context, Details.class);
        detailsIntent.putExtra("ID", id);
        PendingIntent piDetails = PendingIntent.getActivity(context, 0, detailsIntent, 0);


        String text = "";
        String[] dateElements = splitDate(rawDate);
        text = "Don't forget your ride with " + messageBody + " at " + dateElements[2] + ".";


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon) //this will appear as a tiny icon on the side if a large icon is set.
                .setColor(context.getResources().getColor(R.color.colorNotification))
                .setContentTitle(messageTitle)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setOngoing(true) // this doesnt allow the notification to be swiped away
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setCategory("CATEGORY_CALL")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.ic_dismiss, "DISMISS", piDismiss)
                .addAction(R.drawable.ic_details, "DETAILS", piDetails)
                .setContentIntent(piOpen);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifId /* ID of notification */, notificationBuilder.build());


    }


    private String[] splitDate(String rawDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = null;
        String nameOfDay="", date ="", time="";

        try {
            d = format.parse(rawDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        nameOfDay = dayFormat.format(d);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        date = dateFormat.format(d);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        time = timeFormat.format(d);

        return new String[] {nameOfDay, date, time};
    }

}
