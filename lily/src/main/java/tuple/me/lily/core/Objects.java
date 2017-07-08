package tuple.me.lily.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by gokul-4192
 */
public class Objects {
    @Nullable
    public static <T> T ofNullable(@Nullable T val, @NonNull T defaultVal) {
        checkNotNull(defaultVal);
        return val != null ? val : defaultVal;
    }

    @Nullable
    public static <T> T checkNotNull(@Nullable T val) {
        if (val == null) {
            throw new IllegalStateException("Object value is null");
        }
        return val;
    }

    @Nullable
    public static <T> T checkNotNull(@Nullable T val, @NonNull String message) {
        if (val == null) {
            throw new IllegalStateException(message);
        }
        return val;
    }

    public static boolean assertVal(boolean cond) {
        if (!cond) {
            throw new IllegalStateException("Assertion filed");
        }
        return true;
    }

    public static boolean assertVal(boolean cond, String message) {
        if (!cond) {
            throw new IllegalStateException(message);
        }
        return true;
    }
}
