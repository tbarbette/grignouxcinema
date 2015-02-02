package be.itstudents.tom.android.cinema.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import be.itstudents.tom.android.cinema.Cinema;
import be.itstudents.tom.android.cinema.Film;
import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

public class ScheduleListResultFragment extends Fragment {
	
	 	public class ResultCursorAdapter extends  CursorAdapter {

			public ResultCursorAdapter(Context context, Cursor c) {
				super(context, c, 0);
			}

			@Override
			public void bindView(View view, Context context, Cursor cur) {
				TableRow row = (TableRow)view;
				update(row, cur);
			}
			
			private void update(TableRow row, Cursor cur) {
				long cinema = Long.parseLong(cur.getString(cur.getColumnIndex(Seance.SEANCE_CINEMA)));
			    Calendar calendar = CalendarUtils.parseDate(cur.getString(cur.getColumnIndex(Seance.SEANCE_DATE)));			    
			    String titre = cur.getString(cur.getColumnIndex(Seance.SEANCE_TITLE));
	        	final Film film = new Film(titre, cur.getString(cur.getColumnIndex(Seance.SEANCE_FILMID)));
	        	/*if (i % 2 == 0) {
	        		bgColor = 0xFF050505;
	        	} else {
	        		bgColor = 0xFF1A1A1A;
	        	}
	        	row.setBackgroundColor(bgColor);
	        	*/
	        	
	        	ImageView i = (ImageView) row.findViewById(R.id.schedule_result_row_image);
		        switch ((int)cinema) {
		                case (int)Cinema.PARC:
		                        i.setImageResource(R.drawable.parc);
		                        break;
		                case (int)Cinema.CHURCHILL:
		                        i.setImageResource(R.drawable.churchill);
		                        break;
		                case (int)Cinema.SAUVENIERE:
		                        i.setImageResource(R.drawable.sauveniere);
		                        break;
		        }
			        
		       /* if (mFullDate) {
		        	TextView t = new TextView(context);
		        	SimpleDateFormat timeFormater = new SimpleDateFormat("EEE dd/MM");
		        	
		        	t.setText(timeFormater.format(calendar.getTime()));
		       
			        t.setGravity(Gravity.RIGHT);
			        t.setLayoutParams(new LayoutParams(
			                  LayoutParams.MATCH_PARENT,
			                  LayoutParams.WRAP_CONTENT));
			
			        t.setPadding(   (int)(getContext().getResources().getDisplayMetrics().density * 4),
		                    0,
		                    (int)(getContext().getResources().getDisplayMetrics().density * 2),
		                    0);
			        t.setTextSize(15);
			        this.addView(t);
		        }*/
		        
			        TextView hourt = (TextView)row.findViewById(R.id.schedule_result_row_hourtext);
			        //TODO Use newer API
			        SimpleDateFormat timeFormater = new SimpleDateFormat("HH:mm");
			        hourt.setText(timeFormater.format(calendar.getTime()));
			        
			        TextView titreText = (TextView)row.findViewById(R.id.schedule_result_row_titretext);
			        titreText.setText(titre);
			        
			        row.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							//A little dirty but prevents full redraw of the view
							if (ScheduleListFragment.mFilmManager != null) {
								ScheduleListFragment.mFilmManager.showFilmDetail(film);
							}
						}
					});
			      
			}

			@Override
			public View newView(Context context, Cursor cur, ViewGroup parent) {
				TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.schedule_list_result_row, parent, false);
				row.setClickable(true);
				update(row, cur);
				return row;
			}
	 	}
	 
		public int delta;
		private GridView mTable;
		private Calendar mDate;
		private boolean mFullDate;		
		private ResultCursorAdapter mCursorAdapter;
		private ProgressBar mSpinner;

		
		
	    public void setPosition(int position) {
	    	this.delta = position;
	    	mDate = Calendar.getInstance();
	    	mDate.add(Calendar.DATE, delta);
			CalendarUtils.zero(mDate);
	    }
	    	    
	    @Override
		public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	if (savedInstanceState != null)
	    		setPosition(savedInstanceState.getInt("position"));
	    	
			
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("position", this.delta);			
		}

		public Calendar getDate() {
			return mDate;
		}

		public void setDisplayFullDate(boolean full) {
			mFullDate = full;
		}				

		
		public boolean isSearch() {
			return mDate == null;
		}
		
		public void displayResult(Cursor cur) {
	    	
	    /*
	        
	    	int i = 0;
	    	int bgColor;
	    	double ss = Math.max(getResources().getDisplayMetrics().widthPixels , getResources().getDisplayMetrics().heightPixels ) / getResources().getDisplayMetrics().density;

	        do {
	        	
	            i++;
	        } while (cur.moveToNext());

	        hasData = true;
	        cur.close();
	    	} catch (CursorIndexOutOfBoundsException e
			private SimpleCursorAdapter mCursorAdapter;) {
	    		Log.e(ScheduleListFragment.TAG,"Pas de réponses du provider...");
	    		return;
	    	}*/
	    }
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.schedule_list_result, container, false);
	        mTable = (GridView)v.findViewById(R.id.result_table);
	        
			/*if (mFullDate) {
				mTable.setColumnShrinkable(3, true);
				mTable.setColumnStretchable(3, true);
			} else {
				mTable.setColumnShrinkable(2, true);
				mTable.setColumnStretchable(2, true);
			}*/
	        
			mCursorAdapter = new ResultCursorAdapter(getActivity(), null);
			mTable.setAdapter(mCursorAdapter);
			mSpinner = (ProgressBar)v.findViewById(R.id.result_progress);
			
			setListShown(false);
			
	        getLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					 // This is called when a new Loader needs to be created.  This
			        // sample only has one Loader, so we don't care about the ID.
			        // First, pick the base URI to use depending on whether we are
			        // currently filtering.
			    	String columns[] = new String[] { Seance.SEANCE_ID, Seance.SEANCE_TITLE, Seance.SEANCE_DATE, Seance.SEANCE_CINEMA, Seance.SEANCE_FILMID};
			    	
			    	if (getDate() == null) {
			    		System.err.println("NULL DATE !!!");
			    		return null;
			    	}
					return new CursorLoader(getActivity(),Uri.withAppendedPath(Uri.withAppendedPath(Seance.CONTENT_URI,"seances"),CalendarUtils.dateFormat.format(getDate().getTime())),   // The content URI of the words table
			    			columns,                        // The columns to return for each row
			    			null,                    // Selection criteria
			    			null,                     // Selection criteria
			    		    Seance.SEANCE_DATE + " ASC");
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
					mCursorAdapter.swapCursor(data);
					
			        setListShown(true);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> arg0) {
					mCursorAdapter.swapCursor(null);
				}
	        	
			});

	        return v;
	    }
		
		public void setListShown(boolean visible) {
			if (!visible) {
				mSpinner.setVisibility(View.VISIBLE);
				mTable.setVisibility(View.GONE);
			} else {
				mSpinner.setVisibility(View.GONE);
				mTable.setVisibility(View.VISIBLE);
			}
		}
	
}
