package com.ulan.timetable.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;

/**
 * Created by Ulan on 28.09.2018.
 */
@SuppressLint("AppCompatCustomView")
public class LinedEditText extends EditText {
    @NonNull
    private final Rect mRect;
    @NonNull
    private final Paint mPaint;

    @SuppressLint("ResourceAsColor")
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(PreferenceUtil.getTextColorSecondary(context));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        int height = getHeight();
        int line_height = getLineHeight();

        int count = height / line_height;

        if (getLineCount() > count)
            count = getLineCount();

        Rect r = mRect;
        Paint paint = mPaint;
        int baseline = getLineBounds(0, r);

        for (int i = 0; i < count; i++) {

            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            baseline += getLineHeight();
        }

        super.onDraw(canvas);
    }
}
