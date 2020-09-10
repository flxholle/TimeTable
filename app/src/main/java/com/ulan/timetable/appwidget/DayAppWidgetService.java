package com.ulan.timetable.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ulan.timetable.R;
import com.ulan.timetable.appwidget.Dao.AppWidgetDao;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.profiles.ProfileManagement;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.PreferenceUtil;
import com.ulan.timetable.utils.WeekUtils;

import java.util.ArrayList;
import java.util.Calendar;

import static com.ulan.timetable.utils.NotificationUtil.getCurrentDay;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
public class DayAppWidgetService extends RemoteViewsService {

    @NonNull
    @Override
    public RemoteViewsFactory onGetViewFactory(@NonNull Intent intent) {
        return new DayAppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    private static class DayAppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context mContext;
        private ArrayList<Week> content;
        private final int mAppWidgetId;

        DayAppWidgetRemoteViewsFactory(Context context, @NonNull Intent intent) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            mContext = context;
        }

        @Override
        public void onCreate() {
            long currentTime = AppWidgetDao.getAppWidgetCurrentTime(mAppWidgetId, System.currentTimeMillis(), mContext);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(currentTime);
            ProfileManagement.initProfiles(mContext);
            int profile = AppWidgetDao.getAppWidgetProfile(mAppWidgetId, ProfileManagement.loadPreferredProfilePosition(), mContext);
            content = new DbHelper(mContext, profile).getWeek(getCurrentDay(calendar.get(Calendar.DAY_OF_WEEK)), calendar);
        }

        @Override
        public void onDataSetChanged() {
            onCreate();
        }

        @Override
        public void onDestroy() {
            content.clear();
        }

        @Override
        public int getCount() {
            return content.size();
        }

        @NonNull
        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_day_appwidget);
            Week week = content.get(position);

//        String lessons = getLessons(content, mContext);
            if (week != null) {
                String time;
                if (PreferenceUtil.showTimes(mContext))
                    time = week.getFromTime() + " - " + week.getToTime();
                else {
                    int start = WeekUtils.getMatchingScheduleBegin(week.getFromTime(), mContext);
                    int end = WeekUtils.getMatchingScheduleEnd(week.getToTime(), mContext);
                    if (start == end) {
                        time = start + ". " + mContext.getString(R.string.lesson);
                    } else {
                        time = start + ".-" + end + ". " + mContext.getString(R.string.lesson);
                    }
                }

                StringBuilder text = new StringBuilder(week.getSubject()).append(": ").append(time);
                if (!week.getRoom().trim().isEmpty())
                    text.append(", ").append(week.getRoom());
                if (!week.getTeacher().trim().isEmpty())
                    text.append(" (").append(week.getTeacher()).append(")");
                rv.setTextViewText(R.id.widget_text, text.toString());
            }

            //Set OpenApp Button intent
            Intent intent = new Intent();
            intent.putExtra("keyData", position);
            rv.setOnClickFillInIntent(R.id.widget_linear, intent);
            return rv;
        }

        @Nullable
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}