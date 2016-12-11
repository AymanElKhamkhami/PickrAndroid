package dmcs.pickr.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dmcs.pickr.R;
import dmcs.pickr.broadcastreceivers.AlarmSchedulerReceiver;

public class Alert extends AppCompatActivity {

    Vibrator vibrator;
    Button stop;
    ImageView image;
    TextView title, content;
    MediaPlayer sound;
    Ringtone ringtone;
    AlarmManager alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);


        stop = (Button) findViewById(R.id.button_stop);
        image = (ImageView) findViewById(R.id.image_alarm);
        title = (TextView)  findViewById(R.id.txt_title);
        content = (TextView)  findViewById(R.id.txt_content);

        String senderEmail;
        String senderName;
        String date;

        Bundle bundle = getIntent().getExtras();
        senderEmail = bundle.getString("SENDER_EMAIL");
        senderName = bundle.getString("SENDER_NAME");
        date = bundle.getString("PICKUP_TIME");

        Bitmap imageBitmap = getBitmapFromStorage(senderEmail);
        image.setImageBitmap(imageBitmap);
        image.setBackgroundColor(0x00000000);


        title.setText("Your ride is soon");
        content.setText("Your ride with " + senderName + " is " + calculateRemainingTime(date) + " minutes away");

        startAlarm();
    }



    public void startAlarm() {
        //Start Vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long pattern[] = {0/*time before start*/, 1000, 1000};
        vibrator.vibrate(pattern, 0);

        //Start Ringtone
        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmTone);
        ringtone.play();
        //sound = MediaPlayer.create(this, R.raw.d);
        //sound.start();
    }


    public void dismissAlarm(View v) {
        //sound.stop();
        ringtone.stop();
        vibrator.cancel();

        //Intent intent = new Intent(this, null);
        PendingIntent piDismiss = PendingIntent.getBroadcast(getApplicationContext(), 11 /*My code set for alarm pending intents*/, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(piDismiss);

        this.finish();

    }


    public void snoozeAlarm(View v) {
        ringtone.stop();
        vibrator.cancel();

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Intent snoozeIntent = new Intent(this, AlarmSchedulerReceiver.class);
        PendingIntent piSnooze = PendingIntent.getBroadcast(getApplicationContext(), 11 /*My code set for alarm pending intents*/, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(piSnooze);

        alarm.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis() + (5000 * 60), piSnooze);

        Toast.makeText(getApplicationContext(), "Snoozed for 5 minutes", Toast.LENGTH_LONG).show();
        this.finish();
    }


    private Bitmap getBitmapFromStorage(String fileName) {

        Bitmap imageBitmap = null;

        //File mediaStorageDir = new File(getApplicationContext().getFilesDir() + "/images") ;
        File mediaStorageDir = new File(getApplicationContext().getFilesDir() + "/images");
        File mediaFile;
        if (mediaStorageDir.exists()){
            mediaFile = new File(mediaStorageDir, fileName + ".jpg");
            try {
                imageBitmap = BitmapFactory.decodeStream(new FileInputStream(mediaFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return  imageBitmap;
    }


    private long calculateRemainingTime(String rawDate) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = null;

        try {
            d = format.parse(rawDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        Date nowDate = now.getTime();

        long diff = d.getTime() - nowDate.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        return minutes;
    }

}
