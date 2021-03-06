package tuple.me.dtools.apk.permissions;

import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;
import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.apk.ApkJobs;
import tuple.me.dtools.apk.ApkModel;
import tuple.me.dtools.apk.ApkUtils;
import tuple.me.dtools.apk.backuprestore.ApkAdapter;
import tuple.me.dtools.base.RecyclerViewFragment;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.toasty.Toasty;

public class PermissionsFragment extends RecyclerViewFragment implements MaterialCab.Callback {


    Disposable apkJobDisposable = null;
    Flowable<List<ApkModel>> listApkJob = ApkJobs.getApkListWithPermissions().buffer(10);
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
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.apk_permission);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<ApkModel>() {
                @Override
                public void onItemClick(ApkModel object) {
                    ApkUtils.showAppOptions(object, getFragmentManager(), getContext());
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
            case R.id.item_uninstall:
                List<ApkModel> apks = adapter.getSelectedItems();
                ApkUtils apkUtils = ApkUtils.getInstance();
                for (int i = 0; i < apks.size(); i++) {
                    apkUtils.uninstallApp(apks.get(i));
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        adapter.resetMultiSelect();
        return true;
    }
}
