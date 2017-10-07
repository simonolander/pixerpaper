package se.olander.android.pixelpaper.traces;

import android.graphics.Canvas;
import android.graphics.Paint;

public class ExpandingCircle extends Trace {
    static float RADIUS = 100;

    public ExpandingCircle(float x, float y, long timestamp, Paint paint) {
        super(x, y, timestamp, paint);
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(5);
    }

    public void draw(Canvas canvas, long timestamp) {
        if (isExpired(timestamp)) {
            return;
        }

        getPaint().setAlpha((int) (fTimeRemaining(timestamp) * 255));
        float radius = fTimePassed(timestamp) * RADIUS;
        canvas.drawCircle(getX(), getY(), radius, getPaint());
    }
}
