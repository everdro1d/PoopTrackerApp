package com.everdro1d.pooptracker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Global Variables
    private long startTime = 0;

    private int shortestTime = 0, longestTime = 0;

    private int totalAllTime = 0, totalThisWeek = 0, totalToday = 0;

    private int previousDay, previousWeek;

    private int hour, minute;

    private String menuItem2Time = "";
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        runTimer();
        createNotificationChannel();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel() {
        // Create the NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = R.id.menuItem2;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.add(Menu.NONE, id, 0, "Reminder Time: " + menuItem2Time).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
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
            case R.id.menuItem2:
                popupTimePicker();
                return true;
            // add more menu options

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Save the state of the stopwatch if it's about to be destroyed.
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

        Log.v("onSaveInstanceState ", "SAVING STATE");
        Log.v("Saving State", savedInstanceState.toString());
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

        Log.v("onStart ", "SET SHAREDPREFS");
        Log.v("Setting SharedPreferences", sharedPref.toString());

        // Update the textViews
        // Get the text view.
        TextView textTAT = findViewById(R.id.textTAT);
        textTAT.setText(String.valueOf(totalAllTime));

        TextView textTTW = findViewById(R.id.textTTW);
        textTTW.setText(String.valueOf(totalThisWeek));

        TextView textTT = findViewById(R.id.textTT);
        textTT.setText(String.valueOf(totalToday));

        setTimeTextView(findViewById(R.id.textShort), shortestTime);

        setTimeTextView(findViewById(R.id.textLong), longestTime);
        invalidateOptionsMenu();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onStop() {
        super.onStop();

        // Create object of SharedPreferences.
        SharedPreferences sharedPref = getSharedPreferences("varPrefs", 0);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        //put your value
        editor.putLong("startTime", startTime);
        editor.putInt("shortestTime", shortestTime);
        editor.putInt("longestTime", longestTime);
        editor.putInt("totalAllTime", totalAllTime);
        editor.putInt("totalThisWeek", totalThisWeek);
        editor.putInt("totalToday", totalToday);
        editor.putInt("previousDay", previousDay);
        editor.putInt("previousWeek", previousWeek);
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.putString("menuItem2Time", menuItem2Time);

        //commits your edits
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDate();
    }

    // Resets the timer and calls addTotal(). (also fart sound)
    public void onClickAdd(View view) {
        setInterval();
        startTime = System.currentTimeMillis();
        addTotal();

        // fart sound
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
        Calendar c = Calendar.getInstance();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int currentWeek = c.get(Calendar.WEEK_OF_MONTH);

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

    // Calculates the difference in seconds from the current time and the time that the stopwatch started.
    // Returns seconds elapsed.
    public int checkTime() {
        if (startTime == 0) {
            return 0;
        }
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
        int now = checkTime();

        if (now < shortestTime || shortestTime == 0) {
            setTimeTextView(findViewById(R.id.textShort), checkTime());
            shortestTime = now;
        }

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

        onStart();

    }

    private void handleNotification(int hour) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        long millis = 86400000;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), millis, pendingIntent);
    }

    public void popupTimePicker() {
        //TODO make material dialog to select hours
        TimePickerDialog.OnTimeSetListener onTimeSetListener
                = (timePicker, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = 0;
                    menuItem2Time = String.format(Locale.getDefault(), "%02d:%02d",hour, minute);
                    invalidateOptionsMenu();
                    handleNotification(hour);
                };


        TimePickerDialog timePickerDialog = new TimePickerDialog(this, 0,  onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }
}

