package be.itstudents.tom.android.cinema.activity;

import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.activity.ScheduleListFragment.TitleManager;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.UpdateDialogFragment;
import be.itstudents.tom.android.cinema.service.CinemaSyncer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment {
	boolean mDualPane;
	
	final String TAG = "CinemaScheduleActivity";

	ScheduleListFragment scheduleList;
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.schedule, container, false);

	        // Check that the activity is using the layout version with
	        // the fragment_container FrameLayout
	        if (view.findViewById(R.id.fragment_container) != null) {
	        	mDualPane = false;
	        			
	        	
	        	//TODO : check this...
	            // However, if we're being restored from a previous state,
	            // then we don't need to do anything and should return or else
	            // we could end up with overlapping fragments.
	            if (savedInstanceState != null) {
	                return null;
	            }

	            // Create a new Fragment to be placed in the activity layout
	            scheduleList = new ScheduleListFragment();
	            
	            //TODO : Useless for me
	            // In case this activity was started with special instructions from an
	            // Intent, pass the Intent's extras to the fragment as arguments
	            scheduleList.setArguments(getActivity().getIntent().getExtras());
	            
	            // Add the fragment to the 'fragment_container' FrameLayout
	            getChildFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, scheduleList).commit();
	        } else {
	        	//Do nothing as the XML inflate the Framgents itself
	        	mDualPane = true;
	        	scheduleList = (ScheduleListFragment)getChildFragmentManager().findFragmentById(R.id.schedule_list_fragment);
	        }       
	        return view;
		
	    }
	 
}
