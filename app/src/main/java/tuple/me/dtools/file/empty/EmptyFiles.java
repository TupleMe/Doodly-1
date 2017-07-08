package tuple.me.dtools.file.empty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.util.ProgressJob;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.core.StringUtil;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.fastscroll.FastScroller;
import tuple.me.lily.views.toasty.Toasty;

public class EmptyFiles extends Fragment implements MaterialCab.Callback, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView list;
    private FastScroller fastScroller;
    private SwipeRefreshLayout refreshLayout;
    private MultiStateView multiStateView;
    private MaterialProgressBar progressBar;
    private FloatingActionButton clean;
    CompositeDisposable compositeDisposable;
    EmptyFilesAdapter adapter;
    NavHandler cab;
    Disposable storageDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.empty_files_fragment, container, false);
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
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, true, true));
        if (ViewUtils.getActivityForFragment(this) instanceof MainActivity) {
            cab = (MainActivity) ViewUtils.getActivityForFragment(this);
        }
        clean = (FloatingActionButton) rootView.findViewById(R.id.fab);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter!=null && !adapter.isEmpty()){
                    deleteItems(CollectionUtils.toArrayList(adapter.dataSet));
                }
            }
        });
    }

    private void finishSetupList() {
        if (adapter == null) {
            adapter = new EmptyFilesAdapter(getContext());
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.empty_files_folders);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<Item>() {
                @Override
                public void onItemClick(Item file) {

                }
            });
            list.setAdapter(adapter);
            fastScroller.setRecyclerView(list);
        }
        adapter.setDisabled(false);
        fastScroller.setVisibility(View.VISIBLE);
        cab.getCab().finish();
        if (adapter.isEmpty()) {
            multiStateView.setView(R.layout.common_empty);
        } else {
            multiStateView.emptyStateView();
        }
        clean.setVisibility(View.VISIBLE);
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

    public static final String[] ignores = new String[]{".image", ".thumb", ".cache", ".video"};
    Flowable<List<SystemFile>> storageJob = Flowable.create(new FlowableOnSubscribe<SystemFile>() {
        @Override
        public void subscribe(@NonNull FlowableEmitter<SystemFile> e) throws Exception {
            List<String> dirs = FileUtils.getStorageDirectories(Contexter.getAppContext());
            for (String dir : dirs) {
                fillFiles(dir, e);
            }
            e.onComplete();
        }

        public void fillFiles(String path, FlowableEmitter<SystemFile> emitter) {
            SystemFile file = new SystemFile(path);
            if (file.isDir) {
                File[] childs = file.file.listFiles();
                if (childs != null) {
                    if (childs.length == 0) {
                        emitter.onNext(file);
                    } else {
                        for (File child : childs) {
                            SystemFile fileToAdd = new SystemFile(child);
                            if (fileToAdd.isDir) {
                                if (!fileToAdd.file.getPath().contains("Android/data/") && !StringUtil.equalsAny(fileToAdd.file.getName(), ignores))
                                    fillFiles(fileToAdd.file.getPath(), emitter);
                            } else {
                                if (FileUtils.isEmptyFile(fileToAdd.file) && !fileToAdd.file.getName().equals(".nomedia")) {
                                    emitter.onNext(fileToAdd);
                                }
                            }
                        }
                    }
                }
            }
        }
    }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).buffer(100);


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
                List<Item> apks = adapter.getSelectedItems();
                deleteItems(apks);
                break;
        }
        return false;
    }

    private void deleteItems(List<Item> apks) {
        final ProgressJob<Item> apkModelProgressJob = new ProgressJob<>(getContext(), apks);
        apkModelProgressJob.setTitle(R.string.back_up);
        apkModelProgressJob.actionHandler(new ProgressJob.ActionHandler<Item>() {
            @Override
            public boolean doAction(Item o) throws Exception {
                return !(o instanceof SystemFile) || FileManagerUtil.deleteFile(((SystemFile) o).file, getContext());
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

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        adapter.resetMultiSelect();
        return true;
    }

    @Override
    public void onRefresh() {
        cab.getCab().finish();
        if (storageDisposable == null || storageDisposable.isDisposed()) {
            fastScroller.setVisibility(View.GONE);
            multiStateView.emptyStateView();
            adapter.clear();
            clean.setVisibility(View.GONE);
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

