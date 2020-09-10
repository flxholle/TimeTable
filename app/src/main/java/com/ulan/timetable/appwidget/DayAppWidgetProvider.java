package com.ulan.timetable.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.ulan.timetable.R;
import com.ulan.timetable.activities.MainActivity;
import com.ulan.timetable.appwidget.Dao.AppWidgetDao;
import com.ulan.timetable.utils.PreferenceUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
public class DayAppWidgetProvider extends AppWidgetProvider {
    int lastAppWidgetId = 0;
    String lastAction = null;

    private static final String ACTION_RESTORE = "com.ulan.timetable" + ".ACTION_RESTORE";
    private static final String ACTION_YESTERDAY = "com.ulan.timetable" + ".ACTION_YESTERDAY";
    private static final String ACTION_TOMORROW = "com.ulan.timetable" + ".ACTION_TOMORROW";
    private static final String ACTION_NEW_DAY = "com.ulan.timetable" + ".ACTION_NEW_DAY";

    private static final int ONE_DAY_MILLIS = 86400000;

    @Override
    public void onEnabled(@NonNull Context context) {
        registerNewDayBroadcast(context);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {

        if (isAlarmManagerNotSet(context)) {
            registerNewDayBroadcast(context);
        }

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, DayAppWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            long currentTimeMillis = System.currentTimeMillis();
            AppWidgetDao.saveAppWidgetCurrentTime(appWidgetId, currentTimeMillis, context);

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
            rv.setRemoteAdapter(R.id.lv_day_appwidget, intent);
            rv.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
            rv.setTextViewText(R.id.tv_date, getDateText(currentTimeMillis, context));
            rv.setInt(R.id.fl_root, "setBackgroundColor", AppWidgetDao.getAppWidgetBackgroundColor(appWidgetId, Color.TRANSPARENT, context));

            rv.setOnClickPendingIntent(R.id.imgBtn_restore, makePendingIntent(context, appWidgetId, ACTION_RESTORE));
            rv.setOnClickPendingIntent(R.id.imgBtn_yesterday, makePendingIntent(context, appWidgetId, ACTION_YESTERDAY));
            rv.setOnClickPendingIntent(R.id.imgBtn_tomorrow, makePendingIntent(context, appWidgetId, ACTION_TOMORROW));

            Intent listviewClickIntent = new Intent(context, MainActivity.class);
            listviewClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            listviewClickIntent.setAction(Intent.ACTION_VIEW);
            PendingIntent listviewPendingIntent = PendingIntent.getActivity(context, appWidgetId, listviewClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.lv_day_appwidget, listviewPendingIntent);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    private static String getDateText(long currentTimeMillis, Context context) {
        String date = new SimpleDateFormat("E  d.M.", Locale.getDefault()).format(currentTimeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);

        if (PreferenceUtil.isTwoWeeksEnabled(context)) {
            date += " (";
            if (PreferenceUtil.isEvenWeek(context, calendar))
                date += context.getString(R.string.even_week);
            else
                date += context.getString(R.string.odd_week);
            date += ")";
        }

        return date;
    }

    @Override
    public void onDeleted(Context context, @NonNull int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDao.deleteAppWidget(appWidgetId, context);
        }
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        unregisterNewDayBroadcast(context);
        AppWidgetDao.clear(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }

    private PendingIntent makePendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void updateAppWidgetConfig(@NonNull AppWidgetManager appWidgetManager, int appWidgetId, int backgroundColor, int timeStyle, int profile, @NonNull Context context) {
        AppWidgetDao.saveAppWidgetConfig(appWidgetId, backgroundColor, timeStyle, profile, context);

        Intent intent = new Intent(context, DayAppWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
        views.setRemoteAdapter(R.id.lv_day_appwidget, intent);
        views.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
        views.setInt(R.id.fl_root, "setBackgroundColor", backgroundColor);
        views.setTextViewText(R.id.tv_date, getDateText(System.currentTimeMillis(), context));
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        /*
         * Whenever a request is received, checks if anything changes before using the data
         */
        if (intent.getAction().equals(lastAction) && lastAppWidgetId == intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)) {
            return;
        }

        updateValues(intent);

        if (ACTION_NEW_DAY.equals(lastAction)) {
            notifyUpdate(context);
            return;
        }

        if (ACTION_RESTORE.equals(lastAction) || ACTION_YESTERDAY.equals(lastAction) || ACTION_TOMORROW.equals(lastAction)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);

            long currentTime;
            long newTime;

            if (ACTION_RESTORE.equals(lastAction)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.INVISIBLE);
                newTime = System.currentTimeMillis();
            } else if (ACTION_YESTERDAY.equals(lastAction)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(lastAppWidgetId, System.currentTimeMillis(), context);
                newTime = currentTime - ONE_DAY_MILLIS;
            } else { //ACTION_TOMORROW
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(lastAppWidgetId, System.currentTimeMillis(), context);
                newTime = currentTime + ONE_DAY_MILLIS;
            }
            if (("" + newTime).substring(0, 7).equalsIgnoreCase(("" + System.currentTimeMillis()).substring(0, 7))) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.INVISIBLE);
            }

            AppWidgetDao.saveAppWidgetCurrentTime(lastAppWidgetId, newTime, context);
            rv.setTextViewText(R.id.tv_date, getDateText(newTime, context));

            appWidgetManager.notifyAppWidgetViewDataChanged(lastAppWidgetId, R.id.lv_day_appwidget);
            appWidgetManager.partiallyUpdateAppWidget(lastAppWidgetId, rv);
        }

        super.onReceive(context, intent);
    }

    public void notifyUpdate(@NonNull Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                DayAppWidgetProvider.class));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void registerNewDayBroadcast(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar midnight = Calendar.getInstance(Locale.getDefault());
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 1); //
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), ONE_DAY_MILLIS, pendingIntent);
    }

    private void unregisterNewDayBroadcast(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private boolean isAlarmManagerNotSet(Context context) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) == null;
    }

    private void updateValues(Intent intent) {
        lastAction = intent.getAction();
        lastAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

}