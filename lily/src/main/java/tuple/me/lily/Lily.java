package tuple.me.lily;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.Prefs;
import tuple.me.lily.util.Tracker;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Lily {

    private static Lily instance;
    public static final String LILY = "Lily";

    public Lily(@NonNull Context context, @Nullable initCallback callback) {
        Contexter.init(context);
        Prefs.init(context);
        CommonUtil.init(context);
        if (callback == null || callback.needThemeEngine()) {
            ThemeEngine.init();
        }
        if (callback == null || callback.needTracker()) {
            Tracker.init();
        }
    }

    public synchronized static void init(@NonNull Context context) {
        if (instance == null) {
            instance = new Lily(context, null);
        }
    }

    public synchronized static void init(@NonNull Context context, initCallback callback) {
        if (instance == null) {
            instance = new Lily(context, callback);
        }
    }

    public static Lily getInstance() {
        return instance;
    }

    public interface initCallback {
        boolean needThemeEngine();

        boolean needTracker();
    }
}
