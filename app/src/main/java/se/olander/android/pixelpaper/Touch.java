package se.olander.android.pixelpaper;

import android.graphics.Canvas;
import android.graphics.Paint;

abstract class Touch {

    static long DURATION = 1000;
    static float INITIAL_RADIUS = 10;

    private final float x;
    private final float y;
    private final long timestamp;
    private final Paint paint;

    Touch(float x, float y, long timestamp, Paint paint) {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
        this.paint = paint;
    }

    boolean isExpired(long timestamp) {
        return timestamp - getTimestamp() > DURATION;
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    long getTimestamp() {
        return timestamp;
    }

    Paint getPaint() {
        return paint;
    }

    float fTimePassed(long timestamp) {
        return (float) (timestamp - getTimestamp()) / DURATION;
    }

    float fTimeRemaining(long timestamp) {
        return 1 - fTimePassed(timestamp);
    }

    abstract void draw(Canvas canvas, long timestamp);

    @Override
    public String toString() {
        return "Touch{" +
                "x=" + x +
                ", y=" + y +
                ", timestamp=" + timestamp +
                ", paint=" + paint +
                '}';
    }
}
