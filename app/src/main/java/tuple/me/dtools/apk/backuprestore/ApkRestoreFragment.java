package tuple.me.dtools.apk.backuprestore;

import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;
import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.apk.ApkJobs;
import tuple.me.dtools.apk.ApkModel;
import tuple.me.dtools.apk.ApkUtils;
import tuple.me.dtools.base.RecyclerViewFragment;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.util.ProgressJob;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.toasty.Toasty;

public class ApkRestoreFragment extends RecyclerViewFragment implements MaterialCab.Callback {


    Disposable apkJobDisposable = null;
    Flowable<List<ApkModel>> listApkJob = ApkJobs.getBackedUpApks().buffer(25);
    ApkAdapter adapter;
    NavHandler cab;

    @Override
    public void initConfig() {
        hasRefreshOption = true;
    }

    @Override
    public void onRefreshList() {
        if (apkJobDisposable == null || apkJobDisposable.isDisposed()) {
            cab.getCab().finish();
            fastScroller.setVisibility(View.GONE);
            adapter.clear();
            adapter.setDisabled(true);
            refreshLayout.setRefreshing(false);
            setLoadingState();
            apkJobDisposable = listApkJob.subscribeWith(new DisposableSubscriber<List<ApkModel>>() {
                @Override
                public void onNext(List<ApkModel> apkModels) {
                    adapter.addAll(apkModels);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    Toasty.error(Contexter.getAppContext(), R.string.error_occurred);
                }

                @Override
                public void onComplete() {
                    finishSetupList();
                    setEmptyState();
                    apkJobDisposable.dispose();
                }
            });
        } else {
            Toasty.error(getContext(), R.string.task_already_running);
        }
    }

    private void finishSetupList() {
        if (adapter == null) {
            adapter = new ApkAdapter(getContext());
            adapter.dataSet = new SortedList<>(ApkModel.class, new SortedListAdapterCallback<ApkModel>(adapter) {

                @Override
                public int compare(ApkModel o1, ApkModel o2) {
                    return o1.compareTo(o2);
                }

                @Override
                public boolean areContentsTheSame(ApkModel oldItem, ApkModel newItem) {
                    return oldItem.getPackageName().equals(newItem.getPackageName());
                }

                @Override
                public boolean areItemsTheSame(ApkModel item1, ApkModel item2) {
                    return item1.getPackageName().equals(item2.getPackageName());
                }
            });
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.apk_restore);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<ApkModel>() {
                @Override
                public void onItemClick(ApkModel object) {
                    File apkFile = ApkUtils.getApkFile(object.getPackageInfo());
                    if (apkFile != null) {
                        FileManagerUtil.showFileOptions(apkFile, getFragmentManager(), getContext());
                    }
                }
            });
            list.setAdapter(adapter);
            fastScroller.setRecyclerView(list);
        }
        adapter.setDisabled(false);
        fastScroller.setVisibility(View.VISIBLE);
        cab.getCab().finish();
    }

    @Override
    protected void initViews() {
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, true, true));
        if (ViewUtils.getActivityForFragment(this) instanceof MainActivity) {
            cab = (MainActivity) ViewUtils.getActivityForFragment(this);
        }
        finishSetupList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (apkJobDisposable != null && !apkJobDisposable.isDisposed()) {
            apkJobDisposable.dispose();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefreshList();
    }

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
                final ArrayList<File> selectedItems = getSelectedFiles();
                DialogUtil.showConfirmDialog(getContext(), "Are you sure want to delete " + selectedItems.size() + (selectedItems.size() > 1 ? " items?" : " item?"), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        ProgressJob<File> deleteJob = new ProgressJob<>(getContext(), selectedItems);
                        deleteJob.setTitle(R.string.deleteting);
                        deleteJob.actionHandler(new ProgressJob.ActionHandler<File>() {
                            @Override
                            public boolean doAction(File o) throws Exception {
                                FileManagerUtil.deleteFile(o, getContext());
                                return true;
                            }

                            @Override
                            public void onComplete() {
                                onRefreshList();
                            }
                        });
                        deleteJob.start();
                    }
                });
                break;
            case R.id.item_share:
                final ArrayList<File> selectedItems1 = getSelectedFiles();
                FileManagerUtil.shareFiles(getContext(), selectedItems1.toArray(new File[selectedItems1.size()]));
                break;
            case R.id.item_install:
                ApkUtils.getInstance().installApps(adapter.getSelectedItems());
        }
        return false;
    }

    public ArrayList<File> getSelectedFiles() {
        List<ApkModel> apksToBackup = adapter.getSelectedItems();
        ArrayList<File> selectedItems = new ArrayList<>();
        for (ApkModel apkModel : apksToBackup) {
            File apkFile = ApkUtils.getApkFile(apkModel.getPackageInfo());
            if (apkFile != null) {
                selectedItems.add(new File(apkModel.getPackageInfo().applicationInfo.publicSourceDir));
            }
        }
        return selectedItems;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        adapter.resetMultiSelect();
        return true;
    }
}

