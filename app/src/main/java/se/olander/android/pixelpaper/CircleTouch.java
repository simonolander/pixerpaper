package se.olander.android.pixelpaper;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by sios on 2017-09-30.
 */

public class CircleTouch extends Touch {

    CircleTouch(float x, float y, long timestamp, Paint paint) {
        super(x, y, timestamp, paint);
    }

    void draw(Canvas canvas, long timestamp) {
        if (isExpired(timestamp)) {
            return;
        }

        long remainingTime = DURATION - (timestamp - getTimestamp());
        float radius = INITIAL_RADIUS * remainingTime / DURATION;
        canvas.drawCircle(getX(), getY(), radius, getPaint());
    }
}
