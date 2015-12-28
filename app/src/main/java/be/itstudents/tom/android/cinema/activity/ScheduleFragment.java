package be.itstudents.tom.android.cinema.activity;

import android.os.Bundle;

import be.itstudents.tom.android.cinema.R;

public class ScheduleFragment extends DualPaneFragment {


    private static final String SCHEDULE_LIST_TAG = "SCHEDULE_LIST";
    final String TAG = "CinemaScheduleActivity";

    @Override
    protected DualPaneListFragment newListInstance() {
        return new ScheduleListFragment();
    }

    @Override
    protected int getDualPaneLayout() {
        return R.layout.schedule;
    }

    @Override
    protected int getContainerFragmentId() {
        return R.id.schedule_fragment_container;
    }

    @Override
    protected int getDetailFragmentId() {
        return R.id.schedule_film_detail_fragment;
    }

    @Override
    protected String getListTag() {
        return SCHEDULE_LIST_TAG;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (listFragment != null)
            outState.putInt("position", ((ScheduleListFragment) listFragment).getPosition());
    }
}
