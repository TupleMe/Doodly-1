package tuple.me.lily.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import tuple.me.lily.Contexter;
import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.views.changelog.view.ChangeLogRecyclerView;


/**
 * Created by gokul-4192 on 0024 24-Dec-16.
 */
@SuppressWarnings({"SpellCheckingInspection", "UnusedDeclaration"})
public class DialogUtil {

    public static MaterialDialog showBasicDialog(@NonNull Context c, String fabskin, int theme1, String[] texts) {
        MaterialDialog.Builder a = new MaterialDialog.Builder(c);
        a.title(texts[0]);
        a.content(texts[1]);
        a.backgroundColor(ThemeEngine.primaryBackGround);
        a.widgetColor(Color.parseColor(fabskin));
        if (theme1 == 1) {
            a.theme(Theme.DARK);
        }
        a.positiveText(texts[2]);
        a.positiveColor(Color.parseColor(fabskin));
        a.negativeText(texts[3]);
        a.negativeColor(Color.parseColor(fabskin));
        if (texts[4] != (null)) {
            a.neutralText(texts[4]);
            a.neutralColor(Color.parseColor(fabskin));
        }
        return a.build();
    }

    @NonNull
    public static MaterialDialog.Builder getBasicDialog(@NonNull Context c, String[] texts) {
        MaterialDialog.Builder a = new MaterialDialog.Builder(c);
        a.title(texts[0]);
        a.content(texts[1]);
        a.backgroundColor(ThemeEngine.primaryBackGround);
        a.widgetColor(ThemeEngine.primary);
        if (ThemeEngine.isDark()) {
            a.theme(Theme.DARK);
        }
        if (texts[2] != null) {
            a.positiveText(texts[2]);
            a.positiveColor(ThemeEngine.primary);
        }
        if (texts[3] != null) {
            a.negativeText(texts[3]);
            a.negativeColor(ThemeEngine.primaryLight);
        }
        if (texts[4] != null) {
            a.neutralText(texts[4]);
            a.neutralColor(ThemeEngine.primary);
        }
        return a;
    }

    @NonNull
    public static MaterialDialog.Builder getBasicDialog(@NonNull Context c, @NonNull @StringRes int[] str) {
        String[] strs = new String[str.length];
        for (int itr = 0; itr < str.length; itr++) {
            if (str[itr] != -1) {
                strs[itr] = c.getString(str[itr]);
            } else {
                strs[itr] = null;
            }
        }
        return getBasicDialog(c, strs);
    }

    @NonNull
    public static MaterialDialog.Builder getBlankDialogBuilder(@NonNull Context context) {
        MaterialDialog.Builder a = new MaterialDialog.Builder(context);
        a.negativeColor(ThemeEngine.primary);
        a.positiveColor(ThemeEngine.primary);
        a.neutralColor(ThemeEngine.primary);
        if (ThemeEngine.isDark()) {
            a.theme(Theme.DARK);
        }
        a.backgroundColor(ThemeEngine.primaryBackGround);
        a.widgetColor(ThemeEngine.primary);
        return a;
    }

    @NonNull
    public static MaterialDialog.Builder getBlankProgressDialogBuilder(@NonNull Context context, int max) {
        MaterialDialog.Builder builder = getBlankDialogBuilder(context);
        builder.progress(false, max, true);
        return builder;
    }

    @NonNull
    public static MaterialDialog.Builder getBlankLoadingDialogBuilder(@NonNull Context context) {
        MaterialDialog.Builder builder = getBlankDialogBuilder(context);
        builder.progress(true, 0);
        return builder;
    }

    public static Dialog getFullScreenDialog(@NonNull Context context, @LayoutRes int contentLayout) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentLayout);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    public static void showChangeLog(Activity activity, @RawRes int change_log) {
        final MaterialDialog.Builder a = DialogUtil.getBlankDialogBuilder(activity);
        a.positiveText(R.string.cancel);
        a.title(R.string.change_log);
        a.autoDismiss(true);
        a.customView(R.layout.change_log_dialog, false);
        a.typeface(FontCache.getFont("Montserrat-Regular.otf"), FontCache.getFont("Montserrat-Regular.otf"));
        MaterialDialog x = a.build();
        x.show();
        View dv = x.getCustomView();
        ChangeLogRecyclerView log = (ChangeLogRecyclerView) dv.findViewById(R.id.change_log_view);
        log.setChangeLogXml(change_log);
    }

    public static void showConfirmDialog(@NonNull Context context, @StringRes int message, MaterialDialog.SingleButtonCallback onConfirm) {
        showConfirmDialog(context, context.getString(message), onConfirm);
    }

    public static void showConfirmDialog(@NonNull Context context, String message, MaterialDialog.SingleButtonCallback onConfirm) {
        getBlankDialogBuilder(context).title(message).onPositive(onConfirm).positiveText(R.string.confirm).negativeText(R.string.cancel).show();
    }
}
