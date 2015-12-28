package be.itstudents.tom.android.cinema.activity;

import android.support.v4.app.Fragment;

import be.itstudents.tom.android.cinema.FilmManager;

/**
 * Created by Tom on 27-12-15.
 */
public class DualPaneListFragment extends Fragment {
    protected FilmManager filmManager;

    public void setFilmManager(FilmManager filmManager) {
        this.filmManager = filmManager;
    }
}
