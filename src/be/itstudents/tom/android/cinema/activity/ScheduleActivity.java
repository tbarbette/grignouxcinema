package be.itstudents.tom.android.cinema.activity;

import java.text.ParseException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import be.itstudents.tom.android.cinema.service.CinemaSyncer;
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.views.FilmDetail;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.views.*;
import be.itstudents.tom.android.cinema.datafetcher.Horaire;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;


public class ScheduleActivity extends Activity {
	/*--------------*
	 * Variables
	 *--------------*/

	private final static String PREFS_NAME = "cinema.prefs";

	

	static final int DIALOG_ABOUT = 0;
	static final int DIALOG_SEARCH = 1;
	static final int DIALOG_UPDATE = 2;

	public final static boolean log = true;

	private LinearLayout mainlayout;
	private ResultView searchView = null;

	public static final String TAG = "ScheduleActivity";
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper flipView;
	final Handler mHandler = new Handler();

	public static int SWIPE_MIN_DISTANCE = 120;
	public static int SWIPE_MAX_OFF_PATH = 200;
	public static int SWIPE_THRESHOLD_VELOCITY = 200;
	public GestureDetector gestureDetector; 

	public static Film clickedFilm;

	private boolean fetchingData = false;

	public boolean goNext;

	/*--------------*
	 * Menu
	 *--------------*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}


	protected Dialog onCreateDialog(int id) {
		final Activity act = this;
		final Dialog dialog;
		AlertDialog alert;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case DIALOG_ABOUT:

			builder.setMessage(R.string.abouttxt)
			.setCancelable(true);

			alert = builder.create();
			alert.setButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();					
				}} );
			dialog = alert;
			break;
		case DIALOG_UPDATE:
			builder.setMessage(R.string.slowtxt)
			.setCancelable(true);
			alert = builder.create();
			alert.setButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();					
				}} );
			dialog = alert;
			break;

		case DIALOG_SEARCH:
			Context mContext = this;
			dialog = new Dialog(mContext);

			dialog.setContentView(R.layout.editdialog);
			dialog.setTitle(R.string.searchtxt);

			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(android.R.drawable.ic_menu_search);

			Button btn = (Button) dialog.findViewById(R.id.searchgo);
			btn.setText(R.string.searchgo);
			btn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String columns[] = new String[] { Seance.SEANCE_ID, Seance.SEANCE_TITLE, Seance.SEANCE_DATE, Seance.SEANCE_CINEMA, Seance.SEANCE_FILMID};

					EditText text = (EditText)dialog.findViewById(R.id.text);


					if (text.getText().toString().equals("")) {
						dismissDialog(DIALOG_SEARCH);
						return;
					}
					Cursor cur = managedQuery(
							Uri.withAppendedPath(
									Uri.withAppendedPath(
											Seance.CONTENT_URI,"search")
											,text.getText().toString()),
											columns, null, null, Seance.SEANCE_DATE + " ASC");

					if (searchView != null) mainlayout.removeView(searchView);
					searchView = new ResultView(act, null, true);
					searchView.scroll.setOnTouchListener(new TouchListener(new GestureDetector(new SimpleOnGestureListener(){

						@Override
						public boolean onSingleTapConfirmed(MotionEvent e) {
							goYoutube();
							return super.onSingleTapConfirmed(e);
						}

						@Override
						public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
							return (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH);
						}

					})));

					mainlayout.removeView(flipView);



					searchView.displayResult(cur);
					mainlayout.addView(searchView);
					dismissDialog(DIALOG_SEARCH);
				}
			});
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.aboutbtn:
			showDialog(DIALOG_ABOUT);
			return true;
		case R.id.searchbtn:
			showDialog(DIALOG_SEARCH);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*--------------*
	 * Creator and functions
	 *--------------*/

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (currentDate != null)
			outState.putString("dateView", CalendarUtils.dateFormat.format(currentDate.getTime()));
		
	}
	Calendar showView;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar dateView = Calendar.getInstance();
		CalendarUtils.zero(dateView);
		 
		showView = (Calendar) dateView.clone();
		if (savedInstanceState != null) {
			String showViews = savedInstanceState.getString("dateView");
			if (showViews != null && !showViews.equals("")) {

				try {
					showView.setTime(CalendarUtils.dateFormat.parse(showViews));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		Intent svc = new Intent(this, CinemaSyncer.class);
		startService(svc);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cinemahoraires);  

		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		SWIPE_MIN_DISTANCE =(int)(ViewConfiguration.get(this).getScaledTouchSlop() * 5);
		
		SWIPE_THRESHOLD_VELOCITY = (int)(ViewConfiguration.get(this).getScaledMinimumFlingVelocity() * 1.5);

		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

		slideLeftIn.setAnimationListener(new OnScrollView());

		flipView =(ViewFlipper)findViewById(R.id.flipper);
		gestureDetector = new GestureDetector(new MyGestureDetector());

		int i = 0;

		while (dateView.before(showView) || dateView.equals(showView)) {
			fetchingData = true;
			
			final ResultView firstView = new ResultView(this, dateView, false);

			Cursor cur;
			cur = Horaire.getSeancesAtDate((Calendar)dateView.clone(), ScheduleActivity.this);

			(new OnResultReceived(cur, firstView)).run();
			
			dateView.add(Calendar.DATE, 1);
			i++;
		}
		flipView.setDisplayedChild(i - 1);
		mainlayout = (LinearLayout) findViewById(R.id.mainlayout); 


		
		if (settings.getBoolean("slowMessage", false)==false) {
			showDialog(DIALOG_UPDATE);	            
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("slowMessage", true);
			editor.commit();	            
		}	    

		if (settings.getBoolean("updMessage18", false)==false) {
			Dialog d = new Dialog(this);	
			d.setTitle("Cinéma Grignoux devient OpenSource !");
			TextView t = new TextView(this);
			t.setText(Html.fromHtml("Ce programme est désormais OpenSource ! Cela signifie que tout le monde peut contribuer à l'améliorer.<br/>\n<br/>\nRendez-vous sur <a href=\"http://github.com/MappaM/grignouxcinema\">http://github.com/MappaM/grignouxcinema</a> pour contribuer ou signaler un problème et laisser la communauté s'en occuper."));
			float dp = getResources().getDisplayMetrics().density;
			t.setPadding((int)(4*dp),(int)(4*dp),(int)(4*dp),(int)(4*dp));
			d.setContentView(t);
			d.show();
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("updMessage18", true);
			editor.commit();	            
		}	
	}
	
	private void addResultView(final Calendar date, final int index)
	{
		fetchingData = true;
		
		final ResultView firstView = new ResultView(this, date, false);

		Thread t = new Thread() {
			public void run() {

				Cursor cur;
				cur = Horaire.getSeancesAtDate(date, ScheduleActivity.this);

				mHandler.post(new OnResultReceived(cur, firstView));          	
			}};
			t.start();


	}
	
	/*--------------*
	 * Listeners
	 *--------------*/	
	
	class TouchListener implements OnTouchListener {
		
		GestureDetector gestDect;
		public TouchListener(GestureDetector gestDect) {
			this.gestDect = gestDect;
		}
		@Override
		public boolean onTouch(View v, MotionEvent event) { 
			if (gestDect.onTouchEvent(event))
				return true;
			else
				return false;
		}
	}

	class OnResultReceived implements Runnable {
		private Cursor cur;
		private ResultView view;

		public OnResultReceived(Cursor cur, ResultView view) {
			this.cur = cur;
			this.view = view;
		}


		public void run() {
			view.displayResult(cur);
			view.scroll.setOnTouchListener(new TouchListener(gestureDetector));
			flipView.addView(view);

			if (showView.equals(view.getDate())) {
				Calendar newDate = (Calendar) (view.getDate().clone());
				newDate.add(Calendar.DATE, 1);
				addResultView(newDate, 1);
			}
			if (goNext) {
				goNext = false;
				if (ScheduleActivity.log)
					Log.i(TAG,"Le passage a la vue suivante a été reporté car les données ne sont pas encore disponibles");
				view.table.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						flipView.showNext();
						fetchingData = false;
					}

				},100);
			} else 
				fetchingData = false;
		}
	};
	FilmDetail filmDetail;

	public Calendar currentDate;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && (searchView != null)) {
			mainlayout.removeView(searchView);
			mainlayout.addView(flipView);
			searchView = null;
			
			return true;
			
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			showDialog(DIALOG_SEARCH);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	class OnScrollView implements AnimationListener{

		@Override
		public void onAnimationEnd(final Animation animation) {

			if (animation == slideLeftIn) {
				if (flipView.indexOfChild(flipView.getCurrentView()) == (flipView.getChildCount() - 1)) {
					Calendar newDate = (Calendar) (((ResultView)flipView.getCurrentView()).getDate()).clone(); 
					newDate.add(Calendar.DATE, 1);
					addResultView(newDate, flipView.getChildCount());
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	}
	

	
	public void goYoutube() {
		if (clickedFilm != null) {
			filmDetail = new FilmDetail(this, clickedFilm);
			filmDetail.show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		managedQuery(Uri.withAppendedPath(Seance.CONTENT_URI,"close"),null, null, null,null);
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
				goYoutube();
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			else {
				try {
					if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

						if(canFlipRight()){

							flipView.setInAnimation(slideLeftIn);
							flipView.setOutAnimation(slideLeftOut); 
							//fetchingData.acquire();
							flipView.showNext();
							currentDate = ((ResultView)flipView.getChildAt(flipView.getDisplayedChild())).getDate();
							//fetchingData.release();
						} else { 
							return false;
						}

					} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						if(canFlipLeft()){
							flipView.setInAnimation(slideRightIn);
							flipView.setOutAnimation(slideRightOut);
							//fetchingData.acquire();

							flipView.showPrevious();
							//fetchingData.release();
						} else {
							Toast t = Toast.makeText(getApplicationContext(),R.string.nopast,Toast.LENGTH_SHORT);
							t.show();
						}
					} 
				} catch (Exception e) {
					// nothing
				}
				return true;
			}
		} 

		boolean canFlipRight() {
			Debug.stopMethodTracing();
			int index = flipView.indexOfChild(flipView.getCurrentView())+ 1;
			if (index == flipView.getChildCount()) {
				if (fetchingData) {
					goNext = true;
					return false;
				}
			}
			if (!((ResultView)(flipView.getChildAt(index))).hasData()) {				
				Toast t = Toast.makeText(getApplicationContext(), R.string.nodata, Toast.LENGTH_SHORT);
				t.show();
				return false;
			} else {
				return true;
			}

		}

		boolean canFlipLeft() {
			return (flipView.indexOfChild(flipView.getCurrentView()) > 0);
		}
	}

}