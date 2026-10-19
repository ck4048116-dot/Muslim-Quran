package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IslamicReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.muslimquran.muslimquran.ACTION_DAILY_REMINDER" -> {
                val title = intent.getStringExtra("title") ?: "Muslim Quran Reminder"
                val message = intent.getStringExtra("message") ?: "Time for remembrance and worship."
                val arabic = intent.getStringExtra("arabic")
                val notifId = (intent.getStringExtra("reminder_id") ?: "reminder").hashCode()

                NotificationHelper.showNotification(
                    context = context,
                    notificationId = notifId,
                    channelId = NotificationHelper.CHANNEL_REMINDERS_ID,
                    title = title,
                    message = if (!arabic.isNullOrEmpty()) "$arabic\n$message" else message,
                    subText = "Daily Islamic Reminder"
                )
            }
            "com.muslimquran.muslimquran.ACTION_PRAYER_REMINDER" -> {
                val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
                val time = intent.getStringExtra("time") ?: ""
                val city = intent.getStringExtra("city") ?: ""

                NotificationHelper.showNotification(
                    context = context,
                    notificationId = prayerName.hashCode(),
                    channelId = NotificationHelper.CHANNEL_PRAYERS_ID,
                    title = "Time for $prayerName Prayer ($time)",
                    message = "حيّ على الصلاة ، حيّ على الفلاح\nIt is now time for $prayerName in $city.",
                    subText = "Adhan Alert"
                )
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-schedule all active reminders on reboot
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val reminders = db.reminderDao().getAllReminders().first()
                    reminders.filter { it.isEnabled }.forEach { reminder ->
                        NotificationHelper.scheduleDailyReminder(context, reminder)
                    }
                }
            }
        }
    }
}
