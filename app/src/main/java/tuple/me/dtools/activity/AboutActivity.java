package tuple.me.dtools.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import tuple.me.dtools.R;
import tuple.me.dtools.base.BaseActivity;
import tuple.me.lily.Contexter;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.DialogUtil;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        TextView version = (TextView) findViewById(R.id.version_text);
        version.setText(commonUtil.getVersionName());
        //noinspection ConstantConditions
        getSupportActionBar().setBackgroundDrawable(Contexter.getColorDrawable(R.color.colorPrimaryDark));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.app_name);
    }

    public void openQuora(View view) {
        commonUtil.openInWeb("https://www.quora.com/profile/Gokul-133");
    }

    public void openFaceBook(View view) {
        commonUtil.openInWeb("https://www.facebook.com/tupleme.gokul");
    }

    public void rateUs(View view) {
        commonUtil.openInPlayStore(getPackageName());
    }

    public void otherApps(View view) {
        commonUtil.openOtherApps();
    }

    public void reportBug(View view) {
        CommonUtil.sendMail(AboutActivity.this, getString(R.string.choose_app), "Model :" + Build.MODEL + ",Android Version : " + Build.VERSION.RELEASE + ",Apk : " + getString(R.string.app_name) + ",Apk Version : " + commonUtil.getVersionName(), "", getString(R.string.email));
    }

    public void sendFeedBack(View view) {
        reportBug(view);
    }

    public void translate(View view) {
        reportBug(view);
    }

    public void share(View view) {
        CommonUtil.shareThisApp(this);
    }

    public void changeLog(View view) {
        DialogUtil.showChangeLog(this, R.raw.change_log);
    }
}
