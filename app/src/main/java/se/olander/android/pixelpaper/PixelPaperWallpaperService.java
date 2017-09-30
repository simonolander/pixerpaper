package se.olander.android.pixelpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PixelPaperWallpaperService extends WallpaperService {
    private static final String DEFAULT_FILE_NAME = "sunset.gif";
    private static final String BACKGROUND_FILE_KEY = "background_file";
    private static final String TRACE_KEY = "trace";
    private static final String TRACE_DURATION = "trace_duration";
    private static final String SPARK_COLOR_KEY = "spark_color";
    private static final String SPARK_POINTS = "spark_points";
    private static final String SPARK_VELOCITY = "spark_velocity";
    private static final String SPARK_GRAVITY = "spark_gravity";

    @Override
    public Engine onCreateEngine() {
        return new MovieWallpaperEngine();
    }

    private class MovieWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final static String TAG = "MovieWallpaperEngine";

        private final Handler handler;

        private final List<Touch> touches;

        private Movie movie;

        private boolean trace;

        private Paint paint;
        {
            paint = new Paint();
            paint.setColor(Color.RED);
        }

        MovieWallpaperEngine() {
            this.handler = new Handler();
            this.touches = new LinkedList<>();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.registerOnSharedPreferenceChangeListener(this);
            setMovie(prefs.getString(BACKGROUND_FILE_KEY, DEFAULT_FILE_NAME));
        }

        private void setMovie(String filename) {
            Log.d(TAG, "Setting new movie: " + filename);
            try {
                this.movie = Movie.decodeStream(getResources().getAssets().open(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        private Runnable drawer = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private void drawMovie(Canvas canvas) {
            canvas.save();
            float sx = (float) getSurfaceHolder().getSurfaceFrame().width() / movie.width();
            float sy = (float) getSurfaceHolder().getSurfaceFrame().height() / movie.height();
            canvas.scale(sx, sy);
            movie.setTime((int) (System.currentTimeMillis() % movie.duration()));
            movie.draw(canvas, 0, 0);
            canvas.restore();
        }

        private void drawTrace(Canvas canvas) {
            synchronized (touches) {
                long timestamp = System.currentTimeMillis();
                Iterator<Touch> it = touches.iterator();
                while (it.hasNext()) {
                    Touch touch = it.next();
                    if (touch.isExpired(timestamp)) {
                        it.remove();
                    }
                    else {
                        touch.draw(canvas, timestamp);
                    }
                }
            }
        }

        private void draw() {
            if (isVisible()) {
                Canvas canvas = getSurfaceHolder().lockCanvas();

                canvas.drawColor(Color.BLACK);
                drawMovie(canvas);

                if (trace) {
                    drawTrace(canvas);
                }

                getSurfaceHolder().unlockCanvasAndPost(canvas);
                handler.removeCallbacks(drawer);
                handler.postDelayed(drawer, 20);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                handler.post(drawer);
            }
            else {
                handler.removeCallbacks(drawer);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawer);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.d(TAG, "Preference changed: " + key);
            if (Objects.equals(key, BACKGROUND_FILE_KEY)) {
                setMovie(prefs.getString(key, DEFAULT_FILE_NAME));
            }
            else if (Objects.equals(key, TRACE_KEY)) {
                trace = prefs.getBoolean(key, false);
            }
            else if (Objects.equals(key, TRACE_DURATION)) {
                String value = prefs.getString(key, "1000");
                Touch.DURATION = NumberUtils.toInt(value, 1000);
            }
            else if (Objects.equals(key, SPARK_COLOR_KEY)) {
                int value = prefs.getInt(key, Color.WHITE);
                paint.setColor(value);
            }
            else if (Objects.equals(key, SPARK_POINTS)) {
                String value = prefs.getString(key, "100");
                SparkTouch.SPARK_POINTS = NumberUtils.toInt(value, 100);
            }
            else if (Objects.equals(key, SPARK_VELOCITY)) {
                String value = prefs.getString(key, "100");
                SparkTouch.SPARK_VELOCITY = NumberUtils.toDouble(value, 0.05);
            }
            else if (Objects.equals(key, SPARK_GRAVITY)) {
                String value = prefs.getString(key, "100");
                SparkTouch.SPARK_GRAVITY = NumberUtils.toDouble(value, 0.0006);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Touch touch = new SparkTouch(event.getX(), event.getY(), System.currentTimeMillis(), new Paint(paint));
                    synchronized (touches) {
                        touches.add(touch);
                    }
                    break;
            }
        }
    }
}
