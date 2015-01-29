package be.itstudents.tom.android.cinema.activity;

import java.text.ParseException;
import java.util.Calendar;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.FilmManager;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.datafetcher.Horaire;
import be.itstudents.tom.android.cinema.dialogs.AboutDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment;
import be.itstudents.tom.android.cinema.dialogs.SearchDialogFragment.SearchManager;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;
import be.itstudents.tom.android.cinema.views.ResultView;


public class ScheduleListFragment extends Fragment {
	/*--------------*
	 * Variables
	 *--------------*/

	public final static boolean log = true;
	public FilmManager filmManager;

	private FrameLayout mainlayout;
	private ResultView searchView = null;

	public static final String TAG = "CinemaScheduleListFragment";
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainmenu, menu);
		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.aboutbtn:
			new AboutDialogFragment().show(getFragmentManager(), this.TAG);
			return true;
		case R.id.searchbtn:
			SearchDialogFragment s = new SearchDialogFragment();
			s.show(getFragmentManager(), this.TAG);
			s.setSearchManager(new SearchManager() {
				
				@Override
				public void doSearch(String pattern) {
					//TODO : Use newer API
					String columns[] = new String[] { Seance.SEANCE_ID, Seance.SEANCE_TITLE, Seance.SEANCE_DATE, Seance.SEANCE_CINEMA, Seance.SEANCE_FILMID};

					 Cursor cur = getActivity().managedQuery(
							 Uri.withAppendedPath(
									 Uri.withAppendedPath(
											 Seance.CONTENT_URI,"search")
											 ,pattern),
											 columns, null, null, Seance.SEANCE_DATE + " ASC");

					 if (searchView != null) mainlayout.removeView(searchView);
					 searchView = new ResultView(getActivity(), null, true);
					 searchView.scroll.setOnTouchListener(new TouchListener(new GestureDetector(new SimpleOnGestureListener(){

						 @Override
						 public boolean onSingleTapConfirmed(MotionEvent e) {
							 openFilmDetails();
							 return super.onSingleTapConfirmed(e);
						 }

						 @Override
						 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
							 return (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH);
						 }

					 })));
					 refreshDate(null);

					 mainlayout.removeView(flipView);

					 searchView.displayResult(cur);
					 mainlayout.addView(searchView);
				}
			});
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (currentDate != null)
			outState.putString("dateView", CalendarUtils.dateFormat.format(currentDate.getTime()));
		
	}
	Calendar showView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.schedule_list, container, false);
		flipView =(ViewFlipper)v.findViewById(R.id.flipper);
		
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


		
		int i = 0;

		while (dateView.before(showView) || dateView.equals(showView)) {
			fetchingData = true;
			
			final ResultView firstView = new ResultView(getActivity(), (Calendar)dateView.clone(), false);
			refreshDate(dateView);

			Cursor cur;
			cur = Horaire.getSeancesAtDate((Calendar)dateView.clone(), getActivity());

			(new OnResultReceived(cur, firstView)).run();
			
			dateView.add(Calendar.DATE, 1);
			i++;
		}
		
		flipView.setDisplayedChild(i - 1);
		
		mainlayout = (FrameLayout)v.findViewById(R.id.schedule_list_layout);
		return v;
	}
	
	interface TitleManager {
		public void changeTitle(String title);
	}
	
	private TitleManager titleManager;
	
	public void setTitleManager(TitleManager titleManager) {
		this.titleManager = titleManager; 
	}
	
    private void refreshDate(Calendar date) {
        if (date != null) {
        	//TODO deprecated
        	String jour = (String)android.text.format.DateFormat.format(
                                "EEEE " +
                                android.text.format.DateFormat.DATE + android.text.format.DateFormat.DATE +
                                "/" +
                                android.text.format.DateFormat.MONTH + android.text.format.DateFormat.MONTH +
                                "/" +
                                android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR + android.text.format.DateFormat.YEAR
                                ,date);
        	//TODO : this is bad...
                ((MainActivity)getActivity()).setTitle(getResources().getString(R.string.horaire_of) + " " + jour.toLowerCase());
                
        } else {
        	((MainActivity)getActivity()).setTitle(R.string.results);
        }
    }
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		SWIPE_MIN_DISTANCE =(int)(ViewConfiguration.get(getActivity()).getScaledTouchSlop() * 5);
		
		SWIPE_THRESHOLD_VELOCITY = (int)(ViewConfiguration.get(getActivity()).getScaledMinimumFlingVelocity() * 1.5);

		slideLeftIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_out);

		slideLeftIn.setAnimationListener(new OnScrollView());
		slideRightIn.setAnimationListener(new OnScrollView());

		//TODO deprecated
		gestureDetector = new GestureDetector(new MyGestureDetector());



	}
	
	
	
	private void addResultView(final Calendar date, final int index)
	{
		fetchingData = true;
		
		
		final ResultView firstView = new ResultView(getActivity(), date, false);

		Thread t = new Thread() {
			public void run() {

				Cursor cur;
				cur = Horaire.getSeancesAtDate(date, getActivity());
				
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
				if (ScheduleListFragment.log)
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


	public Calendar currentDate;

	
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
			refreshDate(((ResultView)flipView.getCurrentView()).getDate());
		}
	}
	
		
	public void setFilmManager(FilmManager filmManager) {
		this.filmManager = filmManager;		
	}
	
	public boolean openFilmDetails() {
		if (clickedFilm != null && filmManager != null) {
			filmManager.showFilmDetail(clickedFilm);
			return true;
		}
		return false;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().managedQuery(Uri.withAppendedPath(Seance.CONTENT_URI,"close"),null, null, null,null);
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			openFilmDetails();
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
							Toast t = Toast.makeText(getActivity(),R.string.nopast,Toast.LENGTH_SHORT);
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
				Toast t = Toast.makeText(getActivity(), R.string.nodata, Toast.LENGTH_SHORT);
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



	//TODO : Should be handled with the new backstack system
	public boolean backPressed() {
		/*if (searchView == null)
			return false;
		
		mainlayout.removeView(searchView);
		mainlayout.addView(flipView);
		searchView = null;		
		return true;*/
		return true;
	}

}