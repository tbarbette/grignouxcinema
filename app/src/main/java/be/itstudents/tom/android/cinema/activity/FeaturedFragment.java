package be.itstudents.tom.android.cinema.activity;

import be.itstudents.tom.android.cinema.R;

public class FeaturedFragment extends DualPaneFragment {


    private static final String FEATURED_LIST_TAG = "FEATURED_LIST";
    final String TAG = "CinemaFeaturedActivity";

    @Override
    protected DualPaneListFragment newListInstance() {
        return new FeaturedListFragment();
    }

    @Override
    protected int getDualPaneLayout() {
        return R.layout.featured;
    }

    @Override
    protected int getContainerFragmentId() {
        return R.id.featured_fragment_container;
    }

    @Override
    protected int getDetailFragmentId() {
        return R.id.featured_film_detail_fragment;
    }

    @Override
    protected String getListTag() {
        return FEATURED_LIST_TAG;
    }

}
