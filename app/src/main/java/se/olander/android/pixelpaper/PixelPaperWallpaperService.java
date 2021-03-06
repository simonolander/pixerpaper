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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import se.olander.android.pixelpaper.traces.ExpandingCircle;
import se.olander.android.pixelpaper.traces.FallingPixels;
import se.olander.android.pixelpaper.traces.FallingSparkles;
import se.olander.android.pixelpaper.traces.Trace;

import static se.olander.android.pixelpaper.C.BACKGROUND_FILE_DEFAULT;
import static se.olander.android.pixelpaper.C.BACKGROUND_FILE_KEY;
import static se.olander.android.pixelpaper.C.BACKGROUND_FILE_RANDOMIZE_DEFAULT;
import static se.olander.android.pixelpaper.C.BACKGROUND_FILE_RANDOMIZE_KEY;
import static se.olander.android.pixelpaper.C.SPARK_GRAVITY_KEY;
import static se.olander.android.pixelpaper.C.SPARK_POINTS_DEFAULT;
import static se.olander.android.pixelpaper.C.SPARK_POINTS_KEY;
import static se.olander.android.pixelpaper.C.SPARK_VELOCITY_KEY;
import static se.olander.android.pixelpaper.C.TRACE_COLOR_DEFAULT;
import static se.olander.android.pixelpaper.C.TRACE_COLOR_KEY;
import static se.olander.android.pixelpaper.C.TRACE_DEFAULT;
import static se.olander.android.pixelpaper.C.TRACE_DURATION_DEFAULT;
import static se.olander.android.pixelpaper.C.TRACE_DURATION_KEY;
import static se.olander.android.pixelpaper.C.TRACE_KEY;
import static se.olander.android.pixelpaper.C.TRACE_TYPE_DEFAULT;
import static se.olander.android.pixelpaper.C.TRACE_TYPE_KEY;
import static se.olander.android.pixelpaper.C.TRACE_TYPE_PIXELS;
import static se.olander.android.pixelpaper.C.TRACE_TYPE_POND;
import static se.olander.android.pixelpaper.C.TRACE_TYPE_SPARK;

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
                InputStream in = getResources().getAssets().open(filename);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[16384];

                int nRead;
                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                byte[] movieBytes = buffer.toByteArray();
                if (movieBytes.length == 0) {
                    throw new IllegalStateException("Movie bytes is empty");
                }

                Movie movie = Movie.decodeByteArray(movieBytes, 0, movieBytes.length);
                if (movie == null) {
                    throw new IllegalStateException("Movie is null, number of movie bytes: " + movieBytes.length);
                }
                if (movie.width() <= 0) {
                    throw new IllegalStateException("movie.width() <= 0, number of movie bytes: " + movieBytes.length);
                }
                if (movie.height() <= 0) {
                    throw new IllegalStateException("movie.height() <= 0, number of movie bytes: " + movieBytes.length);
                }
                if (movie.duration() <= 0) {
                    throw new IllegalStateException("movie.duration() <= 0, number of movie bytes: " + movieBytes.length);
                }
                this.movie = movie;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        private final Runnable drawer = new Runnable() {
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
                if (canvas == null) {
                    Log.d(TAG, "draw: canvas is null, aborting");
                    return;
                }

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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (prefs.getBoolean(BACKGROUND_FILE_RANDOMIZE_KEY, BACKGROUND_FILE_RANDOMIZE_DEFAULT)) {
                    String[] filenames = getResources().getStringArray(R.array.background_values);
                    String filename = filenames[new Random().nextInt(filenames.length)];
                    prefs.edit()
                            .putString(BACKGROUND_FILE_KEY, filename)
                            .apply();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawer);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.d(TAG, "Preference changed: [" + key + ", " + prefs.getAll().get(key) + "]");
            if (BACKGROUND_FILE_KEY.equals(key)) {
                setMovie(prefs.getString(key, BACKGROUND_FILE_DEFAULT));
            }
            else if (TRACE_KEY.equals(key)) {
                trace = prefs.getBoolean(key, TRACE_DEFAULT);
            }
            else if (TRACE_DURATION_KEY.equals(key)) {
                Trace.DURATION = prefs.getInt(key, TRACE_DURATION_DEFAULT);
            }
            else if (TRACE_COLOR_KEY.equals(key)) {
                int value = prefs.getInt(key, TRACE_COLOR_DEFAULT);
                paint.setColor(value);
            }
            else if (SPARK_POINTS_KEY.equals(key)) {
                FallingSparkles.SPARK_POINTS = prefs.getInt(key, SPARK_POINTS_DEFAULT);
            }
//            else if (SPARK_VELOCITY_KEY.equals(key)) {
//                String value = prefs.getString(key, null);
//                FallingSparkles.SPARK_VELOCITY = NumberUtils.toDouble(value, SPARK_VELOCITY_DEFAULT);
//            }
//            else if (SPARK_GRAVITY_KEY.equals(key)) {
//                String value = prefs.getString(key, null);
//                FallingSparkles.SPARK_GRAVITY = NumberUtils.toDouble(value, SPARK_GRAVITY_DEFAULT);
//            }
            else if (TRACE_TYPE_KEY.equals(key)) {
                traceType = prefs.getString(key, TRACE_TYPE_DEFAULT);
            }
            else {
                Log.d(TAG, "onSharedPreferenceChanged: " + prefs.getAll().get(key));
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
                        case TRACE_TYPE_PIXELS:
                            touch = new FallingPixels(event.getX(), event.getY(), System.currentTimeMillis(), new Paint(paint));
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
