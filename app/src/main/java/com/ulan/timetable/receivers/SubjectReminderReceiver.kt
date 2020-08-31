package com.ulan.timetable.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ulan.timetable.profiles.ProfileManagement
import com.ulan.timetable.utils.DbHelper
import com.ulan.timetable.utils.NotificationUtil
import com.ulan.timetable.utils.PreferenceUtil
import java.util.*

class SubjectReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        setSubjectReminder(context)
        if (PreferenceUtil.isReminder(context))
            NotificationUtil.sendNotificationCurrentLesson(context, true)
    }

    companion object {
        const val SubjectReminderID = 97000
    }
}


fun setSubjectReminder(context: Context) {
    ProfileManagement.initProfiles(context)
    if (!ProfileManagement.isPreferredProfile())
        return
    Thread(Runnable {
        val dbHelper = DbHelper(context, ProfileManagement.getPreferredProfilePosition())
        val calendar = Calendar.getInstance()
        val currentDay = NotificationUtil.getCurrentDay(calendar.get(Calendar.DAY_OF_WEEK))
        val weeks = dbHelper.getWeek(currentDay)

        var lastCalendar = Calendar.getInstance()
        lastCalendar.set(Calendar.HOUR_OF_DAY, 23)
        lastCalendar.set(Calendar.MINUTE, 59)

        for (week in weeks) {
            val weekCalendarStart = Calendar.getInstance()
            val startHour = Integer.parseInt(week.fromTime.substring(0, week.fromTime.indexOf(":")))
            weekCalendarStart.set(Calendar.HOUR_OF_DAY, startHour)
            val startMinute = Integer.parseInt(week.fromTime.substring(week.fromTime.indexOf(":") + 1))
            weekCalendarStart.set(Calendar.MINUTE, startMinute)

            if (((startHour == calendar.get(Calendar.HOUR_OF_DAY) && startMinute > calendar.get(Calendar.MINUTE)) || startHour > calendar.get(Calendar.HOUR_OF_DAY)) && ((startHour == lastCalendar.get(Calendar.HOUR_OF_DAY) && startMinute < lastCalendar.get(Calendar.MINUTE)) || startHour < lastCalendar.get(Calendar.HOUR_OF_DAY))) {
                lastCalendar = weekCalendarStart
            }
        }

        if (lastCalendar.get(Calendar.HOUR_OF_DAY) == 23 && lastCalendar.get(Calendar.MINUTE) == 59)
            return@Runnable

        lastCalendar.add(Calendar.MINUTE, -PreferenceUtil.getReminderTime(context))
        PreferenceUtil.setOneTimeAlarm(context, SubjectReminderReceiver::class.java, lastCalendar.get(Calendar.HOUR_OF_DAY), lastCalendar.get(Calendar.MINUTE), 0, SubjectReminderReceiver.SubjectReminderID)
    }).start()
}