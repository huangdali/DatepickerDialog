package hdl.com.calendardemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdl.calendardialog.CalendarView;
import com.hdl.calendardialog.CalendarViewDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BlankFragment extends Fragment {
    private List<Long> markDays = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 5; i++) {
            markDays.add(System.currentTimeMillis() - i * 24 * 60 * 60 * 1000);
        }
        markDays.add(System.currentTimeMillis() - 12 * 24 * 60 * 60 * 1000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }
    public void onDateChoise(View view){
        CalendarViewDialog.getInstance()
                .init(getActivity())
                .addMarks(markDays)
                .setLimitMonth(true)
                .show(new CalendarView.OnCalendarClickListener() {
                    @Override
                    public void onDayClick(Calendar daySelectedCalendar) {
                        CalendarViewDialog.getInstance().close();
                    }

                    @Override
                    public void onDayNotMarkClick(Calendar daySelectedCalendar) {
                    }
                });
    }


}
