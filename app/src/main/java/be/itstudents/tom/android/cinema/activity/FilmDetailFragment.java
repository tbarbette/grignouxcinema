package be.itstudents.tom.android.cinema.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.R;

public class FilmDetailFragment extends Fragment {
    private Film mFilm;
    private TextView mContent;
    private ImageView mAffiche;
    private ImageView mYoutube;
    private ProgressBar mSpinner;

    public static FilmDetailFragment newInstance(Film f) {
        FilmDetailFragment df = new FilmDetailFragment();
        df.mFilm = f;
        return df;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.film_detail, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilm = new Film(savedInstanceState.getString("film_titre"), savedInstanceState.getString("film_id"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilm == null) return;
        outState.putString("film_titre", mFilm.titre);
        outState.putString("film_id", mFilm.id);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContent = (TextView) view.findViewById(R.id.cinemadetail_content);
        mAffiche = (ImageView) view.findViewById(R.id.cinemadetail_affiche);
        mYoutube = (ImageView) view.findViewById(R.id.cinemadetail_you);


        mSpinner = (ProgressBar) view.findViewById(R.id.cinemadetail_progress);
        if (mFilm != null)
            updateFilm();
        else
            mContent.setText(R.string.waitingforfilm);
    }

    public void updateFilm() {
        mSpinner.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mAffiche.setVisibility(View.GONE);
        mYoutube.setVisibility(View.GONE);
        new RetrieveInfoTask().execute(mFilm);

    }

    public Film getFilm() {
        return mFilm;
    }

    public void setFilm(Film f) {
        mFilm = f;
        mContent.setText(R.string.loading);
        updateFilm();
    }

    class RetrieveInfoTask extends AsyncTask<Film, Void, Film> {


        protected Film doInBackground(Film... f) {
            try {
                f[0].getPoster();
                return f[0];
                //TODO Display an error text
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Film f) {
            if (f == null) return;
            mSpinner.setVisibility(View.GONE);
            mContent.setVisibility(View.VISIBLE);
            mAffiche.setVisibility(View.VISIBLE);
            mYoutube.setVisibility(View.VISIBLE);
            mContent.setText(Html.fromHtml(f.description));
            mAffiche.setImageBitmap(f.affiche);
            mYoutube.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", mFilm.titre);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        //TODO : FInd better than try catch to check if android package is installed
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.youtube.com/results?q=" + mFilm.titre)));
                    }
                }
            });
        }
    }


}
