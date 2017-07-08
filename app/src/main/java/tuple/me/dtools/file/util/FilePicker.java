package tuple.me.dtools.file.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Stack;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import tuple.me.dtools.R;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.explorer.PathExplorerAdapter;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.core.Callback;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.views.fastscroll.FastScroller;
import tuple.me.lily.views.toasty.Toasty;

/**
 * Created by gokul-4192 on 0013 13-May-17.
 */

public class FilePicker {

    private Callback<SystemFile> onFileChosen;
    private Context context;
    private Stack<SystemFile> stack = new Stack<>();
    private RecyclerView filesList;
    private FastScroller fastScroller;
    private PathExplorerAdapter adapter;
    private DisposableSubscriber<List<SystemFile>> storageDisposable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FileFilter fileFilter;
    private MaterialDialog dialog;

    public FilePicker(Context context) {
        this.context = context;
    }

    public FilePicker onSelect(Callback<SystemFile> onFileSelectListener) {
        this.onFileChosen = onFileSelectListener;
        return this;
    }

    public FilePicker filter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
        return this;
    }

    public FilePicker show() {
        dialog = DialogUtil.getBlankDialogBuilder(context)
                .title(R.string.choose_file)
                .positiveText(R.string.back)
                .negativeText(R.string.cancel)
                .customView(R.layout.recycler_view_fragment, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        if (!stack.isEmpty()) {
                            stack.pop();
                            onRefresh();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .show();
        filesList = (RecyclerView) dialog.getCustomView().findViewById(R.id.list);
        fastScroller = (FastScroller) dialog.getCustomView().findViewById(R.id.fastscroll);
        LinearLayoutManager layoutManager = new LinearLayoutManager(filesList.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        filesList.setLayoutManager(layoutManager);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                compositeDisposable.dispose();
            }
        });
        setUpList();
        onRefresh();
        return this;
    }

    private void setUpList() {
        if (adapter == null) {
            adapter = new PathExplorerAdapter(context);
            adapter.setMultiSelectMode(false);
            adapter.setOnClickListener(new OnItemClickListener<Item>() {
                @Override
                public void onItemClick(Item file) {
                    if (file instanceof SystemFile) {
                        if (((SystemFile) file).isDir) {
                            stack.add(((SystemFile) file));
                            onRefresh();
                        } else {
                            dialog.dismiss();
                            onFileChosen.call(((SystemFile) file));
                        }
                    }
                }
            });
            filesList.setAdapter(adapter);
            fastScroller.setRecyclerView(filesList);
        }
        if (adapter.isEmpty()) {
            if (!stack.isEmpty()) {
                Toasty.error(R.string.folder_is_empty);
                stack.pop();
                onRefresh();
            }
        }
        adapter.setDisabled(false);
        fastScroller.setVisibility(View.VISIBLE);
    }

    public void onRefresh() {
        if (storageDisposable == null || storageDisposable.isDisposed()) {
            adapter.clear();
            adapter.setDisabled(true);
            fastScroller.setVisibility(View.GONE);
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
                    compositeDisposable.remove(storageDisposable);
                    setUpList();
                }
            });
            compositeDisposable.add(storageDisposable);
        } else {
            Toasty.error(context, R.string.task_already_running);
        }
    }


    Flowable<List<SystemFile>> storageJob = Flowable.create(new FlowableOnSubscribe<SystemFile>() {
        @Override
        public void subscribe(@NonNull FlowableEmitter<SystemFile> e) throws Exception {
            if (stack.isEmpty()) {
                List<String> storages = FileUtils.getStorageDirectories(Contexter.getAppContext());
                for (String storage : storages) {
                    fillFiles(storage, e);
                }
            } else {
                fillFiles(stack.peek().file.getPath(), e);
            }
            e.onComplete();
        }

        public void fillFiles(String path, FlowableEmitter<SystemFile> emitter) {
            SystemFile parent = new SystemFile(path);
            if (parent.isDir) {
                File[] children = fileFilter == null ? parent.file.listFiles() : parent.file.listFiles(fileFilter);
                if (children != null) {
                    for (File child : children) {
                        if (FileUtils.isPathValid(child.getPath())) {
                            SystemFile fileToAdd = new SystemFile(child);
                            emitter.onNext(fileToAdd);
                        }
                    }
                }
            }
        }
    }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).buffer(50);
}
