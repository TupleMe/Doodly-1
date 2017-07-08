package tuple.me.dtools.file.largefiles;

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
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileFilter;
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
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.util.ProgressJob;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.fastscroll.FastScroller;
import tuple.me.lily.views.toasty.Toasty;

public class LargeFiles extends Fragment implements MaterialCab.Callback, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView list;
    private FastScroller fastScroller;
    private SwipeRefreshLayout refreshLayout;
    private MultiStateView multiStateView;
    private MaterialProgressBar progressBar;

    CompositeDisposable compositeDisposable;
    LargeFilesAdapter adapter;
    NavHandler cab;
    Disposable storageDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.large_files_fragment, container, false);
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
        setHasOptionsMenu(true);
    }

    private void finishSetupList() {
        if (adapter == null) {
            adapter = new LargeFilesAdapter(getContext());
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.empty_files_folders);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<Item>() {
                @Override
                public void onItemClick(Item file) {
                    if (file instanceof SystemFile) {
                        FileManagerUtil.showFileOptions((SystemFile) file, getFragmentManager(), getContext());
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
        EventBus.getDefault().register(this);
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        compositeDisposable.dispose();
    }

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
                File[] childs = file.file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.length() > 1024 * 1024 * 50;
                    }
                });
                if (childs != null) {
                    for (File child : childs) {
                        if (FileUtils.isPathValid(child.getPath())) {
                            SystemFile fileToAdd = new SystemFile(child);
                            if (fileToAdd.isDir) {
                                fillFiles(fileToAdd.file.getPath(), emitter);
                            } else {
                                emitter.onNext(fileToAdd);
                            }
                        }
                    }
                }
            }
        }
    }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).buffer(25);


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
                final List<Item> apks = adapter.getSelectedItems();
                DialogUtil.showConfirmDialog(getContext(), "Are you sure want to delete " + apks.size() + (apks.size() > 1 ? " items?" : " item?"), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        final ProgressJob<Item> apkModelProgressJob = new ProgressJob<>(getContext(), apks);
                        apkModelProgressJob.setTitle(R.string.deleteting);
                        apkModelProgressJob.actionHandler(new ProgressJob.ActionHandler<Item>()

                                                          {
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
                                                          }

                        );
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toasty.success(getContext(), "Menu clicked");
        return super.onOptionsItemSelected(item);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileDelete(FileDelete fileDelete) {
        onRefresh();
    }
}

