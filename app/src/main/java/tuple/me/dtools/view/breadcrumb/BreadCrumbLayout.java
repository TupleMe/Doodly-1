package tuple.me.dtools.view.breadcrumb;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.lily.Contexter;
import tuple.me.lily.core.CollectionUtils;

public class BreadCrumbLayout extends HorizontalScrollView implements View.OnClickListener {

    @ColorInt
    private int contentColorActivated;
    @ColorInt
    private int contentColorDeactivated;

    private boolean removeBackOnClick = true;

    public interface SelectionCallback {
        void onCrumbSelection(BaseCrumb crumb, int index);
    }

    public BreadCrumbLayout(Context context) {
        super(context);
        init();
    }

    public BreadCrumbLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BreadCrumbLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private ArrayList<BaseCrumb> mCrumbs;

    private LinearLayout mChildFrame;
    private int mActive;
    private SelectionCallback mCallback;

    private void init() {
        contentColorActivated = Contexter.getColor(android.R.color.white);
        contentColorDeactivated = Contexter.getColor(R.color.text_light_white);
        setMinimumHeight((int) getResources().getDimension(R.dimen.tab_height));
        setClipToPadding(false);
        setHorizontalScrollBarEnabled(false);
        mCrumbs = new ArrayList<>();
        mChildFrame = new LinearLayout(getContext());
        addView(mChildFrame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void addCrumb(@NonNull BaseCrumb crumb, boolean refreshLayout) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.bread_crumb, this, false);
        view.setTag(mCrumbs.size());
        view.setOnClickListener(this);

        ImageView iv = (ImageView) view.getChildAt(1);
        if (Build.VERSION.SDK_INT >= 19 && iv.getDrawable() != null) {
            iv.getDrawable().setAutoMirrored(true);
        }
        iv.setVisibility(View.GONE);

        mChildFrame.addView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mCrumbs.add(crumb);
        if (refreshLayout) {
            mActive = mCrumbs.size() - 1;
            requestLayout();
        }
        invalidateActivatedAll();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //RTL works fine like this
        View child = mChildFrame.getChildAt(mActive);
        if (child != null)
            smoothScrollTo(child.getLeft(), 0);
    }

    public BaseCrumb findCrumb(@NonNull BaseCrumb crumbToFind) {
        int crumbIndex = mCrumbs.indexOf(crumbToFind);
        if (crumbIndex != -1) {
            return mCrumbs.get(crumbIndex);
        }
        return null;
    }

    public void clearCrumbs() {
        try {
            mCrumbs.clear();
            mChildFrame.removeAllViews();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public BaseCrumb getCrumb(int index) {
        return mCrumbs.get(index);
    }

    public void setCallback(SelectionCallback callback) {
        mCallback = callback;
    }

    private boolean setActive(BaseCrumb newActive) {
        mActive = mCrumbs.indexOf(newActive);
        invalidateActivatedAll();
        boolean success = mActive > -1;
        if (success)
            requestLayout();
        return success;
    }

    void invalidateActivatedAll() {
        for (int i = 0; i < mCrumbs.size(); i++) {
            BaseCrumb crumb = mCrumbs.get(i);
            invalidateActivated(mChildFrame.getChildAt(i), mActive == mCrumbs.indexOf(crumb), false, i < mCrumbs.size() - 1).setText(crumb.getTitle());
        }
    }

    void removeCrumbAt(int index) {
        mCrumbs.remove(index);
        mChildFrame.removeViewAt(index);
    }

    public boolean trim(BaseCrumb crumb) {
        if (!crumb.isValidCrumb()) return false;
        int index = CollectionUtils.lastIndexOf(mCrumbs, crumb);
        boolean removedActive = index >= mActive;
        if (index > -1) {
            while (index <= mCrumbs.size() - 1)
                removeCrumbAt(index);
            if (mChildFrame.getChildCount() > 0) {
                int lastIndex = mCrumbs.size() - 1;
                invalidateActivated(mChildFrame.getChildAt(lastIndex), mActive == lastIndex, false, false);
            }
        }
        return removedActive || mCrumbs.size() == 0;
    }


    void updateIndices() {
        for (int i = 0; i < mChildFrame.getChildCount(); i++)
            mChildFrame.getChildAt(i).setTag(i);
    }

    public void setLastActive() {
        if (mChildFrame.getChildCount() > 0) {
            invalidateActivated(mChildFrame.getChildAt(mChildFrame.getChildCount() - 1), true, true, false);
        }
    }

    public int size() {
        return mCrumbs.size();
    }

    private TextView invalidateActivated(View view, final boolean isActive, final boolean noArrowIfAlone, final boolean allowArrowVisible) {
        int contentColor = isActive ? contentColorActivated : contentColorDeactivated;
        LinearLayout child = (LinearLayout) view;
        TextView tv = (TextView) child.getChildAt(0);
        tv.setTextColor(contentColor);
        ImageView iv = (ImageView) child.getChildAt(1);
        iv.setColorFilter(contentColor, PorterDuff.Mode.SRC_IN);
        if (noArrowIfAlone && getChildCount() == 1)
            iv.setVisibility(View.GONE);
        else if (allowArrowVisible)
            iv.setVisibility(View.VISIBLE);
        else
            iv.setVisibility(View.GONE);
        return tv;
    }

    public int getActiveIndex() {
        return mActive;
    }

    public void setActivatedContentColor(@ColorInt int contentColorActivated) {
        this.contentColorActivated = contentColorActivated;
    }

    public void setDeactivatedContentColor(@ColorInt int contentColorDeactivated) {
        this.contentColorDeactivated = contentColorDeactivated;
    }

    @Override
    public void onClick(View v) {
        int index = (Integer) v.getTag();
        if (index != getActiveIndex() && removeBackOnClick) {
            CollectionUtils.removeAfter(mCrumbs, index);
            for (int itr = mChildFrame.getChildCount() - 1; itr > index; itr--) {
                mChildFrame.removeViewAt(itr);
            }
            updateIndices();
            setLastActive();
        }
        if (mCallback != null) {
            mCallback.onCrumbSelection(mCrumbs.get(index), index);
        }
    }
}