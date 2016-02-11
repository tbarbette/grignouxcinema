package be.itstudents.tom.android.cinema.multipane;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.activity.FilmDetailFragment;

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

        Film film = null;
        if (savedInstanceState != null && savedInstanceState.getString("film_id") != "") {
            film = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
        }

        Handler h = new Handler(Looper.myLooper());
        final Film finalFilm = film;
        h.post(new Runnable() {
            @Override
            public void run() {
                boolean isNew = false;
                if (getChildFragmentManager().findFragmentByTag(getListTag()) != null) {
                    listFragment = (DualPaneListFragment) getChildFragmentManager().findFragmentByTag(getListTag());
                } else {
                    listFragment = newListInstance();
                    getChildFragmentManager().beginTransaction()
                            .replace(getContainerFragmentId(), listFragment, getListTag()).commit();
                }
                if (DualPaneFragment.this.getView().findViewById(getDetailFragmentId()) == null) {
                    mDualPane = false;
                    listFragment.setFilmManager(new FilmManager() {
                        @Override
                        public void showFilmDetail(Film f) {
                            filmDetail = FilmDetailFragment.newInstance(f);
                            getChildFragmentManager().beginTransaction()
                                    .replace(getContainerFragmentId(), filmDetail).addToBackStack(null).commit();
                        }
                    });
                } else {
                    mDualPane = true;
                    filmDetail = (FilmDetailFragment) getChildFragmentManager().findFragmentById(getDetailFragmentId());
                    if (finalFilm != null) {

                        filmDetail.setFilm(finalFilm);
                    }
                    listFragment.setFilmManager(new FilmManager() {
                        @Override
                        public void showFilmDetail(Film f) {
                            filmDetail.setFilm(f);
                        }
                    });
                }
            }
        });
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

        if (filmDetail != null && filmDetail.isVisible() && filmDetail.getFilm() != null) {
            outState.putString("film_id", filmDetail.getFilm().id);
            outState.putString("film_titre", filmDetail.getFilm().titre);
        } else {
            outState.putString("film_id", "");
        }
        if (!mDualPane) {
            System.err.println("Leave from single");
            if (!listFragment.isVisible()) {
                System.err.println("Prepare for bug");
                getChildFragmentManager().popBackStack();
            }
        }
        super.onSaveInstanceState(outState);
    }
}
