package tuple.me.dtools.activity;

import android.Manifest;
import android.os.Bundle;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.dtools.base.BaseActivity;
import tuple.me.lily.util.IntentUtil;

public class SplashScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (isPermissionCheckRequired()) {
            requestPermissions(Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            IntentUtil.transitionActivity(this, MainActivity.class, true);
        }
    }

    @Override
    public void allPermissionsGranted() {
        Timber.d("All permissions Granted");
        IntentUtil.transitionActivity(this, MainActivity.class, true);
    }
}
