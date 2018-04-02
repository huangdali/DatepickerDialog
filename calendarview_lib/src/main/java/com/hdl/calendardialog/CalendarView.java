package com.hdl.calendardialog;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * calendar view
 *
 * @author hdl
 */
public class CalendarView extends LinearLayout {

    // ************************************************************************************************************************************************************************
    // * Attributes
    // ************************************************************************************************************************************************************************

    // View
    private Context context;
    private TextView dateTitle;
    private ImageView leftButton;
    private ImageView rightButton;
    private View rootView;
    private ViewGroup robotoCalendarMonthLayout;

    // Class
    private OnCalendarClickListener onCalendarClickListener;
    private Calendar currentCalendar;
    private Calendar tempCalendar;
    private Calendar lastSelectedDayCalendar;

    private static final String DAY_OF_THE_WEEK_TEXT = "dayOfTheWeekText";
    private static final String DAY_OF_THE_WEEK_LAYOUT = "dayOfTheWeekLayout";

    private static final String DAY_OF_THE_MONTH_LAYOUT = "dayOfTheMonthLayout";
    private static final String DAY_OF_THE_MONTH_TEXT = "dayOfTheMonthText";
    private static final String DAY_OF_THE_MONTH_BACKGROUND = "dayOfTheMonthBackground";
    private static final String DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 = "dayOfTheMonthCircleImage1";
    private static final String DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 = "dayOfTheMonthCircleImage2";
    /**
     * 当前选中的日期
     */
    private Calendar currentSelectedDay;
    /**
     * 是否限制只显示两个月
     */
    private boolean isLimitMonth = false;

    private boolean shortWeekDays = true;

    // ************************************************************************************************************************************************************************
    // * Initialization methods
    // ************************************************************************************************************************************************************************

    public CalendarView(Context context) {
        super(context);
        this.context = context;
        onCreateView();
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        onCreateView();
    }

    private View onCreateView() {
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflate.inflate(R.layout.calendar_picker_layout, this, true);
        findViewsById(rootView);
        setUpEventListeners();
        setUpCalligraphy();
        return rootView;
    }

    public boolean isLimitMonth() {
        return isLimitMonth;
    }

    public void setLimitMonth(boolean limitMonth) {
        isLimitMonth = limitMonth;
        if (isLimitMonth) {
            rightButton.setVisibility(INVISIBLE);
            Calendar calendar = Calendar.getInstance();
            if (currentCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
                rightButton.setVisibility(VISIBLE);
            }
        } else {
            rightButton.setVisibility(VISIBLE);
        }
    }

    private void findViewsById(View view) {

        robotoCalendarMonthLayout = (ViewGroup) view.findViewById(R.id.robotoCalendarDateTitleContainer);
        leftButton = (ImageView) view.findViewById(R.id.leftButton);
        rightButton = (ImageView) view.findViewById(R.id.rightButton);
        if (isLimitMonth) {
            rightButton.setVisibility(INVISIBLE);
        } else {
            rightButton.setVisibility(VISIBLE);
        }
        dateTitle = (TextView) view.findViewById(R.id.monthText);

        for (int i = 0; i < 42; i++) {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int weekIndex = (i % 7) + 1;
            ViewGroup dayOfTheWeekLayout = (ViewGroup) view.findViewWithTag(DAY_OF_THE_WEEK_LAYOUT + weekIndex);

            // Create day of the month
            View dayOfTheMonthLayout = inflate.inflate(R.layout.calendar_day_of_the_month_layout, null);
            View dayOfTheMonthText = dayOfTheMonthLayout.findViewWithTag(DAY_OF_THE_MONTH_TEXT);
            View dayOfTheMonthBackground = dayOfTheMonthLayout.findViewWithTag(DAY_OF_THE_MONTH_BACKGROUND);
            View dayOfTheMonthCircleImage1 = dayOfTheMonthLayout.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1);
            View dayOfTheMonthCircleImage2 = dayOfTheMonthLayout.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2);

