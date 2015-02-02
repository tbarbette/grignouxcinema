package be.itstudents.tom.android.cinema.activity;


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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.dialogs.AboutDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.UpdateDialogFragment;
import be.itstudents.tom.android.cinema.service.CinemaSyncer;

public class MainActivity extends ActionBarActivity {

	private String[] mTabs; 
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
    private int selected = 0;
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
    	
        @SuppressWarnings("rawtypes")
		@Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position, null);
            view.setSelected(true);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position, Fragment fragment) {
    	Fragment currentFragment;
        switch (position) {
        case 0:
        	currentFragment = new ScheduleFragment();
        	selected = position;
        	break;
        case 1:
        	currentFragment = new FeaturedFragment();
        	selected = position;
        	break;
        case 2:
        	currentFragment = new JournalFragment();
        	selected = position;
        	break;    
        default:
        	if (fragment == null) {
        		selectItem(selected, null);
        		return;
        	} else
        		currentFragment = fragment;     	
        }
        
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.content_frame, currentFragment)
                       .commit();
        
        
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(selected, true);
        
        setTitle(mTabs[selected]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
    	mTitle = title;
        getSupportActionBar().setTitle(title);
    }

 	
 	public class MenuImageAdapter extends BaseAdapter {

 	    private String[] items;
 	    private Context context;
		private int layout_id;
		private int[] images;
		private LayoutInflater inflater;

 	    public MenuImageAdapter(Context ctx, int layout_id, String[] names, int[] images_ids, LayoutInflater inflater) {
 	        this.items = names;
 	        this.context = ctx;
 	        this.layout_id = layout_id;
 	        this.images = images_ids;
 	        this.inflater = inflater;
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
			new UpdateDialogFragment().show(getSupportFragmentManager(), MainActivity.TAG);;
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("slowMessage", true);
			editor.commit();	            
		}

	    mTabs = getResources().getStringArray(R.array.tabs_name);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new MenuImageAdapter(this, R.layout.drawer_list_item, mTabs, new int[]{R.drawable.ic_tab_films_on_center,R.drawable.ic_tab_horaires_on_center,R.drawable.ic_tab_journal_on_center},getLayoutInflater()));
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
        
        
		if (savedInstanceState != null) {
			selected = savedInstanceState.getInt("current_index");
			selectItem(-1,getSupportFragmentManager().getFragment(
	                savedInstanceState, "current_fragment"));
		} else {
			selectItem(selected,null);
		}
        
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "current_fragment", getSupportFragmentManager().findFragmentById(R.id.content_frame));
		outState.putInt("current_index", selected);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
    	switch (item.getItemId()) {
			case R.id.aboutbtn:
				new AboutDialogFragment().show(getSupportFragmentManager(), MainActivity.TAG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
    }
}
