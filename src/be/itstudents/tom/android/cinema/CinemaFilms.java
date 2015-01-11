package be.itstudents.tom.android.cinema;

import be.itstudents.tom.android.cinema.views.HeaderBar;
import be.itstudents.tom.android.cinema.datafetcher.FilmList;
import be.itstudents.tom.android.cinema.datafetcher.MainLoader;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CinemaFilms extends Activity {

	@Override
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.cinemafilms);  
		HeaderBar bar = new HeaderBar(this);
		bar.setText(R.string.filmlist);
		LinearLayout main = (LinearLayout)findViewById(R.id.cinameafilmslayout);
		main.addView(bar,0);
		
		
		MainLoader.getMain(this).waitForFinished(this);
		
		try {
			FilmList.available.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FilmList.available.release();
		if (FilmList.getList()==null)
			try {
				FilmList.loadList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (FilmList.getList() != null && FilmList.getList().size() > 0) {
				GridView gridview = (GridView)findViewById(R.id.gridview);
				ImageAdapter adapter = new ImageAdapter(this);
				float dp = getResources().getDisplayMetrics().density;
				int numcolumn = (int) (getResources().getDisplayMetrics().widthPixels / (150 + 20*dp));
				if (numcolumn < 2) numcolumn = 2;
				gridview.setNumColumns(numcolumn);
				gridview.setAdapter(adapter);
				
				gridview.setOnItemClickListener(new OnItemClickListener() {
			        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			        	FilmDetail filmDetail = new FilmDetail(CinemaFilms.this, FilmList.getList().get(position));
						filmDetail.show();
			        }
			    });
		} else {
			TextView text = new TextView(this);
			text.setText(R.string.unconnected_films);
			text.setGravity(android.view.Gravity.CENTER);
			main.addView(text,1,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		}
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return FilmList.getList().size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {  // if it's not recycled, initialize some attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setPadding(0, 0, 0, 0);
				imageView.setAdjustViewBounds(true);
			} else {
				imageView = (ImageView) convertView;
			}

			try {
				
				imageView.setImageBitmap(FilmList.getList().get(position).getAffiche());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return imageView;
		}	
	}

}
