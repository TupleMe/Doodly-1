package tuple.me.lily.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.internal.ScrimInsetsFrameLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tuple.me.lily.util.ViewUtils;

/**
 * Created by gokul-4192 on 0011 11-Mar-17.
 */
public class LeftRightTextView extends RelativeLayout {
    private TextView leftTextView;
    private TextView rightTextView;

    public LeftRightTextView(Context context) {
        super(context);
        initViews(context);
    }

    public LeftRightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public LeftRightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LeftRightTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context);
    }

    private void initViews(Context context) {
        leftTextView = new TextView(context);
        rightTextView = new TextView(context);

        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ViewUtils.convertDpToPx(context, 16);
        leftParams.setMargins(margin, margin, margin, margin);
        addView(leftTextView, leftParams);

        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightParams.setMargins(margin, margin, margin, margin);
        addView(rightTextView, rightParams);
    }

    public TextView getLeftTextView() {
        return leftTextView;
    }

    public TextView getRightTextView() {
        return rightTextView;
    }

    public void setLeftText(String leftText) {
        leftTextView.setText(leftText);
    }

    public void setRightText(String rightText) {
        rightTextView.setText(rightText);
    }

    public void setText(String leftText, String rightText) {
        setLeftText(leftText);
        setRightText(rightText);
    }
}
