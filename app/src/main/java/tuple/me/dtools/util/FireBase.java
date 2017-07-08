package tuple.me.dtools.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by gokul-4192 on 0023 23-Apr-17.
 */
public class FireBase {
    public static FirebaseAnalytics instance;
    public static Context context;

    public static void init(Context context) {
        instance = FirebaseAnalytics.getInstance(context);
        FireBase.context = context;
    }

    public static void logDrawerClick(int id) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, context.getString(id));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, context.getString(id));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "feature");
        instance.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public static void logOpen() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "app_oncreate");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App open");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "feature");
        instance.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
