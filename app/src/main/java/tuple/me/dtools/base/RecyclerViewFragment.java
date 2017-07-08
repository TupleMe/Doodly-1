package tuple.me.dtools.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tuple.me.dtools.R;
import tuple.me.lily.Contexter;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.fastscroll.FastScroller;

public abstract class RecyclerViewFragment extends Fragment {
    public RecyclerView list;
    public FastScroller fastScroller;
    public SwipeRefreshLayout refreshLayout;
    public boolean hasRefreshOption;
    public MultiStateView multiStateView;
    public FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initConfig();
        View rootView;
        if (!hasRefreshOption) {
            rootView = inflater.inflate(R.layout.recycler_view_fragment, container, false);
        } else {
            rootView = inflater.inflate(R.layout.recycler_view_fragment_with_refresh, container, false);
            refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshList();
                }
            });
            refreshLayout.setProgressBackgroundColorSchemeColor(Contexter.getColor(R.color.colorPrimaryDark));
            refreshLayout.setColorSchemeColors(Contexter.getColor(R.color.colorAccent));
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        }
        list = (RecyclerView) rootView.findViewById(R.id.list);
        fastScroller = (FastScroller) rootView.findViewById(R.id.fastscroll);
        multiStateView = (MultiStateView) rootView.findViewById(R.id.multi_state_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(list.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);
        initViews();
        return rootView;
    }

    public abstract void initConfig();

    public abstract void onRefreshList();

    protected abstract void initViews();

    public View setLoadingState() {
        return multiStateView.setView(R.layout.common_loading);
    }

    public void setEmptyState() {
        multiStateView.emptyStateView();
    }
}
