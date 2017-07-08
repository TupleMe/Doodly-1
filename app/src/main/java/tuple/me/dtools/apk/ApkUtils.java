package tuple.me.dtools.apk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import tuple.me.dtools.R;
import tuple.me.dtools.file.util.share.ShareTask;
import tuple.me.lily.Contexter;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.views.bs.IconsBottomSheetFragment;

public class ApkUtils {
    PackageManager packageManager;
    Context context;

    private ApkUtils() {
    }

    public static ApkUtils getInstance() {
        return new ApkUtils(Contexter.getAppContext());
    }

    public ApkUtils(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    public boolean hasLaunchIntent(PackageInfo packageInfo) {
        return getPackageManager().getLaunchIntentForPackage(packageInfo.packageName) != null;
    }

    @Nullable
    public ApkModel getApkModel(PackageInfo packageInfo) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        File apkFile = new File(applicationInfo.publicSourceDir);
        if (apkFile.exists()) {
            return new ApkModel(packageInfo, packageManager.getApplicationLabel(applicationInfo).toString(),apkFile.length(), apkFile.getPath());
        }
        return null;
    }

    public List<PackageInfo> getPackageInfoList() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getInstalledPackages(PackageManager.SIGNATURE_MATCH);
    }

    @NonNull
    public PackageManager getPackageManager() {
        return packageManager;
    }

    @Nullable
    public static File getApkFile(PackageInfo packageInfo) {
        File apkFile = new File(packageInfo.applicationInfo.publicSourceDir);
        if (apkFile.exists()) {
            return apkFile;
        }
        return null;
    }

    public List<ApplicationInfo> getInstalledApplicationInfo() {
        return getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public void uninstallApp(ApkModel apkModel) {
        CommonUtil.getInstance().uninstallApp(apkModel.getPackageName());
    }

    public void uninstallApps(List<ApkModel> apkModels) {
        for (ApkModel apkModel : apkModels) {
            uninstallApp(apkModel);
        }
    }

    public void installApp(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void installApp(ApkModel apkModel) {
        File file = getApkFile(apkModel.getPackageInfo());
        if (file != null) {
            installApp(file);
        }
    }

    public void installApps(List<ApkModel> apkModels) {
        for (ApkModel apkModel : apkModels) {
            installApp(apkModel);
        }
    }

    @Nullable
    public PackageInfo getPackageInfo(@NonNull String packageName, int flags) {
        try {
            return packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }


    public static void shareApps(Context context, List<ApkModel> apksToShare) {
        ArrayList<Uri> files = new ArrayList<>();
        for (int i = 0; i < apksToShare.size(); i++) {
            ApkModel apkModel = apksToShare.get(i);
            File file = ApkUtils.getApkFile(apkModel.getPackageInfo());
            if (file != null) {
                files.add(Uri.fromFile(file));
            }
        }
        new ShareTask(context, files, ThemeEngine.theme, ThemeEngine.primaryDark).execute("*/*");
    }


    public static ApkModel getCacheForApp(PackageInfo packageInfo, PackageManager packageManager,Method sizeInfo) {

        try {
            final AtomicReference<Long> notifier = new AtomicReference<>();
            try {

                sizeInfo.invoke(packageManager, packageInfo.packageName,
                        new IPackageStatsObserver.Stub() { //error
                            public void onGetStatsCompleted(
                                    PackageStats pStats, boolean succeeded)
                                    throws RemoteException {

                                synchronized (notifier) {
                                    notifier.set(pStats.cacheSize);
                                    notifier.notify();
                                }

                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
                notifier.set(0L);
                notifier.notify();
            }

            synchronized (notifier) {
                while (notifier.get() == null)
                    notifier.wait();
            }

            ApplicationInfo applicationInfo;
            applicationInfo = packageInfo.applicationInfo;
            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                File apkFile = new File(applicationInfo.publicSourceDir);
                if (apkFile.exists()) {
                    return new ApkModel(packageInfo, packageManager.getApplicationLabel(applicationInfo).toString(), notifier.get(),apkFile.getPath());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean clearCache(Context context) throws Exception {
        final AtomicReference<Boolean> notifier = new AtomicReference<>();
        PackageManager mPM = context.getPackageManager();
        @SuppressWarnings("rawtypes")
        final Class[] classes = {Long.TYPE, IPackageDataObserver.class};
        try {
            Method localMethod =
                    mPM.getClass().getMethod("freeStorageAndNotify", classes);

            localMethod.invoke(mPM, Long.MAX_VALUE, new IPackageDataObserver.Stub() {

                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                    synchronized (notifier) {
                        notifier.set(succeeded);
                        notifier.notify();

                    }
                }
            });

        } catch (Exception e1) {
            e1.printStackTrace();
            notifier.set(false);
            notifier.notify();

        }

        synchronized (notifier) {
            while (notifier.get() == null)
                notifier.wait();
        }
        return notifier.get();
    }

    private void showDetails(ApkModel apk) {
        CommonUtil.getInstance().showDetailsOfApp(apk.getPackageName());
    }

    public static void showAppOptions(final ApkModel apk, final FragmentManager fm, final Context context) {
        final int[] titles = new int[]{R.string.open_app, tuple.me.lily.R.string.share,R.string.uninstall, R.string.properties};
        final int[] icons = new int[]{tuple.me.lily.R.drawable.ic_folder_open, tuple.me.lily.R.drawable.ic_share, tuple.me.lily.R.drawable.ic_delete, R.drawable.ic_alert_circle};
        final IconsBottomSheetFragment customBottomSheetFragment = IconsBottomSheetFragment.newInstance(titles, icons).setTitle(apk.getAppName());
        customBottomSheetFragment.setItemClickListener(new IconsBottomSheetFragment.OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                switch (titles[id]) {
                    case R.string.open_app:
                        CommonUtil.getInstance().openApp(apk.getPackageName());
                        break;
                    case R.string.share:
                        shareApps(context, Collections.singletonList(apk));
                        break;
                    case R.string.uninstall:
                        ApkUtils.getInstance().uninstallApp(apk);
                        break;
                    case R.string.properties:
                        ApkUtils.getInstance().showDetails(apk);
                        break;
                }
                customBottomSheetFragment.dismiss();
            }
        }).show(fm, "App Details");
    }


    static HashMap<String, Integer> permissionsMap = new HashMap<>();

    static {
        permissionsMap.put(android.Manifest.permission.READ_CALENDAR, R.string.permissions_calender);
        permissionsMap.put(android.Manifest.permission.WRITE_CALENDAR, R.string.permissions_calender);
        permissionsMap.put(android.Manifest.permission.CAMERA, R.string.permissions_camera);
        permissionsMap.put(android.Manifest.permission.READ_CONTACTS, R.string.permissions_contacts);
        permissionsMap.put(android.Manifest.permission.WRITE_CONTACTS, R.string.permissions_contacts);
        permissionsMap.put(android.Manifest.permission.GET_ACCOUNTS, R.string.permissions_contacts);
        permissionsMap.put(android.Manifest.permission.ACCESS_FINE_LOCATION, R.string.permissions_location);
        permissionsMap.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permissions_location);
        permissionsMap.put(android.Manifest.permission.RECORD_AUDIO, R.string.permissions_audio);
        permissionsMap.put(android.Manifest.permission.READ_PHONE_STATE, R.string.permissions_phone);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissionsMap.put(android.Manifest.permission.READ_CALL_LOG, R.string.permissions_phone);
            permissionsMap.put(android.Manifest.permission.WRITE_CALL_LOG, R.string.permissions_phone);
            permissionsMap.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permissions_storage);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            permissionsMap.put(android.Manifest.permission.BODY_SENSORS, R.string.permissions_sensors);
        }
        permissionsMap.put(android.Manifest.permission.ADD_VOICEMAIL, R.string.permissions_phone);
        permissionsMap.put(android.Manifest.permission.USE_SIP, R.string.permissions_phone);
        permissionsMap.put(android.Manifest.permission.PROCESS_OUTGOING_CALLS, R.string.permissions_phone);
        permissionsMap.put(android.Manifest.permission.SEND_SMS, R.string.permissions_sms);
        permissionsMap.put(android.Manifest.permission.RECEIVE_SMS, R.string.permissions_sms);
        permissionsMap.put(android.Manifest.permission.READ_SMS, R.string.permissions_sms);
        permissionsMap.put(android.Manifest.permission.RECEIVE_WAP_PUSH, R.string.permissions_sms);
        permissionsMap.put(android.Manifest.permission.RECEIVE_MMS, R.string.permissions_sms);
        permissionsMap.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permissions_storage);
    }
}
