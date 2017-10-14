package se.olander.android.pixelpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import static se.olander.android.pixelpaper.C.*;

public class PixelPaperPreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PixelPaperPreferenceFragment())
                .commit();
    }

    public static class PixelPaperPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(this);
            setVisiblePreferences(prefs);
        }

        private void setVisiblePreferences(SharedPreferences prefs) {
//            findPreference(TRACE_CATEGORY_KEY).setEnabled(prefs.getBoolean(TRACE_KEY, TRACE_DEFAULT));
            switch (prefs.getString(TRACE_TYPE_KEY, TRACE_TYPE_DEFAULT)) {
                case TRACE_TYPE_POND:
                    findPreference(SPARK_GRAVITY_KEY).setEnabled(false);
                    findPreference(SPARK_POINTS_KEY).setEnabled(false);
                    findPreference(SPARK_VELOCITY_KEY).setEnabled(false);
                    break;
                case TRACE_TYPE_SPARK:
                    findPreference(SPARK_GRAVITY_KEY).setEnabled(true);
                    findPreference(SPARK_POINTS_KEY).setEnabled(true);
                    findPreference(SPARK_VELOCITY_KEY).setEnabled(true);
                    break;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            setVisiblePreferences(sharedPreferences);
        }
    }
}
