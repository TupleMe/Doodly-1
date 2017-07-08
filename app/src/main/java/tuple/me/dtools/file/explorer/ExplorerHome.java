package tuple.me.dtools.file.explorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arasthel.asyncjob.AsyncJob;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.view.StorageList;
import tuple.me.lily.Contexter;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.CircleDrawable;
import tuple.me.lily.views.DividerItemDecoration;

public class ExplorerHome extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.explorer_home_fragment, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View rootView) {
        final ImageView apps = (ImageView) rootView.findViewById(R.id.list_android);
        ImageView audio = (ImageView) rootView.findViewById(R.id.list_audio);
        ImageView video = (ImageView) rootView.findViewById(R.id.list_video);
        ImageView image = (ImageView) rootView.findViewById(R.id.list_image);
        ViewUtils.setViewBackground(apps, new CircleDrawable(Contexter.getColor(R.color.accent_blue)));
        ViewUtils.setViewBackground(audio, new CircleDrawable(Contexter.getColor(R.color.accent_brown)));
        ViewUtils.setViewBackground(video, new CircleDrawable(Contexter.getColor(R.color.accent_deep_purple)));
        ViewUtils.setViewBackground(image, new CircleDrawable(Contexter.getColor(R.color.accent_deep_orange)));
        final StorageList list = (StorageList) rootView.findViewById(R.id.storage_list);
        final RecyclerView recentsList = (RecyclerView) rootView.findViewById(R.id.recent_list);
        recentsList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, true, true));
        LinearLayoutManager layoutManager = new LinearLayoutManager(list.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recentsList.setLayoutManager(layoutManager);
        final PathExplorerAdapter adapter = new PathExplorerAdapter(getContext());
        recentsList.setAdapter(adapter);
        recentsList.setNestedScrollingEnabled(false);
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                List<String> storages = FileUtils.getStorageDirectories(Contexter.getAppContext());
                final List<FileManagerUtil.StorageItem> listItems = new ArrayList<>();
                for (String storage : storages) {
                    listItems.add(new FileManagerUtil.StorageItem(storage));
                }
                final List<SystemFile> recentFiles = FileManagerUtil.listRecentFiles(getContext());
                Timber.d("Recents size " + recentFiles.size());
                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        list.setStorages(listItems);
                        adapter.addAll(recentFiles);
                    }
                });
            }
        });
        list.setStorageClickListener(new StorageList.OnStorageClickListener() {
            @Override
            public void onClick(FileManagerUtil.StorageItem item) {
                Intent path = new Intent();
                path.setAction("loadPath");
                path.putExtra("FRAGMENT_ID", R.string.file_manager);
                path.putExtra("PATH", item.path);
                getContext().sendBroadcast(path);
            }
        });

    }

}
