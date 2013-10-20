package be.itstudents.tom.android.cinema.datafetcher;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import be.itstudents.tom.android.cinema.CinemaHoraires;
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.Views.ResultView;
import be.itstudents.tom.android.cinema.Views.SeanceDetailedRow;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;

public class Horaire {
	
	public static Cursor getSeancesAtDate(Calendar cal, Activity a) {
		String columns[] = new String[] { Seance.SEANCE_ID, Seance.SEANCE_TITLE, Seance.SEANCE_DATE, Seance.SEANCE_CINEMA, Seance.SEANCE_FILMID};
		Cursor cur = a.managedQuery(Uri.withAppendedPath(Uri.withAppendedPath(Seance.CONTENT_URI,"seances"),CalendarUtils.dateFormat.format(cal.getTime())), columns, null, null, Seance.SEANCE_DATE + " ASC");
		return cur;
	}

}
