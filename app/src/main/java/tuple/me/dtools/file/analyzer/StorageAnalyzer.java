package tuple.me.dtools.file.analyzer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.util.ProgressJob;
import tuple.me.dtools.view.breadcrumb.BaseCrumb;
import tuple.me.dtools.view.breadcrumb.BreadCrumbLayout;
import tuple.me.dtools.view.breadcrumb.FileCrumb;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.util.BackButton;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.RangeUtils;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.fastscroll.FastScroller;
import tuple.me.lily.views.toasty.Toasty;

public class StorageAnalyzer extends Fragment implements MaterialCab.Callback, SwipeRefreshLayout.OnRefreshListener, BackButton.OnBackClickListener {
    private RecyclerView list;
    private FastScroller fastScroller;
    private SwipeRefreshLayout refreshLayout;
    private MultiStateView multiStateView;
    private BreadCrumbLayout breadCrumb;
    private MaterialProgressBar progressBar;

    Stack<SystemFile> stack = new Stack<>();
    CompositeDisposable compositeDisposable;
    StorageAnalyzerAdapter adapter;
    private static long totalSize = 0;
    NavHandler cab;
    Disposable storageDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.storage_viewer_fragment, container, false);
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
    }

    private void finishSetupList() {
        if (adapter == null) {
            adapter = new StorageAnalyzerAdapter(getContext());
            adapter.dataSet = new SortedList<>(SystemFile.class, new SortedListAdapterCallback<SystemFile>(adapter) {

                @Override
                public int compare(SystemFile o1, SystemFile o2) {
                    return -RangeUtils.compare(o1.size, o2.size);
                }

                @Override
                public boolean areContentsTheSame(SystemFile oldItem, SystemFile newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areItemsTheSame(SystemFile item1, SystemFile item2) {
                    return item1.equals(item2);
                }
            });
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.empty_files_folders);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<SystemFile>() {
                @Override
                public void onItemClick(SystemFile file) {
                    if (file.isDir) {
                        if (stack.empty() || !stack.peek().equals(file)) {
                            stack.add(file);
                            breadCrumb.addCrumb(new FileCrumb(file), true);
                        }
                        onRefresh();
                    } else {
                        FileManagerUtil.showFileOptions(file.file, getFragmentManager(), getContext());
                    }
                }
            });
            list.setAdapter(adapter);
            fastScroller.setRecyclerView(list);
        }
        adapter.setDisabled(false);
        adapter.setTotalSize(totalSize);
        adapter.notifyDataSetChanged();
        fastScroller.setVisibility(View.VISIBLE);
        cab.getCab().finish();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        cab.getBackButton().addBackClickListener(this);
        compositeDisposable = new CompositeDisposable();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        cab.getBackButton().removeBackClickListener(this);
        compositeDisposable.dispose();
    }

    Flowable<List<SystemFile>> storageJob = Flowable.create(new FlowableOnSubscribe<SystemFile>() {
        @Override
        public void subscribe(@NonNull FlowableEmitter<SystemFile> e) throws Exception {
            totalSize = 0;
            if (stack.isEmpty()) {
                List<String> dirs = FileUtils.getStorageDirectories(Contexter.getAppContext());
                for (String dir : dirs) {
                    fillFiles(dir, e);
                }
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
                            if (fileToAdd.isDir) {
                                long[] stat = FileUtils.getDirSizeWithItems(child, new long[]{0, 0});
                                fileToAdd.size = stat[0];
                                fileToAdd.childItemsCount = stat[1];
                            }
                            totalSize += fileToAdd.size;
                            emitter.onNext(fileToAdd);
                        }
                    }
                }
            }
        }
    }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).buffer(5);


    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_select_all:
                adapter.selectAll();
                break;
            case R.id.item_deselect_all:
                adapter.resetMultiSelect();
                cab.getCab().finish();
                break;
            case R.id.item_delete:
                final List<SystemFile> apks = adapter.getSelectedItems();
                DialogUtil.showConfirmDialog(getContext(), "Are you sure want to delete " + apks.size() + (apks.size() > 1 ? " items?" : " item?"), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        final ProgressJob<SystemFile> apkModelProgressJob = new ProgressJob<>(getContext(), apks);
                        apkModelProgressJob.setTitle(R.string.back_up);
                        apkModelProgressJob.actionHandler(new ProgressJob.ActionHandler<SystemFile>() {
                            @Override
                            public boolean doAction(SystemFile o) throws Exception {
                                return FileManagerUtil.deleteFile(o.file, getContext());
                            }

                            @Override
                            public void onComplete() {
                                if (apkModelProgressJob.getFailedItems().isEmpty()) {
                                    Toasty.success(getContext(), R.string.success);
                                } else {
                                    Toasty.error(getContext(), R.string.operation_incomplete);
                                }
                                onRefresh();
                            }
                        });
                        apkModelProgressJob.start();
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        adapter.resetMultiSelect();
        return true;
    }

    @Override
    public void onRefresh() {
        if (storageDisposable == null || storageDisposable.isDisposed()) {
            cab.getCab().finish();
            fastScroller.setVisibility(View.GONE);
            adapter.clear();
            adapter.setDisabled(true);
            progressBar.setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            storageDisposable = storageJob.subscribeWith(new DisposableSubscriber<List<SystemFile>>() {

                @Override
                public void onNext(List<SystemFile> systemFiles) {
                    adapter.setTotalSize(totalSize);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileDelete(FileDelete fileDelete) {
        onRefresh();
    }

    @Override
    public boolean onClick() {
        if (!stack.empty()) {
            breadCrumb.trim(new FileCrumb(stack.pop()));
            onRefresh();
            return true;
        }
        return false;
    }
}
