package tuple.me.lily.util;

import android.support.annotation.NonNull;

import java.util.HashSet;

import tuple.me.lily.Contexter;
import tuple.me.lily.Lily;

/**
 * Created by gokul-4192 on 0029 29-Dec-16.
 */
public class Tracker {

    @NonNull
    private static Prefs prefs = new Prefs(Contexter.getAppContext(), Lily.LILY);
    private static boolean isNewApk = false;
    @NonNull
    private static HashSet<Integer> features = new HashSet<>();

    public static void init() {
        if (!prefs.contains("tr_when_installed")) {
            prefs.set("tr_when_installed", System.currentTimeMillis());
        }
        isNewApk = CommonUtil.getInstance().isNewApk();
    }

    public static boolean isNew(@NonNull String feature) {
        return isNewApk && features.add(feature.hashCode());
    }

    public static int trackEvent(@NonNull String event) {
        return prefs.setAndGet("tr_" + event, prefs.getInt("tr_" + event, 1) + 1);
    }

    public static int resetEvent(@NonNull String event) {
        prefs.set("tr_" + event, 0);
        return 0;
    }

    public static int getEvent(@NonNull String event) {
        return prefs.getInt("tr_" + event, 0);
    }
}
