package dmcs.pickr.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import dmcs.pickr.activities.Details;
import dmcs.pickr.activities.Login;
import dmcs.pickr.R;
import dmcs.pickr.activities.MapSearch;
import dmcs.pickr.broadcastreceivers.AlarmSchedulerReceiver;
import dmcs.pickr.broadcastreceivers.NotificationDismissReceiver;
import dmcs.pickr.broadcastreceivers.ReminderSchedulerReceiver;

/**
 * Created by Ayman on 04/12/2016.
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private static final String NOTIF_CODE = "0";
    private static final String REMINDER_CODE = "1";
    private static final String ALARM_CODE = "2";

    Bitmap notifIcon;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.

        //POST request content example:
        //{
        //      "to": "/topics/tester1%gmail.com",
        //      "content-available": true,
        //      "priority":"high",
        //      "data":{
        //          "title":"Feedback",
        //          "text":"Ayman",
        //          "type":"approved",
        //          "sender":"ayman.khm%gmail.com",
        //          "request":"31",
        //          "date":"09/12/2016 22:43",
        //          "picture":"url"
        //      }
        //}

        //The data tag should be used so that the notification is handeled by onMessageReceived() even in background.

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("text"));//remoteMessage.getNotification().getBody());



        //message will contain the Push Message
        //String message = remoteMessage.getData().get("message") + "-----";
        String senderName = remoteMessage.getData().get("text");//remoteMessage.getNotification().getBody();
        String title = remoteMessage.getData().get("title");//remoteMessage.getNotification().getTitle();
        String senderEmail = remoteMessage.getData().get("sender").replace('%','@');
        //imageUri will contain URL of the image to be displayed with Notification
        String imageUri = remoteMessage.getData().get("image");
        //If the key AnotherActivity has  value as True then when the user taps on notification, in the app AnotherActivity will be opened.
        //If the key AnotherActivity has  value as False then when the user taps on notification, in the app MainActivity will be opened.

        String type = remoteMessage.getData().get("type");
        String date = remoteMessage.getData().get("date");
        int id = Integer.parseInt(remoteMessage.getData().get("request").toString());
        String pictureUrl = remoteMessage.getData().get("picture");

        String TrueOrFlase = remoteMessage.getData().get("AnotherActivity");


        Log.d("RECEIVED MESSAGE: ", date + " " + id);

        //To get a Bitmap image from the URL received
        //notifIcon = getBitmapfromUrl(imageUri);
        notifIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.notification_icon);

        /*if(type.equals("alert")) {
            sendAlert();
        }*/

        createNotification(title, senderName, notifIcon, TrueOrFlase, type, date, id, senderEmail, pictureUrl);
    }

    /*
    private void sendAlert() {
        Intent alertIntent = new Intent(this, Alert.class);
        //sendBroadcast(alertIntent);
        alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertIntent);

    }*/

    private void createNotification(String messageTitle, String messageBody, Bitmap image, String TrueOrFalse, String type, String rawDate, int id, String senderEmail, String picUrl) {

        int notifId = Integer.parseInt(id + NOTIF_CODE + String.valueOf(ThreadLocalRandom.current().nextInt(0, 99)));

        new DownloadImageTask(senderEmail).execute(picUrl);

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
        dismissIntent.putExtra("NotificationID", notifId);
        //The request code must be unique, otherwise the extras will be overriden
        PendingIntent piDismiss = PendingIntent.getBroadcast(getApplicationContext(), notifId /* Request code */, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //dismissIntent.setAction(ACTION_DISMISS);
        //PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        Intent detailsIntent = new Intent(this, Details.class);
        detailsIntent.putExtra("ID", id);
        PendingIntent piDetails = PendingIntent.getActivity(this, 0, detailsIntent, 0);

        //To make the notification expandable
        /*NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(messageTitle);
        if(type.equals("request")) {
            inboxStyle.addLine(messageBody + " asks to join your ride efregrtrhyhujknmilmuol,oñ,ipoñ,y");
            inboxStyle.addLine("on " + date + ".");

        }*/

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                //.setLargeIcon(image)/*Notification icon image*/
                .setSmallIcon(R.drawable.notification_icon) //this will appear as a tiny icon on the side if a large icon is set.
                .setColor(getResources().getColor(R.color.colorNotification))
                .setContentTitle(messageTitle)

                //.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image))/*Notification with Image*/
                //.setAutoCancel(false)
                .setOngoing(true) // this doesnt allow the notification to be swiped away
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setCategory("CATEGORY_CALL")
                .setPriority(NotificationCompat.PRIORITY_MAX);


        String text = "";
        String[] dateElements = splitDate(rawDate);

        if(type.equals("request")) {

            text = messageBody + " asks to join your ride on " + dateElements[0] + " " + dateElements[1] + ".";
            notificationBuilder
                    .addAction(R.drawable.ic_dismiss, "DISMISS", piDismiss)
                    .addAction(R.drawable.ic_details, "DETAILS", piDetails)
                    .setContentIntent(piOpen);
        }


        else if(type.equals("approved")) {

            text = messageBody + " accepted your ride request on " + dateElements[0] + " " + dateElements[1] + "." + " Your pick-up time is set to: " + dateElements[2] + ".";
            scheduleReminder(rawDate, messageBody, senderEmail, id);
            scheduleAlarm(rawDate, messageBody, senderEmail, id);

            notificationBuilder
                    .addAction(R.drawable.ic_dismiss, "DISMISS", piDismiss)
                    .addAction(R.drawable.ic_details, "DETAILS", piDetails)
                    .setContentIntent(piOpen);
        }


        else if(type.equals("rejected")) {

            text = messageBody + " couldn't accept your ride request on " + dateElements[0] + " " + dateElements[1] + "." ;

            notificationBuilder
                    .addAction(R.drawable.ic_dismiss, "DISMISS", piDismiss);
        }



        notificationBuilder
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifId /* ID of notification */, notificationBuilder.build());


    }


    // A reminder is fired 24 hours before the pickup time
    private void scheduleReminder(String datetime, String senderName, String senderEmail, int id) {
        int reminderId = Integer.parseInt(id + REMINDER_CODE + String.valueOf(ThreadLocalRandom.current().nextInt(0, 99)));

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = null;

        try {
            d = format.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Calendar pickupTime = Calendar.getInstance();
        pickupTime.setTimeInMillis(System.currentTimeMillis());
        pickupTime.setTime(d);


        long oneHourLeft = pickupTime.getTimeInMillis() - (3600 * 1000);

        if(System.currentTimeMillis() < oneHourLeft) {

            //Fire 24h before pickup time
            long triggerAt = pickupTime.getTimeInMillis() - (24 * 3600 * 1000);

            if(System.currentTimeMillis() > triggerAt) {
                triggerAt = System.currentTimeMillis() + 5000;
                Log.d("REMINDER TIME: ", "We're ahead of the reminder time, so we are showing a notification in 5s");

            }

            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent scheduleIntent = new Intent(this, ReminderSchedulerReceiver.class);
            scheduleIntent.putExtra("SENDER", senderName);//senderName, notifIcon, TrueOrFlase, type, date, id
            scheduleIntent.putExtra("DATE", datetime);
            scheduleIntent.putExtra("REQUEST ID", id);
            scheduleIntent.putExtra("REMINDER ID", reminderId);
            PendingIntent piSchedule = PendingIntent.getBroadcast(getApplicationContext(), reminderId /*My code set for reminder pending intents*/, scheduleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, triggerAt, piSchedule);

        }

        else {
            Log.d("REMINDER TIME: ", "Less than an hour left for the ride, no need to send a notification");

        }



    }


    // An alarm is fired 1 hour before the pickup time
    private void scheduleAlarm(String datetime, String senderName, String senderEmail, int id) {

        int alarmId = Integer.parseInt(id + ALARM_CODE + String.valueOf(ThreadLocalRandom.current().nextInt(0, 99)));

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = null;

        try {
            d = format.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Calendar pickupTime = Calendar.getInstance();
        pickupTime.setTimeInMillis(System.currentTimeMillis());
        pickupTime.setTime(d);

        //Fire 1h before pickup time
        long triggerAt = pickupTime.getTimeInMillis() - (3600 * 1000);

        if(System.currentTimeMillis() > triggerAt) { //now is > than alarm time
            triggerAt = System.currentTimeMillis();
        }


        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent scheduleIntent = new Intent(this, AlarmSchedulerReceiver.class);
        scheduleIntent.putExtra("ALARM_ID", alarmId);
        scheduleIntent.putExtra("SENDER_EMAIL", senderEmail);
        scheduleIntent.putExtra("SENDER_NAME", senderName);
        scheduleIntent.putExtra("PICKUP_TIME", datetime);
        PendingIntent piSchedule = PendingIntent.getBroadcast(getApplicationContext(), alarmId /*My code set for alarm pending intents*/, scheduleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, /*alarmTime.getTimeInMillis()*/triggerAt, piSchedule);

        //Toast.makeText(getApplicationContext(), "Alarm scheduled for " + datetime, Toast.LENGTH_LONG);
    }


    /*
    *To get a Bitmap image from the URL received
    * */
    private Bitmap getBitmapfromUrl(String imageUrl) {
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


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        String picName;

        public DownloadImageTask(String senderEmail) {
            picName = senderEmail;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap result = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                result = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(Bitmap result) {
            //Cache the image
            storeImage(result, picName);
        }
    }


    private void storeImage(Bitmap image, String fileName) {

        //String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(getApplicationContext().getFilesDir() + "/images");
        myDir.mkdirs();
        File file1 = new File(myDir, fileName+ ".jpg");
        if (file1.exists())
            file1.delete();
        try {
            FileOutputStream out = new FileOutputStream(file1);
            image.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[] { file1.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });



        /*File pictureFile = getOutputMediaFile(fileName);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this, new String[] { pictureFile.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("ExternalStorage", "Scanned " + path + ":");
                            Log.d("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }*/
    }


    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(String fileName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        //File mediaStorageDir = new File(getApplicationContext().getFilesDir() + "/images") ;
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString() + "/PICKR");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        File mediaFile = null;
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            mediaStorageDir.mkdirs();
        }

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName + ".jpg");

        return mediaFile;
    }

}
