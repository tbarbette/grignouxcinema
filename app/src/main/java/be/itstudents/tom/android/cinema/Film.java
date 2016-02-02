package be.itstudents.tom.android.cinema;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;

/**
 * Represent a film and its informations like the title, the id, a description, ...
 */
public class Film {
    private static final String TAG = "CinemaFilm";
    public String titre;
    public String id;
    public String littleDescription;
    public String description;
    public String imageURL;
    public Bitmap affiche = null;

    public Film(String titre, String id) {
        this.titre = titre;
        this.id = id;
    }

    public Film(String titre, String id, String littleDescription) {
        this(titre, id);
        this.littleDescription = littleDescription;
    }

    /**
     * Return the bitmap of the poster of the film
     *
     * @return the poster as Bitmap
     * @throws Exception if the poster could not be downloaded
     */
    public Bitmap getPoster() throws Exception {
        if (imageURL == null) {
            loadDetails();

        }
        if (affiche == null)
            affiche = DownloadManager.getImage(imageURL);
        return affiche;
    }

    /**
     * Load details of the film from the grignoux website using the film ID
     */
    public void loadDetails() {
        String url = "http://www.grignoux.be/films/" + id;
        String html;
        try {
            html = DownloadManager.getString(url);

            Pattern pattern = Pattern.compile("<meta content=(?:'|\")(.*?)(?:'|\") property='og:([a-z_]+)'>", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);

            while (matcher.find()) {
                if (matcher.group(2).equals("description"))
                    description = matcher.group(1);
                else if (matcher.group(2).equals("image"))
                    imageURL = matcher.group(1).replace("/original/", "/affiche/");
            }
            if (description == null)
                Log.w(TAG, "Description of film " + titre + " is null !");
        } catch (Exception e) {
            System.err.println("La structure de la page détaille a changé !");
            e.printStackTrace();
        }

    }
}
