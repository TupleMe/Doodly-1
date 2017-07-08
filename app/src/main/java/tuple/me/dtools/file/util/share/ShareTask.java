package tuple.me.dtools.file.util.share;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.views.toasty.Toasty;

public class ShareTask extends AsyncTask<String, String, Void> {

    private int appTheme;
    private Context contextc;
    private int fab_skin;
    private ArrayList<Uri> arrayList;
    private ArrayList<Intent> targetShareIntents = new ArrayList<>();
    private ArrayList<String> arrayList1 = new ArrayList<>();
    private ArrayList<Drawable> arrayList2 = new ArrayList<>();

    public ShareTask(Context context, ArrayList<Uri> arrayList, int appTheme, int fab_skin) {
        this.contextc = context;
        this.arrayList = arrayList;
        this.appTheme = appTheme;
        this.fab_skin = fab_skin;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String mime = strings[0];
        Intent shareIntent = new Intent();
        boolean bluetooth_present = false;
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType(mime);
        PackageManager packageManager = contextc.getPackageManager();
        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(shareIntent, 0);
        if (!resInfos.isEmpty()) {
            for (ResolveInfo resInfo : resInfos) {
                String packageName = resInfo.activityInfo.packageName;
                arrayList2.add(resInfo.loadIcon(packageManager));
                arrayList1.add(resInfo.loadLabel(packageManager).toString());
                if (packageName.contains("android.bluetooth")) bluetooth_present = true;
                Intent intent = new Intent();
                System.out.println(resInfo.activityInfo.packageName + "\t" + resInfo.activityInfo.name);
                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType(mime);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayList);
                intent.setPackage(packageName);
                targetShareIntents.add(intent);

            }
        }
        if (!bluetooth_present && appInstalledOrNot("com.android.bluetooth", packageManager)) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(mime);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayList);
            intent.setPackage("com.android.bluetooth");
            targetShareIntents.add(intent);
            arrayList1.add(contextc.getResources().getString(R.string.bluetooth));
            arrayList2.add(contextc.getResources().getDrawable(ThemeEngine.isDark() ? R.drawable.ic_settings_bluetooth_white_36dp : R.drawable.ic_settings_bluetooth_black_24dp));
        }
        return null;
    }

    private boolean appInstalledOrNot(String uri, PackageManager pm) {
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onPostExecute(Void v) {
        if (!targetShareIntents.isEmpty()) {
            MaterialDialog.Builder builder = DialogUtil.getBlankDialogBuilder(contextc);
            builder.title(R.string.share);
            ShareAdapter shareAdapter = new ShareAdapter(contextc, targetShareIntents, arrayList1, arrayList2);
            builder.adapter(shareAdapter, null);
            builder.negativeText(R.string.cancel);
            builder.negativeColor(fab_skin);
            MaterialDialog b = builder.build();
            shareAdapter.updateMatDialog(b);
            b.show();
        } else {
            Toasty.error(contextc, R.string.no_app_found);
        }
    }
}
