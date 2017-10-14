package se.olander.android.pixelpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;


public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "SeekBarPreference";

    private SeekBar seekbar;
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

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Log.d(TAG, "onGetDefaultValue a: " + a);
        Log.d(TAG, "onGetDefaultValue index: " + index);
        return a.getInt(index, 0);
    }

    public void setValue(int progress) {
        if (shouldPersist()) {
            persistInt(progress);
        }

        if (this.progress != progress) {
            this.progress = progress;
            notifyChanged();
        }
    }
}
