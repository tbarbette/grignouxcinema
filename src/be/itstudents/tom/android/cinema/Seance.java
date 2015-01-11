package be.itstudents.tom.android.cinema;

import android.net.Uri;

public class Seance {

	public static final String SEANCE_ID = "_id";
	public static final String SEANCE_TITLE = "title";
	public static final String SEANCE_DATE = "date";
	public static final String SEANCE_CINEMA = "cinema";
	public static final String SEANCE_FILMID = "filmid";
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itstudents.seances";
	public static final Uri    CONTENT_URI = Uri.parse("content://be.itstudents.tom.android.cinema.service.CinemaProvider");
	
}
