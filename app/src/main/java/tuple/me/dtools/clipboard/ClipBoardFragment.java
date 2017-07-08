package tuple.me.dtools.clipboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.arasthel.asyncjob.AsyncJob;

import java.util.ArrayList;
import java.util.List;

import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.sugarmodel.clipboard.ClipboardItem;
import tuple.me.lily.adapters.MultiSelectCabListener;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.MultiStateView;
import tuple.me.lily.views.bs.IconsBottomSheetFragment;
import tuple.me.lily.views.toasty.Toasty;

public class ClipBoardFragment extends Fragment implements OnItemClickListener<ClipboardItem>, MaterialCab.Callback {

    private RecyclerView list;
    private ClipBoardAdapter adapter;
    private Intent clipBoardService;
    private List<ClipboardItem> itemsList;
    private MultiStateView multiStateView;
    private NavHandler cab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_clipboard, container, false);
        list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        multiStateView = (MultiStateView) rootView.findViewById(R.id.multi_state_view);
        //noinspection ConstantConditions
        clipBoardService = new Intent(getContext(), ClipBoardMonitorService.class);
        if (ViewUtils.getActivityForFragment(this) instanceof MainActivity) {
            cab = (MainActivity) ViewUtils.getActivityForFragment(this);
        }
        setupList();
        return rootView;
    }


    private ClipBoardMonitorService mService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ClipBoardMonitorService.LocalBinder binder = (ClipBoardMonitorService.LocalBinder) service;
            mService = binder.getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void setupList() {
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                final ArrayList<ClipboardItem> items = ClipboardItem.getAll();
                itemsList = items;
                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        setUpAdapter(items);
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unbindService(serviceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!ClipBoardMonitorService.isServiceRunning)
            getContext().startService(clipBoardService);
        getContext().bindService(clipBoardService, serviceConnection, Context.BIND_AUTO_CREATE);
        setupList();
    }

    @Override
    public void onItemClick(final ClipboardItem item) {
        final int[] titles = new int[]{R.string.copy_to_clipboard, R.string.delete, R.string.share};
        final IconsBottomSheetFragment iconsBottomSheetFragment = IconsBottomSheetFragment.newInstance(titles, new int[]{R.drawable.ic_clipboard, R.drawable.ic_delete_white_24dp, R.drawable.ic_share})
                .setItemClickListener(new IconsBottomSheetFragment.OnItemClickListener() {
                    @Override
                    public void onItemClick(int id) {
                        id = titles[id];
                        switch (id) {
                            case R.string.copy_to_clipboard:
                                CommonUtil.copyToClipBoard("Clipboard", item.text);
                                Toasty.success(getContext(), R.string.copy_success);
                                break;
                            case R.string.delete:
                                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                                    @Override
                                    public void doOnBackground() {
                                        item.delete();
                                        if (ClipBoardMonitorService.clipVsId != null) {
                                            ClipBoardMonitorService.clipVsId.remove(item.text);
                                        }
                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                Toasty.success(getContext(), R.string.success);
                                                adapter.remove(item);
                                            }
                                        });
                                    }
                                });
                                break;
                            case R.string.share:
                                CommonUtil.shareText(item.text, ViewUtils.getActivityForFragment(ClipBoardFragment.this));
                                break;
                        }
                    }
                });
        iconsBottomSheetFragment.show(getFragmentManager(), "Clipboard");
    }

    public void setUpAdapter(ArrayList<ClipboardItem> item) {
        if (adapter == null) {
            adapter = new ClipBoardAdapter(getContext(), item);
            MultiSelectCabListener listener = new MultiSelectCabListener(cab, this, R.menu.clip_board);
            adapter.setMultiSelectChangeListener(listener);
            adapter.setOnClickListener(ClipBoardFragment.this);
            list.setAdapter(adapter);
        }
        adapter.setDisabled(false);
        adapter.setDataSet(item);
        if (adapter.isEmpty()) {
            View emptyView = multiStateView.setView(R.layout.common_empty);
            TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
            emptyText.setText("No items found \n Copy text to manage via clipboard manager");
        } else {
            multiStateView.emptyStateView();
        }
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
                final List<ClipboardItem> selectedItems = adapter.getSelectedItems();
                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                    @Override
                    public void doOnBackground() {
                        for (ClipboardItem item : selectedItems) {
                            item.delete();
                            if (ClipBoardMonitorService.clipVsId != null) {
                                ClipBoardMonitorService.clipVsId.remove(item.text);
                            }
                        }
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                Toasty.success(getContext(), R.string.success);
                                setupList();
                            }
                        });
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
}

