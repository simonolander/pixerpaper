package se.olander.android.pixelpaper;

import android.graphics.Color;

public final class C {

    public static final String BACKGROUND_FILE_KEY = "background_file";
    public static final String BACKGROUND_FILE_DEFAULT = "sunset.gif";

    public static final String TRACE_KEY = "trace";
    public static final boolean TRACE_DEFAULT = false;

    public static final String TRACE_DURATION_KEY = "trace_duration";
    public static final int TRACE_DURATION_DEFAULT = 1000;

    public static final String TRACE_TYPE_KEY = "trace_type";
    public static final String TRACE_TYPE_POND = "pond";
    public static final String TRACE_TYPE_SPARK = "spark";
    public static final String TRACE_TYPE_DEFAULT = TRACE_TYPE_POND;

    public static final String TRACE_COLOR_KEY = "spark_color";
    public static final int TRACE_COLOR_DEFAULT = Color.WHITE;

    public static final String SPARK_POINTS_KEY = "spark_points";
    public static final int SPARK_POINTS_DEFAULT = 100;

    public static final String SPARK_VELOCITY_KEY = "spark_velocity";
    public static final double SPARK_VELOCITY_DEFAULT = 0.05;

    public static final String SPARK_GRAVITY_KEY = "spark_gravity";
    public static final double SPARK_GRAVITY_DEFAULT = 0.0006;

    public static final String POND_RADIUS_KEY = "pond_radius";
    public static final double POND_RADIUS_DEFAULT = 0.0006;

    private C() {}
}
