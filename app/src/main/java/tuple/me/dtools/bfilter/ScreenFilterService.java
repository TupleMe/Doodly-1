package tuple.me.dtools.bfilter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import tuple.me.dtools.R;
import tuple.me.dtools.constants.PrefConstants;
import tuple.me.dtools.service.NotificationActionService;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.util.Prefs;

public class ScreenFilterService extends Service {

    public View mView;

    private NotificationManager mNotificationManager;
    public static boolean isServiceRunning;
    private Context context;
    private int notificationID = 336;
    public static ScreenFilterService service;

    public static int getColorFilter(int color, int factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1]= 100;
        hsv[2] *= (float) (100 - factor) / 100;
        color = Color.HSVToColor(hsv);
        //int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb((255 * (int) (factor * 0.80)) / 100, r, g, b);
    }

    @Override
    public void onCreate() {
        isServiceRunning = true;
        mView = new View(this);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mView, mParams);
        Prefs prefs= Prefs.getInstance();
        int alpha = prefs.getInt(PrefConstants.BLUE_LIGHT_OPACITY,60);
        int color = prefs.getInt(PrefConstants.BLUE_LIGHT_COLOR, 5);
        mView.setBackgroundColor(getColorFilter(Color.parseColor(ThemeEngine.colors[color]), alpha));
        super.onCreate();
        service = this;
        context = getApplicationContext();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        setupNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(
                mView);
        mView = null;
        isServiceRunning = false;
        dismissNotification();
    }

    private void setupNotification() {
        Intent notificationIntent = new Intent(context, BlueLightFilterActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.closable_notification_small);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(smallNotification)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(intent);
        Notification notification = mBuilder.build();
        smallNotification.setTextViewText(R.id.text, getString(R.string.screen_filter_running));

        Intent stop = new Intent(context, NotificationActionService.class)
                .setAction(NotificationActionService.BLUE_LIGHT_STOP);
        PendingIntent stopPI = PendingIntent.getService(this, 0, stop, 0);
        smallNotification.setOnClickPendingIntent(R.id.stop, stopPI);
        mNotificationManager.notify(notificationID, notification);
    }


    private void dismissNotification() {
        mNotificationManager.cancel(notificationID);
    }
}
