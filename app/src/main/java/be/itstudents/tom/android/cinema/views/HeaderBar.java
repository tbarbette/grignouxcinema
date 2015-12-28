package be.itstudents.tom.android.cinema.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class HeaderBar extends TextView {

    public HeaderBar(Activity activity) {
        super(activity);
        setBackgroundColor(Color.rgb(220, 220, 220));
        setTextColor(Color.BLACK);
        setTypeface(Typeface.DEFAULT_BOLD);
        setPadding(2, 4, 2, 4);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
    }

}
