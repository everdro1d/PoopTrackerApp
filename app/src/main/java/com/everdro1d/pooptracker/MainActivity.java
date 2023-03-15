package com.everdro1d.pooptracker;

import static android.text.format.DateFormat.is24HourFormat;
import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//TODO
// Create signed apk        https://www.youtube.com/watch?v=ieWtCaWkzYQ | https://developer.android.com/studio/publish/preparing
// publish to play store    https://www.youtube.com/watch?v=5GHT4QtotE4

public class MainActivity extends AppCompatActivity {
    private long startTime = 0, triggerTime = 0;
    private int shortestTime = 0, longestTime = 0, totalAllTime = 0,
            totalThisWeek = 0, totalToday = 0, previousDay, previousWeek;
    public int hour, minute;
    private String menuItem2Time = "";
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime");
            shortestTime = savedInstanceState.getInt("shortestTime");
            longestTime = savedInstanceState.getInt("longestTime");
            totalAllTime = savedInstanceState.getInt("totalAllTime");
            totalThisWeek = savedInstanceState.getInt("totalThisWeek");
            totalToday = savedInstanceState.getInt("totalToday");
            previousDay = savedInstanceState.getInt("previousDay");
            previousWeek = savedInstanceState.getInt("previousWeek");
            menuItem2Time = savedInstanceState.getString("menuItem2Time");
            hour = savedInstanceState.getInt("hour");
            minute = savedInstanceState.getInt("minute");
            triggerTime = savedInstanceState.getLong("triggerTime");
        }

        incrementStopwatchSeconds();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notificationTitle);
        String description = "PoopTracker App Default Notification Channel.";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("1", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = R.id.menuItem2;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.add(Menu.NONE, id, 0, "Reminder Time: " + menuItem2Time)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT)
             .show();
        int itemId = item.getItemId();
        if (itemId == R.id.menuItem1) {
            clearAllStats();
            mp = MediaPlayer.create(this, R.raw.flush);
            try {
                if (mp.isPlaying()) {
                    mp.stop();
                    mp.reset();
                    mp.release();
                    mp = MediaPlayer.create(this, R.raw.flush);
                }
                mp.start();
            } catch (Exception e) { e.printStackTrace();}
            return true;
        } else if (itemId == R.id.menuItem2) {
            createTimePickerPopup();
            return true;
        } else if (itemId == R.id.menuItem3) {
            switchTheme();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putLong("startTime", startTime);
        savedInstanceState.putInt("shortestTime", shortestTime);
        savedInstanceState.putInt("longestTime", longestTime);
        savedInstanceState.putInt("totalAllTime", totalAllTime);
        savedInstanceState.putInt("totalThisWeek", totalThisWeek);
        savedInstanceState.putInt("totalToday", totalToday);
        savedInstanceState.putInt("previousDay", previousDay);
        savedInstanceState.putInt("previousWeek", previousWeek);
        savedInstanceState.putString("menuItem2Time", menuItem2Time);
        savedInstanceState.putInt("hour", hour);
        savedInstanceState.putInt("minute", minute);
        savedInstanceState.putLong("triggerTime", triggerTime);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        startTime = sharedPref.getLong("startTime", startTime);
        shortestTime = sharedPref.getInt("shortestTime", shortestTime);
        longestTime = sharedPref.getInt("longestTime", longestTime);
        totalAllTime = sharedPref.getInt("totalAllTime", totalAllTime);
        totalThisWeek = sharedPref.getInt("totalThisWeek", totalThisWeek);
        totalToday = sharedPref.getInt("totalToday", totalToday);
        previousDay = sharedPref.getInt("previousDay", previousDay);
        previousWeek = sharedPref.getInt("previousWeek", previousWeek);
        menuItem2Time = sharedPref.getString("menuItem2Time", menuItem2Time);
        hour = sharedPref.getInt("hour", hour);
        minute = sharedPref.getInt("minute", minute);
        triggerTime = sharedPref.getLong("triggerTime", triggerTime);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isDark", isNightModeActive(this));
        editor.apply();

        updateTextView(R.id.textTAT, totalAllTime);

        updateTextView(R.id.textTTW, totalThisWeek);

        updateTextView(R.id.textTTD, totalToday);

        setTimeTextView(findViewById(R.id.textShort), shortestTime);
        setTimeTextView(findViewById(R.id.textLong), longestTime);

        invalidateOptionsMenu();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onStop() {
        super.onStop();
        
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        
        editor.putLong("startTime", startTime);
        editor.putInt("shortestTime", shortestTime);
        editor.putInt("longestTime", longestTime);
        editor.putInt("totalAllTime", totalAllTime);
        editor.putInt("totalThisWeek", totalThisWeek);
        editor.putInt("totalToday", totalToday);
        editor.putInt("previousDay", previousDay);
        editor.putInt("previousWeek", previousWeek);
        editor.putString("menuItem2Time", menuItem2Time);
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.putLong("triggerTime", triggerTime);

        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDate();
    }

    public void onClickStartStopwatch(View view) {
        setLongestShortestTime();
        startTime = System.currentTimeMillis();
        addTotal();

        mp = MediaPlayer.create(this, R.raw.fart);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = MediaPlayer.create(this, R.raw.fart);
            }
            mp.start();
        } catch(Exception e) { e.printStackTrace();}
    }

    public void addTotal() {
        totalAllTime++;
        totalThisWeek++;
        totalToday++;

        updateTextView(R.id.textTAT, totalAllTime);
        updateTextView(R.id.textTTW, totalThisWeek);
        updateTextView(R.id.textTTD, totalToday);
    }

    private void updateTextView(int R_id, int value) {
        TextView textView = findViewById(R_id);
        textView.setText(String.valueOf(value));
    }

    private void incrementStopwatchSeconds() {
        final TextView timeView = findViewById(R.id.textTime);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                if (triggerTime - System.currentTimeMillis() <= -1) {
                    scheduleNotification(hour, minute);
                }
                setTimeTextView(timeView, checkTime());
                handler.postDelayed(this, 1000);
            }
        });
    }


    public void checkDate() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int currentWeek = calendar.get(Calendar.WEEK_OF_MONTH);

        if (previousDay != currentDay) {
            if ((previousDay == 7) || ((previousDay - 1) < currentDay)) {
                totalThisWeek = 0;
                previousWeek = currentWeek;
            }
            
            totalToday = 0;
            previousDay = currentDay;
        } else if (previousWeek != currentWeek) {
            totalThisWeek = 0;
            previousWeek = currentWeek;
        }
    }
    
    public int checkTime() {
        if (startTime == 0) { return 0;}

        long currentTimeInMillis = System.currentTimeMillis();
        return (int) ((currentTimeInMillis - startTime) / 1000);
    }

    public void setTimeTextView(TextView timeView, int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String time = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, secs);

        timeView.setText(time);
    }

    public void setLongestShortestTime() {
        int currentTime = checkTime();

        if (currentTime < shortestTime || shortestTime == 0) {
            setTimeTextView(findViewById(R.id.textShort), currentTime);
            shortestTime = currentTime;
        }
        if (currentTime > longestTime || longestTime == 0) {
            setTimeTextView(findViewById(R.id.textLong), currentTime);
            longestTime = currentTime;
        }
    }

    @SuppressLint("ApplySharedPref")
    public void clearAllStats() {
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("startTime", 0);
        editor.putInt("shortestTime", 0);
        editor.putInt("longestTime", 0);
        editor.putInt("totalAllTime", 0);
        editor.putInt("totalThisWeek", 0);
        editor.putInt("totalToday", 0);
        editor.commit();
        onStart();

    }

    public void scheduleNotification(int hour, int minute) {
        Intent alarmIntent = new Intent(this, AlarmReceiverNotification.class);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        triggerTime = calendar.getTimeInMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public boolean areNotificationsEnabled() {
        NotificationManager manager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!manager.areNotificationsEnabled()) {
            return false;
        }
        List<NotificationChannel> channels = manager.getNotificationChannels();
        for (NotificationChannel channel : channels) {
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                return false;
            }
        }
        return true;
    }

    private void createNotificationPopup() {
        new MaterialAlertDialogBuilder(MainActivity.this, R.style.AlertDialogStyle)
                .setTitle("Enable Notifications?")
                .setMessage("Notifications are disabled. Enable notifications to receive daily reminders.")
                .setPositiveButton("ENABLE", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    public void createTimePickerPopup() {
        boolean notifications = areNotificationsEnabled();
        if (!notifications) {
            createNotificationPopup();
            return;
        }

        boolean isSystem24Hour = is24HourFormat(this);
        int clockFormat = TimeFormat.CLOCK_24H;
        if (!isSystem24Hour) { clockFormat = TimeFormat.CLOCK_12H;}

        MaterialTimePicker mTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setInputMode(INPUT_MODE_CLOCK)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select notification time")
                .build();

        mTimePicker.show(getSupportFragmentManager(), "TimePicker");

        mTimePicker.addOnPositiveButtonClickListener(v -> {
            hour = mTimePicker.getHour();
            minute = mTimePicker.getMinute();
            invalidateOptionsMenu();
            scheduleNotification(hour, minute);
            timeFormat(hour, minute);
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void timeFormat(int hour, int minute) {
        //gets the system clock format
        boolean isSystem24Hour = is24HourFormat(this);
        //formats the time
        try {
            String _24HourTime = hour + ":" + minute;
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("kk:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a");
            Date _24HourDt = _24HourSDF.parse(_24HourTime);
            if (!isSystem24Hour)
            {
                assert _24HourDt != null;
                menuItem2Time = _12HourSDF.format(_24HourDt);}
            else {
                menuItem2Time = String.format(Locale.getDefault(),"%02d:%02d", hour, minute);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchTheme() {
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (sharedPref.getBoolean("isDark", false)) {
            editor.putBoolean("isDark", false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            editor.putBoolean("isDark", true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        editor.apply();
        recreate();
    }
    public static boolean isNightModeActive(Context context) {
        int defaultNightMode = AppCompatDelegate.getDefaultNightMode();
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        }
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false;
        }

        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
        }
        return false;
    }
}

