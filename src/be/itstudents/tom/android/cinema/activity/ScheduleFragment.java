package be.itstudents.tom.android.cinema.activity;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment {
	boolean mDualPane;

	final String TAG = "CinemaScheduleActivity";

	final static String MASTER_FRAGMENT_SINGLE_PANE = "LIST_SINGLE";

	private static final String SCHEDULE_LIST_TAG = "SCHEDULE_LIST";
	private ScheduleListFragment scheduleList;

	private FilmDetailFragment scheduleDetail;




	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.schedule, container, false);

		/*
		 * This function becomes a little complex because we allow switching 
		 * 	between single-pane and dual-pane, while handling rotation.
		 * 	As both view contain a framelayout with id R.id.fragment_container,
		 *  android will always restore the last fragment which was in that view
		 *  , no matter what was shown in it.
		 */


		boolean isNew; //isNew will tell if the list fragment is a new one, and therefore should be put in its destination view
		
		//Create list fragment if doesn't exist, recover it if not
		if ((ScheduleListFragment)getChildFragmentManager().findFragmentByTag(SCHEDULE_LIST_TAG) != null) {
			scheduleList = (ScheduleListFragment)getChildFragmentManager().findFragmentByTag(SCHEDULE_LIST_TAG);
			isNew = false;
		} else {
			//Create a new Fragment to be placed in the activity layout
			scheduleList = new ScheduleListFragment();
			isNew = true;
		}

		// Check that the activity is using the layout version with dual pane
		if (view.findViewById(R.id.film_detail_fragment) == null) {
			mDualPane = false;

			//Update the film manager, as we could come from dual pane mode
			scheduleList.setFilmManager(new FilmManager() {					
				@Override
				public void showFilmDetail(Film f) {
					scheduleDetail = FilmDetailFragment.newInstance(f);
					getChildFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, scheduleDetail).addToBackStack(null).commit();
				}
			});

			//Launch the film detail fragment if user was viewing one
			if (savedInstanceState != null && savedInstanceState.getString("film_id") != "") {
				Film f = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
				if (getChildFragmentManager().findFragmentById(R.id.fragment_container) instanceof FilmDetailFragment) {
					//The fragment will restore it's state by itself, but we have to update the reference for "onPause"
					scheduleDetail = (FilmDetailFragment)getChildFragmentManager().findFragmentById(R.id.fragment_container);
				} else {        		
					//Detail view does not exist, so we come from dual pane
					scheduleDetail = FilmDetailFragment.newInstance(f);

					//Replace the current container by the scheduleList
					if (isNew)
						getChildFragmentManager().beginTransaction()
						.replace(R.id.fragment_container, scheduleList, SCHEDULE_LIST_TAG).commit();

					//Change the view by the detail view, and add that transaction to back stack so user can go back
					getChildFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, scheduleDetail).addToBackStack(null).commit();
				}
			} else {	        	
				//Replace the current container by the scheduleList
				if (isNew)
					getChildFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, scheduleList, SCHEDULE_LIST_TAG).commit();
			}


		} else {
			mDualPane = true;

			//If we come from single pane, and it's a detail fragment which was printed, we have to remove it
			if (getChildFragmentManager().findFragmentById(R.id.fragment_container) instanceof FilmDetailFragment) {
				getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			} 


			scheduleDetail = (FilmDetailFragment)getChildFragmentManager().findFragmentById(R.id.film_detail_fragment);
			
			//If there was a film show, update schduleDetail (was already done if we didn't switch from landscape)
			if (savedInstanceState != null && savedInstanceState.getString("film_id") != "") {
				Film film = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
				scheduleDetail.setFilm(film);      
			}

			//Update the film manager, as we could come from single pane mode
			scheduleList.setFilmManager(new FilmManager() {					
				@Override
				public void showFilmDetail(Film f) {

					scheduleDetail.setFilm(f);
				}
			});
			//Add the fragment to the 'fragment_container' FrameLayout
			if (isNew)
				getChildFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, scheduleList,SCHEDULE_LIST_TAG).commit();
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
				if (getChildFragmentManager().getBackStackEntryCount() == 0)
					return false;
				getChildFragmentManager().popBackStack();
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);	
		outState.putInt("position",scheduleList.getPosition());
		if (scheduleDetail != null && scheduleDetail.isVisible() && scheduleDetail.getFilm() != null) {
			outState.putString("film_id", scheduleDetail.getFilm().id);
			outState.putString("film_titre", scheduleDetail.getFilm().titre);
		} else {
			outState.putString("film_id", "");
		}
	}


	@Override
	public void onPause() {
		super.onPause();
	}



}
