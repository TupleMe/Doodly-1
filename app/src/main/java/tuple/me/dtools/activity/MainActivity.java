package tuple.me.dtools.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialcab.MaterialCab;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.dtools.adapter.DrawerAdapter;
import tuple.me.dtools.apk.backuprestore.ApkMainFragment;
import tuple.me.dtools.apk.cache.CacheFragment;
import tuple.me.dtools.apk.permissions.PermissionsFragment;
import tuple.me.dtools.base.BaseActivity;
import tuple.me.dtools.bfilter.BlueLightFilterActivity;
import tuple.me.dtools.clipboard.ClipBoardFragment;
import tuple.me.dtools.constants.Constants;
import tuple.me.dtools.file.analyzer.StorageAnalyzer;
import tuple.me.dtools.file.dup.DuplicateFinder;
import tuple.me.dtools.file.empty.EmptyFiles;
import tuple.me.dtools.file.explorer.ExplorerHome;
import tuple.me.dtools.file.explorer.PathExplorer;
import tuple.me.dtools.file.largefiles.LargeFiles;
import tuple.me.dtools.fragement.SystemOverView;
import tuple.me.dtools.qr.QRActivity;
import tuple.me.dtools.screenrecorder.RecordingService;
import tuple.me.dtools.screenrecorder.ScreenRecorder;
import tuple.me.dtools.util.FireBase;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.NavHandler;
import tuple.me.lily.model.Item;
import tuple.me.lily.model.ListItem;
import tuple.me.lily.util.BackButton;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.CustomBroadcastReceiver;
import tuple.me.lily.util.IntentUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.toasty.Toasty;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, NavHandler {
    public static final String FRAGMENT_TO_OPEN = "FRAGMENT_TO_OPEN";
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ArrayList<Item> navItems = Constants.getNavModel();
    private MaterialCab cab;
    private BackButton backButton = new BackButton();
    private PathReceiver pathReceiver;
    private ListView navigationListView;

    IntentFilter filter = new IntentFilter("loadPath");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        if (pathReceiver == null) {
            pathReceiver = new PathReceiver();
        }
        if (getIntent() != null) {
            handleIntent(getIntent());
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        int openFragment = R.string.over_view;
        if (intent != null) {
            openFragment = intent.getIntExtra(FRAGMENT_TO_OPEN, R.string.over_view);
        }
        if (handleOpen(openFragment)) {
            setToolbarTitle(openFragment);
        }
    }

    private void setupUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setBackgroundDrawable(Contexter.getColorDrawable(R.color.colorPrimaryDark));
        setToolbarTitle(R.string.app_name);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationListView = (ListView) findViewById(R.id.menu_drawer);
        navigationListView.setAdapter(new DrawerAdapter(this, navItems));
        navigationListView.setOnItemClickListener(this);
        cab = new MaterialCab(this, R.id.cab_stub).setPopupMenuTheme(R.style.appCompatDark);
    }

    public void setToolbarTitle(@StringRes int title) {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(Contexter.getString(title));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.clip_board, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (navItems.get(position) instanceof ListItem) {
            if (handleOpen(((ListItem) navItems.get(position)).title)) {
                FireBase.logDrawerClick(((ListItem) navItems.get(position)).title);
                setToolbarTitle(((ListItem) navItems.get(position)).title);
            }
        }
        //noinspection AndroidLintRtlHardcoded
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    public boolean handleOpen(int id) {
        switch (id) {
            case R.string.apk_manager:
                return replaceMainFragment(new ApkMainFragment(), id);
            case R.string.clipboard_manager:
                return replaceMainFragment(new ClipBoardFragment(), id);
            case R.string.screen_filter:
                startBlueLightFilter();
                return false;
            case R.string.qr_scanner:
                IntentUtil.transitionActivity(this, QRActivity.class);
                return false;
            case R.string.duplicate_files:
                return replaceMainFragment(new DuplicateFinder(), id);
            case R.string.storage_analyzer:
                return replaceMainFragment(new StorageAnalyzer(), id);
            case R.string.over_view:
                return replaceMainFragment(new SystemOverView(), id);
            case R.string.about:
                IntentUtil.transitionActivity(this, AboutActivity.class);
                return false;
            case R.string.screen_recorder:
                return replaceMainFragment(new ScreenRecorder(), id);
            case R.string.permissions:
                return replaceMainFragment(new PermissionsFragment(), id);
            case R.string.large_files:
                return replaceMainFragment(new LargeFiles(), id);
            case R.string.empty_files_folders:
                return replaceMainFragment(new EmptyFiles(), id);
            case R.string.cache_cleaner:
                return replaceMainFragment(new CacheFragment(), id);
            case R.string.file_manager:
                return replaceMainFragment(new ExplorerHome(), id);
        }
        return false;
    }

    private void startBlueLightFilter() {

        if (CommonUtil.isAndroidM()) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                IntentUtil.transitionActivity(this, BlueLightFilterActivity.class);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            IntentUtil.transitionActivity(this, BlueLightFilterActivity.class);
        }

    }

    private boolean replaceMainFragment(Fragment fragment, int id) {
        if (navigationListView.getAdapter() instanceof DrawerAdapter) {
            ((DrawerAdapter) navigationListView.getAdapter()).setSelectedItem(id);
        }
        FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
        manager.replace(R.id.container, fragment);
        manager.commit();
        return true;
    }

    @Override
    public MaterialCab getCab() {
        return cab;
    }

    @Override
    public BackButton getBackButton() {
        return backButton;
    }

    private static final int CREATE_SCREEN_CAPTURE = 4242;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScreenRecording(View view) {
        if (!RecordingService.isRecorderRunning) {
            MediaProjectionManager manager =
                    (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent intent = manager.createScreenCaptureIntent();
            this.startActivityForResult(intent, CREATE_SCREEN_CAPTURE);

        } else {
            Intent screenRecordingService = new Intent(getApplicationContext(), RecordingService.class);
            screenRecordingService.addCategory(RecordingService.class.getName());
            stopService(screenRecordingService);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent caputureService = new Intent(getApplicationContext(), RecordingService.class);
                caputureService.putExtra(RecordingService.PROJECTION_INDENT, data);
                caputureService.putExtra(RecordingService.PROJECTION_INDENT_CODE, resultCode);
                caputureService.addCategory(RecordingService.class.getName());
                startService(caputureService);
            } else {
                Toasty.error(this, R.string.exception_screen_capture_service);
            }
        }
    }


    private class PathReceiver extends CustomBroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int id = arg1.getExtras().getInt("FRAGMENT_ID");
            if (id == R.string.file_manager) {
                String path = arg1.getExtras().getString("PATH");
                replaceMainFragment(PathExplorer.newInstance(path), R.string.file_manager);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CustomBroadcastReceiver.unregiter(pathReceiver, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pathReceiver.register(this, filter);
    }

    public void otherApps(View view) {
        commonUtil.openOtherApps();
    }

    public void sendFeedBack(View view) {
        CommonUtil.sendMail(this, getString(R.string.choose_app), "Model :" + Build.MODEL + ",Android Version : " + Build.VERSION.RELEASE + ",Apk : " + getString(R.string.app_name) + ",Apk Version : " + commonUtil.getVersionName(), "", getString(R.string.email));
    }

    @Override
    public void onBackPressed() {
        if (backButton.isClickObserved()) {
            return;
        }
        super.onBackPressed();
    }
}
