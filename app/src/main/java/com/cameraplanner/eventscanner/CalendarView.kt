package com.cameraplanner.eventscanner

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CalendarView : AppCompatActivity() {

    private lateinit var datesListAdapter: ArrayAdapter<String>
    private val datesList = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_view)

        setupBackButton()
        setupEventsListView()
        loadDatesList()
        processDatesAndTimes()
    }

    override fun onPause() {
        super.onPause()
        saveDatesList()
    }


    private fun setupBackButton() {
        val buttonCalendar: Button = findViewById(R.id.button_back)
        buttonCalendar.setOnClickListener {
            gotoCamera()
        }
    }

    private fun setupEventsListView() {
        val eventsListView: ListView = findViewById(R.id.eventsListView)
        datesListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datesList)
        eventsListView.adapter = datesListAdapter

        eventsListView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteConfirmationDialog(position)
            true
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Date")
            .setMessage("Are you sure you want to delete this date?")
            .setPositiveButton("Yes") { _, _ ->
                deleteDate(position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteDate(position: Int) {
        if (position < datesList.size) {
            datesList.removeAt(position)
            datesListAdapter.notifyDataSetChanged()
            saveDatesList()
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun processDatesAndTimes() {
        val dates = intent.getStringArrayListExtra("EXTRA_DATES") ?: return

        for (date in dates) {
            promptUserToNameDate(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun promptUserToNameDate(date: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)

        builder.setTitle("Name this date: $date")
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { _, _ ->
            val dateName = editText.text.toString()
            val displayText = "$date: $dateName"
            datesList.add(displayText)
            datesListAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    private fun saveDatesList() {
        val sharedPreferences = getSharedPreferences("CalendarViewPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("DatesList", datesList.toSet())
        editor.apply()
    }

    private fun loadDatesList() {
        val sharedPreferences = getSharedPreferences("CalendarViewPrefs", Context.MODE_PRIVATE)
        datesList.addAll(sharedPreferences.getStringSet("DatesList", mutableSetOf())!!)
        datesListAdapter.notifyDataSetChanged()
    }


    private fun gotoCamera() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

/*

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Date Reminder Channel"
            val descriptionText = "Channel for Date Reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DATE_REMINDER_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(date: String, time: String, dateName: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateTime = LocalDateTime.parse("$date $time", formatter)
        val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC) * 1000

        // Ensure the notification is set for a future date
        if (timestamp <= System.currentTimeMillis()) {
            Toast.makeText(this, "Can't set a notification for a past date", Toast.LENGTH_SHORT).show()
            return
        }

        // Check for SCHEDULE_EXACT_ALARM permission if necessary (API level 31 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasScheduleExactAlarmPermission()) {
                requestScheduleExactAlarmPermission()
                return
            }
        }

        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("DATE_NAME", dateName)

        // Use a unique request code for each pending intent
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasScheduleExactAlarmPermission(): Boolean {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }



    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestScheduleExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity(intent)
    }*/
