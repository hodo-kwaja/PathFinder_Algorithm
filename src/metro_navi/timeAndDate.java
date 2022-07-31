package metro_navi;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

class timeAndDate {
    int hour;
    int minute;
    char weekType;
    LocalTime nowTime = LocalTime.now();
    LocalDate nowDate = LocalDate.now();
    void getHour() {
        hour  = nowTime.getHour();
    }
    void getMinute() {
        minute = nowTime.getMinute();
    }
    void getDayOfWeek() {
        //나중에 공휴일 처리하자
        DayOfWeek dayOfWeek = nowDate.getDayOfWeek();
        int dayOfWeekNumber = dayOfWeek.getValue();
        System.out.println(dayOfWeekNumber);
        if (dayOfWeekNumber == 7) {
            weekType = 'U';
        }
        else if (dayOfWeekNumber == 6) {
            weekType = 'A';
        }
        else {
            weekType = 'W';
        }
    }

    void calculateTime(int hour, int minute, timeTable schedule) {
        schedule.hour = hour;
        schedule.minute = schedule.minute + minute;
        if(schedule.minute >= 60) {
            schedule.minute = schedule.minute - 60;
            schedule.hour = schedule.hour + 1;
        }
    }
}
