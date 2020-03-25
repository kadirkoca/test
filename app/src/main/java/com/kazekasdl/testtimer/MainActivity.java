package com.kazekasdl.testtimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    EditText inputTime;
    TextView remainTime,progressPercentage;
    Button toggle;
    CountDownTimer timer;
    ProgressBar progress;
    int passedSecs,remainSecs;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggle = findViewById(R.id.togglebtn);
        inputTime = findViewById(R.id.timerinput);
        remainTime = findViewById(R.id.remaintime);
        progress = findViewById(R.id.progressBar);
        progressPercentage = findViewById(R.id.progressPercentage);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleTimer();
            }
        });
    }

    private void Tick() {
        progress.setProgress(progress.getMax()-remainSecs);
        remainTime.setText(String.valueOf(remainSecs));
        double d = passedSecs*100/progress.getMax();
        progressPercentage.setText(String.valueOf(d)+"%");
    }

    private void Done() {
        inputTime.setEnabled(true);
        progress.setProgress(progress.getMax());
        progressPercentage.setText("100%");
        toggle.setText("Start");
        passedSecs = 0;
        remainTime.setText("Done");
    }

    private void ToggleTimer() {
        String it = inputTime.getText().toString();

        if(it.isEmpty()){
              return;
        }
        int inputsecs = Integer.parseInt(it);

        if(timer!=null){
            toggle.setText("Resume");
            timer.cancel();
            timer = null;
            cancelAlarm();
            if(passedSecs!=0){
                inputTime.setEnabled(true);
                return;
            }
        }

        progress.setMax(inputsecs);
        timer = new CountDownTimer((inputsecs - passedSecs)*1000,1000) {

            public void onTick(long millisUntilFinished) {
                passedSecs++;
                remainSecs = (int) (millisUntilFinished / 1000);
                Tick();
            }

            public void onFinish() {
                Done();
            }
        }.start();
        toggle.setText("Pause");
        inputTime.setEnabled(false);
        setAlarm(inputsecs - passedSecs);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("passedTime", passedSecs);
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        passedSecs = in.getInt("passedTime",0);
        ToggleTimer();
    }



    private void setAlarm(int secs){
        if(alarmManager!=null){
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long interval = System.currentTimeMillis() + secs * 1000;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
        }
    }


    private void cancelAlarm(){
        alarmManager.cancel(pendingIntent);
    }
}
