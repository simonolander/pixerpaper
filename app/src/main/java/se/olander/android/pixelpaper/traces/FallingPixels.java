package se.olander.android.pixelpaper.traces;

import android.graphics.Canvas;
import android.graphics.Paint;

public class FallingPixels extends Trace {
    private static final int POINTS = 10;
    private static final float VELOCITY = 0.05f;
    private static final float GRAVITY = 0.0006f;
    private static final float SIZE = 35;

    private final float[] velocities;
    private final float[] sizes;

    public FallingPixels(float x, float y, long timestamp, Paint paint) {
        super(x, y, timestamp, paint);
        this.sizes = new float[POINTS];
        this.velocities = new float[sizes.length * 2];
        for (int i = 0; i < sizes.length; ++i) {
            sizes[i] = (float) (Math.random() * SIZE);

            double angle = Math.random() * 2 * Math.PI;
            double vel = Math.random() * VELOCITY;
            velocities[2 * i] = (float) (vel * Math.cos(angle));
            velocities[2 * i + 1] = (float) (vel * Math.sin(angle));
        }
    }

    public void draw(Canvas canvas, long timestamp) {
        if (isExpired(timestamp)) {
            return;
        }

        float dt = timestamp - getTimestamp();
        float rdt = (DURATION - dt) / DURATION;
        getPaint().setAlpha((int) (255 * rdt));
        for (int i = 0; i < sizes.length / 2; ++i) {
            float vx = velocities[2 * i];
            float vy = velocities[2 * i + 1];
            float cx = getX() + vx * dt;
            float cy = getY() + vy * dt + GRAVITY * dt * dt / 2;
            float size = sizes[i] * rdt;
            float left = cx - size / 2;
            float top = cy - size / 2;
            float right = cx + size / 2;
            float bottom = cy + size / 2;
            canvas.drawRect(left, top, right, bottom, getPaint());
        }
    }
}
