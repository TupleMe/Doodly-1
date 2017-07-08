package tuple.me.dtools.apk.cache;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

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
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.util.async.SimpleJob;
import tuple.me.lily.util.async.doInBackground;
import tuple.me.lily.util.async.onComplete;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.toasty.Toasty;

import static tuple.me.dtools.util.FireBase.context;

public class CacheFragment extends RecyclerViewFragment {


    Disposable apkJobDisposable = null;
    Flowable<List<ApkModel>> listApkJob = ApkJobs.getApkListWithCacheSize().buffer(50);
    CacheCleanerAdapter adapter;
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
            adapter = new CacheCleanerAdapter(getContext());
            adapter.dataSet = new SortedList<>(ApkModel.class, new SortedListAdapterCallback<ApkModel>(adapter) {

                @Override
                public int compare(ApkModel o1, ApkModel o2) {
                    return -o1.compareSize(o2);
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
            adapter.setOnClickListener(new OnItemClickListener<ApkModel>() {
                @Override
                public void onItemClick(ApkModel object) {
                    ApkUtils.showAppOptions(object, getFragmentManager(), getContext());
                }
            });
            list.setAdapter(adapter);
            adapter.setMultiSelectMode(false);
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
        if (CommonUtil.isAndroidM()) {
            Toasty.error(R.string.cache_android_m);
        }
        finishSetupList();
        fab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_delete);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isAndroidM()) {
                    Toasty.error(R.string.cache_android_m);
                    Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                }
                final MaterialDialog loading = DialogUtil.getBlankLoadingDialogBuilder(getContext()).title(R.string.clearing_cache).build();
                loading.show();
                loading.setCancelable(false);
                SimpleJob.simpleJob(new doInBackground<Boolean, Object>() {
                    @Override
                    public Boolean doInBackground(Object[] parms) throws Exception {
                        return ApkUtils.clearCache(Contexter.getAppContext());
                    }
                }, new onComplete<Boolean>() {
                    @Override
                    public boolean onSuccess(Boolean result) {
                        if (result) {
                            loading.dismiss();
                            return false;
                        }
                        onError(null);
                        return false;
                    }

                    @Override
                    public void onError(Exception exception) {
                        loading.dismiss();
                        Toasty.error(R.string.error_occurred);
                    }
                });
            }
        });
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
}
