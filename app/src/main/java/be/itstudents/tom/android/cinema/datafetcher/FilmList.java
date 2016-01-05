package be.itstudents.tom.android.cinema.datafetcher;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.activity.ScheduleListFragment;

public class FilmList {
    public static Semaphore available = new Semaphore(0);
    static volatile LinkedList<Film> list;

    public static void loadList() throws Exception {
        list = new LinkedList<>();

        String html = DownloadManager.getString("http://www.grignoux.be");

        Pattern pattern = Pattern.compile("<strong>Les nouveaux films</strong>(.*?)<div class='sep_small'></div>", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {

            Pattern pfilms = Pattern.compile("data-id='([0-9]+)'.*?>(.*?)</a>", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher mfilms = pfilms.matcher(matcher.group(1));
            while (mfilms.find()) {
                list.add(new Film(mfilms.group(2), mfilms.group(1), mfilms.group(2)));
            }

            available.release();
        } else {

            Log.e(ScheduleListFragment.TAG, "La structure de grignoux.be a changé !");
            available.release();
            throw new Exception("La structure a changée !");
        }
    }

    public static void loadImages() {
        try {

            for (int i = 0; i < list.size(); i++) {
                list.get(i).getPoster();
            }

        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
    }

    public static List<Film> getList() {
        return list;
    }
}
