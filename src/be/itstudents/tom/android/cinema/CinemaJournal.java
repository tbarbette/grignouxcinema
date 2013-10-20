package be.itstudents.tom.android.cinema;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Semaphore;

import be.itstudents.tom.android.cinema.Views.DrawableTie;
import be.itstudents.tom.android.cinema.Views.HeaderBar;
import be.itstudents.tom.android.cinema.Views.ScalableImage;
import be.itstudents.tom.android.cinema.Views.ScalableImage.FlipHandler;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager.OnImageReceived;
import be.itstudents.tom.android.cinema.datafetcher.FilmList;
import be.itstudents.tom.android.cinema.exceptions.NoIdException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CinemaJournal extends Activity {

	

	/*--------------*
	 * Menu
	 *--------------*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.journalmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.grignoux.be/journal/journal_Grignoux2.pdf"));
		this.startActivity(intent);		
		return true;
	}

	public static final String TAG = "CinemaJournal";

	protected void onStop() {
		super.onStop();

		if (viewer != null) {
			viewer.destroy();
			viewer = null;
		}

	}

	private ScalableImage viewer;

	private static int num;
	private static String lastId = null;
	float dp;
	static LinearLayout main;
	Handler mHandler;
	private int currentPage = 0;

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
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
	protected void onPause() {
		super.onPause();
		DownloadManager.stopAll();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("recreate");
		if (savedInstanceState != null) {
			currentPage = savedInstanceState.getInt("currentPage", 0);
		}
		dp = getResources().getDisplayMetrics().density;
		setContentView(R.layout.cinemajournal);  
		HeaderBar bar = new HeaderBar(this);
		bar.setText(R.string.journal);
		main = (LinearLayout)findViewById(R.id.cinemajournallayout);
		main.removeAllViews();
		main.addView(bar,0);
		mHandler = new Handler();

		
		try {


			viewer = getPageViewer(currentPage);
			main.addView(viewer, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));


		} catch (Exception e) {
			TextView text = new TextView(this);

			text.setText(R.string.unconnected_journal);

			text.setGravity(android.view.Gravity.CENTER);
			main.addView(text,1,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		}

	}

	

	private ScalableImage getPageViewer(final int i) throws Exception {
		final ScalableImage viewer = new ScalableImage(this);
		DownloadManager.clearAsyncQ();
		currentPage = i;
		final DrawableTie image;
		if (lastId == null)
			 image = new DrawableTie(viewer, 2272, 3456, null, null);
		else
			 image = new DrawableTie(viewer, 2272, 3456, "http://www.arsbss.be/~tom/grignoux/" + lastId  + "-" + i,".png");

		

		mHandler.postDelayed(new Runnable() {
			public void run() {
				viewer.setImage(image);
			}
		},100);


		viewer.setPadding((int)(8 *dp) , (int)(8 *dp) , (int)(8 *dp) , (int)(8 *dp));
		viewer.setFlipHandler(new ScalableImage.FlipHandler() {
			public void previous() {

				if (i > 0) {
					viewer.setFlipHandler(null);
					viewer.setLoading(true);

					mHandler.removeCallbacksAndMessages(null);
					mHandler.post(new Runnable() {

						@Override
						public void run() {

							ScalableImage v = (ScalableImage)main.getChildAt(1);
							main.removeViewAt(1);
							v.destroy();

							try {
								main.addView(getPageViewer(i - 1), new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
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
							ScalableImage v = (ScalableImage)main.getChildAt(1);
							main.removeViewAt(1);
							v.destroy();

							try {
								main.addView(getPageViewer(i + 1), new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
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

	public static void loadLast() throws Exception {
		try {
			String text = DownloadManager.getString("http://www.arsbss.be/~tom/grignoux/last.html");

			String[] val = text.split(" ");
			lastId  = val[0];

			num = Integer.parseInt(val[1]);

			new Thread(new Runnable() {

				@Override
				public void run() {
					if (CinemaHoraires.log) Log.d(TAG, "Cleaning thread started...");
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						String path = Environment.getExternalStorageDirectory().toString() + "/grignoux/~tom/grignoux/";

						File file = new File(path);

						if (file.exists()) {
							File[] files = file.listFiles();
							for (File f : files) {
								if (! f.getName().startsWith(lastId)) {
									if (CinemaHoraires.log) Log.d(TAG, "Deleting " + f.getName() +" ...");
									f.delete();
								}
							}
						}	


					}

				}
			});

		} catch (Exception e) {
			lastId = null;
		} finally {

		}

	}

}
