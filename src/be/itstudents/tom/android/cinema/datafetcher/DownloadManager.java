package be.itstudents.tom.android.cinema.datafetcher;

import java.io.File;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;


public class DownloadManager {

	static HashMap<String, String> cacheString = new HashMap<String, String>();
	static LinkedList<ImageDownloader> downloaders = new LinkedList<ImageDownloader>();
	
	public static void startAsyncT(int n) {
		for (int i = 0; i < n; i++)
			downloaders.add(new ImageDownloader());
	
		for (ImageDownloader d : downloaders) {
			d.start();
		}
	}
	
	public static void stopAll() {
		imgToRetrieveS.drainPermits();
		imgToRetrieve.clear();
		for (ImageDownloader d : downloaders) {
			d.stopped = true;
			d.interrupt();
		}
		downloaders.clear();
	}
	
	public static void clearAsyncQ() {
		imgToRetrieveS.drainPermits();
		imgToRetrieve.clear();
		for (ImageDownloader d : downloaders) {
			d.interrupt();
		}
	}

	public static HttpResponse get(String url) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		return client.execute(request);

	}

	public static String getString(String url) throws Exception {

		if (cacheString.get(url) != null) {
			cacheString.get(url);
		} else {			
			cacheString.put(url, HttpHelper.request(get(url)));
		}
		return cacheString.get(url);
	}

	public static Bitmap getImage(String url) throws Exception {
		URL u = new URL(url);

		String path;

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory().toString() + "/grignoux/";
			if (!(new File(path)).exists()) {				
				(new File(path)).mkdirs();
				(new File(path + ".nomedia")).createNewFile();
			}

			File file = new File(path + u.getFile());
			Bitmap bmp = null;
			if (file.exists()) {
				//Log.i(ScheduleActivity.TAG, "EXIST : " + url);
				FileInputStream fIn = new FileInputStream(file);
				bmp = BitmapFactory.decodeStream(new BufferedInputStream(fIn));
				fIn.close();
			}
			if (bmp == null) {			
				//Log.i(ScheduleActivity.TAG, "DOWNLOAD : " + url);
				new File(file.getParent()).mkdirs();

				FileOutputStream fOut = new FileOutputStream(file);		
				try {
				
				 HttpGet httpRequest = null;

			        try {
			                httpRequest = new HttpGet((new URL(url)).toURI());
			                
			        } catch (URISyntaxException e) {
			                e.printStackTrace();
			        }
			        HttpParams params = new BasicHttpParams();
			        HttpConnectionParams.setConnectionTimeout(params, 4500);
			        HttpConnectionParams.setSoTimeout(params, 4500);
			        HttpClient httpclient = new DefaultHttpClient(params);
			        
			        HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
			        
			        HttpEntity entity = response.getEntity();
			        
			        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
			        
			        int length = (int) bufHttpEntity.getContentLength();
					InputStream input = (InputStream)bufHttpEntity.getContent();
					BufferedInputStream bis = new BufferedInputStream(input);
	
					ByteArrayBuffer baf = new ByteArrayBuffer((length > 4096)? length : 4096);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					
	
					}

				fOut.write(baf.toByteArray());
				fOut.close();

				bmp = BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.length());
				} finally {
					fOut.close();
				}

			}
			return bmp;
		} else {

			InputStream input = (InputStream)u.getContent();
			return BitmapFactory.decodeStream(input);
		}
	}
	
	public static class ImgToRetrieve {
		String url;
		OnImageReceived onReceived;
	};
	
	public static Semaphore imgToRetrieveS = new Semaphore(0);
	
	public static Stack<ImgToRetrieve> imgToRetrieve = new Stack<ImgToRetrieve>();
	public static void getImageAsync(String string, OnImageReceived onReceived) {
		ImgToRetrieve i = new ImgToRetrieve();
		i.url = string;
		i.onReceived = onReceived;
		
		imgToRetrieve.add(i);
		imgToRetrieveS.release();
	}
	
	public interface OnImageReceived {
		public void imageReceived(Bitmap b);
		public boolean stillUsefull();
		public void makeSpace();
	}

	public static int getNumAsyncT() {
		return downloaders.size();
	}

	
}
