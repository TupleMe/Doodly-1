package tuple.me.dtools.base;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.lily.Contexter;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.DialogUtil;
import tuple.me.lily.util.Prefs;

public class BaseActivity extends AppCompatActivity {

    public Prefs prefs;
    public CommonUtil commonUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Prefs.getInstance();
        commonUtil = CommonUtil.getInstance();
        getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isPermissionCheckRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean hasPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean shouldShowPermissionDialog(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    public boolean requestPermissions(String... permissions) {
        List<String> neededPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[neededPermissions.size()]), 101);
            return false;
        }
        allPermissionsGranted();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                List<String> deniedPermissions = new ArrayList<>();
                for (int itr = 0; itr < grantResults.length; itr++) {
                    if (grantResults[itr] != PackageManager.PERMISSION_GRANTED) {
                        Timber.d("Denied permissions" + permissions[itr]);
                        deniedPermissions.add(permissions[itr]);
                    }
                }
                if (deniedPermissions.isEmpty()) {
                    allPermissionsGranted();
                    return;
                }
                final AtomicBoolean br = new AtomicBoolean(false);
                for (String permission : deniedPermissions) {
                    if (!br.get()) {
                        if (!shouldShowPermissionDialog(permission)) {
                            MaterialDialog.Builder builder = DialogUtil.getBasicDialog(this, new int[]{R.string.perm_denied, R.string.pls_grant_man_str, R.string.grant, R.string.cancel, -1});
                            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    CommonUtil.getInstance().showDetailsOfApp(getPackageName());
                                    dialog.dismiss();
                                    br.set(true);
                                    finish();
                                }
                            });

                            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    br.set(true);
                                    finish();
                                }
                            });
                            builder.cancelable(false);
                            builder.build().show();
                            return;
                        } else {
                            requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]));
                            br.set(true);
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void allPermissionsGranted() {

    }
}
