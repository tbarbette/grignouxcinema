package be.itstudents.tom.android.cinema.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import be.itstudents.tom.android.cinema.R;
import be.itstudents.tom.android.cinema.datafetcher.Loader;
import be.itstudents.tom.android.cinema.datafetcher.Loader.LoaderEvent;
import be.itstudents.tom.android.cinema.datafetcher.MainLoader;

public class SplashScreen extends Activity {

    final int BMP_NUM = 3;
    Bitmap[] bmp = new Bitmap[BMP_NUM];
    float dp;
    LoadingAnimation la;
    int o;
    Loader l;
    GameThread g;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dp = getResources().getDisplayMetrics().density;

        o = getResources().getConfiguration().orientation;

        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.sauveniere);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.churchill);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.parc);

        la = new LoadingAnimation(this);
        la.setVisibility(View.VISIBLE);
        setContentView(la);


        l = MainLoader.getMain(this);
        l.setLoaderEvent(new LoaderEvent() {
            private boolean backgrounded;

            public void onProgress(int progress, int total) {

            }

            public void onFinish() {

                if (!backgrounded) {
                    backgrounded = true;
                    g.setTitle("Lancement");
                    g.setStatus("");
                    g.setConverge(true, new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onStatus(String title, String desc) {
                if (title != null) g.setTitle(title);
                if (desc != null) g.setStatus(desc);
            }

            @Override
            public void onCancel() {
                finish();
            }

            @Override
            public void onGoBackground() {
                backgrounded = true;
                g.setTitle("Lancement");
                g.setStatus("");
                g.setConverge(true, new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        l.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (g != null)
            g.setRunning(false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        newConfig.orientation = o;
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(o);

    }

    class LoadingAnimation extends SurfaceView implements SurfaceHolder.Callback {

        SurfaceHolder holder;

        public LoadingAnimation(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
            g = new GameThread(holder, this);
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            g.setRunning(true);
            g.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            boolean retry = true;
            g.setRunning(false);
            while (retry) {
                try {
                    g.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }
    }

    class GameThread extends Thread {
        float x, y;
        boolean converge = false;
        private SurfaceHolder holder;
        private SurfaceView v;
        private String status = "";
        private String title = "Chargement";
        private boolean _run;
        private Runnable callback;
        private long convtime;

        public GameThread(SurfaceHolder holder, SurfaceView v) {
            this.holder = holder;
            this.v = v;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setRunning(boolean _run) {
            this._run = _run;
        }

        public void setConverge(boolean converge, Runnable callback) {
            this.converge = converge;
            this.callback = callback;
            this.convtime = SystemClock.uptimeMillis();
        }


        @Override
        public void run() {

            float centerX = v.getWidth() / 2;
            float centerY = v.getHeight() / 2;

            float bmpWidth[] = new float[BMP_NUM];
            float bmpHeight[] = new float[BMP_NUM];

            for (int b = 0; b < BMP_NUM; b++) {
                bmpWidth[b] = bmp[b].getWidth();
                bmpHeight[b] = bmp[b].getWidth();
            }


            long startTime = SystemClock.uptimeMillis();
            long lastRun = startTime;
            long elapsedTime = 0;
            long thisTime;
            long deltaTime;
            int nNull = 0;
            double div = 1;

            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);

            while (_run) {
                //--------------
                //Timing calcul
                //--------------
                thisTime = SystemClock.uptimeMillis();
                deltaTime = thisTime - lastRun;
                lastRun = thisTime;
                elapsedTime += deltaTime;
                //--------------

                if (!holder.getSurface().isValid()) {
                    Log.d("INFO", "(thisTime - convtime)");
                    continue;

                }
                Canvas canvas = holder.lockCanvas();


                if (converge && (thisTime - convtime) > 0) {
                    div += deltaTime / 5;
                    nNull = 0;
                    for (int b = 0; b < 3; b++) {
                        bmpWidth[b] = (bmpWidth[b] * 0.9f);
                        bmpHeight[b] = (bmpHeight[b] * 0.9f);
                        if (bmpWidth[b] > 3 && bmpHeight[b] > 3) {

                        } else {
                            bmp[b] = null;
                            nNull++;
                        }
                    }

                } else {
                    if (div < 120)
                        div += deltaTime / 20;

                }

                if (div > 200) div = 200;
                double rayon = (200 - div) * dp;

                canvas.drawARGB(255, 0, 0, 0);

                for (int b = 0; b < 3; b++) {
                    if (bmp[b] == null) continue;
                    float left = (float) (centerX + (rayon * Math.cos(((120d * b) + (elapsedTime / 4d)) / 57.295779513d)) - (bmpWidth[b] / 2));
                    float top = (float) (centerY + (rayon * Math.sin(((120d * b) + (elapsedTime / 4d)) / 57.295779513d)) - (bmpHeight[b] / 2));
                    RectF dst = new RectF(left, top, left + bmpWidth[b], top + bmpHeight[b]);

                    canvas.drawBitmap(bmp[b], null, dst, paint);

                }

                int intensity = (int) (210d - Math.abs(100d * Math.sin(((double) elapsedTime / 10d) / 57.295779513d)));
                paint.setColor(Color.rgb(intensity, intensity, intensity));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(16 * dp);

                canvas.drawText(title, centerX, v.getHeight() - (16 + 4 + 14 + 4) * dp, paint);
                paint.setTextSize(14 * dp);
                paint.setColor(Color.rgb(230, 230, 230));
                canvas.drawText(status, centerX, v.getHeight() - (14 + 4) * dp, paint);

                if (nNull == BMP_NUM) {
                    _run = false;
                    callback.run();
                }

                holder.unlockCanvasAndPost(canvas);
                x++;
                y++;

                //--------------
                //Timing pause
                //--------------
                deltaTime = SystemClock.uptimeMillis() - lastRun;

                //--------------

                long timeSleep = 33 - deltaTime;

                try {
                    if (timeSleep > 0)
                        sleep(timeSleep + 0);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

