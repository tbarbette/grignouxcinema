package be.itstudents.tom.android.cinema.service;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import be.itstudents.tom.android.cinema.Seance;
import be.itstudents.tom.android.cinema.activity.ScheduleActivity;
import be.itstudents.tom.android.cinema.utils.CalendarUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class CinemaSyncer extends Service {

	private final static String TAG = "CinemaSyncer";
	
	private final static int PRESYNC_DAYS = 14;
	
    public Timer timer;

    class LaunchSync extends TimerTask {
          public void run() { 
                    if (haveInternet()) {
                    for (int i=0; i < PRESYNC_DAYS; i++) {
                    	if (i > 3)
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	Calendar dateView = Calendar.getInstance();
                    	
                    	dateView.add(Calendar.DATE, i);
                    	CalendarUtils.zero(dateView);
                            try {
                            	Cursor cur =getContentResolver().query(Uri.withAppendedPath(Uri.withAppendedPath(Seance.CONTENT_URI,"seances"),CalendarUtils.dateFormat.format(dateView.getTime())), null, null, null, null);
                            	if (cur != null)
                                	cur.close();
                            } catch (IllegalStateException e) {
                            	
                            }
                            
                            
                    }
            }

            }
    }

    @Override
    public void onCreate() {
              super.onCreate();
              if (ScheduleActivity.log) Log.i(TAG, "Service started");

              timer = new Timer(TAG);

              timer.scheduleAtFixedRate(new LaunchSync(), 20, (long)3600*1000*6);
    }

    private boolean haveInternet() {
        NetworkInfo info=((ConnectivityManager)(getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        if(info==null || !info.isConnected()){
            return false;
        }
        if(info.isRoaming()){
            return true;
        }
        return true;
    }


    @Override
    public IBinder onBind(Intent intent) {
            // TODO Auto-generated method stub
            return null;
    }
}     