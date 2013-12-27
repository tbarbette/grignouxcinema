package be.itstudents.tom.android.cinema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;

public class Film {
	public String titre;
	public String id;
	public String littleDescription;
	public String description;
	public String imageURL;
	public Bitmap affiche = null;
	
	public Film (String titre, String id) {
		this.titre = titre;
		this.id = id;
	}
	
	public Film (String titre, String id, String littleDescription) {
		this(titre, id);
		this.littleDescription = littleDescription;
	}

	public Bitmap getAffiche() throws Exception {
		if (imageURL == null) {
			loadDetails();
			
		}
		if (affiche == null)
			affiche = DownloadManager.getImage(imageURL);
		return affiche;
	}
	
	public void loadDetails() {
		String url = "http://www.grignoux.be/films/" + id;
		 
        String html;
		try {
			html = DownloadManager.getString(url);
		
        Pattern pattern = Pattern.compile("<meta content='(.*?)' property='og:([a-z_]+)'>",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
        	if (matcher.group(2).equals("description"))
        		description = matcher.group(1);
        	else if (matcher.group(2).equals("image"))
        		imageURL = matcher.group(1).replace("/original/","/affiche/");
        }
		} catch (Exception e) {
			System.err.println("La structure de la page détaille a changé !");
			e.printStackTrace();
		}
        
	}
}
