package be.itstudents.tom.android.cinema.datafetcher;

import java.util.EmptyStackException;

import be.itstudents.tom.android.cinema.datafetcher.DownloadManager.ImgToRetrieve;

public class ImageDownloader extends Thread implements Runnable {

	public boolean stopped = false;
	
	@Override
	public void run() {
		super.run();
		
		while (!stopped) {
			
			try {
				DownloadManager.imgToRetrieveS.acquire();
			} catch (InterruptedException e1) {
				continue;
			}
			if (stopped) return;
			ImgToRetrieve i;
			try {
				i = DownloadManager.imgToRetrieve.pop();
			} catch (EmptyStackException e) {
				e.printStackTrace();
				continue;
			}
			if (!i.onReceived.stillUsefull()) continue;
			
			try {
				i.onReceived.imageReceived(DownloadManager.getImage(i.url));
			} catch (OutOfMemoryError o) {
				i.onReceived.makeSpace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				i = null;
			}
			
		}
		
	}
	
}