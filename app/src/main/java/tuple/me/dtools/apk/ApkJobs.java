package tuple.me.dtools.apk;


import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import tuple.me.dtools.file.SystemFile;
import tuple.me.lily.Contexter;
import tuple.me.lily.core.StringUtil;
import tuple.me.lily.util.FileUtils;

@SuppressWarnings("UnusedDeclaration")
public class ApkJobs {

    public static Flowable<ApkModel> getListApkJob() {
        return Flowable.create(new FlowableOnSubscribe<ApkModel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ApkModel> e) throws Exception {
                ApkUtils apkUtils = ApkUtils.getInstance();
                List<PackageInfo> packageInfoList = apkUtils.getPackageInfoList();
                if (packageInfoList != null) {
                    for (int itr = packageInfoList.size() - 1; itr >= 0; itr--) {
                        try {
                            ApkModel apkModel;
                            if (apkUtils.hasLaunchIntent(packageInfoList.get(itr)) && ((apkModel = apkUtils.getApkModel(packageInfoList.get(itr))) != null)) {
                                e.onNext(apkModel);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).onBackpressureBuffer().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(AndroidSchedulers.mainThread());
    }

    public static Flowable<ApkModel> getApkListWithPermissions() {
        return Flowable.create(new FlowableOnSubscribe<ApkModel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ApkModel> e) throws Exception {
                ApkUtils apkUtils = ApkUtils.getInstance();
                List<ApplicationInfo> applicationInfoList = apkUtils.getInstalledApplicationInfo();
                if (applicationInfoList != null) {
                    for (int itr = applicationInfoList.size() - 1; itr >= 0; itr--) {
                        try {
                            PackageInfo packageInfo = apkUtils.getPackageInfo(applicationInfoList.get(itr).packageName, PackageManager.GET_PERMISSIONS);
                            ApkModel apkModel;
                            if (packageInfo != null && (apkModel = apkUtils.getApkModel(packageInfo)) != null) {
                                apkModel.getPackageInfo().requestedPermissions = packageInfo.requestedPermissions;
                                apkModel.setPermissions();
                                e.onNext(apkModel);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).onBackpressureBuffer().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(AndroidSchedulers.mainThread());
    }

    public static final String[] ignores = new String[]{".image", ".thumb", ".cache", ".video"};

    public static Flowable<ApkModel> getBackedUpApks() {
        final ApkUtils apkUtils = ApkUtils.getInstance();
        return Flowable.create(new FlowableOnSubscribe<ApkModel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ApkModel> e) throws Exception {
                List<String> dirs = FileUtils.getStorageDirectories(Contexter.getAppContext());
                for (String dir : dirs) {
                    fillFiles(dir, e);
                }
                e.onComplete();
            }

            public void fillFiles(final String path, FlowableEmitter<ApkModel> emitter) {
                final SystemFile file = new SystemFile(path);
                if (file.isDir) {
                    File[] childs = file.file.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return (pathname.isDirectory() && !StringUtil.equalsAny(pathname.getName(), ignores)) || (FileUtils.getFileExtension(pathname).equalsIgnoreCase("apk"));
                        }
                    });
                    if (childs != null) {
                        for (File child : childs) {
                            if (child.isDirectory()) {
                                fillFiles(child.getPath(), emitter);
                            } else {
                                PackageInfo info = apkUtils.getPackageManager().getPackageArchiveInfo(child.getPath(), 0);
                                if (info != null) {
                                    emitter.onNext(new ApkModel(info, child.getName().replaceAll(".apk", ""), child.length(), child.getPath()));
                                }
                            }
                        }
                    }
                }
            }
        }, BackpressureStrategy.BUFFER).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread());
    }

    public static Flowable<ApkModel> getApkListWithCacheSize() {
        return Flowable.create(new FlowableOnSubscribe<ApkModel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<ApkModel> e) throws Exception {
                ApkUtils apkUtils = ApkUtils.getInstance();
                List<ApplicationInfo> applicationInfoList = apkUtils.getInstalledApplicationInfo();
                PackageManager pm = Contexter.getAppContext().getPackageManager();
                Method sizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                if (applicationInfoList != null) {
                    for (int itr = applicationInfoList.size() - 1; itr >= 0; itr--) {
                        try {
                            PackageInfo packageInfo = apkUtils.getPackageInfo(applicationInfoList.get(itr).packageName, PackageManager.GET_PERMISSIONS);
                            ApkModel appModel = ApkUtils.getCacheForApp(packageInfo, pm, sizeInfo);
                            if (appModel != null) {
                                e.onNext(appModel);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).onBackpressureBuffer().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(AndroidSchedulers.mainThread());

    }
}
