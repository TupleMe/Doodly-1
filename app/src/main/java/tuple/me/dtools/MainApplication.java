package tuple.me.dtools;

import android.app.Application;
import android.graphics.Color;
import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.orm.SugarContext;

import timber.log.Timber;
import tuple.me.dtools.util.FireBase;
import tuple.me.lily.BuildConfig;
import tuple.me.lily.Contexter;
import tuple.me.lily.Lily;
import tuple.me.lily.ThemeEngine;

/**
 * Created by gokul-4192 on 0031 31-Dec-16.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        Lily.init(this, new Lily.initCallback() {
            @Override
            public boolean needThemeEngine() {
                return false;
            }

            @Override
            public boolean needTracker() {
                return true;
            }
        });

        FireBase.init(this);
        FireBase.logOpen();
        ThemeEngine.theme = ThemeEngine.DARK;
        ThemeEngine.iconColor = Color.WHITE;
        ThemeEngine.primary = Contexter.getColor(R.color.colorAccent);
        ThemeEngine.primaryDark = Contexter.getColor(R.color.colorAccent);
        ThemeEngine.primaryLight = Contexter.getColor(R.color.colorAccent);
        ThemeEngine.primaryBackGround = Contexter.getColor(R.color.colorPrimaryDark);
        SugarContext.init(this);
    }

    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            if (t != null) {
                if (priority == Log.ERROR) {
                    FirebaseCrash.report(t);
                }
            }
        }
    }
}
