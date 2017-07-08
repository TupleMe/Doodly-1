package tuple.me.lily.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class CustomBroadcastReceiver extends BroadcastReceiver {
    public boolean isRegistered;


    public Intent register(Context context, IntentFilter filter) {
        isRegistered = true;
        return context.registerReceiver(this, filter);
    }

    public static boolean unregiter(CustomBroadcastReceiver receiver, Context context) {
        return receiver == null || receiver.unregister(context);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);  // edited
            isRegistered = false;
            return true;
        }
        return false;
    }

}
