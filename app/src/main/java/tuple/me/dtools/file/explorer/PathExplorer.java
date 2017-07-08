package tuple.me.dtools.file.explorer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;

import java.io.File;
import java.util.List;
import java.util.Stack;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.view.breadcrumb.BaseCrumb;
import tuple.me.dtools.view.breadcrumb.BreadCrumbLayout;
import tuple.me.dtools.view.breadcrumb.FileCrumb;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.fastscroll.FastScroller;
import tuple.me.lily.views.toasty.Toasty;

public class PathExplorer extends Fragment implements MaterialCab.Callback, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView list;
    private FastScroller fastScroller;
    private SwipeRefreshLayout refreshLayout;
    private MultiStateView multiStateView;
    private BreadCrumbLayout breadCrumb;
    private MaterialProgressBar progressBar;

    Stack<SystemFile> stack = new Stack<>();
    CompositeDisposable compositeDisposable;
    PathExplorerAdapter adapter;
    NavHandler cab;
    Disposable storageDisposable;
    String root;

    public static PathExplorer newInstance(String path) {
        PathExplorer detailsDialog = new PathExplorer();
        Bundle args = new Bundle();
        args.putString("root_path", path);
        detailsDialog.setArguments(args);
        return detailsDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        root = getArguments().getString("root_path");
        rootView = inflater.inflate(R.layout.storage_viewer_fragment, container, false);
        initViews(rootView);
        finishSetupList();
        return rootView;
    }


    protected void initViews(View rootView) {
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setProgressBackgroundColorSchemeColor(Contexter.getColor(R.color.colorPrimaryDark));
        refreshLayout.setColorSchemeColors(Contexter.getColor(R.color.colorAccent));
        list = (RecyclerView) rootView.findViewById(R.id.list);
        progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progress_bar);
        fastScroller = (FastScroller) rootView.findViewById(R.id.fastscroll);
        multiStateView = (MultiStateView) rootView.findViewById(R.id.multi_state_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(list.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);
        breadCrumb = (BreadCrumbLayout) rootView.findViewById(R.id.bread_crumb);
        breadCrumb.setCallback(new BreadCrumbLayout.SelectionCallback() {
            @Override
            public void onCrumbSelection(BaseCrumb crumb, int index) {
                if (crumb instanceof FileCrumb) {
                    if (CollectionUtils.removeAfter(stack, ((FileCrumb) crumb).systemFile)) {
                        if (storageDisposable != null) {
                            storageDisposable.dispose();
                        }
                        onRefresh();
                    }
                } else {
                    if (!stack.empty()) {
                        stack.clear();
                        if (storageDisposable != null) {
                            storageDisposable.dispose();
                        }
                        onRefresh();
                    }
                }
            }
        });
        breadCrumb.addCrumb(new BaseCrumb("root"), true);
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, true, true));
        if (ViewUtils.getActivityForFragment(this) instanceof MainActivity) {
            cab = (MainActivity) ViewUtils.getActivityForFragment(this);
        }
        setHasOptionsMenu(true);
    }

    private void finishSetupList() {
        if (adapter == null) {
            adapter = new PathExplorerAdapter(getContext());
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.apk_backup);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<Item>() {
                @Override
                public void onItemClick(Item file) {
                    if (file instanceof SystemFile) {
                        if (((SystemFile) file).isDir) {
                            if (stack.empty() || !stack.peek().equals(file)) {
                                stack.add(((SystemFile) file));
                                breadCrumb.addCrumb(new FileCrumb(((SystemFile) file)), true);
                            }
                            onRefresh();
                        }
                    }
                }
            });
            list.setAdapter(adapter);
            fastScroller.setRecyclerView(list);
        }
        adapter.setDisabled(false);
        fastScroller.setVisibility(View.VISIBLE);
        cab.getCab().finish();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        compositeDisposable = new CompositeDisposable();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        compositeDisposable.dispose();
    }

    Flowable<List<SystemFile>> storageJob = Flowable.create(new FlowableOnSubscribe<SystemFile>() {
        @Override
        public void subscribe(@NonNull FlowableEmitter<SystemFile> e) throws Exception {
            if (stack.isEmpty()) {
                fillFiles(root, e);
            } else {
                fillFiles(stack.peek().file.getPath(), e);
            }
            e.onComplete();
        }

        public void fillFiles(String path, FlowableEmitter<SystemFile> emitter) {
            SystemFile file = new SystemFile(path);
            if (file.isDir) {
                File[] childs = file.file.listFiles();
                if (childs != null) {
                    for (File child : childs) {
                        if (FileUtils.isPathValid(child.getPath())) {
                            SystemFile fileToAdd = new SystemFile(child);
                            emitter.onNext(fileToAdd);
                        }
                    }
                }
            }
        }
    }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).buffer(50);


    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        adapter.resetMultiSelect();
        return true;
    }

    @Override
    public void onRefresh() {
        if (storageDisposable == null || storageDisposable.isDisposed()) {
            fastScroller.setVisibility(View.GONE);
            adapter.clear();
            adapter.setDisabled(true);
            progressBar.setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            storageDisposable = storageJob.subscribeWith(new DisposableSubscriber<List<SystemFile>>() {

                @Override
                public void onNext(List<SystemFile> systemFiles) {
                    adapter.addAll(systemFiles);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {
                    finishSetupList();
                    compositeDisposable.remove(storageDisposable);
                }
            });
            compositeDisposable.add(storageDisposable);
        } else {
            Toasty.error(getContext(), R.string.task_already_running);
        }
    }
}

