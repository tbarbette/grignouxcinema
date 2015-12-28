package be.itstudents.tom.android.cinema.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.datafetcher.FilmList;

public class FeaturedListFragment extends DualPaneListFragment {

    static int nImplementations = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cinemafilms, container, false);
        //TODO : Still needed?
        LinearLayout main = (LinearLayout) v.findViewById(R.id.cinameafilmslayout);

        try {
            FilmList.available.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FilmList.available.release();
        if (FilmList.getList() == null)
            try {
                FilmList.loadList();
            } catch (Exception e) {
                e.printStackTrace();
            }

        if (FilmList.getList() != null && FilmList.getList().size() > 0) {
            GridView gridview = (GridView) v.findViewById(R.id.gridview);
            ImageAdapter adapter = new ImageAdapter(getActivity());
            float dp = getResources().getDisplayMetrics().density;
            int numcolumn = (int) (getResources().getDisplayMetrics().widthPixels / (150 + 20 * dp));
            if (numcolumn < 2) numcolumn = 2;
            gridview.setNumColumns(numcolumn);
            gridview.setAdapter(adapter);

            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (filmManager != null)
                        filmManager.showFilmDetail(FilmList.getList().get(position));
                }
            });
        } else {
            TextView text = new TextView(getActivity());
            text.setText(R.string.unconnected_films);
            text.setGravity(android.view.Gravity.CENTER);
            main.addView(text, 1, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

            System.err.println(savedInstanceState.getString("STATE"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("STATE", "okay");
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
