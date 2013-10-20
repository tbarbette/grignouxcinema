package be.itstudents.tom.android.cinema.Views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import be.itstudents.tom.android.cinema.Cinema;
import be.itstudents.tom.android.cinema.CinemaHoraires;
import be.itstudents.tom.android.cinema.Film;

//NORMAL
import be.itstudents.tom.android.cinema.R;

public class SeanceDetailedRow extends TableRow {
	String titre;
	Context context;
	int bgColor;
	protected final String TAG = "CinemaActivity-Row";
    public SeanceDetailedRow(Context context, final Film film, Calendar calendar, long cinema, boolean longDate, int bgColor) {
        super(context);

        this.bgColor = bgColor;
        this.titre = film.titre;
        this.context = context;
        
        this.setBackgroundColor(bgColor);
        
        this.setLayoutParams(new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        ImageView i = new ImageView(context);
        switch ((int)cinema) {
                case (int)Cinema.PARC:
                        i.setImageResource(R.drawable.parc);
                        break;
                case (int)Cinema.CHURCHILL:
                        i.setImageResource(R.drawable.churchill);
                        break;
                case (int)Cinema.SAUVENIERE:
                        i.setImageResource(R.drawable.sauveniere);
                        break;
        }
        i.setLayoutParams(new LayoutParams(
                (int)(getContext().getResources().getDisplayMetrics().density * 26f),
                (int)(getContext().getResources().getDisplayMetrics().density * 26f)));
        i.setPadding((int)(getContext().getResources().getDisplayMetrics().density * 2f),
		(int)(getContext().getResources().getDisplayMetrics().density * 8f),
		(int)(getContext().getResources().getDisplayMetrics().density * 2f),
		(int)(getContext().getResources().getDisplayMetrics().density * 6f));
        this.addView(i);


     
        
        if (longDate) {
        	TextView t = new TextView(context);
        	SimpleDateFormat timeFormater = new SimpleDateFormat("EEE dd/MM");
        	
        	t.setText(timeFormater.format(calendar.getTime()));
       
	        t.setGravity(Gravity.RIGHT);
	        t.setLayoutParams(new LayoutParams(
	                  LayoutParams.FILL_PARENT,
	                  LayoutParams.WRAP_CONTENT));
	
	        t.setPadding(   (int)(getContext().getResources().getDisplayMetrics().density * 4),
                    0,
                    (int)(getContext().getResources().getDisplayMetrics().density * 2),
                    0);
	        t.setTextSize(15);
	        this.addView(t);
        }
        TextView hourt = new TextView(context);
        SimpleDateFormat timeFormater = new SimpleDateFormat("HH:mm");
    	
        hourt.setText(timeFormater.format(calendar.getTime()));
        hourt.setGravity(Gravity.LEFT);
        hourt.setLayoutParams(new LayoutParams(
                  LayoutParams.WRAP_CONTENT,
                  LayoutParams.WRAP_CONTENT));

        hourt.setPadding(	0,
                            0,
                            (int)(getContext().getResources().getDisplayMetrics().density * 4),
                            0);
        hourt.setTextSize(15);
        this.addView(hourt);
        
        TextView titreText = new TextView(context);
        titreText.setText(titre);
        titreText.setGravity(Gravity.RIGHT);
        titreText.setTextColor(Color.WHITE);
        titreText.setLayoutParams(new LayoutParams(
                  LayoutParams.FILL_PARENT,
                  LayoutParams.WRAP_CONTENT));
        
        titreText.setPadding(   0,
        				(int)(getContext().getResources().getDisplayMetrics().density * 4f),
                        (int)(getContext().getResources().getDisplayMetrics().density * 4f),
                        (int)(getContext().getResources().getDisplayMetrics().density * 4f));

        titreText.setTextSize(15);
        titreText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				CinemaHoraires.clickedFilm = film;
	 	    	return false;
			}
        	
        });
        
        this.addView(titreText);
    }
    
 

 }