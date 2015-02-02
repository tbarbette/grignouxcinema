package be.itstudents.tom.android.cinema.datafetcher;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.activity.JournalFragment;

public class MainLoader extends Loader {
		
	private Activity context;

	public MainLoader(Activity context) {
		this.context = context;
	}
	
	public static MainLoader getMain(Activity a) {
	//	if (main == null)
			return new MainLoader(a);
		//else
			//return main;
	}
	
	@Override
	public void run() {

		try {
			callOnStatus(null, "Horaires de ce jour");
			Cursor cur = context.getContentResolver().query(
	    			Uri.withAppendedPath(Seance.CONTENT_URI,"preload"),
	    			null,
	    			null,
	    			null,
	    			null   				    			
	    			
	    			);
			if (cur == null || cur.getCount() == 0) {
				callOnStatus("Pas de connexion internet", "Et vous n'avez aucun horaire pré-téléchargé...");
				sleep(8000);
				callOnCancel();
				return;
			}
			
			callOnGoBackground();

			callOnStatus(null, "Films de la semaine");
			FilmList.loadList();
			
			callOnStatus(null, "Affiche des films");
			FilmList.loadImages();

			callOnStatus(null, "Journal des grignoux");
			JournalFragment.loadLast();


		} catch (Exception e1) {
			callOnStatus("", "Impossible de se connecter...");
		}

		callOnFinish();
	}

	public void waitForFinished(Activity a) {
		
	}
	
}
