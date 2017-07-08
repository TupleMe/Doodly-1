package tuple.me.lily.core;

import android.support.annotation.Nullable;

/**
 * Created by gokul-4192 on 0024 24-Dec-16.
 */
public class Precondition {
    public static boolean or(@Nullable Object val1, @Nullable Object val2){
        return val1!=null || val2!=null;
    }
}
