package tuple.me.lily.views.toasty;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import tuple.me.lily.Contexter;
import tuple.me.lily.R;
import tuple.me.lily.views.CustomTextView;

/**
 * Created by gokul-4192 on 0028 28-Dec-16.
 */
public class Toasty {
    public static void success(@NonNull Context context, @StringRes int message) {
        success(context, context.getString(message));
    }

    public static void success(@NonNull Context context, @NonNull String message) {
        Pair<Toast, ImageView> toasty = getToast(context, Color.DKGRAY, message);
        Toast toast = toasty.first;
        ImageView icon = toasty.second;
        icon.setImageResource(R.drawable.ic_check_circle);
        toast.show();
    }

    public static void error(@StringRes int message) {
        error(Contexter.getAppContext(), Contexter.getString(message));
    }

    public static void error(@NonNull Context context, @StringRes int message) {
        error(context, context.getString(message));
    }

    public static void error(@NonNull Context context, @NonNull String message) {
        Pair<Toast, ImageView> toasty = getToast(context, Color.DKGRAY, message);
        Toast toast = toasty.first;
        ImageView icon = toasty.second;
        icon.setImageResource(R.drawable.ic_alert_circle);
        toast.show();
    }


    public static void empty(@NonNull Context context, @NonNull String message) {
        Pair<Toast, ImageView> toasty = getToast(context, Color.GRAY, message);
        Toast toast = toasty.first;
        ImageView icon = toasty.second;
        icon.setVisibility(View.GONE);
        toast.show();
    }

    public static void empty(@NonNull Context context, @StringRes int message) {
        error(context, context.getString(message));
    }


    @NonNull
    private static Pair<Toast, ImageView> getToast(@NonNull Context context, int color, @NonNull String message) {
        Toast toast = new Toast(context);
        toast.setDuration(message.length() < 30 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(message.length() < 30 ? R.layout.toast_layout : R.layout.toast_layout_vertical, null);
        toast.setView(view);
        CardView bg = (CardView) view.findViewById(R.id.toast_bg);
        bg.setCardBackgroundColor(color);
        CustomTextView meg = (CustomTextView) view.findViewById(R.id.message);
        meg.setText(message);
        ImageView icon = (ImageView) view.findViewById(R.id.toast_icon);
        return new Pair<>(toast, icon);
    }
}
