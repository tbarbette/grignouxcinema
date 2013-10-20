package be.itstudents.tom.android.cinema;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;

public class CinemaProvider extends ContentProvider {

    private static final String TAG = "CinemaProvider";

    private static final String DATABASE_NAME = "plannings.db";

    private static final int DATABASE_VERSION = 49;

    private static final int SEARCH_PERIOD = 14;
    
    public static final String AUTHORITY = "be.itstudents.tom.android.cinema.CinemaProvider";

    private static final Semaphore sem = new Semaphore(1);
    
    private static final int SEANCES = 1;
    private static final int SEANCE_DATE = 2;
    private static final int SEANCES_CINEMA = 3;
    private static final int SEANCE_CINEMA = 4;
    private static final int SEANCES_SEARCH = 5;
    private static final int CLOSE = 6;
    private static final UriMatcher sUriMatcher;

    static {
	    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	    sUriMatcher.addURI(CinemaProvider.AUTHORITY, "seances", SEANCES);
	    sUriMatcher.addURI(CinemaProvider.AUTHORITY, "seances/*/", SEANCE_DATE);
	    sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*", SEANCES_CINEMA);
	    sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*/seances", SEANCES_CINEMA);
	    sUriMatcher.addURI(CinemaProvider.AUTHORITY, "cinema/*/seances/*", SEANCE_CINEMA);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "search/*", SEANCES_SEARCH);
        sUriMatcher.addURI(CinemaProvider.AUTHORITY, "close", CLOSE);
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
        NetworkInfo info=((ConnectivityManager)(getContext().getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();  
        if(info==null || !info.isConnected()){  
            return false;  
        }  
        if(info.isRoaming()){  
            return true;  
        }
        return true;  
    } 
    
    public void fetchData(Calendar date){

        if (haveInternet()) {
               prepareDb();
                String columns[] = new String[] { Seance.SEANCE_DATE};
                
                Calendar today = (Calendar)date.clone();
                CalendarUtils.zero(today);
                Calendar tomorrow = (Calendar)today.clone();
                tomorrow.add(Calendar.DATE, 1);
              
                Cursor c = db.query("seances", columns, Seance.SEANCE_DATE+" > \'"+ CalendarUtils.dateFormat.format(today.getTime()) +"\' AND "+Seance.SEANCE_DATE+" < \'"+ CalendarUtils.dateFormat.format(tomorrow.getTime()) +"\'", null, null, null, null);

                if (c.moveToFirst()) {
                	if (CinemaHoraires.log) Log.i(TAG, "Séances de cinema pour pour le "+CalendarUtils.jourFormat.format(date.getTime())+" déjà  dans la base.");
                        c.close();
                        db.close();
                        db = null;
                } else {
                		c.close();
                        db.close();
                        db = null;
                        //String url = "http://www.grignoux.be/agenda-du-" + CalendarUtils.onlineFormat.format(date.getTime());
                        String url = "http://grignoux.be/films.json?date=" + CalendarUtils.onlineFormat.format(date.getTime());
                        try{                            
                            String html = DownloadManager.getString(url);
                            JSONObject jObject = new JSONObject(html);
                            html = jObject.getString("html");
                            Pattern pattern = Pattern.compile("<div class='([a-z]*)'>(.*?)</div>",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(html);
                            
                            
                            int i =0;
                            while (matcher.find()) {
                            	 
                            	long cinema = 0;
                            	if ( matcher.group(1).equals("sauveniere")) {
                            		cinema = Cinema.SAUVENIERE;
                            	} else if ( matcher.group(1).equals("parc")) {
                            		cinema = Cinema.PARC;
                            	} else if ( matcher.group(1).equals("churchill")) {
                            		cinema = Cinema.CHURCHILL;
                            	}
                            
                            	//<span class='time'>\n12:00\n</span>\n<br>\n<a class='film_tip' data-id='3326' href='/films/3326'>Comme un lion</a>
                                Pattern pfilms = Pattern.compile("([0-9]{2}):([0-9]{2}).*?data\\-id='([0-9]+)'.*?>(.*?)</a>",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                                Matcher mfilms = pfilms.matcher(matcher.group(2));
                                while (mfilms.find()) {
                                	
                                        ContentValues values = new ContentValues();
                                    values.put(Seance.SEANCE_TITLE,mfilms.group(4).replace("&amp;","&"));
                                    values.put(Seance.SEANCE_FILMID, mfilms.group(3));
                                    values.put(Seance.SEANCE_CINEMA,Long.toString(cinema));
                                        

                                    
                                    Calendar seanceDate = Calendar.getInstance();
                                    seanceDate.set(	date.get(Calendar.YEAR),
                                    				date.get(Calendar.MONTH),
                                    				date.get(Calendar.DAY_OF_MONTH),
                                    				Integer.parseInt(mfilms.group(1)),
                                    				Integer.parseInt(mfilms.group(2)));
                       
                                    values.put(Seance.SEANCE_DATE, CalendarUtils.dateFormat.format(seanceDate.getTime()));
                                    if (db != null) {
                                    	db.close();
                                    	db = null;
                                    }
                                        db= dbHelper.getWritableDatabase();
                                        db.insert("seances", "", values);
                                        db.close();
                                        db = null;
                                        i++;
                                }

                            }
                            if (CinemaHoraires.log) Log.i(TAG, i + " séances de cinema récupérées pour le "+CalendarUtils.jourFormat.format(date.getTime())+".");

                        }catch(Exception ex){

                        	if (CinemaHoraires.log) Log.e(TAG, "Impossible de récupérer le planning !");
                            ex.printStackTrace();
                        }
                }
        }
       

    }
    SQLiteDatabase db = null;
    public void prepareDb() {
    	if (db==null) {
            db = dbHelper.getReadableDatabase();
            
    	}
    }
    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    	
    	try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	Calendar thisDay = null;
		Calendar tomorrowDay;
        String where = null;
        switch (sUriMatcher.match(uri)) {
        case SEANCES:
            break;

        case SEANCE_DATE:
        		thisDay = Calendar.getInstance();
			try {
				thisDay.setTime(CalendarUtils.dateFormat.parse(uri.getPathSegments().get(1)));
				CalendarUtils.zero(thisDay); 
                tomorrowDay = (Calendar)thisDay.clone();
                tomorrowDay.add(Calendar.DATE, 1);
                where = Seance.SEANCE_DATE+" > \'"+CalendarUtils.dateFormat.format(thisDay.getTime()) +"\' AND "+Seance.SEANCE_DATE+" < \'"+ CalendarUtils.dateFormat.format(tomorrowDay.getTime()) +"\'";
} catch (ParseException e) {
				
				e.printStackTrace();
			}

            break;
        case SEANCES_CINEMA:
                 where =  Seance.SEANCE_CINEMA+" = "+ uri.getPathSegments().get(1);
            break;
        case SEANCE_CINEMA:
        	//TODO : Repair
        	//thisDay = Long.parseLong(uri.getPathSegments().get(3));
             //   where = Seance.SEANCE_CINEMA+" = "+ uri.getPathSegments().get(1) + " AND " + Seance.SEANCE_DATE+" > \'"+ Long.toString(thisDay) +"\' AND "+Seance.SEANCE_DATE+" < \'"+ Long.toString(thisDay+ 86400000) +"\'";
            break;
        case SEANCES_SEARCH:
        	 thisDay = Calendar.getInstance();
	
				CalendarUtils.zero(thisDay); 
				Calendar searchTo = (Calendar)thisDay.clone();
				searchTo.add(Calendar.DATE, SEARCH_PERIOD);
                where = Seance.SEANCE_DATE+" > \'"+ CalendarUtils.dateFormat.format(thisDay.getTime()) +"\' AND "+Seance.SEANCE_DATE+" < \'"+ CalendarUtils.dateFormat.format(searchTo.getTime()) +"\' AND " + Seance.SEANCE_TITLE+" LIKE( \'%"+ uri.getPathSegments().get(1) +"%\' )";
                break;
        case CLOSE:
        	if (db != null) {
        		db.close();
        		db = null;
        	}
      
        	break;
        default:
        	 sem.release();
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
        sem.release();
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }
}