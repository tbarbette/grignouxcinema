package be.itstudents.tom.android.cinema.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.views.DrawableTie;
import be.itstudents.tom.android.cinema.views.ScalableImage;


//TODO : This calss uses the old way of doing things. A Pager with Fragments should be better.
public class JournalFragment extends Fragment {


    public static final String TAG = "JournalActivity";
    public static String journalUrl = "http://www.grignoux.be";
    static LinearLayout main;
    private static int num;
    private static String lastId = null;
    float dp;
    Handler mHandler;
    private ScalableImage viewer;
    private int currentPage = 0;

    public static void loadLast() throws Exception {
        try {
            String text = DownloadManager.getString("http://grignoux.tombarbette.be/last.html");

            String[] val = text.split(" ");
            lastId = val[0];

            num = Integer.parseInt(val[1]);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String path = Environment.getExternalStorageDirectory().toString() + "/grignoux/tombarbette.be/";

                        File file = new File(path);

                        if (file.exists()) {
                            File[] files = file.listFiles();
                            for (File f : files) {
                                if (!f.getName().startsWith(lastId)) {
                                    f.delete();
                                }
                            }
                        }


                    }

                }
            });


            String pdfurl = DownloadManager.getString("http://www.grignoux.be/");

            Pattern p = Pattern.compile("href=\"(/system/papers/pdfs/[0-9]{3}/[0-9]{3}/[0-9]{3}/original/[a-zA-Z0-9_?.]+)\"");
            Matcher matcher = p.matcher(pdfurl);

            if (matcher.find()) {
                journalUrl = "http://www.grignoux.be/" + matcher.group(1);
            }
            Log.d("JournalActivity", "URL du PDF du journal : " + journalUrl);


        } catch (Exception e) {
            lastId = null;
        } finally {

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.journalmenu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(JournalFragment.journalUrl));
        this.startActivity(intent);
        return true;
    }

    public void onStop() {
        super.onStop();

        if (viewer != null) {
            viewer.destroy();
            viewer = null;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.journal, container, false);
        main = (LinearLayout) v.findViewById(R.id.cinema_journal_layout);
        //TODO : set loading if loading of the url is ongoing
        try {
            viewer = getPageViewer(currentPage);
            main.addView(viewer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        } catch (Exception e) {
            e.printStackTrace();
            TextView text = new TextView(getActivity());

            text.setText(R.string.unconnected_journal);

            text.setGravity(android.view.Gravity.CENTER);
            main.addView(text, 1, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        DownloadManager.startAsyncT(10);

        if (lastId == null)
            try {
                loadLast();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
    }

    @Override
    public void onPause() {
        super.onPause();

        DownloadManager.stopAll();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("currentPage", 0);
        }
        dp = getResources().getDisplayMetrics().density;


        mHandler = new Handler();


    }

    private ScalableImage getPageViewer(final int i) throws Exception {
        final ScalableImage viewer = new ScalableImage(getActivity());
        DownloadManager.clearAsyncQ();
        currentPage = i;
        final DrawableTie image;
        if (lastId == null)
            image = new DrawableTie(viewer, 2272, 3456, null, null);
        else
            image = new DrawableTie(viewer, 2272, 3456, "http://grignoux.tombarbette.be/" + lastId + "/" + i, ".png");


        Runnable currentMessage = new Runnable() {
            public void run() {
                viewer.setImage(image);
            }
        };
        mHandler.postDelayed(currentMessage, 100);


        viewer.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), (int) (8 * dp));
        viewer.setFlipHandler(new ScalableImage.FlipHandler() {
            public void previous() {

                if (i > 0) {
                    viewer.setFlipHandler(null);
                    viewer.setLoading(true);

                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            if (main.getChildCount() > 0) {
                                ScalableImage v = (ScalableImage) main.getChildAt(0);
                                main.removeViewAt(0);
                                v.destroy();
                            }
                            try {
                                main.addView(getPageViewer(i - 1), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }

            public void next() {
                if (i < num - 1) {
                    viewer.setFlipHandler(null);
                    viewer.setLoading(true);
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (main.getChildCount() > 0) {
                                ScalableImage v = (ScalableImage) main.getChildAt(0);
                                main.removeViewAt(0);
                                v.destroy();
                            }
                            try {
                                main.addView(getPageViewer(i + 1), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });


        return viewer;
    }

}
