package com.example.SpendingTracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Reminder_Activity extends AppCompatActivity {

    private EditText editTextPaymentNote;
    private TimePicker timePickerReminder;
    private Button buttonSetReminder;

    private static final String CHANNEL_ID = "ReminderChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        editTextPaymentNote = findViewById(R.id.editTextPaymentNote);
        timePickerReminder = findViewById(R.id.timePickerReminder);
        buttonSetReminder = findViewById(R.id.buttonSetReminder);

        buttonSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = editTextPaymentNote.getText().toString();
                int hour, minute;

                if (Build.VERSION.SDK_INT >= 23) {
                    hour = timePickerReminder.getHour();
                    minute = timePickerReminder.getMinute();
                } else {
                    hour = timePickerReminder.getCurrentHour();
                    minute = timePickerReminder.getCurrentMinute();
                }

                scheduleReminder(hour, minute, note);
            }
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Channel";
            String description = "Channel for payment reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleReminder(int hour, int minute, String note) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Get the selected date from the DatePicker
        DatePicker datePickerReminder = findViewById(R.id.datePickerReminder);
        int year = datePickerReminder.getYear();
        int month = datePickerReminder.getMonth();
        int day = datePickerReminder.getDayOfMonth();
        calendar.set(year, month, day);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderNotificationReceiver.class);
        intent.putExtra("note", note);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Reminder set for " + formatDateAndTime(calendar) + " with note: " + note, Toast.LENGTH_LONG).show();
    }

    private String formatDateAndTime(Calendar calendar) {
        String dateTimeFormat = "dd/MM/yyyy HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
        return format.format(calendar.getTime());
    }


}
