package tuple.me.dtools.file.dup;

import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.base.RecyclerViewFragment;
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.util.ProgressJob;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.core.Callback;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.util.async.SimpleJob;
import tuple.me.lily.util.async.doInBackground;
import tuple.me.lily.util.async.onComplete;
import tuple.me.lily.views.DividerItemDecoration;
import tuple.me.lily.views.toasty.Toasty;

public class DuplicateFinder extends RecyclerViewFragment implements MaterialCab.Callback {
    private SimpleJob<Pair<ArrayList<Item>, Map<String, Set<File>>>, Void> duplicateTask = new SimpleJob<>();

    NavHandler cab;
    private DuplicateFileAdapter adapter;

    @Override
    protected void initViews() {
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, true, true));
        if (ViewUtils.getActivityForFragment(this) instanceof MainActivity) {
            cab = (MainActivity) ViewUtils.getActivityForFragment(this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        duplicateTask.register(preExecute, doInBackground, onComplete);
        EventBus.getDefault().register(this);
        onRefreshList();
    }

    @Override
    public void onRefreshList() {
        cab.getCab().finish();
        duplicateTask.doAgainIfCompleted();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        duplicateTask.unregister();
    }

    @Override
    public void initConfig() {
        hasRefreshOption = true;
    }

    Callback<Void> preExecute = new Callback<Void>() {
        @Override
        public void call(Void val) {
            fastScroller.setVisibility(View.GONE);
            refreshLayout.setRefreshing(false);
            setLoadingState();
        }
    };

    doInBackground<Pair<ArrayList<Item>, Map<String, Set<File>>>, Void> doInBackground = new doInBackground<Pair<ArrayList<Item>, Map<String, Set<File>>>, Void>() {
        @Override
        public Pair<ArrayList<Item>, Map<String, Set<File>>> doInBackground(Void[] parms) throws Exception {
            return DuplicateFinderUtil.findAllDuplicates();
        }
    };

    onComplete<Pair<ArrayList<Item>, Map<String, Set<File>>>> onComplete = new onComplete<Pair<ArrayList<Item>, Map<String, Set<File>>>>() {
        @Override
        public boolean onSuccess(Pair<ArrayList<Item>, Map<String, Set<File>>> result) {
            setEmptyState();
            setList(result);
            return false;
        }

        @Override
        public void onError(Exception exception) {

        }
    };

    public void setList(Pair<ArrayList<Item>, Map<String, Set<File>>> result) {
        if (adapter == null) {
            adapter = new DuplicateFileAdapter(getContext(), result.first);
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.duplicate_file);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(new OnItemClickListener<Item>() {
                @Override
                public void onItemClick(Item object) {
                    if (object instanceof SystemFile) {
                        FileManagerUtil.showFileOptions((SystemFile) object, getFragmentManager(), getContext());
                    }
                }
            });
            list.setAdapter(adapter);
            fastScroller.setRecyclerView(list);
        } else {
            adapter.resetDataSet(result.first);
        }
        fastScroller.setVisibility(View.VISIBLE);
        cab.getCab().finish();
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
                final List<Item> filesToDelete = adapter.getSelectedItems();
                DialogUtil.showConfirmDialog(getContext(), "Are you sure want to delete " + filesToDelete.size() + (filesToDelete.size() > 1 ? " items?" : " item?"), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        final ProgressJob<Item> apkModelProgressJob = new ProgressJob<>(getContext(), filesToDelete);
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
                                onRefreshList();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileDelete(FileDelete fileDelete) {
        if (adapter != null) {
            adapter.remove(fileDelete.file);
        }
    }
}
