package tuple.me.lily.views.toasty;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import tuple.me.lily.R;
import tuple.me.lily.core.Callback;
import tuple.me.lily.util.ViewUtils;

/**
 * Created by gokul-4192 on 0028 28-Dec-16.
 */
public class ToastyDialog {

    private Context context;
    private Dialog bottomDialog;

    public ToastyDialog(@NonNull Context context) {
        this.context = context;
        bottomDialog = new Dialog(context, R.style.BottomDialogs);
        View view = LayoutInflater.from(context).inflate(R.layout.toasty_dialog, null);
        bottomDialog.setContentView(view);
    }


    @NonNull
    public ToastyDialog show(@NonNull View view) {
        bottomDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams wmlp = bottomDialog.getWindow().getAttributes();
        wmlp.y = ViewUtils.getViewYPos(view) + view.getMeasuredHeight() / 2;
        bottomDialog.show();
        return this;
    }

    public static class Builder {

        private static final int LIGHT = 0;
        private static final int DARK = 1;
        private String title;
        private String positive;
        private String nagative;
        private String content;
        private int layout;
        private Callback<ToastyDialog> postiveListener;
        private Callback<ToastyDialog> negativeListener;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({LIGHT, DARK})
        private @interface Theme {
        }

        private Context context;
        private int LayoutRes = -1;
        private int theme = 0;

        public Builder(Context context) {
            this.context = context;
        }

        @NonNull
        public Builder theme(@Theme int theme) {
            this.theme = theme;
            return this;
        }

        @NonNull
        public Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        @NonNull
        public Builder title(@StringRes int stringRes) {
            return title(context.getString(stringRes));
        }


        @NonNull
        public Builder positive(@NonNull String positive) {
            this.positive = positive;
            return this;
        }

        @NonNull
        public Builder positive(@StringRes int positive) {
            return positive(context.getString(positive));
        }

        @NonNull
        public Builder negative(@NonNull String nagative) {
            this.nagative = nagative;
            return this;
        }

        @NonNull
        public Builder negative(@StringRes int negative) {
            return positive(context.getString(negative));
        }

        @NonNull
        public Builder content(@NonNull String content) {
            this.content = content;
            return this;
        }

        @NonNull
        public Builder content(@StringRes int content) {
            return content(context.getString(content));
        }

        @NonNull
        public Builder view(@android.support.annotation.LayoutRes int layoutRes) {
            this.layout = layoutRes;
            return this;
        }

        @NonNull
        public Builder onPositive(Callback<ToastyDialog> listener) {
            this.postiveListener = listener;
            return this;
        }

        @NonNull
        public Builder onNegative(Callback<ToastyDialog> listener) {
            this.negativeListener = listener;
            return this;
        }
    }

}

