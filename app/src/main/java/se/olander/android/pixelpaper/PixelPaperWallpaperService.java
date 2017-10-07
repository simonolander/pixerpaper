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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import se.olander.android.pixelpaper.traces.ExpandingCircle;
import se.olander.android.pixelpaper.traces.FallingSparkles;
import se.olander.android.pixelpaper.traces.Trace;

import static se.olander.android.pixelpaper.C.*;

public class PixelPaperWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MovieWallpaperEngine();
    }

    private class MovieWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final static String TAG = "MovieWallpaperEngine";

        private final Handler handler;
        private final List<Trace> touches;
        private final Paint paint;

        private Movie movie;

        private boolean trace;

        private String traceType;

        MovieWallpaperEngine() {
            this.handler = new Handler();
            this.touches = new LinkedList<>();
            this.paint = new Paint();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            onSharedPreferenceChanged(prefs, BACKGROUND_FILE_KEY);
            onSharedPreferenceChanged(prefs, TRACE_KEY);
            onSharedPreferenceChanged(prefs, TRACE_DURATION_KEY);
            onSharedPreferenceChanged(prefs, TRACE_COLOR_KEY);
            onSharedPreferenceChanged(prefs, SPARK_POINTS_KEY);
            onSharedPreferenceChanged(prefs, SPARK_VELOCITY_KEY);
            onSharedPreferenceChanged(prefs, SPARK_GRAVITY_KEY);
            onSharedPreferenceChanged(prefs, TRACE_TYPE_KEY);
            prefs.registerOnSharedPreferenceChangeListener(this);
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
                Iterator<Trace> it = touches.iterator();
                while (it.hasNext()) {
                    Trace touch = it.next();
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
                setMovie(prefs.getString(key, BACKGROUND_FILE_DEFAULT));
            }
            else if (Objects.equals(key, TRACE_KEY)) {
                trace = prefs.getBoolean(key, false);
            }
            else if (Objects.equals(key, TRACE_DURATION_KEY)) {
                String value = prefs.getString(key, null);
                Trace.DURATION = NumberUtils.toInt(value, TRACE_DURATION_DEFAULT);
            }
            else if (Objects.equals(key, TRACE_COLOR_KEY)) {
                int value = prefs.getInt(key, TRACE_COLOR_DEFAULT);
                paint.setColor(value);
            }
            else if (Objects.equals(key, SPARK_POINTS_KEY)) {
                String value = prefs.getString(key, null);
                FallingSparkles.SPARK_POINTS = NumberUtils.toInt(value, SPARK_POINTS_DEFAULT);
            }
            else if (Objects.equals(key, SPARK_VELOCITY_KEY)) {
                String value = prefs.getString(key, null);
                FallingSparkles.SPARK_VELOCITY = NumberUtils.toDouble(value, SPARK_VELOCITY_DEFAULT);
            }
            else if (Objects.equals(key, SPARK_GRAVITY_KEY)) {
                String value = prefs.getString(key, null);
                FallingSparkles.SPARK_GRAVITY = NumberUtils.toDouble(value, SPARK_GRAVITY_DEFAULT);
            }
            else if (Objects.equals(key, POND_RADIUS_KEY)) {
                String value = prefs.getString(key, null);
                FallingSparkles.SPARK_GRAVITY = NumberUtils.toDouble(value, POND_RADIUS_DEFAULT);
            }
            else if (Objects.equals(key, TRACE_TYPE_KEY)) {
                traceType = prefs.getString(key, TRACE_TYPE_DEFAULT);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    final Trace touch;
                    switch (traceType) {
                        case TRACE_TYPE_SPARK:
                            touch = new FallingSparkles(event.getX(), event.getY(), System.currentTimeMillis(), new Paint(paint));
                            break;
                        case TRACE_TYPE_POND:
                        default:
                            touch = new ExpandingCircle(event.getX(), event.getY(), System.currentTimeMillis(), new Paint(paint));
                            break;
                    }
                    synchronized (touches) {
                        touches.add(touch);
                    }
                    break;
            }
        }
    }
}
