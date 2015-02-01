package be.itstudents.tom.android.cinema.activity;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment {
	boolean mDualPane;
	
	final String TAG = "CinemaScheduleActivity";

	ScheduleListFragment scheduleList;

	private FilmDetailFragment scheduleDetail;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.schedule, container, false);

	    // Check that the activity is using the layout version with
	    // the fragment_container FrameLayout
	    if (view.findViewById(R.id.fragment_container) != null) {
	    	mDualPane = false;
	        		

	    		if (savedInstanceState != null) {
	    			//Restore fragment
	    			scheduleList = (ScheduleListFragment)getChildFragmentManager().getFragment(
	    	                savedInstanceState, "current_fragment");
	    		} else {
	    			//Create a new Fragment to be placed in the activity layout
	    			scheduleList = new ScheduleListFragment();
	    		}
		        scheduleList.setFilmManager(new FilmManager() {					
					@Override
					public void showFilmDetail(Film f) {
						getChildFragmentManager().beginTransaction()
	                    .replace(R.id.fragment_container, FilmDetailFragment.newInstance(f)).addToBackStack(null).commit();
					}
				});
		        
	        	//Add the fragment to the 'fragment_container' FrameLayout
	        	getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scheduleList).commit();

	        } else {
	        	//Do nothing as the XML inflate the Fragments itself
	        	mDualPane = true;
	        	scheduleDetail = (FilmDetailFragment)getChildFragmentManager().findFragmentById(R.id.film_detail_fragment); 
	        	scheduleList = (ScheduleListFragment)getChildFragmentManager().findFragmentById(R.id.schedule_list_fragment);
		        scheduleList.setFilmManager(new FilmManager() {					
					@Override
					public void showFilmDetail(Film f) {
						scheduleDetail.setFilm(f);
					}
				});
	        }

	        return view;
		
	    }

	
	@Override
	public void onResume() {

	    super.onResume();

	    getView().setFocusableInTouchMode(true);
	    getView().requestFocus();
	    getView().setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				getChildFragmentManager().popBackStack();
				return true;
			}
	    });
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);		
		getChildFragmentManager().putFragment(outState, "current_fragment", scheduleList);
	}


	@Override
	public void onPause() {
		super.onPause();
	}
		
	
	 
}
