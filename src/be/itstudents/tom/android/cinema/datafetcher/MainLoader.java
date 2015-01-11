package be.itstudents.tom.android.cinema.datafetcher;

import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import be.itstudents.tom.android.cinema.activity.JournalActivity;

public class MainLoader extends Loader {
		
	private Activity context;
	private static MainLoader main;

	public MainLoader(Activity context) {
		this.context = context;
		this.main = this;
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
			Cursor cur = Horaire.getSeancesAtDate(Calendar.getInstance(), context);
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
			JournalActivity.loadLast();


		} catch (Exception e1) {
			callOnStatus("", "Impossible de se connecter...");
		}

		callOnFinish();
	}

	public void waitForFinished(Activity a) {
		
	}
	
}
