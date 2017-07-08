package tuple.me.lily.util;

/**
 * Created by gokul-4192 on 0017 17-Dec-16.
 */
public class RangeUtils {
    public static float getValueInRange(float min, float max, float value) {
        float minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static float safeDivide(long val, long div) {
        return (div == 0) ? 0 : (float) val / (float) div;
    }

    public static String getPercent(long fragment, long total) {
        return (int) (safeDivide(fragment, total) * 100) + "%";
    }
}
