package be.itstudents.tom.android.cinema.activity;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment.SearchManager;
import be.itstudents.tom.android.cinema.service.CinemaProvider;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;


public class ScheduleListFragment extends Fragment {

	private FilmManager mFilmManager;

	public static final String TAG = "CinemaScheduleListFragment";

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.schedulemenu, menu);
		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.searchbtn:
			SearchDialogFragment s = new SearchDialogFragment();
			s.show(getFragmentManager(), this.TAG);
			s.setSearchManager(new SearchManager() {
				
				@Override
				public void doSearch(String pattern) {
					//TODO : Use newer API
					String columns[] = new String[] { Seance.SEANCE_ID, Seance.SEANCE_TITLE, Seance.SEANCE_DATE, Seance.SEANCE_CINEMA, Seance.SEANCE_FILMID};

					/* Cursor cur = getActivity().managedQuery(
							 Uri.withAppendedPath(
									 Uri.withAppendedPath(
											 Seance.CONTENT_URI,"search")
											 ,pattern),
											 columns, null, null, Seance.SEANCE_DATE + " ASC");

					 if (searchView != null) mainlayout.removeView(searchView);
					 searchView = new ResultView(getActivity(), null, true);

					 refreshDate(null);

					 mainlayout.removeView(flipView);

					 searchView.displayResult(cur);
					 mainlayout.addView(searchView);*/
				}
			});
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

	private int mPosition;


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

		public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

		@Override
	    public CharSequence getPageTitle (int position) {
			Calendar date = Calendar.getInstance();
			date.add(Calendar.DATE, position);
			CalendarUtils.zero(date);
			String jour = (String)android.text.format.DateFormat.format(
                    "EEEE " +
                    android.text.format.DateFormat.DATE + android.text.format.DateFormat.DATE +
                    "/" +
                    android.text.format.DateFormat.MONTH + android.text.format.DateFormat.MONTH +
                    "/" +
                    android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR
                    ,date);
			jour = Character.toUpperCase(jour.charAt(0)) + jour.substring(1);
			return jour;
	    }
		 
        @Override
        public Fragment getItem(int position) {
            ScheduleListResultFragment resultView = new ScheduleListResultFragment();
            resultView.setFilmManager(mFilmManager);
            resultView.setPosition(position);
            resultView.setDisplayFullDate(false);
            return resultView;
        }

        @Override
        public int getCount() {
            return CinemaProvider.SEARCH_PERIOD;
        }
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.schedule_list, container, false);
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) v.findViewById(R.id.schedule_list_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPager != null)
			outState.putInt("currentPosition",mPager.getCurrentItem());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mPosition = savedInstanceState.getInt("currentPosition");
		}
		setHasOptionsMenu(true);
	}
	
	public void setFilmManager(FilmManager filmManager) {
		this.mFilmManager = filmManager;		
	}

}