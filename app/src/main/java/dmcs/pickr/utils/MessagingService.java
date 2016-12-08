package dmcs.pickr.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import dmcs.pickr.Alert;
import dmcs.pickr.Login;
import dmcs.pickr.R;

/**
 * Created by Ayman on 04/12/2016.
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    Bitmap bitmap;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.

        //POST request content example:
        //{
        //"to": "/topics/testTopic",
        //        "content-available": true,
        //        "priority":"high",
        //        "data":{
        //          "title":"Soon ride!",
        //          "text":"Your ride with Ayman is 2 hours away",
        //          "type":"hh"
        //        }
        //}

        //The data tag should be used so that the notification is handeled by onMessageReceived() even in background.

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("text"));//remoteMessage.getNotification().getBody());



        //message will contain the Push Message
        //String message = remoteMessage.getData().get("message") + "-----";
        String message = remoteMessage.getData().get("text");//remoteMessage.getNotification().getBody();
        String title = remoteMessage.getData().get("title");//remoteMessage.getNotification().getTitle();
        //imageUri will contain URL of the image to be displayed with Notification
        String imageUri = remoteMessage.getData().get("image");
        //If the key AnotherActivity has  value as True then when the user taps on notification, in the app AnotherActivity will be opened.
        //If the key AnotherActivity has  value as False then when the user taps on notification, in the app MainActivity will be opened.

        String type = remoteMessage.getData().get("type");
        String date = remoteMessage.getData().get("date");
        String TrueOrFlase = remoteMessage.getData().get("AnotherActivity");

        //To get a Bitmap image from the URL received
        //bitmap = getBitmapfromUrl(imageUri);
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.notification_icon);

        if(type.equals("alert")) {
            sendAlert();
        }

        else
            sendNotification(title, message, bitmap, TrueOrFlase, type, date);
    }


    private void sendAlert() {
        Intent alertIntent = new Intent(this, Alert.class);
        //sendBroadcast(alertIntent);
        alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertIntent);

    }

    private void sendNotification(String messageTitle, String messageBody, Bitmap image, String TrueOrFalse, String type, String date) {

        Intent openIntent = new Intent(this, Login.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.putExtra("AnotherActivity", TrueOrFalse);
        PendingIntent piOpen = PendingIntent.getActivity(this, 0 /* Request code */, openIntent, PendingIntent.FLAG_ONE_SHOT);

        //set intents and pending intents to call service on click of "snooze" action button of notification
        Intent snoozeIntent = new Intent();
        snoozeIntent.setAction("ACTION_SNOOZE");
        PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0);

        //set intents and pending intents to call service on click of "dismiss" action button of notification
        Intent dismissIntent = new Intent(this, NotificationDismissReceiver.class);
        PendingIntent piDismiss = PendingIntent.getBroadcast(this, 0, dismissIntent, 0);
        //dismissIntent.setAction(ACTION_DISMISS);
        //PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        //To make the notification expandable
        /*NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(messageTitle);
        if(type.equals("request")) {
            inboxStyle.addLine(messageBody + " asks to join your ride efregrtrhyhujknmilmuol,oñ,ipoñ,y");
            inboxStyle.addLine("on " + date + ".");

        }*/

        String text = "";

        if(type.equals("request")) {
            text = messageBody + " asks to join your ride on " + date + ".";
        }

        else if(type.equals("approved")) {
            text = messageBody + " accepted your ride request on " + date + "." ;
        }

        else if(type.equals("rejected")) {
            text = messageBody + " couldn't accept your ride request on " + date + "." ;
        }


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //.setLargeIcon(image)/*Notification icon image*/
                .setSmallIcon(R.drawable.notification_icon) //this is will appear as a tiny icon on the side if large icon is set.
                .setColor(getResources().getColor(R.color.colorNotification))
                .setContentTitle(messageTitle)
                .setContentText(text)
                //.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image))/*Notification with Image*/
                //.setAutoCancel(false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setOngoing(true) // this doesnt allow the notification to be swiped away
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setCategory("CATEGORY_CALL")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.ic_alarm_off, "DISMISS", piDismiss)
                .addAction(R.drawable.ic_snooze, "SNOOZE", piSnooze)
                .setContentIntent(piOpen);



        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        /*new Thread() {
            public void run() {

                try {
                    sleep(sharedPref.getInt("alarm_duration",2000) * DateUtils.SECOND_IN_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mNotificationManager.cancel(notificationId);
            }
        }.start();*/
    }

    /*
    *To get a Bitmap image from the URL received
    * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }
}
