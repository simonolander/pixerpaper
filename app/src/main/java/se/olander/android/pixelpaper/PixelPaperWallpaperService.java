package se.olander.android.pixelpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Objects;

public class PixelPaperWallpaperService extends WallpaperService {
    private static final String DEFAULT_FILE_NAME = "sunset.gif";
    private static final String BACKGROUND_FILE_KEY = "background_file";

    @Override
    public Engine onCreateEngine() {
        return new MovieWallpaperEngine();
    }

    private class MovieWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final static String TAG = "MovieWallpaperEngine";

        private final Handler handler;

        private Movie movie;

        MovieWallpaperEngine() {
            this.handler = new Handler();
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

        private void draw() {
            if (isVisible()) {
                Canvas canvas = getSurfaceHolder().lockCanvas();
                canvas.save();
                float sx = (float) getSurfaceHolder().getSurfaceFrame().width() / movie.width();
                float sy = (float) getSurfaceHolder().getSurfaceFrame().height() / movie.height();
                canvas.scale(sx, sy);
                movie.draw(canvas, 0, 0);
                canvas.restore();
                getSurfaceHolder().unlockCanvasAndPost(canvas);

                movie.setTime((int) (System.currentTimeMillis() % movie.duration()));

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
        }
    }
}
