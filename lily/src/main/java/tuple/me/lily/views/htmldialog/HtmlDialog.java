package tuple.me.lily.views.htmldialog;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import tuple.me.lily.ThemeEngine;

/**
 * Created by gokul-pt749 on 06/11/2016.
 */
public class HtmlDialog {

    private int htmlResId;
    private FragmentManager fm;
    private onPositivePressed positivePressed;
    private onNegativePressed negativePressed;
    private onCancelled cancelled;
    private String title;
    private String posTitle;
    private String negTitle;
    private HtmlDialogFragment dialogFragment;

    public HtmlDialog(FragmentManager fm, int htmlResId) {
        this.fm = fm;
        this.htmlResId = htmlResId;
    }


    public HtmlDialog(FragmentManager fm, int htmlResId, int htmlResIdDark) {
        this.fm = fm;
        if (ThemeEngine.isDark()) {
            this.htmlResId = htmlResIdDark;
        } else {
            this.htmlResId = htmlResId;
        }
    }

    @NonNull
    public HtmlDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    @NonNull
    public HtmlDialog setOnPostivePressed(String posTitle, onPositivePressed postivePressed) {
        this.positivePressed = postivePressed;
        this.posTitle = posTitle;
        return this;
    }

    @NonNull
    public HtmlDialog setOnNegativePresssed(String negTitle, onNegativePressed negativePresssed) {
        this.negativePressed = negativePresssed;
        this.negTitle = negTitle;
        return this;
    }

    @NonNull
    public HtmlDialog setOnCancelled(onCancelled cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public void show() {
        dialogFragment =
                HtmlDialogFragment.newInstance(
                        title,
                        htmlResId,
                        posTitle,
                        new onPositivePressed() {
                            @Override
                            public void onPress() {
                                if (positivePressed != null)
                                    positivePressed.onPress();
                            }
                        },
                        negTitle,
                        new onNegativePressed() {
                            @Override
                            public void onPress() {
                                if (negativePressed != null)
                                    negativePressed.onPress();
                            }
                        },
                        new onCancelled() {
                            @Override
                            public void cancel() {
                                if (cancelled != null)
                                    cancelled.cancel();
                            }
                        });

        dialogFragment.show(fm,
                HtmlDialogFragment.TAG_HTML_DIALOG_FRAGMENT);
    }

    public void dismiss() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }

    public interface onPositivePressed {
        void onPress();
    }

    public interface onNegativePressed {
        void onPress();
    }

    public interface onCancelled {
        void cancel();
    }
}
