package be.itstudents.tom.android.cinema.views;

import java.util.Calendar;
import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

//NORMAL
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.activity.ScheduleListFragment;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;

public class ResultView extends LinearLayout {
	public TableLayout table;
	public ScrollView scroll;
	private Calendar date;
	public boolean hasData = false;
	private Activity activity;
	
	public Calendar getDate() {
		return date;
	}
	
	public boolean hasData() {
		return this.hasData;
	}
	
	public ResultView(Activity activity, Calendar date, boolean longDate) {
		super(activity);
		this.activity = activity;
		this.date = date;
		this.setOrientation(LinearLayout.VERTICAL);
		
		scroll = new ScrollView(activity);
		scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		table = new TableLayout(activity);
		if (longDate) {
			table.setColumnShrinkable(3, true);
			table.setColumnStretchable(3, true);
		} else {
			table.setColumnShrinkable(2, true);
			table.setColumnStretchable(2, true);
		}
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		scroll.addView(table);
		

		this.addView(scroll);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	}
	

	public boolean isSearch() {
		return date == null;
	}

	
	public void displayResult(Cursor cur) {
		//TODO deprecated
		activity.startManagingCursor(cur);    
    	if (cur == null) {
    		Log.e(ScheduleListFragment.TAG,"Pas de réponses du provider...");
    		return;
    	}
   	
    	try {
        if (!cur.moveToFirst()) {       	
            hasData = false;
            cur.close();
            return;   
        }
    
        
    	int i = 0;
    	int bgColor;
    	double ss = Math.max(getResources().getDisplayMetrics().widthPixels , getResources().getDisplayMetrics().heightPixels ) / getResources().getDisplayMetrics().density;

        do {
        	
        	Film film = new Film(cur.getString(cur.getColumnIndex(Seance.SEANCE_TITLE)), cur.getString(cur.getColumnIndex(Seance.SEANCE_FILMID)));
        	if (ss < 500 || i % 2 == 0) {
        		bgColor = 0xFF050505;
        	} else {
        		bgColor = 0xFF1A1A1A;
        	}
            TableRow tr = new SeanceDetailedRow(activity.getApplicationContext(), film, CalendarUtils.parseDate(cur.getString(cur.getColumnIndex(Seance.SEANCE_DATE))),Long.parseLong(cur.getString(cur.getColumnIndex(Seance.SEANCE_CINEMA))),isSearch(),bgColor);
            table.addView(tr);
            i++;
        } while (cur.moveToNext());

        hasData = true;
        cur.close();
    	} catch (CursorIndexOutOfBoundsException e) {
    		Log.e(ScheduleListFragment.TAG,"Pas de réponses du provider...");
    		return;
    	}
    }
}
