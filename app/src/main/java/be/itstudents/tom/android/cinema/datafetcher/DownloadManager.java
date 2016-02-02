package be.itstudents.tom.android.cinema.datafetcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.Semaphore;


public class DownloadManager {

    public static Semaphore imgToRetrieveS = new Semaphore(0);
    public static Stack<ImgToRetrieve> imgToRetrieve = new Stack<ImgToRetrieve>();
    static HashMap<String, String> cacheString = new HashMap<String, String>();
    static LinkedList<ImageDownloader> downloaders = new LinkedList<ImageDownloader>();

    public static void startAsyncT(int n) {
        for (int i = 0; i < n; i++)
            downloaders.add(new ImageDownloader());

        for (ImageDownloader d : downloaders) {
            d.start();
        }
    }

    public static void stopAll() {
        imgToRetrieveS.drainPermits();
        imgToRetrieve.clear();
        for (ImageDownloader d : downloaders) {
            d.stopped = true;
            d.interrupt();
        }
        downloaders.clear();
    }

    public static void clearAsyncQ() {
        imgToRetrieveS.drainPermits();
        imgToRetrieve.clear();
        for (ImageDownloader d : downloaders) {
            d.interrupt();
        }
    }

    public static String get(String url) throws Exception {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        InputStream in = new BufferedInputStream(conn.getInputStream());
        return IOUtils.toString(in);

    }

    public static String getString(String url) throws Exception {

        if (cacheString.get(url) != null) {
            cacheString.get(url);
        } else {
            cacheString.put(url, get(url));
        }
        return cacheString.get(url);
    }

    public static Bitmap getImage(String url) throws Exception {
        URL u = new URL(url);

        String path;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().toString() + "/grignoux/";
            if (!(new File(path)).exists()) {
                (new File(path)).mkdirs();
                (new File(path + ".nomedia")).createNewFile();
            }

            File file = new File(path + u.getFile());

            if (!file.exists()) {
                //Log.i(ScheduleActivity.TAG, "DOWNLOAD : " + url);
                new File(file.getParent()).mkdirs();

                FileOutputStream fOut = new FileOutputStream(file);
                try {

                    HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
                    conn.setConnectTimeout(4500);
                    conn.setReadTimeout(4500);

                    InputStream input = conn.getInputStream();
                    IOUtils.copy(input, fOut);
                    input.close();
                    fOut.close();
                } finally {
                    fOut.close();
                }

            }
            FileInputStream fIn = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(new BufferedInputStream(fIn));
            fIn.close();
            return bmp;
        }
        InputStream input = (InputStream) u.getContent();
        return BitmapFactory.decodeStream(input);

    }

    public static void getImageAsync(String string, OnImageReceived onReceived) {
        ImgToRetrieve i = new ImgToRetrieve();
        i.url = string;
        i.onReceived = onReceived;

        imgToRetrieve.add(i);
        imgToRetrieveS.release();
    }

    public static int getNumAsyncT() {
        return downloaders.size();
    }

    public interface OnImageReceived {
        void imageReceived(Bitmap b);

        boolean stillUsefull();

        void makeSpace();
    }

    public static class ImgToRetrieve {
        String url;
        OnImageReceived onReceived;
    }


}