            // Set tags to identify them
            int viewIndex = i + 1;
            dayOfTheMonthLayout.setTag(DAY_OF_THE_MONTH_LAYOUT + viewIndex);
            dayOfTheMonthText.setTag(DAY_OF_THE_MONTH_TEXT + viewIndex);
            dayOfTheMonthBackground.setTag(DAY_OF_THE_MONTH_BACKGROUND + viewIndex);
            dayOfTheMonthCircleImage1.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + viewIndex);
            dayOfTheMonthCircleImage2.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + viewIndex);

            dayOfTheWeekLayout.addView(dayOfTheMonthLayout);
        }
    }

    private void setUpEventListeners() {

        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCalendarClickListener == null) {
                    throw new IllegalStateException("You must assign a valid OnCalendarClickListener first!");
                }

                // Decrease month
                currentCalendar.add(Calendar.MONTH, -1);
                lastSelectedDayCalendar = null;
                updateView();
                onCalendarClickListener.onLeftButtonClick();
                updateMark();
                if (isLimitMonth) {
                    leftButton.setVisibility(INVISIBLE);
                    rightButton.setVisibility(VISIBLE);
                }
            }
        });

        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCalendarClickListener == null) {
                    throw new IllegalStateException("You must assign a valid OnCalendarClickListener first!");
                }

                // Increase month
                currentCalendar.add(Calendar.MONTH, 1);
                lastSelectedDayCalendar = null;
                updateView();
                onCalendarClickListener.onRightButtonClick();
                updateMark();
                if (isLimitMonth) {
                    leftButton.setVisibility(VISIBLE);
                    rightButton.setVisibility(INVISIBLE);
                }
            }
        });
    }

    /**
     * 当前日期的开始时间毫秒值
     */
    private long currentTimeMillis = 0;

    /**
     * 初始化当前时间
     */
    private void setUpCalligraphy() {
        // Initialize calendar for current month
        Calendar currentCalendar = Calendar.getInstance();
        tempCalendar = Calendar.getInstance();
        setCalendar(currentCalendar);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        currentTimeMillis = DateUtils.getTodayStart(currentCalendar.getTimeInMillis());
    }

    // ************************************************************************************************************************************************************************
    // * Auxiliary UI methods
    // ************************************************************************************************************************************************************************

    /**
     * 设置标题
     */
    private void setUpMonthLayout() {
        String dateText = new DateFormatSymbols(Locale.getDefault()).getShortMonths()[currentCalendar.get(Calendar.MONTH)];
        dateText = dateText.substring(0, 1).toUpperCase() + dateText.subSequence(1, dateText.length());
        dateTitle.setText(String.format("%s / %s", currentCalendar.get(Calendar.YEAR), dateText));
    }

    /**
     * 设置  周 栏的值
     */
    private void setUpWeekDaysLayout() {
        TextView dayOfWeek;
        String dayOfTheWeekString;
        String[] weekDaysArray = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
        for (int i = 1; i < weekDaysArray.length; i++) {
            dayOfWeek = (TextView) rootView.findViewWithTag(DAY_OF_THE_WEEK_TEXT + getWeekIndex(i, currentCalendar));
            dayOfTheWeekString = weekDaysArray[i].replace("周", "");
            dayOfWeek.setText(dayOfTheWeekString);
        }
    }

    private void setUpDaysOfMonthLayout() {

        TextView dayOfTheMonthText;
        View circleImage1;
        View circleImage2;
        ViewGroup dayOfTheMonthContainer;
        ViewGroup dayOfTheMonthBackground;

        for (int i = 1; i < 43; i++) {

            dayOfTheMonthContainer = (ViewGroup) rootView.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i);
            dayOfTheMonthBackground = (ViewGroup) rootView.findViewWithTag(DAY_OF_THE_MONTH_BACKGROUND + i);
            dayOfTheMonthText = (TextView) rootView.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i);
            circleImage1 = rootView.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + i);
            circleImage2 = rootView.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + i);

            dayOfTheMonthText.setVisibility(View.INVISIBLE);
            circleImage1.setVisibility(View.INVISIBLE);
            circleImage2.setVisibility(View.GONE);

            // Apply styles
            dayOfTheMonthText.setBackgroundResource(android.R.color.transparent);
            dayOfTheMonthText.setTypeface(null, Typeface.NORMAL);
            dayOfTheMonthText.setTextColor(ContextCompat.getColor(context, R.color.roboto_calendar_day_of_the_month_font));
            dayOfTheMonthContainer.setBackgroundResource(android.R.color.transparent);
            dayOfTheMonthContainer.setOnClickListener(null);
            dayOfTheMonthBackground.setBackgroundResource(android.R.color.transparent);
        }
    }

    /**
     * 设置天数
     */
    private void setUpDaysInCalendar() {
        Calendar auxCalendar = Calendar.getInstance(Locale.getDefault());
        auxCalendar.setTime(currentCalendar.getTime());
        auxCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfMonth = auxCalendar.get(Calendar.DAY_OF_WEEK);
        TextView dayOfTheMonthText;
        ViewGroup dayOfTheMonthContainer;
        ViewGroup dayOfTheMonthLayout;

        // Calculate dayOfTheMonthIndex
        int dayOfTheMonthIndex = getWeekIndex(firstDayOfMonth, auxCalendar);
        for (int i = 1; i <= auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++, dayOfTheMonthIndex++) {
            dayOfTheMonthContainer = (ViewGroup) rootView.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + dayOfTheMonthIndex);
            dayOfTheMonthText = (TextView) rootView.findViewWithTag(DAY_OF_THE_MONTH_TEXT + dayOfTheMonthIndex);
            if (dayOfTheMonthText == null) {
                break;
            }
            dayOfTheMonthContainer.setOnClickListener(onDayOfMonthClickListener);
//            dayOfTheMonthContainer.setOnLongClickListener(onDayOfMonthLongClickListener);
            dayOfTheMonthText.setVisibility(View.VISIBLE);
            dayOfTheMonthText.setText(String.valueOf(i));
            tempCalendar.set(Calendar.YEAR, auxCalendar.get(Calendar.YEAR));
            tempCalendar.set(Calendar.MONTH, auxCalendar.get(Calendar.MONTH));
            tempCalendar.set(Calendar.DAY_OF_MONTH, i);
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
            tempCalendar.set(Calendar.MINUTE, 0);
            tempCalendar.set(Calendar.SECOND, 0);
            tempCalendar.set(Calendar.MILLISECOND, 0);
            if (currentTimeMillis >= tempCalendar.getTimeInMillis()) {
                //可选范围(今天之前的可选)
                dayOfTheMonthText.setTextColor(context.getResources().getColor(R.color.roboto_calendar_day_of_the_month_font));
            } else {
                dayOfTheMonthText.setTextColor(context.getResources().getColor(R.color.roboto_calendar_day_of_the_month_font_p));
                //今天之后的天数，不可选
            }
        }

        for (int i = 36; i < 43; i++) {
            dayOfTheMonthText = (TextView) rootView.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i);
            dayOfTheMonthLayout = (ViewGroup) rootView.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i);
            if (dayOfTheMonthText.getVisibility() == INVISIBLE) {
                dayOfTheMonthLayout.setVisibility(GONE);
            } else {
                dayOfTheMonthLayout.setVisibility(VISIBLE);
            }
        }
    }

    private void markDayAsCurrentDay() {
        // If it's the current month, mark current day
        Calendar nowCalendar = Calendar.getInstance();
        if (nowCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) && nowCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(nowCalendar.getTime());

            ViewGroup dayOfTheMonthBackground = getDayOfMonthBackground(currentCalendar);
            dayOfTheMonthBackground.setBackgroundResource(R.drawable.ring);
        }
    }

    public Calendar getCurrentSelectedDay() {
        return currentSelectedDay;
    }

    public void setSelectedDay(long currentTimeMillis) {
        //获取当前的
        int curMonth = 0;
        if (currentCalendar != null) {
            curMonth = currentCalendar.get(Calendar.MONTH);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTimeMillis));
        this.currentSelectedDay = calendar;
