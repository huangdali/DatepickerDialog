package com.hdl.calendardialog;


import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HDL on 2018/3/6.
 *
 * @author HDL
 */

public class CalendarViewDialog {
    private static CalendarViewDialog mCalendarViewDialog;
    private Dialog dialog;
    private List<Long> marksDays = new ArrayList<>();
    private CalendarView calendarView;
    private Context context;

    private CalendarViewDialog() {
    }

    public static CalendarViewDialog getInstance() {
        if (mCalendarViewDialog == null) {
            synchronized (CalendarViewDialog.class) {
                if (mCalendarViewDialog == null) {
                    mCalendarViewDialog = new CalendarViewDialog();
                }
            }
        }
        return mCalendarViewDialog;
    }

    /**
     * 显示
     *
     * @param context
     * @return
     */
    public CalendarViewDialog init(Context context) {
        if (dialog == null || calendarView == null || this.context != context) {
            this.context = context;
            dialog = new Dialog(context, R.style.DialogTheme);
            View view = View.inflate(context, R.layout.dialog_calendar, null);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
//                    Log.e("hdltag", "onTouch(CalendarViewDialog.java:53):--------------------");
                    return true;
                }
            });
            calendarView = view.findViewById(R.id.robotoCalendarPicker);
            calendarView.setShortWeekDays(true);
            calendarView.showDateTitle(true);
            calendarView.updateView();
            dialog.setContentView(view);
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int displayWidth = dm.widthPixels;
            int displayHeight = dm.heightPixels;
            android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
            p.width = (int) (displayWidth * 0.9);    //宽度设置为屏幕的0.55
            p.height = (int) (displayHeight * 0.7);    //高度设置为屏幕的0.28
            dialog.getWindow().setAttributes(p);     //设置生效
        }
        return this;
    }

    private CalendarView.OnCalendarClickListener onCalendarClickListener;

    public void show(CalendarView.OnCalendarClickListener onCalendarClickListener) {
        this.onCalendarClickListener = onCalendarClickListener;
        if (onCalendarClickListener != null) {
            calendarView.setOnCalendarClickListener(onCalendarClickListener);
        }
        if (dialog != null) {
            dialog.show();
        }
        if (currentTimeMillis!=0) {
//            Log.e("hdltag", "show(CalendarViewDialog.java:86):----------------"+DateUtils.getDateByCurrentTime(calendarView.getCurrentSelectedDay().getTimeInMillis()));
            calendarView.setSelectedDay(calendarView.getCurrentSelectedDay().getTimeInMillis());
        }
    }

    public CalendarViewDialog addMarks(List<Long> marks) {
        marksDays.clear();
        marksDays.addAll(marks);
        if (calendarView != null) {
            calendarView.addMarkDays(marksDays);
        }
        return this;
    }

    public void close() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private boolean isLimitMonth;

    public boolean isLimitMonth() {
        return isLimitMonth;
    }

    public CalendarViewDialog setLimitMonth(boolean limitMonth) {
        isLimitMonth = limitMonth;
        if (calendarView != null) {
            calendarView.setLimitMonth(isLimitMonth);
        }
        return this;
    }

    private long currentTimeMillis;

    public CalendarViewDialog setSelectedDay(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
        if (calendarView != null) {
            calendarView.setSelectedDay(currentTimeMillis);
        }
        return this;
    }
}
