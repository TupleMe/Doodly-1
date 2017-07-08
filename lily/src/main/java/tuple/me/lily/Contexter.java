package tuple.me.lily;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

/**
 * Created by gokul.
 */
@SuppressWarnings({"SpellCheckingInspection", "UnusedDeclaration"})
public class Contexter {
    static Context mContext;
    public static Resources resources;

    public static void init(@NonNull Context context) {
        mContext = context;
        resources = context.getResources();
    }

    public static Context getAppContext() {
        return mContext;
    }

    public static int dpToPixels(int dp) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pixelsToDp(int pixcels) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(pixcels * ((float) DisplayMetrics.DENSITY_DEFAULT / metrics.densityDpi));
    }

    public static int getColor(@ColorRes int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }

    public static boolean getBoolean(@BoolRes int boolId) {
        return resources.getBoolean(boolId);
    }

    @NonNull
    public static String getString(@StringRes int stringId) {
        return resources.getString(stringId);
    }

    public static int getInt(@IntegerRes int intId) {
        return resources.getInteger(intId);
    }

    public static Drawable getDrawable(@DrawableRes int icDrawable) {
        return ContextCompat.getDrawable(mContext, icDrawable);
    }

    public static ColorDrawable getColorDrawable(@ColorRes int color) {
        return new ColorDrawable(getColor(color));
    }

    public static AssetManager getAssets() {
        return mContext.getAssets();
    }
}
