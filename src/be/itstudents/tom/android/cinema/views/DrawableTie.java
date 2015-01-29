package be.itstudents.tom.android.cinema.views;

import java.util.concurrent.Semaphore;

import be.itstudents.tom.android.cinema.activity.JournalFragment;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager;
import be.itstudents.tom.android.cinema.datafetcher.DownloadManager.OnImageReceived;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

public class DrawableTie extends Drawable  implements ScalableImage.OnMove {

	private DrawableTieState state;
	private ScalableImage viewer;
	private int width;
	private int height;
	private String baseUrl;
	private Handler mHandler;
	private String extension;

	public DrawableTie(ScalableImage viewer, int width, int height, String baseUrl, String extension) {
		this.state = new DrawableTieState();
		this.viewer = viewer;
		viewer.setOnMove(this);
		this.width = width;
		this.height = height;
		setBounds(0, 0, width, height);
		this.baseUrl = baseUrl;
		this.extension = extension;
		mHandler = new Handler();
	}	

	public static Semaphore available = new Semaphore(1);
	
		@Override
		public void onMove(final double scale, final float dx,final float dy) {
			
			if (state.loader.availablePermits() <= 0) {
				state.lastCallScale = scale;
				state.lastCallDx = dx;
				state.lastCallDy = dy;
				return;
			} 

			try {
				state.loader.acquire();

				int totlevel = 16;

				double l = 1.0d/scale;

				final float sleft = -dx;
				final float stop = -dy;

				double logl = Math.log(l) / Math.log(2);
				int n = (int) Math.ceil(Math.pow(2,(Math.ceil(logl))));

				int level = totlevel / n; 


				if (level != state.level) {
					state.level = level;
					DownloadManager.clearAsyncQ();
					state.bmp = new Bitmap[level][level];
					state.n = n;
					System.gc();
				}

				int tw = width / n;
				int th = height / n;

				state.bw = width / level;
				state.bh = height / level;

				final float viewwidth = (float)(viewer.getWidth() / scale);
				final float viewheight = (float)(viewer.getHeight() / scale);
				if (baseUrl == null) {
					viewer.invalidate();
					return;
					
				}
				(new Thread() {
					@Override
					public void run() {
						try {
							available.acquire();

							try {
								for (int bj = 0; bj < state.level; bj++) {

									int top = bj * state.bh;
									if (((top + state.bh) <= (stop / scale)) || (top - state.bh > (stop / scale) + viewheight)) {
										if (state.level >= state.clearLevel) 
											for (int bi = 0; bi < state.level; bi++) {
												state.bmp[bj][bi] = null;
											}
										continue;
									}
									for (int bi = 0; bi < state.level; bi++) {
										try {
											int left = bi * state.bw;						
											if ((left + state.bw < (sleft / scale)) ||(left - state.bw > (sleft / scale) + viewwidth)) {
												if (state.level >= state.clearLevel) state.bmp[bj][bi] = null;
												continue;
											}
											if (state.bmp[bj][bi] != null) continue;

											final int levelAtLaunch = state.level;
											final int bjAtLaunch = bj;
											final int biAtLaunch = bi;
											DownloadManager.getImageAsync(baseUrl + "-" + state.level + "-" + bj + "-" + bi + extension,new OnImageReceived() {
												public boolean stillUsefull() {
													return (state.bmp[bjAtLaunch][biAtLaunch] == null)
															&& !viewer.isDestroyed();
												}

												public void imageReceived(Bitmap b) {
													if (state.level == levelAtLaunch)
														state.bmp[bjAtLaunch][biAtLaunch] = b;
													mHandler.post(new Runnable() {

														@Override
														public void run() {
															viewer.invalidate();
														}
													});

												}

												@Override
												public void makeSpace() {
													Log.d(JournalFragment.TAG, "No more memory for DrawableTie ! Switching from economy level " + state.clearLevel + " to " + state.level + " !");
													DownloadManager.clearAsyncQ();
													if (state.clearLevel  > state.level)
														state.clearLevel = state.level;
													for (int bj = 0; bj < state.level; bj++) {
														for (int bi = 0; bi < state.level; bi++) {
															state.bmp[bj][bi] = null;
														}
													}
													if (state.lastCallScale == -1) {
														state.lastCallScale = scale;
														state.lastCallDx = dx;
														state.lastCallDy = dy;
													}
													state.loader.release();
													double lastCallScale = state.lastCallScale;
													state.lastCallScale = -1;	
													onMove(lastCallScale, state.lastCallDx, state.lastCallDy);
												}
											});


										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									state.loader.release();
									if (state.lastCallScale != -1) {	
										double lastCallScale = state.lastCallScale;
										state.lastCallScale = -1;	
										onMove(lastCallScale, state.lastCallDx, state.lastCallDy);						
									}

								}
							} finally {
								available.release();
							}
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

					}
				}).start();	
			} catch (InterruptedException e1) {
				
			}


		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setAlpha(int alpha) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getOpacity() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void draw(Canvas canvas) {

			float[] values = new float[9];
			canvas.getMatrix().getValues(values);
			float sleft = -values[Matrix.MTRANS_X];
			float stop = -values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			canvas.drawColor(Color.WHITE);

			float viewwidth = viewer.getWidth() / scale;
			float viewheight = viewer.getHeight() / scale;
			try {
				if (baseUrl == null) throw new Exception();
			for (int bj = 0; bj < state.level; bj++) {
				int top = bj * state.bh;
				if (((top + state.bh) <= (stop / scale)) || (top - state.bh > (stop / scale) + viewheight)) {
					if (state.level >= state.clearLevel) 
						for (int bi = 0; bi < state.level; bi++) {
							state.bmp[bj][bi] = null;
						}
					continue;
				}
				for (int bi = 0; bi < state.level; bi++) {
					
						int left = bi * state.bw;						
						if ((left + state.bw < (sleft / scale)) ||(left - state.bw > (sleft / scale) + viewwidth)) {

							if (state.level >= state.clearLevel) state.bmp[bj][bi] = null;
							continue;
						}
						Bitmap b = state.bmp[bj][bi];
						Rect dst = new Rect((int)left, (int)top, (int)(left + (int)state.bw), (int)(top + (int)state.bh));
						Paint paint = new Paint();
						paint.setDither(true);
						paint.setAntiAlias(true);
						if (b == null) {

							paint.setColor(Color.rgb(50,50,50));

							paint.setStrokeWidth(10);
							canvas.drawRect(dst, paint);
							paint.setTextAlign(Align.CENTER);
							paint.setColor(Color.rgb(220,220,220));
							paint.setTextSize(state.n * 10);

							canvas.drawText("Chargement...", dst.centerX(), dst.centerY(), paint);
						} else {
							Rect src = new Rect((int)0, (int)0, (int)b.getWidth(), (int)b.getHeight());								

							canvas.drawBitmap(b, src, dst, paint);
						}

					
				}
			}
			} catch (Exception e) {
				Paint paint = new Paint();
				paint.setDither(true);
				paint.setAntiAlias(true);
	
				
				canvas.drawColor(Color.BLACK);
				paint.setStrokeWidth(10);
				
				paint.setTextAlign(Align.CENTER);
				paint.setColor(Color.rgb(150,55,65));
				paint.setTextSize(state.n * 20);
				canvas.drawText("Erreur...", sleft + (viewwidth / 2), stop + (viewheight / 2), paint);
				paint.setTextSize(state.n * 10);
				paint.setColor(Color.rgb(254,240,245));
				stop += state.n * 22;
				canvas.drawText("Votre appareil ne dispose probablement plus d'assez de mémoire", sleft + (viewwidth / 2), stop + (viewheight / 2), paint);
				stop += state.n * 12;
				canvas.drawText("ou votre connexion internet est défaillante...", sleft + (viewwidth / 2), stop + (viewheight / 2), paint);
			}


			
		}
		public class DrawableTieState {
			protected int clearLevel = 16;
			int level;
			int n;
			int bw;
			int bh;
			Bitmap[][] bmp;
			Semaphore loader = new Semaphore(1);
			protected double lastCallScale = -1;
			protected float lastCallDx;
			protected float lastCallDy;
		}
	}

