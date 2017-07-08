package tuple.me.lily.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public class IntentUtil {

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void transitionActivity(@NonNull Activity activity, Class toActivity) {
        transitionActivity(activity, toActivity, false);
    }


    public static void transitionActivity(@NonNull Activity activity, Class toActivity, boolean withFinish) {
        Intent in = new Intent(activity, toActivity);
        in.setAction(Intent.ACTION_MAIN);
        if (withFinish) {
            final int enter_anim = android.R.anim.fade_in;
            final int exit_anim = android.R.anim.fade_out;
            activity.overridePendingTransition(enter_anim, exit_anim);
            activity.finish();
            activity.overridePendingTransition(enter_anim, exit_anim);
        }
        activity.startActivity(in);
    }

}
