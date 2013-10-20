package be.itstudents.tom.android.cinema.Views;

import java.util.Calendar;
import java.util.Date;



import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

//NORMAL
import be.itstudents.tom.android.cinema.CinemaHoraires;
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;

public class ResultView extends LinearLayout {
	public TableLayout table;
	public TextView text;
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
		text = new HeaderBar(activity);		
		this.addView(text);
		
		scroll = new ScrollView(activity);
		scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		table = new TableLayout(activity);
		if (longDate) {
			table.setColumnShrinkable(3, true);
			table.setColumnStretchable(3, true);
		} else {
			table.setColumnShrinkable(2, true);
			table.setColumnStretchable(2, true);
		}
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		scroll.addView(table);
		

		this.addView(scroll);
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		refreshDate(date);
	}
	
    private void refreshDate(Calendar date) {
        if (date != null) {
        String jour = (String)android.text.format.DateFormat.format(
                                "EEEE " +
                                android.text.format.DateFormat.DATE + android.text.format.DateFormat.DATE +
                                "/" +
                                android.text.format.DateFormat.MONTH + android.text.format.DateFormat.MONTH +
                                "/" +
                                android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR
                                ,date);
                jour = jour.substring(0,1).toUpperCase() + jour.substring(1);
                text.setText(jour);
        } else {
                text.setText(R.string.results);
        }
    }

	public boolean isSearch() {
		return date == null;
	}

	
	public void displayResult(Cursor cur) {
		activity.startManagingCursor(cur);    
    	if (cur == null) {
    		Log.e(CinemaHoraires.TAG,"Pas de réponses du provider...");
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
    		Log.e(CinemaHoraires.TAG,"Pas de réponses du provider...");
    		return;
    	}
    }
}
