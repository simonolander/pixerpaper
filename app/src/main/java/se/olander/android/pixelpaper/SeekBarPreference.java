package se.olander.android.pixelpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "SeekBarPreference";

    private SeekBar seekbar;
    private TextView summary;
    private int progress;

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.seekbar_preference);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.summary = view.findViewById(android.R.id.summary);
        this.seekbar = view.findViewById(R.id.seekbar);
        this.seekbar.setProgress(this.progress);
        this.seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int value;
        if (restorePersistedValue) {
            value = getPersistedInt(progress);
        }
        else if (defaultValue instanceof Integer) {
            value = (Integer) defaultValue;
        }
        else {
            value = progress;
        }

        setValue(value);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
//        Log.d(TAG, "onStartTrackingTouch: " + seekBar);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        Log.d(TAG, "onStopTrackingTouch: " + seekBar);
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    public CharSequence getSummary() {
        return Integer.toString(progress);
    }

    private void setValue(int progress) {
        if (shouldPersist()) {
            persistInt(progress);
        }

        if (this.progress != progress) {
            this.progress = progress;
            if (summary != null) {
                summary.setText(getSummary());
            }
        }
    }
}
