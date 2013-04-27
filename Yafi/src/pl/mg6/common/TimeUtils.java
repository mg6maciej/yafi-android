package pl.mg6.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	
	private TimeUtils() {
	}
	
	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public static long getTimestamp() {
		return System.nanoTime() / 1000000L;
	}

	public static String formatTime(int time, boolean appendMillis) {
		StringBuilder builder = new StringBuilder();
		if (time < 0) {
			builder.append('-');
			time = -time;
		}
		int hours = time / 3600000;
		time -= hours * 3600000;
		int minutes = time / 60000;
		time -= minutes * 60000;
		int seconds = time / 1000;
		time -= seconds * 1000;
		int millis = time  / 100;
		if (hours > 0) {
			builder.append(hours);
			builder.append(':');
			if (minutes < 10) {
				builder.append('0');
			}
		}
		builder.append(minutes);
		builder.append(':');
		if (seconds < 10) {
			builder.append('0');
		}
		builder.append(seconds);
		if (appendMillis) {
			builder.append('.');
			builder.append(millis);
		}
		return builder.toString();
	}
	
	public static String formatDate(Date date) {
		DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
		return hourFormat.format(date);
	}
}
