package be.itstudents.tom.android.cinema.activity;


import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.dialogs.UpdateDialogFragment;
import be.itstudents.tom.android.cinema.service.CinemaSyncer;

//TODO FragmentTabHost
public class MainActivity extends ActionBarActivity {

	private String[] mTabs; 
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
    	
        @SuppressWarnings("rawtypes")
		@Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            view.setSelected(true);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
    	Fragment fragment;
        switch (position) {
        case 0:
        	fragment = new ScheduleFragment();
        	break;
        case 1:
        	fragment = new FeaturedFragment();
        	break;
        case 2:
        	fragment = new JournalFragment();
        	break;    
        default:
        	fragment = null;
        	//TODO : Handle case?
        }
        
    	//TODO : May be usefull, remove if not
    	/*Bundle args = new Bundle();
    	args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
    	fragment.setArguments(args);*/
        

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.content_frame, fragment)
                       .commit();
        
        
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mTabs[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    

    @Override
    public void setTitle(CharSequence title) {
    	mTitle = title;
        getSupportActionBar().setTitle(title);
    }
    

    //TODO : Should be handled with the new backstack system
 	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		/*if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			return scheduleList.backPressed();
			
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			new SearchDialogFragment().show(getSupportFragmentManager(), this.TAG);
			return true;
		}
*/
		return super.onKeyDown(keyCode, event);
	}
 	
 	
 	
 	public class MenuImageAdapter extends BaseAdapter {

 	    private String[] items;
 	    private Context context;
		private int layout_id;
		private int[] images;

 	    public MenuImageAdapter(Context ctx, int layout_id, String[] names, int[] images_ids) {
 	        this.items = names;
 	        this.context = ctx;
 	        this.layout_id = layout_id;
 	        this.images = images_ids;
 	    }


 	    @Override
 	    public int getCount() {
 	        return items == null ? 0 : items.length;
 	    }

 	    @Override
 	    public Object getItem(int position) {
 	        return items[position];
 	    }

 	    @Override
 	    public long getItemId(int position) {
 	        return position;
 	    }

 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	    	TextView row;
 	    	if (convertView == null) {
 	    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	            row = (TextView)inflater.inflate(layout_id, parent, false);
 	        } else {
 	        	row = (TextView)convertView;
 	        }
 	        
 	    	
 	    	row.setText(items[position]);
 	    	row.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(images[position]), null, null, null);

 	        return row;
 	    }

 	    public class ViewHolder {
 	        TextView title;
 	    }


 	}

 	
	private final static String PREFS_NAME = "cinema.prefs";
	private static final String TAG = "CinemaMainActivity";
 	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    
	    setContentView(R.layout.cinemamenu);
	    
		Intent svc = new Intent(this, CinemaSyncer.class);
		startService(svc);
		
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		if (settings.getBoolean("slowMessage", false)==false) {
			new UpdateDialogFragment().show(getSupportFragmentManager(), this.TAG);;
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("slowMessage", true);
			editor.commit();	            
		}	    

/*			if (settings.getBoolean("updMessage18", false)==false) {
			Dialog d = new Dialog(this);	
			d.setTitle("Cinéma Grignoux devient OpenSource !");
			TextView t = new TextView(this);
			t.setText(Html.fromHtml("Ce programme est désormais OpenSource ! Cela signifie que tout le monde peut contribuer à l'améliorer.<br/>\n<br/>\nRendez-vous sur <a href=\"http://github.com/MappaM/grignouxcinema\">http://github.com/MappaM/grignouxcinema</a> pour contribuer ou signaler un problème et laisser la communauté s'en occuper."));
			float dp = getResources().getDisplayMetrics().density;
			t.setPadding((int)(4*dp),(int)(4*dp),(int)(4*dp),(int)(4*dp));
			d.setContentView(t);
			d.show();
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("updMessage18", true);
			editor.commit();	            
		}	*/
		

	    mTabs = getResources().getStringArray(R.array.tabs_name);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new MenuImageAdapter(this, R.layout.drawer_list_item, mTabs, new int[]{R.drawable.ic_tab_films,R.drawable.ic_tab_horaires,R.drawable.ic_tab_journal}));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getResources().getString(R.string.drawer_name));
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        selectItem(0);
        
	    
	  
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
}
