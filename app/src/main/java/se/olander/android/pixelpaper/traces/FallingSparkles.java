package se.olander.android.pixelpaper.traces;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Arrays;

public class FallingSparkles extends Trace {
    public static int SPARK_POINTS = 100;
    private static final double SPARK_VELOCITY = 0.05;
    private static final double SPARK_GRAVITY = 0.0006;

    private final float[] sparkPoints;
    private final double[] sparkVelocities;

    public FallingSparkles(float x, float y, long timestamp, Paint paint) {
        super(x, y, timestamp, paint);
        this.sparkPoints = new float[SPARK_POINTS * 2];
        this.sparkVelocities = new double[sparkPoints.length];
        for (int i = 0; i < sparkPoints.length / 2; ++i) {
            double angle = Math.random() * 2 * Math.PI;
            double vel = Math.random() * SPARK_VELOCITY;
            sparkVelocities[2 * i] = vel * Math.cos(angle);
            sparkVelocities[2 * i + 1] = vel * Math.sin(angle);
        }
    }

    public void draw(Canvas canvas, long timestamp) {
        if (isExpired(timestamp)) {
            return;
        }

        double dt = timestamp - getTimestamp();
        double rdt = (DURATION - dt) / DURATION;
        for (int i = 0; i < sparkPoints.length / 2; ++i) {
            double vx = sparkVelocities[2 * i];
            double vy = sparkVelocities[2 * i + 1];
            double px = getX() + vx * dt;
            double py = getY() + vy * dt + SPARK_GRAVITY * dt * dt / 2;
            sparkPoints[2 * i] = (float) px;
            sparkPoints[2 * i + 1] = (float) py;
        }
        getPaint().setAlpha((int) (255 * rdt));
        canvas.drawPoints(sparkPoints, getPaint());
    }

    @Override
    public String toString() {
        return "FallingSparkles{" +
                "sparkPoints=" + Arrays.toString(sparkPoints) +
                ", sparkVelocities=" + Arrays.toString(sparkVelocities) +
                "} " + super.toString();
    }
}
