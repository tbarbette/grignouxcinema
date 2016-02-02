package be.itstudents.tom.android.cinema.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.itstudents.tom.android.cinema.Cinema;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;

public class CinemaProvider extends ContentProvider {


    public static final int SEARCH_PERIOD = 14;
    public static final String AUTHORITY = "be.itstudents.tom.android.cinema.service.CinemaProvider";
    private static final String DATABASE_NAME = "plannings.db";
    private static final int DATABASE_VERSION = 59;
    private static final int PRELOAD = 7;
    private static final int SEANCES = 1;
    private static final int SEANCE_DATE = 2;
    private static final int SEANCES_CINEMA = 3;
    private static final int SEANCE_CINEMA = 4;
    private static final int SEANCES_SEARCH = 5;
    private static final int CLOSE = 6;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "preload", PRELOAD);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "seances", SEANCES);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "seances/*/", SEANCE_DATE);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*", SEANCES_CINEMA);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*/seances", SEANCES_CINEMA);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*/seances/*", SEANCE_CINEMA);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "search/*", SEANCES_SEARCH);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "search/", SEANCES_SEARCH);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "close", CLOSE);
    }

    SQLiteDatabase db = null;
    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;

    }

    private boolean haveInternet() {
        NetworkInfo info = ((ConnectivityManager) (getContext().getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return true;
        }
        return true;
    }

    public synchronized void fetchData(Calendar date) {

        if (haveInternet()) {
            prepareDb();
            String columns[] = new String[]{Seance.SEANCE_DATE};

            Calendar today = (Calendar) date.clone();
            CalendarUtils.zero(today);
            Calendar tomorrow = (Calendar) today.clone();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            String where = Seance.SEANCE_DATE + " > \'" + CalendarUtils.dateFormat.format(today.getTime()) + "\' AND " + Seance.SEANCE_DATE + " < \'" + CalendarUtils.dateFormat.format(tomorrow.getTime()) + "\'";
            Cursor c = db.query("seances", columns, where, null, null, null, null);

            if (c != null && c.getCount() > 0) {

                c.close();

            } else {
                c.close();

                //String url = "http://www.grignoux.be/agenda-du-" + CalendarUtils.onlineFormat.format(date.getTime());
                String url = "http://grignoux.be/films.json?date=" + CalendarUtils.onlineFormat.format(date.getTime());
                try {
                    String html = DownloadManager.getString(url);
                    JSONObject jObject = new JSONObject(html);
                    html = jObject.getString("html");
                    Pattern pattern = Pattern.compile("<div class='([a-z]*)'>(.*?)</div>", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(html);


                    while (matcher.find()) {

                        long cinema = 0;
                        if (matcher.group(1).equals("sauveniere")) {
                            cinema = Cinema.SAUVENIERE;
                        } else if (matcher.group(1).equals("parc")) {
                            cinema = Cinema.PARC;
                        } else if (matcher.group(1).equals("churchill")) {
                            cinema = Cinema.CHURCHILL;
                        }

                        //<span class='time'>\n12:00\n</span>\n<br>\n<a class='film_tip' data-id='3326' href='/films/3326'>Comme un lion</a>
                        Pattern pfilms = Pattern.compile("([0-9]{2}):([0-9]{2}).*?data\\-id='([0-9]+)'.*?>(.*?)</a>", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                        Matcher mfilms = pfilms.matcher(matcher.group(2));
                        while (mfilms.find()) {

                            ContentValues values = new ContentValues();
                            values.put(Seance.SEANCE_TITLE, mfilms.group(4).replace("&amp;", "&"));
                            values.put(Seance.SEANCE_FILMID, mfilms.group(3));
                            values.put(Seance.SEANCE_CINEMA, Long.toString(cinema));

                            Calendar seanceDate = Calendar.getInstance();
                            seanceDate.set(date.get(Calendar.YEAR),
                                    date.get(Calendar.MONTH),
                                    date.get(Calendar.DAY_OF_MONTH),
                                    Integer.parseInt(mfilms.group(1)),
                                    Integer.parseInt(mfilms.group(2)));

                            values.put(Seance.SEANCE_DATE, CalendarUtils.dateFormat.format(seanceDate.getTime()));


                            db.insert("seances", "", values);

                        }

                    }
                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }
        }


    }

    public synchronized void prepareDb() {
        if (db == null) {
            db = dbHelper.getWritableDatabase();

        }
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        Calendar thisDay = null;
        Calendar tomorrowDay;
        String where = null;
        switch (sUriMatcher.match(uri)) {
            case SEANCES:
                break;

            case PRELOAD:
                thisDay = Calendar.getInstance();
                CalendarUtils.zero(thisDay);

                break;

            case SEANCE_DATE:
                thisDay = Calendar.getInstance();
                try {
                    thisDay.setTime(CalendarUtils.dateFormat.parse(uri.getPathSegments().get(1)));
                    CalendarUtils.zero(thisDay);
                    tomorrowDay = (Calendar) thisDay.clone();
                    tomorrowDay.add(Calendar.DAY_OF_YEAR, 1);
                    where = Seance.SEANCE_DATE + " > \'" + CalendarUtils.dateFormat.format(thisDay.getTime()) + "\' AND " + Seance.SEANCE_DATE + " < \'" + CalendarUtils.dateFormat.format(tomorrowDay.getTime()) + "\'";
                } catch (ParseException e) {

                    e.printStackTrace();
                }

                break;
            case SEANCES_CINEMA:
                where = Seance.SEANCE_CINEMA + " = " + uri.getPathSegments().get(1);
                break;
            case SEANCE_CINEMA:
                //TODO : Repair
                //thisDay = Long.parseLong(uri.getPathSegments().get(3));
                //   where = Seance.SEANCE_CINEMA+" = "+ uri.getPathSegments().get(1) + " AND " + Seance.SEANCE_DATE+" > \'"+ Long.toString(thisDay) +"\' AND "+Seance.SEANCE_DATE+" < \'"+ Long.toString(thisDay+ 86400000) +"\'";
                break;
            case SEANCES_SEARCH:
                thisDay = Calendar.getInstance();

                CalendarUtils.zero(thisDay);
                Calendar searchTo = (Calendar) thisDay.clone();
                searchTo.add(Calendar.DAY_OF_YEAR, SEARCH_PERIOD);
                String cinemas = "";
                if (uri.getQueryParameter("churchill") != null)
                    cinemas += Cinema.CHURCHILL + ",";
                if (uri.getQueryParameter("sauveniere") != null)
                    cinemas += Cinema.SAUVENIERE + ",";
                if (uri.getQueryParameter("parc") != null)
                    cinemas += Cinema.PARC + ",";
                cinemas = cinemas.substring(0, cinemas.length() - 1);
                where = Seance.SEANCE_CINEMA + " IN (" + cinemas + ") AND " + Seance.SEANCE_DATE + " > \'" + CalendarUtils.dateFormat.format(thisDay.getTime()) + "\' AND " + Seance.SEANCE_DATE + " < \'" + CalendarUtils.dateFormat.format(searchTo.getTime()) + "\'" + (uri.getPathSegments().size() > 1 ? " AND " + Seance.SEANCE_TITLE + " LIKE( \'%" + uri.getPathSegments().get(1) + "%\' )" : "");
                break;
            case CLOSE:
                if (db != null) {
                    db.close();
                    db = null;
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (thisDay != null) {
            fetchData(thisDay);
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("seances");

        prepareDb();
        Cursor c;
        c = qb.query(db, projection, where, selectionArgs, null, null, "date");

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE seances (" + Seance.SEANCE_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + Seance.SEANCE_TITLE + " VARCHAR(255)," + Seance.SEANCE_DATE
                    + " TEXT ,"
                    + Seance.SEANCE_CINEMA + " INTEGER, "
                    + Seance.SEANCE_FILMID + " INTEGER );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS seances");
            onCreate(db);
        }
    }
}