package dmcs.pickr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import dmcs.pickr.utils.AlarmSchedulerReceiver;

public class Alert extends AppCompatActivity {

    Vibrator vibrator;
    Button stop;
    MediaPlayer sound;
    Ringtone ringtone;
    AlarmManager alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);

        stop = (Button) findViewById(R.id.button_stop);

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

}
