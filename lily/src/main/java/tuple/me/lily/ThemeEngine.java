package tuple.me.lily;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import tuple.me.lily.util.Prefs;


/**
 * Created by gokul.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class ThemeEngine {
    public static int theme;
    public static int primary;
    public static int primaryDark;
    public static int primaryLight;
    public static int iconColor;
    public static int primaryBackGround;
    public static final int LIGHT = 0;
    public static final int DARK = 1;

    public static Prefs prefs;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DARK, LIGHT})
    public @interface MainTheme {
    }


    public static final String[] colors = new String[]{
            "#f44336", "#e91e63", "#9c27b0", "#673ab7",
            "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4",
            "#009688", "#4caf50", "#8bc34a", "#ffc107",
            "#ff9800", "#ff5722", "#795548", "#212121",
            "#607d8b", "#004d40"
    };

    public static final String[] iconColors = new String[]{
            "#f44336", "#e91e63", "#9c27b0", "#673ab7",
            "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4",
            "#009688", "#4caf50", "#8bc34a", "#ffc107",
            "#ff9800", "#ff5722", "#795548", "#212121",
            "#607d8b", "#004d40", "#eeeeee", "#424242"
    };

    public static void init() {
        prefs = new Prefs(Contexter.getAppContext(), Lily.LILY);
        theme = prefs.getInt("theme", LIGHT);
        primary = Color.parseColor(colors[prefs.getInt("accent_color", 0)]);
        primaryDark = darker(primary, 0.85f);
        primaryLight = lighter(primary, 1.15f);
        iconColor = Color.parseColor(iconColors[prefs.getInt("icon_color", iconColors.length - 1)]);
    }

    public static void setTheme(@MainTheme int mTheme) {
        if (theme != mTheme) {
            theme = mTheme;
            prefs.set("theme", mTheme);
            recycle();
        }
    }

    public static void setPrimary(int position) {
        primary = Color.parseColor(colors[position]);
        primaryDark = darker(primary, 0.8f);
        primaryLight = lighter(primary, 1.2f);
        prefs.set("accent_color", position);
        recycle();
    }

    public static void recycle() {

    }


    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static int lighter(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.min((int) (r * factor), 255),
                Math.min((int) (g * factor), 255),
                Math.min((int) (b * factor), 255));
    }

    public static String hexString(int color) {
        String colorStr;
        if (Color.alpha(color) == 255) {
            colorStr = String.format("#%06X", color & 0xFFFFFF);

        } else {
            colorStr = String.format("#%08X", color);
        }
        return colorStr.toLowerCase();
    }

    public static void injectTheme(@NonNull Activity activity) {
        if (theme == 0) {
            activity.getWindow().setBackgroundDrawableResource(android.R.color.white);
        } else {
            activity.getWindow().setBackgroundDrawableResource(R.color.holo_dark_background);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ThemeEngine.primaryDark);
        }

        if (Build.VERSION.SDK_INT >= 21) {

            String accentSkin = ThemeEngine.hexString(ThemeEngine.primary);
            switch (accentSkin) {
                case "#f44336":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_red);
                    else
                        activity.setTheme(R.style.pref_accent_dark_red);
                    break;
                case "#e91e63":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_pink);
                    else
                        activity.setTheme(R.style.pref_accent_dark_pink);
                    break;

                case "#9c27b0":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_purple);
                    else
                        activity.setTheme(R.style.pref_accent_dark_purple);
                    break;

                case "#673ab7":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_deep_purple);
                    else
                        activity.setTheme(R.style.pref_accent_dark_deep_purple);
                    break;

                case "#3f51b5":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_indigo);
                    else
                        activity.setTheme(R.style.pref_accent_dark_indigo);
                    break;

                case "#2196f3":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_blue);
                    else
                        activity.setTheme(R.style.pref_accent_dark_blue);
                    break;

                case "#03a9f4":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_light_blue);
                    else
                        activity.setTheme(R.style.pref_accent_dark_light_blue);
                    break;

                case "#00bcd4":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_cyan);
                    else
                        activity.setTheme(R.style.pref_accent_dark_cyan);
                    break;

                case "#009688":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_teal);
                    else
                        activity.setTheme(R.style.pref_accent_dark_teal);
                    break;

                case "#4caf50":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_green);
                    else
                        activity.setTheme(R.style.pref_accent_dark_green);
                    break;

                case "#8bc34a":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_light_green);
                    else
                        activity.setTheme(R.style.pref_accent_dark_light_green);
                    break;

                case "#ffc107":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_amber);
                    else
                        activity.setTheme(R.style.pref_accent_dark_amber);
                    break;

                case "#ff9800":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_orange);
                    else
                        activity.setTheme(R.style.pref_accent_dark_orange);
                    break;

                case "#ff5722":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_deep_orange);
                    else
                        activity.setTheme(R.style.pref_accent_dark_deep_orange);
                    break;

                case "#795548":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_brown);
                    else
                        activity.setTheme(R.style.pref_accent_dark_brown);
                    break;

                case "#212121":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_black);
                    else
                        activity.setTheme(R.style.pref_accent_dark_black);
                    break;

                case "#607d8b":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_blue_grey);
                    else
                        activity.setTheme(R.style.pref_accent_dark_blue_grey);
                    break;

                case "#004d40":
                    if (theme == 0)
                        activity.setTheme(R.style.pref_accent_light_super_su);
                    else
                        activity.setTheme(R.style.pref_accent_dark_super_su);
                    break;
            }
        } else {
            if (theme == 1) {
                activity.setTheme(R.style.appCompatDark);
            } else {
                activity.setTheme(R.style.appCompatLight);
            }
        }
    }

    public static boolean isDark() {
        return ThemeEngine.theme == ThemeEngine.DARK;
    }

    public static void setTextColor(@NonNull TextView textView, @ColorRes int lightColor, @ColorRes int darkColor){
        if(isDark()){
            textView.setTextColor(Contexter.getColor(darkColor));
        }else {
            textView.setTextColor(Contexter.getColor(lightColor));
        }
    }
}
