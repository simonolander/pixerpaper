package se.olander.android.pixelpaper;

import android.content.Context;
import android.content.res.TypedArray;
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
    private int value;

    private int min = 0;
    private int step = 1;

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

        if (attrs != null) {
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                switch (attrs.getAttributeName(i)) {
                    case "min":
                        min = attrs.getAttributeIntValue(i, 0);
                        break;
                    case "step":
                        step = attrs.getAttributeIntValue(i, 1);
                        break;
                }
            }

            if (step <= 0) {
                Log.e(TAG, "SeekBarPreference illegal step: " + step);
                step = 1;
            }
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.summary = view.findViewById(android.R.id.summary);
        this.seekbar = view.findViewById(R.id.seekbar);
        this.seekbar.setProgress(toProgress(this.value));
        this.seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int value;
        if (restorePersistedValue) {
            value = getPersistedInt(this.value);
        }
        else if (defaultValue instanceof Integer) {
            value = (Integer) defaultValue;
        }
        else {
            value = this.value;
        }

        setValue(value);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setValue(fromProgress(progress));
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
        return "" + value;
    }

    private void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (this.value != value) {
            this.value = value;
            if (summary != null) {
                summary.setText(getSummary());
            }
        }
    }

    private int toProgress(int value) {
        return (value - min) / step;
    }

    private int fromProgress(int progress) {
        return progress * step + min;
    }
}
