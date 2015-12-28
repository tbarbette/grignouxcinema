package be.itstudents.tom.android.cinema.activity;

import android.net.Uri;
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

import java.util.Calendar;

import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment.SearchManager;
import be.itstudents.tom.android.cinema.service.CinemaProvider;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;


public class ScheduleListFragment extends DualPaneListFragment {

    public static final String TAG = "CinemaScheduleListFragment";
    private static final int SEARCH_LOADER = 0;
    private static final String SEARCH_TAG = "SEARCH_FRAGMENT";
    private static ScheduleListFragment instance;
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

    public ScheduleListFragment() {
        super();
        instance = this;
    }

    public static FilmManager getFilmManager() {
        return instance.filmManager;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedulemenu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchbtn:
                SearchDialogFragment s = new SearchDialogFragment();
                s.show(getFragmentManager(), ScheduleListFragment.TAG);
                s.setSearchManager(new SearchManager() {

                    @Override
                    public void doSearch(final String pattern) {
                        ScheduleListResultFragment resultView = new ScheduleListResultFragment();
                        resultView.setUri(Uri.withAppendedPath(
                                Uri.withAppendedPath(
                                        Seance.CONTENT_URI, "search")
                                , pattern));
                        resultView.setDisplayFullDate(true);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.schedule_fragment_container, resultView, SEARCH_TAG).addToBackStack(null).commit();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_list, container, false);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) v.findViewById(R.id.schedule_list_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        if (mPosition > 0)
            mPager.setCurrentItem(mPosition);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPager != null)
            outState.putInt("currentPosition", mPager.getCurrentItem());
        else
            outState.putInt("currentPosition", mPosition);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("currentPosition");
        }
        setHasOptionsMenu(true);
    }

    public int getPosition() {
        if (mPager != null)
            return mPager.getChildCount();
        else
            return mPosition;
    }

    public void setPosition(int p) {
        mPosition = p;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, position);
            CalendarUtils.zero(date);
            String jour = (String) android.text.format.DateFormat.format(
                    "EEEE " +
                            android.text.format.DateFormat.DATE + android.text.format.DateFormat.DATE +
                            "/" +
                            android.text.format.DateFormat.MONTH + android.text.format.DateFormat.MONTH +
                            "/" +
                            android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR
                    , date);
            jour = Character.toUpperCase(jour.charAt(0)) + jour.substring(1);
            return jour;
        }

        @Override
        public Fragment getItem(int position) {
            ScheduleListResultFragment resultView = new ScheduleListResultFragment();

            resultView.setPosition(position);
            resultView.setDisplayFullDate(false);
            resultView.setUri(Uri.withAppendedPath(Uri.withAppendedPath(Seance.CONTENT_URI, "seances"), CalendarUtils.dateFormat.format(resultView.getDate().getTime())));
            return resultView;
        }

        @Override
        public int getCount() {
            return CinemaProvider.SEARCH_PERIOD;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}