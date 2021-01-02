package com.ulan.timetable.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;

import com.ulan.timetable.R;
import com.ulan.timetable.activities.TeachersActivity;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.receivers.DoNotDisturbReceiversKt;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.ColorPalette;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.PreferenceUtil;
import com.ulan.timetable.utils.WeekUtils;

import java.util.ArrayList;
import java.util.Objects;


/**
 * Created by Ulan on 08.09.2018.
 */
public class WeekAdapter extends ArrayAdapter<Week> {

    @NonNull
    private final AppCompatActivity mActivity;
    private final DbHelper dbHelper;
    @NonNull
    private final ArrayList<Week> weeklist;
    private Week week;
    private final ListView mListView;

    private static class ViewHolder {
        TextView subject;
        TextView teacher;
        TextView time;
        TextView room;
        ImageView popup;
        CardView cardView;
    }

    public WeekAdapter(DbHelper dbHelper, @NonNull AppCompatActivity activity, ListView listView, int resource, @NonNull ArrayList<Week> objects) {
        super(activity, resource, objects);
        this.dbHelper = dbHelper;
        mActivity = activity;
        weeklist = objects;
        mListView = listView;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String subject = Objects.requireNonNull(getItem(position)).getSubject();
        String teacher = Objects.requireNonNull(getItem(position)).getTeacher();
        String time_from = Objects.requireNonNull(getItem(position)).getFromTime();
        String time_to = Objects.requireNonNull(getItem(position)).getToTime();
        String room = Objects.requireNonNull(getItem(position)).getRoom();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        week = new Week(subject, teacher, room, time_from, time_to, color);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.listview_week_adapter, parent, false);
            holder = new ViewHolder();
            holder.subject = convertView.findViewById(R.id.subject);
            holder.teacher = convertView.findViewById(R.id.teacher);
            holder.time = convertView.findViewById(R.id.time);
            holder.room = convertView.findViewById(R.id.room);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            holder.cardView = convertView.findViewById(R.id.week_cardview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Setup colors based on Background
        int textColor = ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK);
        holder.subject.setTextColor(textColor);
        holder.teacher.setTextColor(textColor);
        holder.time.setTextColor(textColor);
        holder.room.setTextColor(textColor);
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.roomimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.teacherimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.timeimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.popupbtn), ColorStateList.valueOf(textColor));
        convertView.findViewById(R.id.line).setBackgroundColor(textColor);


        holder.subject.setText(week.getSubject());
        holder.teacher.setText(week.getTeacher());
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.teacher.setBackgroundResource(outValue.resourceId);
        holder.teacher.setOnClickListener((View v) -> mActivity.startActivity(new Intent(mActivity, TeachersActivity.class)));

        holder.room.setText(week.getRoom());
        holder.room.setOnClickListener(null);

        if (PreferenceUtil.showTimes(getContext()))
            holder.time.setText(WeekUtils.localizeTime(getContext(), week.getFromTime()) + " - " + WeekUtils.localizeTime(getContext(), week.getToTime()));
        else {
            int start = WeekUtils.getMatchingScheduleBegin(week.getFromTime(), getContext());
            int end = WeekUtils.getMatchingScheduleEnd(week.getToTime(), getContext());
            if (start == end) {
                holder.time.setText(start + ". " + getContext().getString(R.string.lesson));
            } else {
                holder.time.setText(start + ".-" + end + ". " + getContext().getString(R.string.lesson));
            }
        }

        holder.cardView.setCardBackgroundColor(week.getColor());
        holder.popup.setOnClickListener(v -> {
            ContextThemeWrapper theme = new ContextThemeWrapper(mActivity, PreferenceUtil.isDark(getContext()) ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
            final PopupMenu popup = new PopupMenu(theme, holder.popup);
            popup.inflate(R.menu.popup_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.delete_popup) {
                        AlertDialogsHelper.getDeleteDialog(getContext(), () -> {
                            dbHelper.deleteWeekById(Objects.requireNonNull(getItem(position)));
                            dbHelper.updateWeek(Objects.requireNonNull(getItem(position)));
                            weeklist.remove(position);
                            notifyDataSetChanged();
                            DoNotDisturbReceiversKt.setDoNotDisturbReceivers(mActivity, false);
                        }, getContext().getString(R.string.delete_week, week.getSubject()));
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
                        AlertDialogsHelper.getEditSubjectDialog(dbHelper, mActivity, alertLayout, () -> notifyDataSetChanged(), weeklist.get(position));
                        notifyDataSetChanged();
                        return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);


        return convertView;
    }

    @NonNull
    public ArrayList<Week> getWeekList() {
        return weeklist;
    }

    public Week getWeek() {
        return week;
    }

    private void hidePopUpMenu(@NonNull ViewHolder holder) {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems.size() > 0) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int key = checkedItems.keyAt(i);
                if (checkedItems.get(key)) {
                    holder.popup.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            holder.popup.setVisibility(View.VISIBLE);
        }
    }

}
