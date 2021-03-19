package com.gamingmesh.jobs.stuff;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.gamingmesh.jobs.Jobs;

public class TimeManage {

    public static int timeInInt() {
	return timeInInt(System.currentTimeMillis());
    }

    public static int timeInInt(Long time) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeInMillis(time);
	return Integer.valueOf(new SimpleDateFormat("YYMMdd").format(calendar.getTime()));
    }

    public static String to24hourShort(Long ticks) {
	long years = ticks / 1000 / 60 / 60 / 24 / 365;
	ticks = ticks - (years * 1000 * 60 * 60 * 24 * 365);

	long days = ticks / 1000 / 60 / 60 / 24;
	ticks = ticks - (days * 1000 * 60 * 60 * 24);

	long hours = ticks / 1000 / 60 / 60;
	ticks = ticks - (hours * 1000 * 60 * 60);

	long minutes = ticks / 1000 / 60;
	ticks = ticks - (minutes * 1000 * 60);

	long sec = ticks / 1000;
	ticks = ticks - (sec * 1000);

	String time = "";

	if (days > 0)
	    time += Jobs.getLanguage().getMessage("general.info.time.days", "%days%", days);

	if (hours > 0 || (minutes > 0 || sec > 0) && days != 0 && hours == 0)
	    time += Jobs.getLanguage().getMessage("general.info.time.hours", "%hours%", hours);

	if (minutes > 0 || sec > 0 && minutes == 0 && (hours != 0 || days != 0))
	    time += Jobs.getLanguage().getMessage("general.info.time.mins", "%mins%", minutes);

	if (sec > 0)
	    time += Jobs.getLanguage().getMessage("general.info.time.secs", "%secs%", sec);

	if (time.isEmpty())
	    time += Jobs.getLanguage().getMessage("general.info.time.secs", "%secs%", 0);

	return time;
    }
}
