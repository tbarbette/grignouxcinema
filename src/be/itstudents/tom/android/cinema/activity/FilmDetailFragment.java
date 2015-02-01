package be.itstudents.tom.android.cinema.activity;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.R;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FilmDetailFragment extends Fragment {
    private Film mFilm;
	private TextView mContent;
	private ImageView mAffiche;
	private ImageView mYoutube;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.film_detail, container, false);
    }
	
	
    
    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContent = (TextView)view.findViewById(R.id.cinemadetail_content);
		mAffiche = (ImageView)view.findViewById(R.id.cinemadetail_affiche);
		mYoutube = (ImageView)view.findViewById(R.id.cinemadetail_you);
		if (mFilm != null)
			updateFilm();
	}

    class RetrieveInfoTask extends AsyncTask<Film, Void, Film> {



		protected Film doInBackground(Film... f) {
            try {
        			f[0].getAffiche();
        			return f[0];
        			//TODO Display an error text
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Film f) {
        	if (f == null) return;
        	mContent.setText(Html.fromHtml(f.description));
        	mAffiche.setImageBitmap(f.affiche);
        	mYoutube.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				//TODO repair
    			}
    		});
        }
    }
    
    public void updateFilm() {
    	new RetrieveInfoTask().execute(mFilm);
    	
    }

	public static FilmDetailFragment newInstance(Film f) {
    	FilmDetailFragment df = new FilmDetailFragment();
    	df.mFilm = f;
    	return df;
    }



	public void setFilm(Film f) {
		mFilm = f;
		mContent.setText(R.string.loading);
		updateFilm();
	}
	


}
