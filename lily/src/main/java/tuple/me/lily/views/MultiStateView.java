package tuple.me.lily.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by gokul-4192 on 0025 25-Feb-17.
 */
public class MultiStateView extends FrameLayout {

    private MultiViewState multiViewState = new MultiViewState();
    private ArrayList<View> nonInternalViews = new ArrayList<>(5);

    public MultiStateView(Context context) {
        super(context);
    }

    public MultiStateView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MultiStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction)
                || (multiViewState.currentView != null && multiViewState.currentView.canScrollVertically(direction));
    }

    @Override
    public void addView(View child) {
        if (!multiViewState.isInternalView(child)) {
            nonInternalViews.add(child);
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!multiViewState.isInternalView(child)) {
            nonInternalViews.add(child);
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!multiViewState.isInternalView(child)) {
            nonInternalViews.add(child);
        }
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (!multiViewState.isInternalView(child)) {
            nonInternalViews.add(child);
        }
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!multiViewState.isInternalView(child)) {
            nonInternalViews.add(child);
        }
        super.addView(child, index, params);
    }


    public View setView(@LayoutRes int layout) {
        for (View nonInternalView : nonInternalViews) {
            nonInternalView.setVisibility(GONE);
            invalidate();
        }
        if (layout == multiViewState.currentViewId) {
            View currentView = getCurrentView();
            currentView.setVisibility(VISIBLE);
            return currentView;
        }
        return multiViewState.getAndSet(layout);
    }

    public void emptyStateView() {
        for (View nonInternalView : nonInternalViews) {
            nonInternalView.setVisibility(VISIBLE);
        }
        if (multiViewState.currentView != null) {
            multiViewState.currentView.setVisibility(GONE);
        }
    }

    public int getCurrentViewId() {
        return multiViewState.getCurrentViewId();
    }

    public View getCurrentView() {
        return multiViewState.getCurrentView();
    }

    public class MultiViewState {
        private ArrayMap<Integer, View> layoutVsViews = new ArrayMap<>();
        private int currentViewId = -1;
        private View currentView;

        public View getView(@LayoutRes int id) {
            if (!layoutVsViews.containsKey(id)) {
                layoutVsViews.put(id, View.inflate(getContext(), id, null));
            }
            return layoutVsViews.get(id);
        }

        private int getCurrentViewId() {
            return currentViewId;
        }

        private View getCurrentView() {
            return currentView;
        }

        public View getAndSet(@LayoutRes int id) {
            if (currentView != null) {
                currentView.setVisibility(View.GONE);
            }
            currentViewId = id;
            currentView = getView(id);
            addView(currentView);
            currentView.setVisibility(VISIBLE);
            return currentView;
        }

        public boolean isInternalView(View viewToCheck) {
            return layoutVsViews.containsValue(viewToCheck);
        }
    }
}
