package be.itstudents.tom.android.cinema.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtils {
	public static void zero(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
		cal.set(Calendar.MINUTE, 0);                 // set minute in hour
		cal.set(Calendar.SECOND, 0);                 // set second in minute
		cal.set(Calendar.MILLISECOND, 0);
	}

	public final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public final static DateFormat onlineFormat = new SimpleDateFormat("yyyyMMdd");
	public final static DateFormat jourFormat = new SimpleDateFormat("dd/MM/yyyy");
	public static Calendar parseDate(String string) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateFormat.parse(string));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return cal;
	}
}
