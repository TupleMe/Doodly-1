package tuple.me.lily.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import tuple.me.lily.Contexter;
import tuple.me.lily.Lily;
import tuple.me.lily.R;
import tuple.me.lily.core.Objects;
import tuple.me.lily.views.toasty.Toasty;

@SuppressWarnings({"UnusedDeclaration"})
public class CommonUtil {
    private Context context;
    private Prefs prefs;
    private static CommonUtil utils;

    private CommonUtil(@NonNull Context context) {
        this.context = context;
        prefs = new Prefs(context, Lily.LILY);
    }

    public static synchronized void init(@NonNull Context context) {
        if (utils == null) {
            Objects.checkNotNull(context);
            utils = new CommonUtil(context);
        }
    }

    public static synchronized CommonUtil getInstance() {
        Objects.checkNotNull(utils, CommonUtil.class.getSimpleName() +
                " is not initialized, call initializeInstance(..) method first.");
        return utils;
    }

    public static boolean hasStoragePermission(@NonNull Context context) {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean hasSettingsPermission(@NonNull Context context) {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }


    public boolean isNewApk() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (prefs.getInt("apk_version_code", 0) < info.versionCode) {
                prefs.set("apk_version_code", info.versionCode);
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Nullable
    public String getVersionName() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void openInPlayStore(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            openInWeb("http://play.google.com/store/apps/details?id=" + context.getPackageName());
        }
    }

    public void openInWeb(String URL) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(URL)).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    public void openOtherApps() {
        Uri uri = Uri.parse("market://developer?id=Tuple.me");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/developer?id=Tuple.me"))
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void openApp(String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if(launchIntent!=null){
            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
            );
            context.startActivity(launchIntent);
        } else {
            Toasty.error(context, R.string.failed_to_open_app);
        }
    }

    public boolean isAppInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        return launchIntent != null;
    }

    public void showDetailsOfApp(String packageName) {
        try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    public void uninstallApp(String packageName) {
        try {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    public static void sendMail(@NonNull Activity activity, String chooserTitle, String subject, String body, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        activity.startActivity(Intent.createChooser(intent, chooserTitle));
    }

    public static void shareThisApp(@NonNull Activity activity) {
        ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setText("Hey!Checkout this app.Download it from here :" + "http://play.google.com/store/apps/details?id=" + activity.getPackageName())
                .setChooserTitle("Choose app to share")
                .startChooser();
    }

    public static void shareText(String text, Activity activity) {
        ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setText(text)
                .setChooserTitle("Choose app to share")
                .startChooser();
    }

    public static boolean isAndroidM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void copyToClipBoard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) Contexter.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    public static String getDateString(long mills) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("dd.MM.yyyy-KK:mm a");
        return simpleDateFormat.format(new Date(mills));
    }

    @Nullable
    public static String getClipBoardText() {
        ClipboardManager mClipboardManager = (ClipboardManager) Contexter.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = mClipboardManager.getPrimaryClip();
        if (clip.getItemCount() > 0)
            return clip.getItemAt(0).toString();
        else
            return null;
    }

    public static void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (Exception ignored) {
        }
    }
}


