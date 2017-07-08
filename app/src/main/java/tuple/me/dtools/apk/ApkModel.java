package tuple.me.dtools.apk;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.RangeUtils;

@SuppressWarnings("UnusedDeclaration")
public class ApkModel implements Comparable<ApkModel> {
    private PackageInfo packageInfo;
    private char sectionChar;
    private String appName;
    private long size;
    private String sizeStr;
    public ArrayList<Integer> permissionsString;

    public ApkModel(PackageInfo packageInfo, String appName, long size, String filePath) {
        this.packageInfo = packageInfo;
        this.appName = appName.trim();
        this.sectionChar = Character.toUpperCase(appName.charAt(0));
        this.size = size;
        this.sizeStr = FileUtils.getReadableFileSize(size);
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        if (Build.VERSION.SDK_INT >= 8) {
            appInfo.sourceDir = filePath;
            appInfo.publicSourceDir = filePath;
        }
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public ApplicationInfo getApplicationInfo() {
        return packageInfo.applicationInfo;
    }

    public String getPackageName() {
        return packageInfo.packageName;
    }

    public String getVersionName() {
        return packageInfo.versionName;
    }

    public int getVersionCode() {
        return packageInfo.versionCode;
    }

    public char getSectionChar() {
        return sectionChar;
    }

    public String getSize() {
        return sizeStr;
    }

    public void setSize(String size) {
        this.sizeStr = size;
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public int compareTo(@NonNull ApkModel a) {
        return this.appName.compareToIgnoreCase(a.appName);
    }

    public int compareSize(@NonNull ApkModel a) {
        return RangeUtils.compare(this.size, a.size);
    }

    public void setPermissions() {
        if (packageInfo.requestedPermissions != null) {
            String[] permissions = packageInfo.requestedPermissions;
            permissionsString = new ArrayList<>();
            for (String permission : permissions) {
                if (ApkUtils.permissionsMap.containsKey(permission)) {
                    CollectionUtils.addIfNotContains(permissionsString, ApkUtils.permissionsMap.get(permission));
                }
            }
        }
    }
}

