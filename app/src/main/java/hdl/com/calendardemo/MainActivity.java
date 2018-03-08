package hdl.com.calendardemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.hdl.calendardialog.CalendarView;
import com.hdl.calendardialog.CalendarViewDialog;
import com.hdl.calendardialog.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private List<Long>  markDays = new ArrayList<>();
    //    private RobotoCalendarView robotoCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //默认显示本地回放
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_parent,new BlankFragment()).commit();

//        // Gets the calendar from the view
//        robotoCalendarView = (RobotoCalendarView) findViewById(R.id.robotoCalendarPicker);
//
//        // Set listener, in this case, the same activity
//        robotoCalendarView.show(this);
//
//        robotoCalendarView.setShortWeekDays(true);
//
//        robotoCalendarView.showDateTitle(true);
//
//        robotoCalendarView.updateView();
        for (int i = 0; i < 5; i++) {
            markDays.add(System.currentTimeMillis() - i * 24 * 60 * 60 * 1000);
        }
        markDays.add(System.currentTimeMillis() - 12 * 24 * 60 * 60 * 1000);
//        robotoCalendarView.addMarkDays(markDays);
    }


    public void onDateChoise(View view) {
        CalendarViewDialog.getInstance()
                .init(this)
                .addMarks(markDays)
                .setLimitMonth(true)
                .show(new CalendarView.OnCalendarClickListener() {
                    @Override
                    public void onDayClick(Calendar daySelectedCalendar) {
                        CalendarViewDialog.getInstance().close();
                        Toast.makeText(MainActivity.this, "选择的天数 : " + DateUtils.getDateTime(daySelectedCalendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDayNotMarkClick(Calendar daySelectedCalendar) {
                        Toast.makeText(MainActivity.this, "当前时间无回放（没有标记）", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
