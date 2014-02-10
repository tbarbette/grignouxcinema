package be.itstudents.tom.android.cinema;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.datafetcher.HttpHelper;


import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class FilmDetail extends Dialog {
	
	String details;
	private Film film;
	TextView titre;
	private TextView content;
	private LinearLayout flip;
	private Context context;
	private ImageButton you;
	private ImageView affiche;
	
	public FilmDetail(Context context, Film film) {
		super(context);
		this.film = film;
		this.context = context;
		setContentView(R.layout.cinemadetail);
		content = (TextView)findViewById(R.id.cinemadetail_content);
		affiche = (ImageView)findViewById(R.id.cinemadetail_affiche);
		you = (ImageButton)findViewById(R.id.cinemadetail_you);
		flip = (LinearLayout)findViewById(R.id.cinemadetail_flip);
		this.setTitle(film.titre);
		you.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goYoutube();
			}
		});
		final Handler mHandler = new Handler();
		mHandler.postAtFrontOfQueue(new Runnable(){
			@Override
			public void run() {
				mHandler.postDelayed(new Fetcher(), 100);
			}
			
		});
		

	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);

	    if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
	        // Tapped outside so we finish the activity
	        this.dismiss();
	    }
	    return super.dispatchTouchEvent(ev);
	}
	
	public void goYoutube() {
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.setPackage("com.google.android.youtube");
		intent.putExtra("query", film.titre);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		}
		catch (Exception err) {
			context.startActivity(new Intent(Intent.ACTION_VIEW,
					 Uri.parse("http://m.youtube.com/#/results?search_query=" + film.titre)));
		}
	}
	
	
	class Fetcher implements Runnable {
		
	public Fetcher() {

	}
	
	@Override
	public void run() {
		
		String url = "http://www.grignoux.be/films/" + film.id;
        
        try{
            
            String html = DownloadManager.getString(url);
            Pattern pattern = Pattern.compile("(<table class='film_infos'>.*</table>).<table class='album'>(.*)</table>.<p><p>(.*?)</p></p>",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
            	
            	Pattern photosP = Pattern.compile("href=\"(.*?)\".*?src=\"(.*?)\"",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                final Matcher photosM = photosP.matcher(matcher.group(2));
                boolean vis = false;
           
                while (photosM.find()) {
                	vis = true;
                	ImageView i = new ImageView(context);
                	
                	Bitmap bitmap = DownloadManager.getImage("http://www.grignoux.be/" + photosM.group(2));
                	i.setImageBitmap(bitmap); 
                    i.setOnClickListener(new PhotoClick(photosM.group(1)));                
                	flip.addView(i,0);                	
                	
                }
            	
                if (vis) {
                	flip.setVisibility(View.VISIBLE); 
                	
                }
            	String desc = matcher.group(1).replace("</tr>","</tr><br>");
            	String infos = matcher.group(3);
            
            	
	            Pattern filmP = Pattern.compile("(.*?)<div.*</div>",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	            Matcher filmM = filmP.matcher(desc);
	            if (filmM.find()) {
	            	desc = filmM.group(1);
	            }

           	content.setText(Html.fromHtml(desc + infos));
          
            content.setMovementMethod(LinkMovementMethod.getInstance());
            content.setVisibility(View.VISIBLE);
             /*   Pattern pfilms = Pattern.compile("",Pattern.MULTILINE |  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher mfilms = pfilms.matcher(matcher.group(1));
                while (mfilms.find()) {


            }*/
            affiche.setImageBitmap(film.getAffiche());
            affiche.setOnClickListener(new PhotoClick(film.imageURL.replace("/affiche/","/original/")));
            } else {
            	content.setText("Désolé, la fiche de ce film n\'est pas disponible...");
            }

        }catch(Exception ex) {
        	ex.printStackTrace();
        	content.setText("La fiche du film n'est pas disponible hors-ligne.");
        }
	}
	}
	
	class PhotoClick implements View.OnClickListener {
		private String url;
		
		public PhotoClick(String url) {
			this.url = url;
		}
			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(Intent.ACTION_VIEW,
						 Uri.parse((url.contains("grignoux.be")?"":"http://www.grignoux.be/") + url)));

			}
	}
}
