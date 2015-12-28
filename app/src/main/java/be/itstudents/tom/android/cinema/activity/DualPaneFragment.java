package be.itstudents.tom.android.cinema.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.FilmManager;

/**
 * Created by Tom on 27-12-15.
 */
public abstract class DualPaneFragment extends Fragment {
    protected boolean mDualPane;

    protected DualPaneListFragment listFragment;
    private FilmDetailFragment filmDetail;

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
        View view = inflater.inflate(getDualPaneLayout(), container, false);

		/*
         * This function becomes a little complex because we allow switching
		 * 	between single-pane and dual-pane, while handling rotation.
		 * 	As both view contain a framelayout with id getContainerFragmentId(),
		 *  android will always restore the last fragment which was in that view
		 *  , no matter what was shown in it.
		 */

        boolean isNew; //isNew will tell if the list fragment is a new one, and therefore should be put in its destination view

        //Create list fragment if doesn't exist, recover it if not
        if (getChildFragmentManager().findFragmentByTag(getListTag()) != null) {
            listFragment = (DualPaneListFragment) getChildFragmentManager().findFragmentByTag(getListTag());
            isNew = false;
        } else {
            //Create a new Fragment to be placed in the activity layout
            listFragment = newListInstance();
            isNew = true;
        }

        // Check that the activity is using the layout version with dual pane
        if (view.findViewById(getDetailFragmentId()) == null) {
            mDualPane = false;

            //Update the film manager, as we could come from dual pane mode
            listFragment.setFilmManager(new FilmManager() {
                @Override
                public void showFilmDetail(Film f) {
                    System.err.println("Show detail 2");
                    filmDetail = FilmDetailFragment.newInstance(f);
                    getChildFragmentManager().beginTransaction()
                            .replace(getContainerFragmentId(), filmDetail).addToBackStack(null).commit();
                }
            });

            //Launch the film detail fragment if user was viewing one
            if (savedInstanceState != null && savedInstanceState.getString("film_id") != "") {
                Film f = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
                if (getChildFragmentManager().findFragmentById(getContainerFragmentId()) instanceof FilmDetailFragment) {
                    //The fragment will restore it's state by itself, but we have to update the reference for "onPause"
                    filmDetail = (FilmDetailFragment) getChildFragmentManager().findFragmentById(getContainerFragmentId());
                } else {
                    //Detail view does not exist, so we come from dual pane
                    filmDetail = FilmDetailFragment.newInstance(f);

                    //Replace the current container by the listFragment
                    if (isNew)
                        getChildFragmentManager().beginTransaction()
                                .replace(getContainerFragmentId(), listFragment, getListTag()).commit();

                    //Change the view by the detail view, and add that transaction to back stack so user can go back
                    getChildFragmentManager().beginTransaction()
                            .replace(getContainerFragmentId(), filmDetail).addToBackStack(null).commit();
                }
            } else {
                //Replace the current container by the listFragment
                if (isNew)
                    getChildFragmentManager().beginTransaction()
                            .replace(getContainerFragmentId(), listFragment, getListTag()).commit();
            }


        } else {
            mDualPane = true;

            //If we come from single pane, and it's a detail fragment which was printed, we have to remove it
            if (getChildFragmentManager().findFragmentById(getContainerFragmentId()) instanceof FilmDetailFragment) {
                getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }


            filmDetail = (FilmDetailFragment) getChildFragmentManager().findFragmentById(getDetailFragmentId());

            //If there was a film show, update schduleDetail (was already done if we didn't switch from landscape)
            if (savedInstanceState != null && savedInstanceState.getString("film_id") != "") {
                Film film = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
                filmDetail.setFilm(film);
            }

            //Update the film manager, as we could come from single pane mode
            listFragment.setFilmManager(new FilmManager() {
                @Override
                public void showFilmDetail(Film f) {
                    System.err.println("Show detail");
                    filmDetail.setFilm(f);
                }
            });
            //Add the fragment to the 'fragment_container' FrameLayout
            if (isNew)
                getChildFragmentManager().beginTransaction()
                        .replace(getContainerFragmentId(), listFragment, getListTag()).commit();
        }

        return view;

    }

    protected abstract DualPaneListFragment newListInstance();

    protected abstract int getDualPaneLayout();

    protected abstract int getContainerFragmentId();

    protected abstract int getDetailFragmentId();

    protected abstract String getListTag();


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
        if (filmDetail != null && filmDetail.isVisible() && filmDetail.getFilm() != null) {
            outState.putString("film_id", filmDetail.getFilm().id);
            outState.putString("film_titre", filmDetail.getFilm().titre);
        } else {
            outState.putString("film_id", "");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
