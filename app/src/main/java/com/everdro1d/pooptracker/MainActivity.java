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
import android.provider.Settings;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {
    // Global Variables
    private long startTime = 0;

    private int shortestTime = 0, longestTime = 0;

    private int totalAllTime = 0, totalThisWeek = 0, totalToday = 0;

    private int previousDay, previousWeek;

    public int hour, minute;

    private String menuItem2Time = "";

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get saved state if it exists
        if (savedInstanceState != null) {
            Log.v("onCreate ", "EXISTING STATE");

            // Get the previous state if the activity has been destroyed and recreated.
            startTime
                = savedInstanceState.getLong("startTime");

            shortestTime
                = savedInstanceState.getInt("shortestTime");

            longestTime
                = savedInstanceState.getInt("longestTime");

            totalAllTime
                = savedInstanceState.getInt("totalAllTime");

            totalThisWeek
                = savedInstanceState.getInt("totalThisWeek");

            totalToday
                = savedInstanceState.getInt("totalToday");

            previousDay
                = savedInstanceState.getInt("previousDay");

            previousWeek
                = savedInstanceState.getInt("previousWeek");

            menuItem2Time
                = savedInstanceState.getString("menuItem2Time");

            hour
                = savedInstanceState.getInt("hour");

            minute
                = savedInstanceState.getInt("minute");

            Log.v("onSaveInstanceState ", "SAVING STATE");
            Log.v("Saving State", savedInstanceState.toString());

        } else {
            Log.v("onCreate ", "EMPTY STATE");
        }
        // increment seconds & create notification channel
        runTimer();
        createNotificationChannel();
    }


    // Creates the Notification Channel
    private void createNotificationChannel() {
        // Create the NotificationChannel
        CharSequence name = "Have You Pooped?";
        String description = "PoopTracker App Default Notification Channel.";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("1", name, importance);
        channel.setDescription(description);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // creates the menu and adds the reminder time beside it
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = R.id.menuItem2;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.add(Menu.NONE, id, 0, "Reminder Time: " + menuItem2Time).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    // Select a menu item
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            // Clear all stats
            case R.id.menuItem1:
                clearAllStats();
                // flush sound
                mp = MediaPlayer.create(this, R.raw.flush);
                try {
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.reset();
                        mp.release();
                        mp = MediaPlayer.create(this, R.raw.flush);
                    }
                    mp.start();
                } catch(Exception e) { e.printStackTrace(); }
                return true;
            // Set a reminder
            case R.id.menuItem2:
                popupTimePicker();
                return true;
            case R.id.menuItem3:
                switchTheme();
            // add more menu items here
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Save the state of the stopwatch if it's about to be destroyed.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the state of the app.
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

        // Log the state of the app.
        Log.v("onSaveInstanceState ", "SAVING STATE");
        Log.v("Saving State", savedInstanceState.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the shared preferences
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

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isDark", isNightModeActive(this));
        editor.apply();

        // Log
        //Log.v("onStart ", "SET SHAREDPREFS");

        // Update the textViews
        TextView textTAT = findViewById(R.id.textTAT);
        textTAT.setText(String.valueOf(totalAllTime));

        TextView textTTW = findViewById(R.id.textTTW);
        textTTW.setText(String.valueOf(totalThisWeek));

        TextView textTT = findViewById(R.id.textTT);
        textTT.setText(String.valueOf(totalToday));

        setTimeTextView(findViewById(R.id.textShort), shortestTime);
        setTimeTextView(findViewById(R.id.textLong), longestTime);

        // Update the menu
        invalidateOptionsMenu();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onStop() {
        super.onStop();

        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        // now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        // put value
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

        // commit edits
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // resets weekly and daily totals if the week or day has changed.
        checkDate();
    }

    // Resets the timer and calls addTotal(). (also fart sound)
    public void onClickAdd(View view) {
        // Reset the timer
        setInterval();
        // Set the start time
        startTime = System.currentTimeMillis();
        // Add to the totals
        addTotal();

        // play the fart sound
        mp = MediaPlayer.create(this, R.raw.fart);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = MediaPlayer.create(this, R.raw.fart);
            }
            mp.start();
        } catch(Exception e) { e.printStackTrace(); }
    }

    // Is called in onClickAdd() and adds 1 to each of the totals before updating the text view.
    public void addTotal() {
        // Add to each variable
        totalAllTime++;
        totalThisWeek++;
        totalToday++;

        // Update the textViews
        // Get the text view.
        TextView textTAT = findViewById(R.id.textTAT);
            textTAT.setText(String.valueOf(totalAllTime));

        TextView textTTW = findViewById(R.id.textTTW);
            textTTW.setText(String.valueOf(totalThisWeek));

        TextView textTT = findViewById(R.id.textTT);
            textTT.setText(String.valueOf(totalToday));
    }

    // The runTimer() method uses a Handler to increment the seconds and update the text view.
    @SuppressWarnings("deprecation")
    private void runTimer() {
        // Get the text view.
        final TextView timeView = findViewById(R.id.textTime);

        // Creates a new Handler
        final Handler handler = new Handler();

        // Call the post() method, passing in a new Runnable.
        // The post() method processes code without a delay, so the code in the Runnable will run almost immediately.
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                setTimeTextView(timeView, checkTime());
                // Post the code again with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }

    // Resets weekly and daily totals.
    // (If you open the app exactly one month after last opening it, the counters will think no time has passed.)
    public void checkDate() {
        // Get the current day and week.
        Calendar c = Calendar.getInstance();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int currentWeek = c.get(Calendar.WEEK_OF_MONTH);

        // If the previous day is not the same as the current day, then reset the daily total.
        if (previousDay != currentDay) {
            // If the previous day was Sunday or the previous day was more than one day
            // before the current day, then reset the weekly total.
            if ((previousDay == 7) || ((previousDay - 1) < currentDay)) {
                totalThisWeek = 0;
                previousWeek = currentWeek;
            }
            // Reset the daily total.
            totalToday = 0;
            previousDay = currentDay;

            //call daily notification
            handleNotification(hour, minute);

        } else if (previousWeek != currentWeek) {
            // If the previous week is not the same as the current week, then reset the weekly total.
            totalThisWeek = 0;
            previousWeek = currentWeek;
        }
    }

    // Calculates the difference in seconds from the current time and the time that the stopwatch started.
    // Returns seconds elapsed.
    public int checkTime() {
        // If the start time is zero, then the stopwatch is not running.
        if (startTime == 0) {
            return 0;
        }
        // Get the elapsed time since the start time.
        long now = System.currentTimeMillis();
        return (int) ((now - startTime) / 1000);
    }

    // Formats seconds into HH:MM:SS and sets the specified textView as the string of time.
    public void setTimeTextView(TextView timeView, int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        // Format the seconds into hours, minutes, and seconds.
        String time = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, secs);

        // Set the text view text.
        timeView.setText(time);
    }

    // If current time is shorter/longer than shortestTime/longestTime then set them.
    public void setInterval() {
        // get the current time
        int now = checkTime();

        // if the current time is shorter than the shortest time, or if the shortest time is 0,
        // then set the shortest time to the current time.
        if (now < shortestTime || shortestTime == 0) {
            setTimeTextView(findViewById(R.id.textShort), checkTime());
            shortestTime = now;
        }

        // if the current time is longer than the longest time, or if the longest time is 0,
        // then set the longest time to the current time.
        if (now > longestTime || longestTime == 0) {
            setTimeTextView(findViewById(R.id.textLong), checkTime());
            longestTime = now;
        }
    }

    // clears all timers & scores
    @SuppressLint("ApplySharedPref")
    public void clearAllStats() {
        // Create SharedPreferences.
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        // Create Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        // sets all values to zero
        editor.putLong("startTime", 0);
        editor.putInt("shortestTime", 0);
        editor.putInt("longestTime", 0);
        editor.putInt("totalAllTime", 0);
        editor.putInt("totalThisWeek", 0);
        editor.putInt("totalToday", 0);

        //Commit edits
        editor.commit();

        // Reset all textViews
        onStart();

    }

    // sets the time for the notification
    public void handleNotification(int hour, int minute) {
        // Create an Intent and set the class that will execute when the Alarm triggers.
        Intent alarmIntent = new Intent(this, AlarmReceiverNotification.class);

        // Create a Calendar object that will contain the date and time of the alarm
        Calendar calendar = Calendar.getInstance();

        // Set the alarm's trigger hour and minute
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        // Set the alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //INEXACT REPEAT TODO: change to EXACT REPEAT & set up a broadcast receiver to trigger it again in 24 hours
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*3600*1000, pendingIntent);

        // Log the alarm time
        Log.v("Alarm", "Alarm set for " + hour + ":" + minute);
        Log.v("Alarm", "Alarm set for " + calendar.getTimeInMillis() + " milliseconds from now");
    }

    //checks if notifications are enabled
    public boolean areNotificationsEnabled() {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
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

    //creates the popup dialog to enable notifications
    private void notificationPopupDialog() {
        new MaterialAlertDialogBuilder(MainActivity.this, R.style.AlertDialogStyle)
                .setTitle("Enable Notifications?")
                .setMessage("Notifications are disabled. Enable notifications to receive daily reminders.")
                .setPositiveButton("ENABLE", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    //creates the TimePicker popup
    public void popupTimePicker() {
        boolean notifications = areNotificationsEnabled();
        if (!notifications) {
            // if notifications are disabled, show a popup dialog to enable them and return
            notificationPopupDialog();
            return;
        }

        //gets the system clock format
        boolean isSystem24Hour = is24HourFormat(this);
        int clockFormat = 1;
            //sets the clock format (defaults to 24 hour)
            if (!isSystem24Hour) {clockFormat = TimeFormat.CLOCK_12H;}

        //creates the time picker
        MaterialTimePicker mTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setInputMode(INPUT_MODE_CLOCK)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select notification time")
                .build();

        //shows the time picker
        mTimePicker.show(getSupportFragmentManager(), "TimePicker");

        //sets the time picker listener
        mTimePicker.addOnPositiveButtonClickListener(v -> {
            //gets the time from the time picker
            hour = mTimePicker.getHour();
            minute = mTimePicker.getMinute();
            //refreshes the menu
            invalidateOptionsMenu();
            //sets the alarm
            handleNotification(hour, minute);
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
            else { menuItem2Time = String.format(Locale.getDefault(),"%02d:%02d", hour, minute);}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch between light and dark theme
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

