package be.itstudents.tom.android.cinema;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.widget.TabHost;

public class CinemaMenu extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.cinemamenu);
	    
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, CinemaHoraires.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("horaires").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_horaires))
	                  .setContent(intent);
	    tabHost.addTab(spec);


	    intent = new Intent().setClass(this, CinemaFilms.class);
	    
	    spec = tabHost.newTabSpec("films").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_films))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    

	    intent = new Intent().setClass(this, CinemaJournal.class);
	    
	    spec = tabHost.newTabSpec("journal").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_journal))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
}