//        Log.e("hdltag", "setSelectedDay(CalendarView.java:342):" + DateUtils.getDateByCurrentTime(currentCalendar.getTimeInMillis()));
//        Log.e("hdltag", "setSelectedDay(CalendarView.java:343):" + DateUtils.getDateByCurrentTime(currentTimeMillis));
        if (curMonth == currentSelectedDay.get(Calendar.MONTH)) {//当前月份才标记
//            Log.e("hdltag", "setSelectedDay(CalendarView.java:343):当月，开始标记");
            markDayAsSelectedDay(currentSelectedDay);
        } else {
            if (lastClearSelectedDay != null) {
                clearSelectedDay(lastClearSelectedDay);
            }
        }
    }

    private Calendar lastClearSelectedDay;

    private void markDayAsSelectedDay(Calendar calendar) {
        lastClearSelectedDay = calendar;
//        Log.e("hdltag", "markDayAsSelectedDay(CalendarView.java:351):标记这一天为选中了" + DateUtils.getDateByCurrentTime(calendar.getTimeInMillis()));
        // Clear previous current day mark
        clearSelectedDay(lastSelectedDayCalendar);

        // Store current values as last values
        lastSelectedDayCalendar = calendar;

        // Mark current day as selected
        ViewGroup dayOfTheMonthBackground = getDayOfMonthBackground(calendar);
        dayOfTheMonthBackground.setBackgroundResource(R.drawable.circle_ring);

        TextView dayOfTheMonth = getDayOfMonthText(calendar);
        dayOfTheMonth.setTextColor(ContextCompat.getColor(context, R.color.roboto_calendar_selected_day_font));

        ImageView circleImage1 = getCircleImage1(calendar);
        ImageView circleImage2 = getCircleImage2(calendar);
//        if (circleImage1.getVisibility() == VISIBLE) {
//            DrawableCompat.setTint(circleImage1.getDrawable(), ContextCompat.getColor(context, R.color.roboto_calendar_selected_day_font));
//        }

        if (circleImage2.getVisibility() == VISIBLE) {
            DrawableCompat.setTint(circleImage2.getDrawable(), ContextCompat.getColor(context, R.color.roboto_calendar_selected_day_font));
        }
    }

    private void clearSelectedDay(Calendar calendar) {
        if (calendar != null) {
            ViewGroup dayOfTheMonthBackground = getDayOfMonthBackground(calendar);
            // If it's today, keep the current day style
            Calendar nowCalendar = Calendar.getInstance();
            if (nowCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && nowCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
//                Log.e("hdltag", "clearSelectedDay(CalendarView.java:393):------------当月的----------");
                if (currentCalendar.get(Calendar.MONTH)==calendar.get(Calendar.MONTH)) {
                    dayOfTheMonthBackground.setBackgroundResource(R.drawable.ring);
                }else {
                    dayOfTheMonthBackground.setBackgroundResource(android.R.color.transparent);
                }
            } else {
                dayOfTheMonthBackground.setBackgroundResource(android.R.color.transparent);
            }
            TextView dayOfTheMonth = getDayOfMonthText(calendar);
            dayOfTheMonth.setTextColor(ContextCompat.getColor(context, R.color.roboto_calendar_day_of_the_month_font));
            ImageView circleImage1 = getCircleImage1(calendar);
            ImageView circleImage2 = getCircleImage2(calendar);
            if (circleImage1.getVisibility() == VISIBLE) {
                DrawableCompat.setTint(circleImage1.getDrawable(), ContextCompat.getColor(context, R.color.roboto_calendar_circle_1));
            }

            if (circleImage2.getVisibility() == VISIBLE) {
                DrawableCompat.setTint(circleImage2.getDrawable(), ContextCompat.getColor(context, R.color.roboto_calendar_circle_2));
            }
        }
    }

    private String checkSpecificLocales(String dayOfTheWeekString, int i) {
        // Set Wednesday as "X" in Spanish Locale.getDefault()
        if (i == 4 && Locale.getDefault().getCountry().equals("ES")) {
            dayOfTheWeekString = "X";
        } else {
            dayOfTheWeekString = dayOfTheWeekString.substring(0, 1).toUpperCase();
        }
        return dayOfTheWeekString;
    }

    // ************************************************************************************************************************************************************************
    // * Public calendar methods
    // ************************************************************************************************************************************************************************

    /**
     * Set an specific calendar to the view
     *
     * @param calendar
     */
    public void setCalendar(Calendar calendar) {
        this.currentCalendar = calendar;
        updateView();
    }

    /**
     * Update the calendar view
     */
    public void updateView() {
        setUpMonthLayout();
        setUpWeekDaysLayout();
        setUpDaysOfMonthLayout();
        setUpDaysInCalendar();
        markDayAsCurrentDay();
    }

    public void setShortWeekDays(boolean shortWeekDays) {
        this.shortWeekDays = shortWeekDays;
    }

    /**
     * Clear the view of marks and selections
     */
    public void clearCalendar() {
        updateView();
    }

    public void markCircleImage1(Calendar calendar) {
        if (currentCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && currentCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
            //同一个月的才画
            ImageView circleImage1 = getCircleImage1(calendar);
            circleImage1.setVisibility(View.VISIBLE);
            DrawableCompat.setTint(circleImage1.getDrawable(), ContextCompat.getColor(context, R.color.roboto_calendar_circle_1));
        }
    }

    public void showDateTitle(boolean show) {
        if (show) {
            robotoCalendarMonthLayout.setVisibility(VISIBLE);
        } else {
            robotoCalendarMonthLayout.setVisibility(GONE);
        }
    }

    private List<Long> markDays = new ArrayList<>();

    /**
     * 添加标记的天数
     *
     * @param markDays
     */
    public void addMarkDays(List<Long> markDays) {
        this.markDays.addAll(markDays);
        updateMark();
    }

    private void updateMark() {
        for (Long markDay : markDays) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(markDay);
            markCircleImage1(calendar);
        }
        if (currentSelectedDay != null) {
            setSelectedDay(currentSelectedDay.getTimeInMillis());
        }
    }

    // ************************************************************************************************************************************************************************
    // * Public interface
    // ************************************************************************************************************************************************************************

    public abstract static class OnCalendarClickListener {
        /**
         * 点击某一天了
         *
         * @param daySelectedCalendar
         */
        public abstract void onDayClick(Calendar daySelectedCalendar);

        /**
         * 没有标记的回调
         *
         * @param daySelectedCalendar
         */
        public void onDayNotMarkClick(Calendar daySelectedCalendar) {
        }

        /**
         * 点击右边箭头
         */
        public void onRightButtonClick() {
        }

        /**
         * 点击左边箭头
         */
        public void onLeftButtonClick() {
        }
    }

    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        this.onCalendarClickListener = onCalendarClickListener;
    }

    // ************************************************************************************************************************************************************************
    // * Event handler methods
    // ************************************************************************************************************************************************************************

    private OnClickListener onDayOfMonthClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            // Extract day selected
            ViewGroup dayOfTheMonthContainer = (ViewGroup) view;
            String tagId = (String) dayOfTheMonthContainer.getTag();
            tagId = tagId.substring(DAY_OF_THE_MONTH_LAYOUT.length(), tagId.length());
            TextView dayOfTheMonthText = (TextView) view.findViewWithTag(DAY_OF_THE_MONTH_TEXT + tagId);

            // Extract the day from the text
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfTheMonthText.getText().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if (currentTimeMillis >= calendar.getTimeInMillis()) {
                currentSelectedDay = calendar;
                //只能选择今天之前的日期
                markDayAsSelectedDay(calendar);
                // Fire event
                if (onCalendarClickListener == null) {
                    throw new IllegalStateException("You must assign a valid OnCalendarClickListener first!");
                } else {
                    boolean isExisted = false;
                    for (Long markDay : markDays) {
                        if (DateUtils.getTodayStart(markDay) == calendar.getTimeInMillis()) {
                            isExisted = true;
                            break;
                        }
                    }
                    if (isExisted) {
                        //由于点击之后马上关闭的话看不出选中的效果，所以延迟100ms再回调点击事件
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onCalendarClickListener.onDayClick(calendar);
                            }
                        }, 100);
                    } else {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onCalendarClickListener.onDayNotMarkClick(calendar);
                            }
                        }, 100);
                    }
                }
            }
        }
    };

    private OnLongClickListener onDayOfMonthLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            // Extract day selected
            ViewGroup dayOfTheMonthContainer = (ViewGroup) view;
            String tagId = (String) dayOfTheMonthContainer.getTag();
            tagId = tagId.substring(DAY_OF_THE_MONTH_LAYOUT.length(), tagId.length());
            TextView dayOfTheMonthText = (TextView) view.findViewWithTag(DAY_OF_THE_MONTH_TEXT + tagId);

            // Extract the day from the text
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfTheMonthText.getText().toString()));

            markDayAsSelectedDay(calendar);

            // Fire event
            if (onCalendarClickListener == null) {
                throw new IllegalStateException("You must assign a valid OnCalendarClickListener first!");
            } else {
                onCalendarClickListener.onDayNotMarkClick(calendar);
            }
            return true;
        }
    };

    // ************************************************************************************************************************************************************************
    // * Getter methods
    // ************************************************************************************************************************************************************************

    private ViewGroup getDayOfMonthBackground(Calendar currentCalendar) {
        return (ViewGroup) getView(DAY_OF_THE_MONTH_BACKGROUND, currentCalendar);
    }

    private TextView getDayOfMonthText(Calendar currentCalendar) {
        return (TextView) getView(DAY_OF_THE_MONTH_TEXT, currentCalendar);
    }

    private ImageView getCircleImage1(Calendar currentCalendar) {
        return (ImageView) getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1, currentCalendar);
    }

    private ImageView getCircleImage2(Calendar currentCalendar) {
        return (ImageView) getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2, currentCalendar);
    }

    private int getDayIndexByDate(Calendar currentCalendar) {
        int monthOffset = getMonthOffset(currentCalendar);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        return currentDay + monthOffset;
    }

    private int getMonthOffset(Calendar currentCalendar) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentCalendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayWeekPosition = calendar.getFirstDayOfWeek();
        int dayPosition = calendar.get(Calendar.DAY_OF_WEEK);

        if (firstDayWeekPosition == 1) {
            return dayPosition - 1;
        } else {

            if (dayPosition == 1) {
                return 6;
            } else {
                return dayPosition - 2;
            }
        }
    }

    private int getWeekIndex(int weekIndex, Calendar currentCalendar) {
        int firstDayWeekPosition = currentCalendar.getFirstDayOfWeek();

        if (firstDayWeekPosition == 1) {
            return weekIndex;
        } else {

            if (weekIndex == 1) {
                return 7;
            } else {
                return weekIndex - 1;
            }
        }
    }

    private View getView(String key, Calendar currentCalendar) {
        int index = getDayIndexByDate(currentCalendar);
        return rootView.findViewWithTag(key + index);
    }
}
