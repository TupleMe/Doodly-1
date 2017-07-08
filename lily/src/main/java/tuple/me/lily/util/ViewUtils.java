package tuple.me.lily.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import tuple.me.lily.Contexter;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.core.Optional;

/**
 * Created by gokul.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class ViewUtils {

    public static int getViewYPos(@NonNull View view) {
        int[] location = new int[2];
        location[0] = 0;
        location[1] = (int) view.getY();
        ((View) view.getParent()).getLocationInWindow(location);
        return location[1];
    }

    public static int getViewXPos(@NonNull View view) {
        int[] location = new int[2];
        location[0] = 0;
        location[1] = (int) view.getY();
        ((View) view.getParent()).getLocationInWindow(location);
        return location[0];
    }

    @NonNull
    public static int[] getViewPos(@NonNull View view) {
        int[] location = new int[2];
        location[0] = 0;
        location[1] = (int) view.getY();
        ((View) view.getParent()).getLocationInWindow(location);
        return location;
    }

    public static void setDrawableLazily(@NonNull ImageView view, @DrawableRes int imageId) {
        Integer id = (Integer) view.getTag();
        id = id == null ? 0 : id;
        if (imageId == id) {
            return;
        }
        view.setImageResource(imageId);
        view.setTag(imageId);
    }


    public static void setColorFilter(@NonNull ImageView imageView, @DrawableRes int drawable) {
        Drawable background = Contexter.getDrawable(drawable);
        Drawable wrappedDrawable = DrawableCompat.wrap(background);
        DrawableCompat.setTint(wrappedDrawable, ThemeEngine.iconColor);
        imageView.setImageDrawable(wrappedDrawable);
        imageView.setVisibility(View.VISIBLE);
    }

    public static void setLinearLayoutManager(@NonNull RecyclerView recyclerView, @NonNull Context context) {
        int scrollPosition = 0;
        Optional<RecyclerView.LayoutManager> layoutManager = Optional.of(recyclerView.getLayoutManager());
        if (layoutManager.isPresent() && layoutManager.get() instanceof LinearLayoutManager) {
            scrollPosition = ((LinearLayoutManager) layoutManager.get()).findFirstCompletelyVisibleItemPosition();
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        recyclerView.scrollToPosition(scrollPosition);
    }

    public static void setViewBackground(@NonNull View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        }
    }

    public static boolean isRTL(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else {
            return false;
        }
    }

    @ColorInt
    public static int resolveColor(@NonNull Context context, @AttrRes int color) {
        TypedArray a = context.obtainStyledAttributes(new int[]{color});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    public static int convertDpToPx(@NonNull Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public static int convertPxToDp(@NonNull Context context, int px) {
        if (px <= 0) {
            return 0;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getWindowWidth(@NonNull Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.widthPixels;
    }

    public static int getWindowHeight(@NonNull Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.heightPixels;
    }


    public static float getWindowWidthInDp(@NonNull Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return (metrics.widthPixels / metrics.density);
    }

    public static float getWindowHeightInDp(@NonNull Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return (metrics.heightPixels / metrics.density);
    }

    public static DisplayMetrics getDisplayMetrics(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    public static Activity getActivityForFragment(Fragment fragment) {
        if (fragment.getActivity() != null) {
            return fragment.getActivity();
        }
        if (fragment.getParentFragment() != null) {
            return getActivityForFragment(fragment.getParentFragment());
        }
        return null;
    }

    public static Bitmap createSquareBitmap(Bitmap bitmap) {
        int x = 0;
        int y = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) {
            x = (width - height) / 2;
            //noinspection SuspiciousNameCombination
            width = height;
        } else {
            y = (height - width) / 2;
            //noinspection SuspiciousNameCombination
            height = width;
        }
        return Bitmap.createBitmap(bitmap, x, y, width, height, null, true);
    }

    public static void setColorFilterForBG(ImageView imageView, int color) {
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColor(Contexter.getColor(color));
    }

    @SuppressWarnings({ "unchecked"})
    @CheckResult
    public static <T extends View> T findById(@NonNull View view, @IdRes int id) {
        return (T) view.findViewById(id);
    }

    @SuppressWarnings({ "unchecked"})
    @CheckResult
    public static <T extends View> T findById(@NonNull Activity activity, @IdRes int id) {
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings({ "unchecked"})
    @CheckResult
    public static <T extends View> T findById(@NonNull Dialog dialog, @IdRes int id) {
        return (T) dialog.findViewById(id);
    }
}

